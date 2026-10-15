package br.com.guilherme.javabase.security;

import java.util.Collection;
import java.util.List;

/**
 * Gestao por departamento, lida do access token da requisicao atual ({@link CurrentUserHolder}): cada pessoa da
 * equipe pertence a um departamento ({@code departmentId}) e pode gerir varios ({@code managedDepartments}) - claims
 * emitidos pelo java-test. O gestor aprova os pedidos e enxerga os dados das pessoas dos departamentos que gere; o
 * {@value #ALL_DEPARTMENTS_ROLE} gere todos sem precisar estar listado. Os ids valem ate' o proximo refresh do token.
 *
 * <p>Quem pertence a qual departamento cada servico sabe pela propria replica de usuarios; aqui so' o que vem no
 * token de quem pede.
 */
public final class DepartmentScope {

    /** Role que gere todos os departamentos do cliente. */
    public static final String ALL_DEPARTMENTS_ROLE = "CLIENT_ADMIN";

    private DepartmentScope() {
    }

    /** Gere todos os departamentos ({@value #ALL_DEPARTMENTS_ROLE}). */
    public static boolean managesAll() {
        return ALL_DEPARTMENTS_ROLE.equals(CurrentUserHolder.getRole());
    }

    /** E' gestor: gere todos ou pelo menos um departamento. */
    public static boolean isManager() {
        return managesAll() || !CurrentUserHolder.getManagedDepartmentIds().isEmpty();
    }

    /** Departamentos que gere (vazio pra quem gere todos - use {@link #managesAll()} antes). */
    public static List<Long> managedDepartmentIds() {
        return CurrentUserHolder.getManagedDepartmentIds();
    }

    /** Gere o departamento (sempre, pra quem gere todos; pessoa sem departamento so' quem gere todos). */
    public static boolean manages(final Long departmentId) {
        return managesAll() || departmentId != null && managedDepartmentIds().contains(departmentId);
    }

    /** Gere algum dos departamentos. */
    public static boolean managesAny(final Collection<Long> departmentIds) {
        return managesAll() || departmentIds.stream().anyMatch(DepartmentScope::manages);
    }

    /** Departamento de quem pede. */
    public static Long departmentId() {
        return CurrentUserHolder.getDepartmentId();
    }
}
