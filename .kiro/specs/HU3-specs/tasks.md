# Plan de Implementación: HU-03 — Envío a SQS y Actualización de Estados

## Visión General

Implementación incremental de la integración asíncrona entre el Centralizador (Spring Boot) y una Lambda Node.js mediante dos colas SQS en LocalStack. Se comienza con la infraestructura Docker, luego la Lambda, después los componentes Spring Boot (configuración, DTOs, excepciones, servicios, controlador) y finalmente los tests. Cada paso construye sobre el anterior para mantener el código integrado en todo momento.

## Tareas

- [x] 1. Infraestructura Docker Compose — LocalStack y script de inicialización
  - [x] 1.1 Agregar servicio LocalStack al docker-compose.yml existente
    - Agregar servicio `localstack` con imagen `localstack/localstack:3.5`, puertos `4566:4566`, servicios `sqs,lambda`, volúmenes para scripts de init y Lambda, healthcheck con curl
    - Agregar `depends_on` si es necesario para orden de arranque con MySQL
    - _Requisitos: 1.1, 1.2, 1.3_
  - [x] 1.2 Crear script de inicialización de LocalStack
    - Crear archivo `proyecto/docker/localstack/init-aws.sh` con permisos de ejecución
    - El script debe crear las colas `facturas-envio` y `facturas-respuesta` con `awslocal sqs create-queue`
    - Desplegar la Lambda `lambda-contabilizadora` con runtime `nodejs20.x`, handler `index.handler`, variables de entorno para DB y SQS_RESPONSE_URL
    - Crear event-source-mapping entre la cola `facturas-envio` y la Lambda con `batch-size 1`
    - _Requisitos: 1.2, 3.4_

- [x] 2. Lambda Node.js — Contabilización en db_procesos_masivos
  - [x] 2.1 Crear handler de la Lambda (index.mjs)
    - Crear archivo `proyecto/docker/lambda/index.mjs`
    - Implementar handler que parsea el body del mensaje SQS (JSON)
    - Conectar a MySQL (`db_procesos_masivos`) usando `mysql2/promise` con credenciales de variables de entorno (`DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`)
    - Insertar registro en `facturas_contabilizadas` con todos los campos de la factura y `estado_contable = 'CONTABILIZADA'`, `factura_origen_id = facturaId`
    - En caso de éxito: enviar mensaje a `SQS_RESPONSE_URL` con `{ facturaId, status: "TERMINADO" }`
    - En caso de error: enviar mensaje a `SQS_RESPONSE_URL` con `{ facturaId, status: "ERROR", errorMessage: <descripción> }`
    - Usar AWS SDK v3 para Node.js (`@aws-sdk/client-sqs`) para enviar a la cola de respuesta
    - _Requisitos: 3.1, 3.2, 3.3, 3.4, 6.2_
  - [x] 2.2 Crear package.json para dependencias de la Lambda
    - Crear `proyecto/docker/lambda/package.json` con dependencias `mysql2` y `@aws-sdk/client-sqs`
    - _Requisitos: 3.4_

- [x] 3. Checkpoint — Verificar infraestructura
  - Asegurar que `docker-compose up` levanta LocalStack y MySQL correctamente, que las colas SQS se crean y la Lambda se despliega. Preguntar al usuario si hay dudas.

- [x] 4. Dependencias Spring Boot — AWS SDK SQS y jqwik
  - [x] 4.1 Agregar dependencias al pom.xml
    - Agregar `software.amazon.awssdk:sqs` (AWS SDK v2) como dependencia de compilación
    - Agregar BOM de AWS SDK v2 en `<dependencyManagement>` para gestión de versiones
    - Agregar `net.jqwik:jqwik:1.9.2` con scope `test` para property-based testing
    - _Requisitos: 1.4_

