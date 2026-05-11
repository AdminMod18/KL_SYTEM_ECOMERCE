-- Tabla creada antes con CHECK solo para COMPRA / SOLICITUD_APROBADA; el código acepta CONSULTA_CATALOGO (HU-23).
-- Hibernate ddl-auto no actualiza este CHECK en PostgreSQL → INSERT fallaba con 500.
DO $$
BEGIN
  IF to_regclass('public.eventos_metrica') IS NOT NULL THEN
    ALTER TABLE eventos_metrica DROP CONSTRAINT IF EXISTS eventos_metrica_tipo_check;
  END IF;
END $$;
