# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`java-base` is a shared Spring Boot **library** (packaged as a plain `.jar`, not an executable app) that provides a generic CRUD framework — base entities, DTOs, controllers, services, converters, validators, exception handling and RSQL-style filtering — reused by consumer projects such as `java-test` (sibling repo, same parent directory). It does not run on its own; it is installed to the local `.m2` repo and consumed as a Maven dependency.

It intentionally does **not** extend `spring-boot-starter-parent` (that's for executable apps); versions are managed by importing the `spring-boot-dependencies` BOM instead.

## Commands

```bash
mvn -o clean install -DskipTests   # build and install to local .m2 for java-test to consume
mvn -o clean package                # compile + run tests
mvn -o test -Dtest=ClassName#methodName   # run a single test
```

Use `-o` (offline) once dependencies are already resolved locally — this has been the working pattern in this environment. There is no `src/test` yet; when adding tests, standard Maven Surefire conventions apply (no custom plugin config in `pom.xml`).

After changing anything in `java-base`, **`mvn -o clean install -DskipTests` must be re-run** so `java-test`'s Docker build (which builds `java-base` from source in its own multi-stage Dockerfile) and any local Maven build of `java-test` pick up the change.

QueryDSL Q-classes and Lombok are both annotation-processor generated at compile time (see `maven-compiler-plugin` config in `pom.xml`); a clean build regenerates them into `target/generated-sources`.

## Architecture

### The generic CRUD stack

Every consumer entity flows through the same layered, generic pipeline. For an entity `X`, a consumer project wires up:

`XController extends BaseControllerImpl` → `BaseControllerDispatcherImpl` (internal, constructed by `BaseControllerImpl`) → `XServiceImpl extends BaseServiceImpl` → `XRepository extends BaseRepository` (QueryDSL-enabled Spring Data repo), with `XConverter extends BaseConverterDtoImpl` handling entity↔DTO mapping and an optional `XDtoValidator implements DtoValidator` for business-rule validation on create/update.

Generic type parameters follow this shape everywhere: `<D (full response dto), U (update dto), P (create dto), E (entity), I (id type), B (basic/list-view dto)>`. Getting these in the wrong order is the most common mistake when wiring a new entity.

**Consumer projects use only `XServiceImpl` directly — never create a separate `XService` interface** unless a narrower contract is genuinely needed. `BaseServiceImpl` already implements `BaseService`; adding a per-entity interface on top is pure ceremony this codebase deliberately avoids.

`BaseControllerImpl` provides the five standard REST endpoints (`GET /{id}`, `GET` list, `POST`, `PUT /{id}`, `DELETE /{id}`) for free. Individual endpoints can be disabled per-controller via `getBloquedMethodsCore()`. A sixth, `GET /unique?field=&value=[&id=][&<scope>=...]` → `{ "unique": bool }`, is opt-in: it answers 501 unless the controller declares a whitelist in its constructor via `getUniqueRules().put("<field>", Map.of("<query param>", "<entity path>"))` (e.g. `Map.of("typeService", "typeService.id")`; `Map.of()` = unique in the whole tenant). Every scope param is required, `id` excludes the record itself, values are converted to the attribute types by reflection (`UniqueQuery`), and the check — like every other endpoint, delegated to the dispatcher (`BaseControllerDispatcher.checkUnique`) — uses `BaseService.existsByPredicate` (tenant filter applies, inactive rows count). It is a form convenience (the vue-base yup `.unique()`), so the save must still validate on its own; access is the same as `GETALL`. Hooks for custom behavior around persistence live on `BaseServiceImpl` (`beforeSave`, `afterSave`, `beforeDelete`, `afterDelete`, `saveChildRecords`, `removeChildRecords`) and on the dispatcher (`addEvent(DBEventType.BEFORE_INSERT/AFTER_INSERT/BEFORE_UPDATE/AFTER_UPDATE, ...)`).

### Soft delete is baked into the base, not opt-in

`SimpleBaseId` (the base `@MappedSuperclass` for `id`) carries an `active` boolean defaulting to `true`. `BaseServiceImpl.deleteById` never issues a hard delete — it sets `active = false` and saves. `BaseDispatcherImpl.verifyCurrentFilter` automatically ANDs `active==true` into every **list** query predicate, so soft-deleted rows silently disappear from `GET` list/search results — **unless** the `search` itself has a criterion on the top-level `active` selector (`BooleanBuilderParse.referencesField`), in which case that criterion wins (e.g. `active==false` lists only the inactive rows; this is what the front-end "Situation" filters send). `GET /{id}` is **not** filtered this way — fetching a soft-deleted record by id still works. Any new entity inheriting `SimpleBaseId`/`ClientBaseId` gets this behavior automatically; there is nothing to configure.

### Multi-tenant entities: `ClientBaseId`

`ClientBaseId extends SimpleBaseId` adds a raw `client_id` `Long` column for entities scoped to a tenant. It is deliberately **not** a `@ManyToOne` — `java-base` cannot depend on a consumer project's `Client` entity. A consumer project that wants a navigable `@ManyToOne` to its own `Client` entity declares that field itself instead of using this class. Cross-tenant isolation (verifying a referenced record actually belongs to the caller's client) is the consumer project's responsibility, typically enforced in the entity's `XDtoValidator`.

