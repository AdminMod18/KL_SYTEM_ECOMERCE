package com.marketplace.user.service;

import com.marketplace.user.dto.UsuarioCreateRequest;
import com.marketplace.user.dto.UsuarioResponse;
import com.marketplace.user.dto.UsuarioUpdateRequest;
import com.marketplace.user.entity.Usuario;
import com.marketplace.user.exception.UsuarioBusinessException;
import com.marketplace.user.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Propósito: casos de uso CRUD sobre el agregado Usuario.
 * Patrón: Application Service.
 * Responsabilidad: validar unicidad, persistir y mapear a DTOs de salida.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        normalizar(request);
        assertUnicoCreacion(request.getNombreUsuario(), request.getEmail());

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setEmail(request.getEmail());
        usuario.setNombreCompleto(request.getNombreCompleto());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        normalizar(request);
        Usuario usuario = buscar(id);
        assertUnicoActualizacion(id, request.getNombreUsuario(), request.getEmail());

        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setEmail(request.getEmail());
        usuario.setNombreCompleto(request.getNombreCompleto());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    private void assertUnicoCreacion(String nombreUsuario, String email) {
        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(nombreUsuario)) {
            throw new UsuarioBusinessException("USUARIO_DUPLICADO", "El nombre de usuario ya existe.");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new UsuarioBusinessException("EMAIL_DUPLICADO", "El email ya está registrado.");
        }
    }

    private void assertUnicoActualizacion(Long id, String nombreUsuario, String email) {
        if (usuarioRepository.existsByNombreUsuarioIgnoreCaseAndIdNot(nombreUsuario, id)) {
            throw new UsuarioBusinessException("USUARIO_DUPLICADO", "El nombre de usuario ya existe.");
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new UsuarioBusinessException("EMAIL_DUPLICADO", "El email ya está registrado.");
        }
    }

    private static void normalizar(UsuarioCreateRequest request) {
        request.setNombreUsuario(request.getNombreUsuario().trim());
        request.setEmail(request.getEmail().trim());
        request.setNombreCompleto(request.getNombreCompleto().trim());
    }

    private static void normalizar(UsuarioUpdateRequest request) {
        request.setNombreUsuario(request.getNombreUsuario().trim());
        request.setEmail(request.getEmail().trim());
        request.setNombreCompleto(request.getNombreCompleto().trim());
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn());
    }
}