- [x] 5. Configuración Spring Boot — SqsConfig y application.yml
  - [x] 5.1 Crear SqsConfig.java
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/config/SqsConfig.java`
    - Bean `SqsClient` que lee `aws.sqs.endpoint` y `aws.region` de application.yml
    - Usar `StaticCredentialsProvider` con credenciales `test/test` para LocalStack
    - Usar `endpointOverride` con la URI del endpoint configurado
    - _Requisitos: 1.4_
  - [x] 5.2 Actualizar application.yml con propiedades AWS y scheduling
    - Agregar sección `aws` con `region`, `sqs.endpoint`, `sqs.queue.envio`, `sqs.queue.respuesta`
    - Agregar configuración de `spring.task.scheduling.pool.size: 2`
    - _Requisitos: 1.4_
  - [x] 5.3 Agregar @EnableScheduling a la aplicación
    - Agregar `@EnableScheduling` a `CentralizadorApplication.java` para habilitar el polling del listener
    - _Requisitos: 4.1_

- [x] 6. DTOs y Excepciones
  - [x] 6.1 Crear SqsMessageDto record
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/dto/SqsMessageDto.java`
    - Record con campos: `facturaId`, `cus`, `fechaPago`, `responsabilidadFiscal`, `tipoDocumento`, `numeroDocumento`, `nombres`, `apellidos`, `telefono`, `municipio`, `direccion`, `email`, `valorPack`, `valorPackIva`, `comentarios`
    - _Requisitos: 6.1_
  - [x] 6.2 Crear SqsResponseDto record
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/dto/SqsResponseDto.java`
    - Record con campos: `facturaId`, `status`, `errorMessage`
    - _Requisitos: 6.2_
  - [x] 6.3 Crear FacturaNotFoundException
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/exception/FacturaNotFoundException.java`
    - Excepción que recibe el ID de la factura no encontrada
    - _Requisitos: 5.3_
  - [x] 6.4 Crear InvalidStateTransitionException
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/exception/InvalidStateTransitionException.java`
    - Excepción que recibe estado actual y estado destino no permitido
    - _Requisitos: 7.2_

- [x] 7. SqsProducerService — Envío de mensajes a SQS
  - [x] 7.1 Implementar SqsProducerService
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/service/SqsProducerService.java`
    - Inyectar `SqsClient`, `ObjectMapper` y `@Value("${aws.sqs.queue.envio}")` para la URL de la cola
    - Método `enviarMensaje(Factura factura)`: mapear Factura a SqsMessageDto, serializar a JSON con ObjectMapper, enviar con `sqsClient.sendMessage()`, retornar `true` si exitoso, `false` si falla (registrar error en log)
    - Resolver la URL de la cola con `sqsClient.getQueueUrl()` usando el nombre de la cola
    - _Requisitos: 2.1, 2.3, 2.4, 6.1_
  - [ ]* 7.2 Test unitario de SqsProducerService
    - Mock de SqsClient y ObjectMapper
    - Verificar que `enviarMensaje` invoca `sendMessage` con JSON correcto
    - Verificar que retorna `false` y registra error cuando SqsClient lanza excepción
    - _Requisitos: 2.1, 2.4_
  - [ ]* 7.3 Property test — Round-trip de serialización (Propiedad 1)
    - **Propiedad 1: Round-trip de serialización del mensaje SQS**
    - Generar facturas aleatorias con todos los campos, serializar a JSON (SqsMessageDto), deserializar de vuelta, verificar igualdad de todos los campos
    - **Valida: Requisitos 2.3, 6.1**
  - [ ]* 7.4 Property test — Un mensaje por factura (Propiedad 2)
    - **Propiedad 2: Envío a SQS produce un mensaje por factura**
    - Generar lotes de N facturas (N ≥ 1), verificar que sendMessage se invoca exactamente N veces con mock de SqsClient
    - **Valida: Requisito 2.1**

