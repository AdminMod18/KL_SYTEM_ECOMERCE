package com.marketplace.payment.model;

/**
 * Propósito: catálogo de mecanismos de cobro soportados por la pasarela interna.
 * Patrón: enumeración de dominio enlazada al Factory Method y a Strategy.
 * Responsabilidad: serialización estable en JSON y clave en el mapa de estrategias.
 */
public enum TipoPago {
    ONLINE,
    TARJETA,
    CONSIGNACION
}
