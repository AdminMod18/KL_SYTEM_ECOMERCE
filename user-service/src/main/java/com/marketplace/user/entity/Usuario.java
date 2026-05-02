package com.marketplace.user.entity;

import com.marketplace.user.model.TipoPersonaComprador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Propósito: entidad persistida de usuario del marketplace.
 * Patrón: entidad de dominio JPA (agregado raíz simple).
 * Responsabilidad: almacenar identidad, contacto y marcas de auditoría.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String nombreUsuario;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Column(length = 120)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(length = 300)
    private String direccionResidencia;

    @Column(length = 80)
    private String redSocialTwitter;

    @Column(length = 80)
    private String redSocialInstagram;

    @Column(length = 40)
    private String telefono;

    @Column(length = 120)
    private String paisResidencia;

    @Column(length = 120)
    private String ciudadResidencia;

    @Column(length = 32)
    private String documentoIdentidad;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private TipoPersonaComprador tipoPersona;

    /**
     * BCrypt del password de registro (opcional: usuarios creados sin contraseña no pueden autenticarse vía auth).
     */
    @Column(name = "password_hash", length = 72)
    private String passwordHash;

    /**
     * Roles funcionales emitidos en JWT vía auth-service (p. ej. COMPRADOR, VENDEDOR). Vacío en BD legacy ⇒ se asume COMPRADOR+USER al login.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "rol", length = 32)
    private List<String> roles = new ArrayList<>();

    @Column(nullable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private Instant actualizadoEn;

    @PrePersist
    void onCreate() {
        Instant ahora = Instant.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        actualizadoEn = ahora;
    }

    @PreUpdate
    void onUpdate() {
        actualizadoEn = Instant.now();
    }
}
