package com.marketplace.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "parametros_sistema")
@Getter
@Setter
@NoArgsConstructor
public class ParametroSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String clave;

    @Column(nullable = false, length = 2000)
    private String valor;

    @Column(nullable = false)
    private Instant actualizadoEn;

    @PrePersist
    @PreUpdate
    void touch() {
        actualizadoEn = Instant.now();
    }
}
