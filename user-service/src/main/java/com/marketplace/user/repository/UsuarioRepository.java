package com.marketplace.user.repository;

import com.marketplace.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Propósito: acceso a datos de usuarios.
 * Patrón: Repository (Spring Data JPA).
 * Responsabilidad: consultas CRUD y búsqueda por campos únicos.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNombreUsuarioIgnoreCaseAndIdNot(String nombreUsuario, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByDocumentoIdentidadIgnoreCase(String documentoIdentidad);
}
