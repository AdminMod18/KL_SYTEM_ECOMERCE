-- Repara despliegues donde Hibernate intentó añadir cantidad_stock NOT NULL sin DEFAULT sobre filas existentes.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_name = 'productos'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = current_schema()
        AND table_name = 'productos'
        AND column_name = 'cantidad_stock'
    ) THEN
      ALTER TABLE productos ADD COLUMN cantidad_stock INTEGER NOT NULL DEFAULT 0;
    ELSE
      UPDATE productos SET cantidad_stock = 0 WHERE cantidad_stock IS NULL;
      ALTER TABLE productos ALTER COLUMN cantidad_stock SET DEFAULT 0;
      ALTER TABLE productos ALTER COLUMN cantidad_stock SET NOT NULL;
    END IF;
  END IF;
END $$;
