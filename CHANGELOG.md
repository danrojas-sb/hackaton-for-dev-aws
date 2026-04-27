# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y este proyecto adhiere a [Versionamiento Semántico](https://semver.org/lang/es/).

## [No publicado]

### Agregado
- Se creó estructura del repositorio con carpetas `boveda/` y `proyecto/`
- Se creó documentación del proyecto en Obsidian (Contexto, Sprint-1, Alcance, Estrategia de Trabajo)
- Se creó Docker Compose con MySQL 8.0 y esquemas `centralizador` + `db_procesos_masivos`
- Se creó script SQL de inicialización con tablas `facturas` y `facturas_contabilizadas`
- Se creó carpeta `.kiro/` con sprint-1 para tracking de tareas
- Se creó carpeta `postmanCollections/` para colecciones de Postman
- Se inicializó proyecto Spring Boot 3.4.5 con Maven Wrapper en `proyecto/backend/`
- Se creó entidad JPA `Factura` mapeada a tabla `centralizador.facturas` con 18 columnas
- Se creó enum `EstadoFactura` (PENDIENTE, PROCESO, TERMINADO, ERROR)
- Se creó repositorio `FacturaRepository` (JpaRepository)
- Se configuró `application.yml` con MySQL localhost:3307/centralizador y ddl-auto=validate
- Se creó DTO `FacturaRequest` con validaciones Jakarta (@NotBlank, @NotNull, @Positive, @Size, @Email)
- Se creó DTO `FacturaResponse` con todos los campos + id + estado + fechaCreacion
- Se creó `FacturaService.crearFacturasEnLote()` con mapeo a entidad, estado PENDIENTE y saveAll
- Se creó `FacturaController` con endpoint POST /api/facturas/bulk y manejo de errores de validación
- Se creó `CorsConfig` permitiendo peticiones desde localhost:4200
- Se crearon tests unitarios para `FacturaService` (estado PENDIENTE, lista vacía)
- Se crearon tests de integración para `FacturaController` (200 con facturas, 400 con errores)
- Se actualizó `sprint-1.md` marcando tareas 2 y 3 (HU-01) como completadas
- Se creó spec HU-02 con requirements, design y tasks para consulta de facturas y resumen de estados
- Se agregó query `contarPorEstado()` en `FacturaRepository` con JPQL GROUP BY estado
- Se agregaron métodos `listar(Pageable)` y `resumenEstados()` en `FacturaService`
- Se agregó endpoint GET /api/facturas con paginación (Pageable, default size=20, sort=fechaCreacion,desc)
- Se agregó endpoint GET /api/facturas/estados/resumen con conteo por estado (incluye estados en 0)
- Se crearon tests de integración para GET /api/facturas (200 con Page) y GET /api/facturas/estados/resumen (conteo correcto, estados en 0)
- Se crearon tests unitarios para `FacturaService.listar()` y `resumenEstados()`
- Se creó spec HU-03 con documento de requisitos para envío a SQS y actualización de estados (7 requisitos EARS: infraestructura LocalStack, productor SQS, Lambda contabilizadora, listener respuestas, reintento ERROR, contrato de mensajes, flujo de estados)
- Se creó plan de implementación (tasks.md) para HU-03 con 12 tareas principales: infraestructura Docker/LocalStack, Lambda Node.js, dependencias AWS SDK, configuración SQS, DTOs, excepciones, SqsProducerService, SqsListenerService, FacturaService, FacturaController y 8 property tests con jqwik
- Se agregó servicio LocalStack 3.5 al docker-compose.yml con SQS+Lambda, puerto 4566, healthcheck y depends_on MySQL
- Se creó script de inicialización `docker/localstack/init-aws.sh` que crea colas SQS (facturas-envio, facturas-respuesta), despliega Lambda contabilizadora con nodejs20.x y configura event-source-mapping con batch-size 1
- Se creó handler Lambda Node.js `docker/lambda/index.mjs` que consume mensajes SQS, inserta en `facturas_contabilizadas` con mysql2/promise y envía respuesta (TERMINADO/ERROR) a cola de respuesta con AWS SDK v3
- Se creó `docker/lambda/package.json` con dependencias pinadas mysql2@3.22.3 y @aws-sdk/client-sqs@3.1037.0, tipo ESM y engine Node.js 20.x
- Se agregó BOM de AWS SDK v2 (2.25.27) en `dependencyManagement` del pom.xml para gestión centralizada de versiones
- Se agregó dependencia `software.amazon.awssdk:sqs` (versión gestionada por BOM) para integración con colas SQS
- Se agregó dependencia `net.jqwik:jqwik:1.9.2` con scope test para property-based testing
- Se creó `SqsConfig.java` con bean `SqsClient` configurado para LocalStack (endpoint override, región y credenciales estáticas)
- Se actualizó `application.yml` con propiedades AWS (region, sqs.endpoint, colas envio/respuesta) y `spring.task.scheduling.pool.size: 2`
- Se actualizó `application-test.yml` con propiedades AWS para evitar fallos por configuración faltante en tests
- Se agregó `@EnableScheduling` a `CentralizadorApplication.java` para habilitar el polling del SqsListenerService
- Se creó `SqsMessageDto` (Java record) con 15 campos para el contrato de mensajes de la cola de envío SQS
- Se creó `SqsResponseDto` (Java record) con campos facturaId, status y errorMessage para la cola de respuesta SQS
- Se creó excepción `FacturaNotFoundException` para facturas no encontradas por ID
- Se creó excepción `InvalidStateTransitionException` para transiciones de estado no permitidas en el ciclo de vida de facturas
- Se creó `SqsProducerService` con método `enviarMensaje(Factura)` que mapea a SqsMessageDto, serializa a JSON y envía a la cola SQS de envío, con manejo de errores y logging SLF4J
- Se creó `SqsListenerService` con polling `@Scheduled(fixedDelay=5000)` de la cola SQS de respuesta, deserialización a SqsResponseDto, validación de campos obligatorios, delegación a FacturaService y manejo de JSON inválido y facturas inexistentes
- Se implementó método `validarTransicion()` en FacturaService con máquina de estados (PENDIENTE→PROCESO, PROCESO→TERMINADO, PROCESO→ERROR, ERROR→PENDIENTE) y lanzamiento de InvalidStateTransitionException
- Se implementó método `enviarASqs()` en FacturaService que itera facturas, envía a SQS vía SqsProducerService, cambia estado a PROCESO si exitoso o mantiene PENDIENTE si falla
- Se implementó método `actualizarEstadoDesdeRespuesta()` en FacturaService con @Transactional para actualizar estado según respuesta Lambda (TERMINADO/ERROR)
- Se implementó método `reintentarFactura()` en FacturaService con @Transactional para cambiar ERROR→PENDIENTE y reenviar a SQS
- Se modificó `crearFacturasEnLote()` para invocar `enviarASqs()` después de persistir el lote
- Se inyectó SqsProducerService como dependencia en FacturaService (constructor con dos parámetros)

### Corregido
- Se corrigió bug de delayed expansion en `mvnw.cmd` que impedía ejecutar Maven Wrapper en Windows
