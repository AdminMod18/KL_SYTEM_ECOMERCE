package com.marketplace.solicitud.entity;

import com.marketplace.solicitud.model.SolicitudEstado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Propósito: modelo persistido de solicitud de alta de vendedor (root del agregado).
 * Patrón: Context del State pattern (el estado actual delega reglas de transición).
 * Responsabilidad: conservar datos del solicitante y el estado vigente versionable por transiciones controladas.
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreVendedor;

    @Column(nullable = false, unique = true, length = 32)
    private String documentoIdentidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SolicitudEstado estado;

    @Column(nullable = false)
    private Instant creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }
}
