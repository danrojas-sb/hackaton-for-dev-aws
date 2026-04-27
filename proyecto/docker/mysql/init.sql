-- =============================================================
-- Script de inicialización - Centralizador Facturas Packs
-- Crea los esquemas y tablas necesarias para el entorno local
-- =============================================================

-- Esquema principal del centralizador
CREATE DATABASE IF NOT EXISTS centralizador;

-- Esquema de procesos masivos (contabilización)
CREATE DATABASE IF NOT EXISTS db_procesos_masivos;

-- -------------------------------------------------------------
-- Tabla: facturas (esquema centralizador)
-- Almacena las facturas recibidas con control de estados
-- -------------------------------------------------------------
USE centralizador;

CREATE TABLE IF NOT EXISTS facturas (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    cus                     VARCHAR(20)     NULL,
    fecha_pago              DATE            NULL,
    responsabilidad_fiscal  VARCHAR(100)    NULL,
    tipo_documento          VARCHAR(10)     NOT NULL,
    numero_documento        VARCHAR(20)     NOT NULL,
    nombres                 VARCHAR(100)    NOT NULL,
    apellidos               VARCHAR(100)    NULL,
    telefono                VARCHAR(20)     NULL,
    municipio               VARCHAR(100)    NULL,
    direccion               VARCHAR(255)    NULL,
    email                   VARCHAR(150)    NULL,
    valor_pack              DECIMAL(12,2)   NOT NULL,
    valor_pack_iva          DECIMAL(12,2)   NULL,
    comentarios             TEXT            NULL,
    estado                  ENUM('PENDIENTE','PROCESO','TERMINADO','ERROR') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_estado (estado),
    INDEX idx_numero_documento (numero_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Tabla: facturas_contabilizadas (esquema db_procesos_masivos)
-- Registra la contabilización de facturas procesadas
-- En sprint futuro se reemplaza por escritura a TRONADOR
-- -------------------------------------------------------------
USE db_procesos_masivos;

CREATE TABLE IF NOT EXISTS facturas_contabilizadas (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    cus                     VARCHAR(20)     NULL,
    fecha_pago              DATE            NULL,
    responsabilidad_fiscal  VARCHAR(100)    NULL,
    tipo_documento          VARCHAR(10)     NOT NULL,
    numero_documento        VARCHAR(20)     NOT NULL,
    nombres                 VARCHAR(100)    NOT NULL,
    apellidos               VARCHAR(100)    NULL,
    telefono                VARCHAR(20)     NULL,
    municipio               VARCHAR(100)    NULL,
    direccion               VARCHAR(255)    NULL,
    email                   VARCHAR(150)    NULL,
    valor_pack              DECIMAL(12,2)   NOT NULL,
    valor_pack_iva          DECIMAL(12,2)   NULL,
    comentarios             TEXT            NULL,
    estado_contable         ENUM('CONTABILIZADA','ERROR_CONTABLE') NOT NULL,
    fecha_contabilizacion   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    factura_origen_id       BIGINT          NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_factura_origen (factura_origen_id),
    INDEX idx_estado_contable (estado_contable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
