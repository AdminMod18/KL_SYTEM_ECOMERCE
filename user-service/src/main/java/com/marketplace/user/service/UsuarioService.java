package com.marketplace.user.service;

import com.marketplace.user.dto.UsuarioCreateRequest;
import com.marketplace.user.dto.UsuarioResponse;
import com.marketplace.user.dto.UsuarioUpdateRequest;
import com.marketplace.user.dto.interno.PromoverVendedorResponse;
import com.marketplace.user.dto.interno.VerificarCredencialesResponse;
import com.marketplace.user.entity.Usuario;
import com.marketplace.user.exception.UsuarioBusinessException;
import com.marketplace.user.factory.UsuarioEntidadFactory;
import com.marketplace.user.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Propósito: casos de uso CRUD sobre el agregado Usuario.
 * Patrón: Application Service.
 * Responsabilidad: validar unicidad, persistir y mapear a DTOs de salida.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        normalizar(request);
        assertUnicoCreacion(request.getNombreUsuario(), request.getEmail());

        Usuario usuario = UsuarioEntidadFactory.desdeAlta(request);
        if (request.getPassword() != null) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Optional<VerificarCredencialesResponse> verificarCredencialesSiCoinciden(
            String usernameOrEmail, String passwordRaw) {
        String clave = usernameOrEmail.trim();
        Optional<Usuario> candidato =
                usuarioRepository.findByNombreUsuarioIgnoreCase(clave).or(() -> usuarioRepository.findByEmailIgnoreCase(clave));
        if (candidato.isEmpty()) {
            return Optional.empty();
        }
        Usuario u = candidato.get();
        String hash = u.getPasswordHash();
        if (hash == null || hash.isBlank()) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(passwordRaw, hash)) {
            return Optional.empty();
        }
        return Optional.of(new VerificarCredencialesResponse(u.getNombreUsuario(), rolesEfectivos(u)));
    }

    /**
     * Invocado desde solicitud-service cuando la solicitud pasa a ACTIVA: enlaza comprador existente por documento o email.
     */
    @Transactional
    public PromoverVendedorResponse promoverAVendedorPorDocumentoOCorreo(String documentoIdentidad, String correoElectronico) {
        Optional<Usuario> candidato = buscarUsuarioPorDocumento(documentoIdentidad);
        if (candidato.isEmpty() && correoElectronico != null && !correoElectronico.isBlank()) {
            candidato = usuarioRepository.findByEmailIgnoreCase(correoElectronico.trim());
        }
        if (candidato.isEmpty()) {
            return new PromoverVendedorResponse(false, null, "USUARIO_NO_ENCONTRADO");
        }
        Usuario u = candidato.get();
        List<String> roles = u.getRoles();
        if (roles == null) {
            roles = new ArrayList<>();
            u.setRoles(roles);
        }
        if (!roles.contains("VENDEDOR")) {
            roles.add("VENDEDOR");
        }
        usuarioRepository.save(u);
        return new PromoverVendedorResponse(true, u.getNombreUsuario(), null);
    }

    /**
     * Coincidencia por documento tal cual y, si falla, solo alfanuméricos (ej. solicitud con puntos vs registro sin puntos).
     */
    private Optional<Usuario> buscarUsuarioPorDocumento(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return Optional.empty();
        }
        String trim = documentoIdentidad.trim();
        Optional<Usuario> porExacto = usuarioRepository.findByDocumentoIdentidadIgnoreCase(trim);
        if (porExacto.isPresent()) {
            return porExacto;
        }
        String soloAlfanum = trim.replaceAll("[^0-9A-Za-z]", "");
        if (soloAlfanum.isBlank() || soloAlfanum.equalsIgnoreCase(trim)) {
            return Optional.empty();
        }
        return usuarioRepository.findByDocumentoIdentidadIgnoreCase(soloAlfanum);
    }

    @Transactional(readOnly = true)
    public Optional<List<String>> rolesPorNombreUsuario(String nombreUsuario) {
        String clave = nombreUsuario.trim();
        return usuarioRepository.findByNombreUsuarioIgnoreCase(clave).map(UsuarioService::rolesEfectivos);
    }

    private static List<String> rolesEfectivos(Usuario u) {
        List<String> r = u.getRoles();
        if (r == null || r.isEmpty()) {
            return List.of("COMPRADOR", "USER");
        }
        return List.copyOf(r);
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
        usuario.setNombres(blancoANulo(request.getNombres()));
        usuario.setApellidos(blancoANulo(request.getApellidos()));
        usuario.setDireccionResidencia(blancoANulo(request.getDireccionResidencia()));
        usuario.setRedSocialTwitter(blancoANulo(request.getRedSocialTwitter()));
        usuario.setRedSocialInstagram(blancoANulo(request.getRedSocialInstagram()));
        usuario.setTelefono(blancoANulo(request.getTelefono()));
        usuario.setPaisResidencia(blancoANulo(request.getPaisResidencia()));
        usuario.setCiudadResidencia(blancoANulo(request.getCiudadResidencia()));
        usuario.setDocumentoIdentidad(blancoANulo(request.getDocumentoIdentidad()));
        usuario.setTipoPersona(request.getTipoPersona());
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
        if (request.getPassword() != null) {
            request.setPassword(request.getPassword().trim());
            if (request.getPassword().isEmpty()) {
                request.setPassword(null);
            }
        }
        if (request.getNombreCompleto() != null) {
            request.setNombreCompleto(request.getNombreCompleto().trim());
            if (request.getNombreCompleto().isEmpty()) {
                request.setNombreCompleto(null);
            }
        }
        if (request.getNombres() != null) {
            request.setNombres(request.getNombres().trim());
            if (request.getNombres().isEmpty()) {
                request.setNombres(null);
            }
        }
        if (request.getApellidos() != null) {
            request.setApellidos(request.getApellidos().trim());
            if (request.getApellidos().isEmpty()) {
                request.setApellidos(null);
            }
        }
        if (request.getDireccionResidencia() != null) {
            request.setDireccionResidencia(request.getDireccionResidencia().trim());
            if (request.getDireccionResidencia().isEmpty()) {
                request.setDireccionResidencia(null);
            }
        }
        if (request.getRedSocialTwitter() != null) {
            request.setRedSocialTwitter(request.getRedSocialTwitter().trim());
            if (request.getRedSocialTwitter().isEmpty()) {
                request.setRedSocialTwitter(null);
            }
        }
        if (request.getRedSocialInstagram() != null) {
            request.setRedSocialInstagram(request.getRedSocialInstagram().trim());
            if (request.getRedSocialInstagram().isEmpty()) {
                request.setRedSocialInstagram(null);
            }
        }
        boolean tieneNombres =
                request.getNombres() != null
                        && request.getApellidos() != null
                        && !request.getNombres().isBlank()
                        && !request.getApellidos().isBlank();
        if (tieneNombres
                && (request.getNombreCompleto() == null || request.getNombreCompleto().isBlank())) {
            request.setNombreCompleto(request.getNombres() + " " + request.getApellidos());
        }
        if (request.getNombreCompleto() == null || request.getNombreCompleto().isBlank()) {
            throw new UsuarioBusinessException(
                    "NOMBRE_REQUERIDO",
                    "Indique nombreCompleto o bien los campos nombres y apellidos del caso de estudio.");
        }
        if (request.getTelefono() != null) {
            request.setTelefono(request.getTelefono().trim());
        }
        if (request.getPaisResidencia() != null) {
            request.setPaisResidencia(request.getPaisResidencia().trim());
        }
        if (request.getCiudadResidencia() != null) {
            request.setCiudadResidencia(request.getCiudadResidencia().trim());
        }
        if (request.getDocumentoIdentidad() != null) {
            request.setDocumentoIdentidad(request.getDocumentoIdentidad().trim());
            if (request.getDocumentoIdentidad().isEmpty()) {
                request.setDocumentoIdentidad(null);
            }
        }
        if (request.getTelefono() != null && request.getTelefono().isBlank()) {
            request.setTelefono(null);
        }
        if (request.getPaisResidencia() != null && request.getPaisResidencia().isBlank()) {
            request.setPaisResidencia(null);
        }
        if (request.getCiudadResidencia() != null && request.getCiudadResidencia().isBlank()) {
            request.setCiudadResidencia(null);
        }
        if (request.getDocumentoIdentidad() != null
                && (request.getDocumentoIdentidad().length() < 5
                        || request.getDocumentoIdentidad().length() > 32)) {
            throw new UsuarioBusinessException(
                    "DOCUMENTO_FORMATO", "Si informa documento, debe tener entre 5 y 32 caracteres.");
        }
    }

    private static void normalizar(UsuarioUpdateRequest request) {
        request.setNombreUsuario(request.getNombreUsuario().trim());
        request.setEmail(request.getEmail().trim());
        request.setNombreCompleto(request.getNombreCompleto().trim());
        if (request.getTelefono() != null) {
            request.setTelefono(request.getTelefono().trim());
        }
        if (request.getPaisResidencia() != null) {
            request.setPaisResidencia(request.getPaisResidencia().trim());
        }
        if (request.getCiudadResidencia() != null) {
            request.setCiudadResidencia(request.getCiudadResidencia().trim());
        }
        if (request.getNombres() != null) {
            request.setNombres(request.getNombres().trim());
            if (request.getNombres().isEmpty()) request.setNombres(null);
        }
        if (request.getApellidos() != null) {
            request.setApellidos(request.getApellidos().trim());
            if (request.getApellidos().isEmpty()) request.setApellidos(null);
        }
        if (request.getDireccionResidencia() != null) {
            request.setDireccionResidencia(request.getDireccionResidencia().trim());
            if (request.getDireccionResidencia().isEmpty()) request.setDireccionResidencia(null);
        }
        if (request.getRedSocialTwitter() != null) {
            request.setRedSocialTwitter(request.getRedSocialTwitter().trim());
            if (request.getRedSocialTwitter().isEmpty()) request.setRedSocialTwitter(null);
        }
        if (request.getRedSocialInstagram() != null) {
            request.setRedSocialInstagram(request.getRedSocialInstagram().trim());
            if (request.getRedSocialInstagram().isEmpty()) request.setRedSocialInstagram(null);
        }
        if (request.getDocumentoIdentidad() != null) {
            request.setDocumentoIdentidad(request.getDocumentoIdentidad().trim());
            if (request.getDocumentoIdentidad().isEmpty()) {
                request.setDocumentoIdentidad(null);
            }
        }
        if (request.getTelefono() != null && request.getTelefono().isBlank()) {
            request.setTelefono(null);
        }
        if (request.getPaisResidencia() != null && request.getPaisResidencia().isBlank()) {
            request.setPaisResidencia(null);
        }
        if (request.getCiudadResidencia() != null && request.getCiudadResidencia().isBlank()) {
            request.setCiudadResidencia(null);
        }
        if (request.getDocumentoIdentidad() != null
                && (request.getDocumentoIdentidad().length() < 5
                        || request.getDocumentoIdentidad().length() > 32)) {
            throw new UsuarioBusinessException(
                    "DOCUMENTO_FORMATO", "Si informa documento, debe tener entre 5 y 32 caracteres.");
        }
    }

    private static String blancoANulo(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getDireccionResidencia(),
                usuario.getRedSocialTwitter(),
                usuario.getRedSocialInstagram(),
                usuario.getTelefono(),
                usuario.getPaisResidencia(),
                usuario.getCiudadResidencia(),
                usuario.getDocumentoIdentidad(),
                usuario.getTipoPersona(),
                rolesEfectivos(usuario),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn());
    }
}