- [x] 8. SqsListenerService — Polling de respuestas SQS
  - [x] 8.1 Implementar SqsListenerService
    - Crear `proyecto/backend/src/main/java/com/hackathon/centralizador/service/SqsListenerService.java`
    - Inyectar `SqsClient`, `ObjectMapper`, `FacturaService` y `@Value("${aws.sqs.queue.respuesta}")` para la URL de la cola
    - Método `@Scheduled(fixedDelay = 5000) escucharRespuestas()`: hacer polling con `receiveMessage`, deserializar cada mensaje a `SqsResponseDto`, validar campos obligatorios (`facturaId` y `status`), delegar a `FacturaService.actualizarEstadoDesdeRespuesta()`, eliminar mensaje con `deleteMessage`
    - Manejar JSON inválido: registrar error en log y descartar mensaje
    - Manejar facturaId inexistente: registrar warning en log y descartar mensaje
    - _Requisitos: 4.1, 4.2, 4.3, 4.4, 4.5, 6.3, 6.4_
  - [ ]* 8.2 Test unitario de SqsListenerService
    - Mock de SqsClient, ObjectMapper y FacturaService
    - Verificar deserialización correcta y delegación a FacturaService
    - Verificar manejo de JSON inválido (log error, no excepción)
    - Verificar manejo de facturaId inexistente (log warning)
    - _Requisitos: 4.2, 4.3, 4.4, 6.3, 6.4_
  - [ ]* 8.3 Property test — Deserialización valida campos obligatorios (Propiedad 7)
    - **Propiedad 7: Deserialización valida campos obligatorios**
    - Generar cadenas JSON aleatorias con/sin campos `facturaId` y `status`, verificar que solo se aceptan los que contienen ambos campos obligatorios
    - **Valida: Requisitos 6.3, 6.4**

- [x] 9. FacturaService — Nuevos métodos de integración SQS
  - [x] 9.1 Implementar validarTransicion() en FacturaService
    - Método privado que recibe `EstadoFactura actual` y `EstadoFactura nuevo`
    - Aceptar solo: PENDIENTE→PROCESO, PROCESO→TERMINADO, PROCESO→ERROR, ERROR→PENDIENTE
    - Lanzar `InvalidStateTransitionException` para cualquier otra combinación
    - _Requisitos: 7.1, 7.2_
  - [x] 9.2 Implementar enviarASqs() en FacturaService
    - Inyectar `SqsProducerService` en FacturaService
    - Método `enviarASqs(List<Factura> facturas)`: iterar cada factura, invocar `sqsProducerService.enviarMensaje()`, si exitoso cambiar estado a PROCESO usando `validarTransicion()`, si falla mantener PENDIENTE y registrar error
    - _Requisitos: 2.1, 2.2, 2.4_
  - [x] 9.3 Implementar actualizarEstadoDesdeRespuesta() en FacturaService
    - Método `actualizarEstadoDesdeRespuesta(Long facturaId, String status)`: buscar factura por ID, validar transición con `validarTransicion()`, actualizar estado a TERMINADO o ERROR según `status`, guardar con repository
    - Lanzar `FacturaNotFoundException` si la factura no existe
    - _Requisitos: 4.2, 4.3, 7.3, 7.4_
  - [x] 9.4 Implementar reintentarFactura() en FacturaService
    - Método `reintentarFactura(Long id)`: buscar factura, validar que está en ERROR, cambiar a PENDIENTE usando `validarTransicion()`, enviar a SQS, retornar FacturaResponse
    - Lanzar `FacturaNotFoundException` si no existe, `InvalidStateTransitionException` si no está en ERROR
    - _Requisitos: 5.1, 5.2, 5.4, 5.5_
  - [x] 9.5 Modificar crearFacturasEnLote() para disparar envío a SQS
    - Después de `saveAll`, invocar `enviarASqs(saved)` para enviar las facturas recién creadas a la cola SQS
    - _Requisitos: 2.1, 2.2_
  - [ ]* 9.6 Tests unitarios de FacturaService (métodos nuevos)
    - Mock de SqsProducerService y FacturaRepository
    - Verificar `enviarASqs()`: cambio de estado a PROCESO cuando envío exitoso, permanece PENDIENTE cuando falla
    - Verificar `actualizarEstadoDesdeRespuesta()`: actualiza a TERMINADO o ERROR correctamente
    - Verificar `reintentarFactura()`: cambia ERROR→PENDIENTE, lanza excepciones para otros estados y factura inexistente
    - Verificar `validarTransicion()`: acepta transiciones válidas, rechaza inválidas
    - _Requisitos: 2.2, 2.4, 4.2, 4.3, 5.2, 5.3, 5.4, 7.1, 7.2_
  - [ ]* 9.7 Property test — Máquina de estados (Propiedad 3)
    - **Propiedad 3: Máquina de estados — solo transiciones permitidas**
    - Generar todos los pares de estados del enum EstadoFactura, verificar que `validarTransicion` acepta solo {PENDIENTE→PROCESO, PROCESO→TERMINADO, PROCESO→ERROR, ERROR→PENDIENTE} y rechaza todas las demás
    - **Valida: Requisitos 7.1, 7.2**
  - [ ]* 9.8 Property test — Envío exitoso/fallido y estado (Propiedad 4)
    - **Propiedad 4: Envío exitoso cambia estado, envío fallido lo preserva**
    - Generar facturas en PENDIENTE, simular éxito/fallo de SQS con mock, verificar que estado cambia a PROCESO solo si exitoso
    - **Valida: Requisitos 2.2, 2.4**
  - [ ]* 9.9 Property test — Listener actualiza estado según respuesta (Propiedad 5)
    - **Propiedad 5: Listener actualiza estado según respuesta**
    - Generar facturas en PROCESO, simular respuestas TERMINADO/ERROR, verificar que el estado se actualiza correctamente
    - **Valida: Requisitos 4.2, 4.3**
  - [ ]* 9.10 Property test — Reintento solo desde ERROR (Propiedad 6)
    - **Propiedad 6: Reintento solo desde estado ERROR**
    - Generar facturas en todos los estados, verificar que reintento solo se acepta desde ERROR y se rechaza para PENDIENTE, PROCESO, TERMINADO
    - **Valida: Requisitos 5.2, 5.4**
  - [ ]* 9.11 Property test — Cambio de estado actualiza timestamp (Propiedad 8)
    - **Propiedad 8: Cambio de estado actualiza fecha_actualizacion**
    - Generar transiciones válidas, verificar que `fecha_actualizacion` es mayor o igual al valor previo después de cada transición
    - **Valida: Requisitos 7.3, 7.4**

