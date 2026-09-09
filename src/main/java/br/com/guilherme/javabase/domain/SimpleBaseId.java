package br.com.guilherme.javabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@MappedSuperclass 
@EqualsAndHashCode (callSuper = true)
public class SimpleBaseId extends SimpleAuditory<Long> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
