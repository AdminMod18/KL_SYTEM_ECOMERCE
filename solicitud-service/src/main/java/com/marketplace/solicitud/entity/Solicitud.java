package com.marketplace.solicitud.entity;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.model.TipoPersonaSolicitante;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Propósito: modelo persistido de solicitud de alta de vendedor (root del agregado).
 * Patrón: Context del State pattern (el estado actual delega reglas de transición).
 * Responsabilidad: conservar datos del solicitante, adjuntos y el estado vigente versionable por transiciones controladas.
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

    /** Radicado único visible al solicitante (HU-02). */
    @Column(name = "numero_radicado", unique = true, length = 40)
    private String numeroRadicado;

    /**
     * Nombre para mostrar / envío a integraciones (derivado o razón social compacta).
     */
    @Column(nullable = false)
    private String nombreVendedor;

    @Column(length = 120)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(length = 320)
    private String correoElectronico;

    @Column(length = 120)
    private String paisResidencia;

    @Column(length = 120)
    private String ciudadResidencia;

    @Column(length = 40)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private TipoPersonaSolicitante tipoPersona;

    @Column(nullable = false, unique = true, length = 32)
    private String documentoIdentidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SolicitudEstado estado;

    /**
     * Fin del periodo pagado de suscripción (renovable). Solo aplica a vendedores en ciclo ACTIVA / EN_MORA.
     */
    @Column(name = "proximo_vencimiento_suscripcion")
    private Instant proximoVencimientoSuscripcion;

    /**
     * Momento en que pasó a {@link com.marketplace.solicitud.model.SolicitudEstado#EN_MORA}; usado para cancelar tras la gracia desde mora.
     */
    @Column(name = "entrada_en_mora_en")
    private Instant entradaEnMoraEn;

    @Column(nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudAdjunto> adjuntos = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
        if (numeroRadicado == null || numeroRadicado.isBlank()) {
            numeroRadicado = "RAD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