- [-] 10. Checkpoint — Verificar servicios SQS
  - Asegurar que todos los tests unitarios pasan, que SqsProducerService y SqsListenerService compilan correctamente. Preguntar al usuario si hay dudas.

- [ ] 11. FacturaController — Endpoint PUT /reintentar
  - [~] 11.1 Agregar endpoint PUT /api/facturas/{id}/reintentar
    - Agregar método `reintentarFactura(@PathVariable Long id)` en FacturaController
    - Retornar `ResponseEntity<FacturaResponse>` con HTTP 200 en caso de éxito
    - Agregar `@ExceptionHandler` para `FacturaNotFoundException` → HTTP 404 con mensaje descriptivo
    - Agregar `@ExceptionHandler` para `InvalidStateTransitionException` → HTTP 400 con mensaje indicando que solo facturas en ERROR pueden reintentarse
    - _Requisitos: 5.1, 5.2, 5.3, 5.4, 5.5_
  - [ ]* 11.2 Tests de controlador para PUT /reintentar
    - Usar MockMvc para verificar:
    - Caso 200: factura en ERROR se reintenta exitosamente
    - Caso 404: factura no encontrada
    - Caso 400: factura en estado distinto a ERROR
    - _Requisitos: 5.3, 5.4, 5.5_

- [~] 12. Checkpoint final — Verificar integración completa
  - Asegurar que todos los tests pasan (unitarios y property-based). Verificar que el flujo completo compila sin errores. Preguntar al usuario si hay dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental
- Los property tests validan las propiedades de correctitud universales del diseño
- Los tests unitarios validan ejemplos específicos y casos borde
- Se usa jqwik 1.9.2 para property-based testing en Java
