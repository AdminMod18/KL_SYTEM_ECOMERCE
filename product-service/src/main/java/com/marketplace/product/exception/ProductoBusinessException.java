package com.marketplace.product.exception;

/**
 * Regla de negocio del catálogo (p. ej. vendedor no habilitado para publicar).
 */
public class ProductoBusinessException extends RuntimeException {

    private final String codigo;

    public ProductoBusinessException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
