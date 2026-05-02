package com.marketplace.solicitud.entity;

import com.marketplace.solicitud.model.TipoDocumentoAdjunto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Propósito: documento anexo perteneciente a una solicitud (parte del agregado).
 * Patrón: entidad dependiente del agregado {@link Solicitud} (persistencia ORM).
 * Responsabilidad: conservar tipo, nombre legible y URI local del archivo almacenado.
 */
@Entity
@Table(name = "solicitud_adjuntos")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private TipoDocumentoAdjunto tipo;

    @Column(nullable = false, length = 260)
    private String nombreArchivo;

    @Column(length = 1024)
    private String uriArchivo;
}
