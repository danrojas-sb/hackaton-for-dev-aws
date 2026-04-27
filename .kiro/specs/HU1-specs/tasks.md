  # Tasks: HU-01 - Carga masiva de facturas

  ## Task 2: Inicializar proyecto Spring Boot + entidad Factura
  - [ ] Generar proyecto Spring Boot 3.4.x con Maven Wrapper en proyecto/backend/
  - [ ] Agregar dependencias: web, data-jpa, validation, mysql, lombok, test
  - [ ] Crear enum EstadoFactura (PENDIENTE, PROCESO, TERMINADO, ERROR)
  - [ ] Crear entidad Factura con 14 campos CSV + estado + fechaCreacion + fechaActualizacion
  - [ ] Crear FacturaRepository extends JpaRepository
  - [ ] Configurar application.yml con MySQL localhost:3307/centralizador
  - [ ] Verificar: mvnw spring-boot:run arranca y valida tablas OK

  ## Task 3: Endpoint POST /api/facturas/bulk
  - [ ] Crear FacturaRequest con validaciones Jakarta (@NotBlank, @NotNull, @Positive, @Size)
  - [ ] Crear FacturaResponse con todos los campos + id + estado + fechaCreacion
  - [ ] Crear FacturaService.crearFacturasEnLote() - mapeo, estado PENDIENTE, saveAll
  - [ ] Crear FacturaController con POST /api/facturas/bulk
  - [ ] Crear manejo de errores de validación (@ExceptionHandler)
  - [ ] Crear CorsConfig permitiendo localhost:4200
  - [ ] Verificar: POST con curl retorna 200 con facturas creadas

  ## Tests
  - [ ] Given lista de requests válidos, When crearFacturasEnLote, Then persiste con estado PENDIENTE
  - [ ] Given lista vacía, When crearFacturasEnLote, Then retorna lista vacía
  - [ ] Given JSON válido, When POST /api/facturas/bulk, Then 200 con facturas
  - [ ] Given JSON con campos faltantes, When POST, Then 400 con errores
