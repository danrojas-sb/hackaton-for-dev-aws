  # Design: HU-01 - Carga masiva de facturas

  ## Arquitectura
  Controller -> Service -> Repository -> MySQL (patrón capas)

  ## Componentes

  ### 1. EstadoFactura.java (enum)
  PENDIENTE, PROCESO, TERMINADO, ERROR

  ### 2. Factura.java (entidad JPA)
  - Mapeada a tabla centralizador.facturas
  - @CreationTimestamp en fechaCreacion
  - @UpdateTimestamp en fechaActualizacion
  - @Enumerated(EnumType.STRING) en estado

  ### 3. FacturaRequest.java (DTO entrada)
  - Jakarta Validation: @NotBlank, @NotNull, @Positive, @Size, @Email

  ### 4. FacturaResponse.java (DTO salida)
  - Todos los campos + id + estado + fechaCreacion

  ### 5. FacturaRepository.java
  - JpaRepository<Factura, Long>, saveAll() para lote

  ### 6. FacturaService.java
  - crearFacturasEnLote(List<FacturaRequest>): List<FacturaResponse>
  - Mapea requests a entidades, asigna PENDIENTE, saveAll, mapea a responses

  ### 7. FacturaController.java
  - POST /api/facturas/bulk con @Valid @RequestBody List<FacturaRequest>
  - @ExceptionHandler para errores de validación

  ### 8. CorsConfig.java
  - Permite localhost:4200 (Angular)

  ## Diagrama de secuencia

  Cliente -> Controller: POST /api/facturas/bulk [List<FacturaRequest>]
  Controller -> Service: crearFacturasEnLote(requests)
  Service -> Repository: saveAll(facturas con estado=PENDIENTE)
  Repository -> MySQL: INSERT INTO facturas
  Service -> Controller: List<FacturaResponse>
  Controller -> Cliente: 200 OK

  ## Estructura de archivos

  proyecto/backend/
    pom.xml, mvnw, mvnw.cmd
    src/main/java/com/hackathon/centralizador/
      CentralizadorApplication.java
      model/EstadoFactura.java, Factura.java
      dto/FacturaRequest.java, FacturaResponse.java
      repository/FacturaRepository.java
      service/FacturaService.java
      controller/FacturaController.java
      config/CorsConfig.java
    src/main/resources/application.yml
    src/test/java/.../service/FacturaServiceTest.java


  3. `config.md`

  # Config: HU-01 - Carga masiva de facturas

  ## Stack
  - Java 17 (OpenJDK Temurin 17.0.18)
  - Spring Boot 3.4.x
  - Maven Wrapper (mvnw)
  - MySQL 8.0 (Docker, puerto 3307)

  ## Dependencias pom.xml
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - mysql-connector-j (runtime)
  - lombok (compileOnly)
  - spring-boot-starter-test (test)

  ## application.yml
  - server.port: 8080
  - spring.datasource.url: jdbc:mysql://localhost:3307/centralizador
  - spring.datasource.username: root
  - spring.datasource.password: root123
  - spring.jpa.hibernate.ddl-auto: validate
  - spring.jpa.open-in-view: false

  ## Prerequisitos
  - Docker Desktop con centralizador-mysql corriendo
  - JAVA_HOME = C:\Users\1000940514\Documents\jdk-17.0.18+8

  ## Comandos
  - Compilar: ./mvnw clean compile
  - Test: ./mvnw test
  - Ejecutar: ./mvnw spring-boot:run
  - Endpoint: http://localhost:8080/api/facturas/bulk