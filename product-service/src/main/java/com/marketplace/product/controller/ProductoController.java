package com.marketplace.product.controller;

import com.marketplace.product.dto.ProductoCreateRequest;
import com.marketplace.product.dto.ProductoInteraccionCreateRequest;
import com.marketplace.product.dto.ProductoInteraccionRespuestaRequest;
import com.marketplace.product.dto.ProductoInteraccionResponse;
import com.marketplace.product.dto.ProductoResponse;
import com.marketplace.product.service.ProductoInteraccionService;
import com.marketplace.product.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del catálogo y comunidad del producto (preguntas/comentarios bajo {@code /productos/{id}/interacciones}).
 */
@Tag(name = "Productos", description = "Catálogo de productos y comunidad en ficha")
@RestController
@RequestMapping
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoInteraccionService productoInteraccionService;

    public ProductoController(ProductoService productoService, ProductoInteraccionService productoInteraccionService) {
        this.productoService = productoService;
        this.productoInteraccionService = productoInteraccionService;
    }

    @Operation(summary = "Crear producto", description = "Requiere vendedorSolicitudId cuya solicitud esté en ACTIVA (consulta solicitud-service).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud de vendedor no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Vendedor no en estado ACTIVA", content = @Content),
            @ApiResponse(responseCode = "502", description = "Error HTTP desde solicitud-service", content = @Content),
            @ApiResponse(responseCode = "503", description = "solicitud-service no configurado", content = @Content)
    })
    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto", required = true)
            @Valid @RequestBody ProductoCreateRequest request) {
        return productoService.crear(request);
    }

    @Operation(summary = "Listar productos")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Catálogo")})
    @GetMapping("/productos")
    public List<ProductoResponse> listar(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        return productoService.listar();
    }

    @Operation(summary = "Listar interacciones del producto")
    @GetMapping("/productos/{productoId}/interacciones")
    public List<ProductoInteraccionResponse> listarInteracciones(
            @Parameter(description = "Id del producto", required = true) @PathVariable Long productoId) {
        return productoInteraccionService.listarPorProducto(productoId);
    }

    @Operation(summary = "Publicar pregunta o comentario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Validación", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no existe", content = @Content)
    })
    @PostMapping("/productos/{productoId}/interacciones")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoInteraccionResponse crearInteraccion(
            @Parameter(description = "Id del producto", required = true) @PathVariable Long productoId,
            @Valid @RequestBody ProductoInteraccionCreateRequest body) {
        return productoInteraccionService.crear(productoId, body);
    }

    @Operation(summary = "Responder pregunta (vendedor / demo sin auth)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "No es una pregunta", content = @Content)
    })
    @PutMapping("/productos/{productoId}/interacciones/{interaccionId}/respuesta")
    public ProductoInteraccionResponse responderInteraccion(
            @PathVariable Long productoId,
            @PathVariable Long interaccionId,
            @Valid @RequestBody ProductoInteraccionRespuestaRequest body) {
        return productoInteraccionService.responder(productoId, interaccionId, body);
    }
}
