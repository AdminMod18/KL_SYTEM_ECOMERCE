package com.marketplace.solicitud.repository;

import com.marketplace.solicitud.dto.SolicitudFiltrosConsulta;
import com.marketplace.solicitud.entity.Solicitud;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Propósito: traducir {@link SolicitudFiltrosConsulta} a predicados JPA Criteria.
 * Patrón: Specification (Spring Data) — composición de filtros dinámicos.
 * Responsabilidad: mantener consultas tipadas fuera del facade/servicio.
 */
public final class SolicitudFiltrosSpecification {

    private SolicitudFiltrosSpecification() {}

    public static Specification<Solicitud> construir(SolicitudFiltrosConsulta f) {
        return (root, query, cb) -> {
            List<Predicate> partes = new ArrayList<>();

            if (f.solicitudIdExacta() != null) {
                partes.add(cb.equal(root.get("id"), f.solicitudIdExacta()));
            }

            if (f.documentoIdentidadContiene() != null && !f.documentoIdentidadContiene().isBlank()) {
                String esc = escaparLike(f.documentoIdentidadContiene().trim().toLowerCase());
                if (!esc.isBlank()) {
                    String patron = "%" + esc + "%";
                    partes.add(cb.like(cb.lower(root.get("documentoIdentidad")), patron));
                }
            }

            if (f.estado() != null) {
                partes.add(cb.equal(root.get("estado"), f.estado()));
            }

            if (f.creadoDesdeInclusive() != null) {
                partes.add(cb.greaterThanOrEqualTo(root.get("creadoEn"), f.creadoDesdeInclusive()));
            }

            if (f.creadoHastaInclusive() != null) {
                partes.add(cb.lessThanOrEqualTo(root.get("creadoEn"), f.creadoHastaInclusive()));
            }

            if (f.textoLibreNombreCorreo() != null && !f.textoLibreNombreCorreo().isBlank()) {
                String esc = escaparLike(f.textoLibreNombreCorreo().trim().toLowerCase());
                if (!esc.isBlank()) {
                    String patron = "%" + esc + "%";
                    var nv = root.<String>get("nombreVendedor");
                    var ce = root.<String>get("correoElectronico");
                    var nb = root.<String>get("nombres");
                    var ap = root.<String>get("apellidos");
                    partes.add(
                            cb.or(
                                    cb.like(cb.lower(nv), patron),
                                    cb.like(cb.lower(ce), patron),
                                    cb.like(cb.lower(nb), patron),
                                    cb.like(cb.lower(ap), patron)));
                }
            }

            if (partes.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(partes.toArray(Predicate[]::new));
        };
    }

    /** Evita que el usuario inyecte comodines SQL (%, _). */
    private static String escaparLike(String raw) {
        return raw.replace("%", "").replace("_", "");
    }
}