### Auditing

`SimpleAuditory<I>` (extended by `SimpleBaseId`) auto-populates `createdBy`/`createdAt`/`updatedBy`/`updatedAt` via `@PrePersist`/`@PreUpdate` JPA callbacks, reading the acting user id from `SecurityUtils.getLoggedUserId()` (falls back to `0L` if unavailable — there is currently no authentication wired in, so this always falls back today).

### RSQL-style filtering (`?search=`)

List endpoints accept a `search` query param parsed by `BooleanBuilderParse`/`ProcessFilter` (using `cz.jirutka.rsql:rsql-parser`) into a QueryDSL `BooleanBuilder` against the entity's generated `Q`-class. Supported operators are defined in `SearchOperation` (`==`, `!=`, `>=`, `>`, `<=`, `<`, `=lk=`, `=ctn=`, `=in=`, `=nin=`, `=bt=`, `=sw=`, `=ew=`), and nested-property paths (e.g. `client.id==3`) are supported via dot notation. The `active==true` filter described above is injected through this same parser.

### Exception handling

`GlobalExceptionHandler` (a `@RestControllerAdvice`) is registered automatically in any consumer app via `WebExceptionHandlingAutoConfiguration` (Spring Boot auto-configuration, listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — a consumer's own component-scan never reaches `br.com.guilherme.javabase` on its own, hence the explicit auto-configuration classes for `GlobalExceptionHandler` and `ExceptionDatabaseImpl`). It handles: any `BusinessException` subclass (status taken from `httpStatus()`), `MethodArgumentNotValidException` (Bean Validation failures → 400 with field-level messages), `AsyncRequestNotUsableException`/`AsyncRequestTimeoutException` (an async response - e.g. a Server-Sent Events stream - whose client left or timed out: 503 with no body, since the stream's `text/event-stream` Content-Type cannot carry the JSON `ErrorResponse`), and falls back to 500 for everything else.

Domain-level exceptions extend `BusinessException` (`BadRequestException`, `ConflictException` (409 with a detail - the record changed state and the operation no longer applies, e.g. someone else took a task first; `UpdateError` stays for JPA version conflicts), `EntityNotFound`, `UnauthorizedException`, `UpdateError`, etc.) and take an `ErrorSeverity` + `EnumErrorMessage` (+ optional extra args). `ExceptionDatabaseImpl.translateMessageDatabase` converts low-level JPA/DB exceptions (`DataIntegrityViolationException`, `OptimisticLockingFailureException`) into these domain exceptions inside `BaseServiceImpl`'s insert/update/delete paths, so callers of the service layer only ever see business exceptions.

### Error message i18n

`EnumErrorMessage` resolves messages from `error-messages.properties` (English, default) using the current request's `Locale` via `LocaleContextHolder`. A `pt_BR` translation already exists (`error-messages_pt_BR.properties`); add further locales the same way. Deliberately uses `ResourceBundle.Control.getNoFallbackControl` so an unmapped locale falls back to the bundle's own default (English) rather than the JVM's default locale — otherwise a JVM running with a pt_BR system locale would silently return Portuguese messages to every request regardless of the request's actual `Accept-Language`.

### File storage (`storage/`)

Uploaded files (java-flow service attachments, java-front-desk panel media) go through `FileStorage`. The database of the consumer keeps only the metadata and the key returned by `store`.

- `store(prefix, stream, maxBytes)` streams the content (never fully in memory) under the key `<prefix>/<uuid>`. Empty, or over `maxBytes`, is a 400 and nothing is kept.
- `load(key)` returns a `Resource` to stream in the response; `delete(key)` does not fail if the file is already gone.
- `LocalFileStorage` writes to a directory and moves a `.part` file into place only when complete. The original name never becomes a path.
- `FileStorageAutoConfiguration` creates it when the consumer sets `app.storage.dir` (in docker, a volume owned by the app user). Without the property there is no storage bean; a consumer's own `FileStorage` bean (e.g. S3/MinIO) takes precedence.
- `FileTypes.detect(bufferedStream, allowed)` detects the type from the first bytes (PDF, PNG, JPEG, GIF, WebP, MP4, WebM) without consuming the stream. Each consumer passes the types it accepts; the browser's Content-Type is never trusted.
- Consumers delete stored files after commit (and the new file on rollback). That is their transaction logic, not the storage's.

### Adding a new entity in a consumer project

Extend `SimpleBaseId` (or `ClientBaseId` for tenant-scoped data) for the entity, then create the DTOs, repository, `XServiceImpl`, `XConverter`, optional `XDtoValidator`, and `XController` following the generic type parameter order above. New/changed tables always go through a new Flyway migration in the consumer project — this library has no schema management of its own.

## Known constraint to ask about before changing

Do not add per-entity `XService` interfaces — this was an explicit, deliberate simplification decision; use `XServiceImpl` directly.
