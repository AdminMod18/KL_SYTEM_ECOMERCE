package com.marketplace.product.dto;

import com.marketplace.product.model.CondicionProductoCatalogo;
import com.marketplace.product.model.OriginalidadProducto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando REST para crear productos (caso estudio: catálogo ampliado con atributos físicos y stock).
 */
@Getter
@Setter
public class ProductoCreateRequest {

    @NotNull
    private Long vendedorSolicitudId;

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precio;

    @NotBlank
    @Size(max = 500)
    private String descripcion;

    @NotNull
    private List<@NotBlank String> categorias;

    @Size(max = 120)
    private String subcategoria;

    @Size(max = 120)
    private String marca;

    private OriginalidadProducto originalidad;

    @Size(max = 80)
    private String color;

    @Size(max = 80)
    private String tamano;

    @Min(0)
    private Integer pesoGramos;

    @Size(max = 40)
    private String talla;

    private CondicionProductoCatalogo condicion;

    @Min(0)
    private Integer cantidadStock;

    /**
     * Lista de URLs de imagen (se serializa como lista JSON); opcional.
     */
    private List<@Size(max = 500) String> imagenesUrls;
}
