  # Tasks: HU-01 - Carga masiva de facturas

  ## Task 2: Inicializar proyecto Spring Boot + entidad Factura
  - [x] Generar proyecto Spring Boot 3.4.x con Maven Wrapper en proyecto/backend/
  - [x] Agregar dependencias: web, data-jpa, validation, mysql, lombok, test
  - [x] Crear enum EstadoFactura (PENDIENTE, PROCESO, TERMINADO, ERROR)
  - [x] Crear entidad Factura con 14 campos CSV + estado + fechaCreacion + fechaActualizacion
  - [x] Crear FacturaRepository extends JpaRepository
  - [x] Configurar application.yml con MySQL localhost:3307/centralizador
  - [x] Verificar: mvnw spring-boot:run arranca y valida tablas OK

  ## Task 3: Endpoint POST /api/facturas/bulk
  - [x] Crear FacturaRequest con validaciones Jakarta (@NotBlank, @NotNull, @Positive, @Size)
  - [x] Crear FacturaResponse con todos los campos + id + estado + fechaCreacion
  - [x] Crear FacturaService.crearFacturasEnLote() - mapeo, estado PENDIENTE, saveAll
  - [x] Crear FacturaController con POST /api/facturas/bulk
  - [x] Crear manejo de errores de validación (@ExceptionHandler)
  - [x] Crear CorsConfig permitiendo localhost:4200
  - [x] Verificar: POST con curl retorna 200 con facturas creadas

  ## Tests
  - [x] Given lista de requests válidos, When crearFacturasEnLote, Then persiste con estado PENDIENTE
  - [x] Given lista vacía, When crearFacturasEnLote, Then retorna lista vacía
  - [x] Given JSON válido, When POST /api/facturas/bulk, Then 200 con facturas
  - [x] Given JSON con campos faltantes, When POST, Then 400 con errores
