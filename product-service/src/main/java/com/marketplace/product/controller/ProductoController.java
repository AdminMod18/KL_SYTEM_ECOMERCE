package com.marketplace.product.controller;

import com.marketplace.product.dto.ProductoCreateRequest;
import com.marketplace.product.dto.ProductoResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Propósito: API REST del catálogo de productos.
 * Patrón: Adapter HTTP.
 * Responsabilidad: exponer POST /productos y GET /productos delegando al servicio.
 */
@Tag(name = "Productos", description = "Catálogo de productos")
@RestController
@RequestMapping
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Crear producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content)
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
}
