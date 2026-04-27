# Requirements: HU-01 - Carga masiva de facturas

  ## User Story
  **Como** operador del Centralizador I&E
  **Quiero** cargar un lote de facturas a través del API
  **Para** que se persistan con estado Pendiente y se inicie su procesamiento

  ## Acceptance Criteria

  ### AC-1: Recibe lista de facturas en JSON
  - **Given** un JSON con una lista de facturas válidas
  - **When** se envía POST a /api/facturas/bulk
  - **Then** responde 200 con la lista de facturas creadas incluyendo id, estado y fechaCreacion

  ### AC-2: Persiste con estado PENDIENTE
  - **Given** una lista de N facturas en el request
  - **When** se procesan exitosamente
  - **Then** se crean N registros en centralizador.facturas con estado = PENDIENTE

  ### AC-3: Valida campos obligatorios
  - **Given** una factura sin numeroDocumento, nombres o valorPack
  - **When** se envía POST a /api/facturas/bulk
  - **Then** responde 400 con detalle de los campos faltantes

  ### AC-4: Valida tipos de datos
  - **Given** una factura con valorPack negativo o tipoDocumento vacío
  - **When** se envía POST a /api/facturas/bulk
  - **Then** responde 400 con detalle de la validación fallida

  ## Campos de entrada
  | Campo | Tipo | Obligatorio | Validación |
  |-------|------|-------------|------------|
  | cus | String | No | Max 20 |
  | fechaPago | Date | No | yyyy-MM-dd |
  | responsabilidadFiscal | String | No | Max 100 |
  | tipoDocumento | String | Sí | @NotBlank, max 10 |
  | numeroDocumento | String | Sí | @NotBlank, max 20 |
  | nombres | String | Sí | @NotBlank, max 100 |
  | apellidos | String | No | Max 100 |
  | telefono | String | No | Max 20 |
  | municipio | String | No | Max 100 |
  | direccion | String | No | Max 255 |
  | email | String | No | @Email, max 150 |
  | valorPack | Decimal | Sí | @NotNull, @Positive |
  | valorPackIva | Decimal | No | @Positive |
  | comentarios | String | No | TEXT |

  ## Campos de salida adicionales
  | Campo | Tipo | Descripción |
  |-------|------|-------------|
  | id | Long | ID autogenerado |
  | estado | String | Siempre PENDIENTE al crear |
  | fechaCreacion | DateTime | Timestamp de creación |
  