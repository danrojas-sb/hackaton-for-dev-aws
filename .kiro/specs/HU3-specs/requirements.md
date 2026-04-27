# Documento de Requisitos: HU-03 — Envío a SQS y Actualización de Estados

## Introducción

Esta historia de usuario cubre la integración asíncrona entre el microservicio Spring Boot (Centralizador) y una Lambda Node.js a través de dos colas SQS. El flujo orquesta el envío de facturas en estado PENDIENTE hacia una cola SQS de envío, donde la Lambda las consume, contabiliza en `db_procesos_masivos.facturas_contabilizadas` y responde por una cola SQS de respuesta. El Listener en Spring Boot actualiza el estado de la factura según el resultado. Adicionalmente, se incluye la infraestructura local con LocalStack en Docker Compose y un endpoint de reintento para facturas en ERROR.

## Glosario

- **Centralizador**: Microservicio Spring Boot que gestiona el ciclo de vida de las facturas. Expone el API REST y coordina el envío/recepción de mensajes SQS.
- **SqsProducerService**: Componente del Centralizador responsable de enviar mensajes a la Cola_Envio_SQS y actualizar el estado de la factura a PROCESO.
- **SqsListenerService**: Componente del Centralizador que escucha la Cola_Respuesta_SQS y actualiza el estado de la factura a TERMINADO o ERROR según la respuesta recibida.
- **Lambda_Contabilizadora**: Función Lambda en Node.js que consume mensajes de la Cola_Envio_SQS, inserta registros en `db_procesos_masivos.facturas_contabilizadas` y envía el resultado a la Cola_Respuesta_SQS.
- **Cola_Envio_SQS**: Cola SQS utilizada para enviar facturas desde el Centralizador hacia la Lambda_Contabilizadora.
- **Cola_Respuesta_SQS**: Cola SQS utilizada por la Lambda_Contabilizadora para enviar el resultado de la contabilización de vuelta al Centralizador.
- **LocalStack**: Emulador local de servicios AWS que provee SQS y Lambda para el entorno de desarrollo.
- **Factura**: Entidad persistida en `centralizador.facturas` con un campo `estado` que sigue el flujo PENDIENTE → PROCESO → TERMINADO | ERROR.
- **EstadoFactura**: Enum con valores PENDIENTE, PROCESO, TERMINADO, ERROR que representa el ciclo de vida de una factura.
- **facturas_contabilizadas**: Tabla en el esquema `db_procesos_masivos` donde la Lambda_Contabilizadora registra la contabilización de cada factura.

## Requisitos

### Requisito 1: Infraestructura local con LocalStack y SQS

**Historia de Usuario:** Como desarrollador, quiero tener LocalStack configurado en Docker Compose con las colas SQS creadas automáticamente, para poder desarrollar y probar la integración SQS sin depender de AWS.

#### Criterios de Aceptación

1. WHEN el comando `docker-compose up` se ejecuta, THE Docker_Compose SHALL iniciar un contenedor de LocalStack junto con el contenedor MySQL existente.
2. WHEN el contenedor de LocalStack alcanza estado healthy, THE Docker_Compose SHALL crear la Cola_Envio_SQS y la Cola_Respuesta_SQS mediante un script de inicialización.
3. THE Docker_Compose SHALL exponer el servicio LocalStack en el puerto 4566 del host.
4. WHEN el Centralizador se inicia, THE Centralizador SHALL conectarse a las colas SQS en LocalStack utilizando la URL del endpoint configurada en `application.yml`.

---

### Requisito 2: Envío de facturas a la Cola SQS

**Historia de Usuario:** Como sistema, quiero enviar las facturas en estado PENDIENTE a la Cola_Envio_SQS al momento de su creación, para iniciar el proceso de contabilización asíncrona.

#### Criterios de Aceptación

1. WHEN el Centralizador persiste un lote de facturas con estado PENDIENTE, THE SqsProducerService SHALL enviar un mensaje por cada factura a la Cola_Envio_SQS.
2. WHEN el SqsProducerService envía exitosamente un mensaje a la Cola_Envio_SQS, THE SqsProducerService SHALL actualizar el estado de la factura de PENDIENTE a PROCESO en la tabla `centralizador.facturas`.
3. THE SqsProducerService SHALL incluir en el cuerpo del mensaje SQS el identificador de la factura y los datos necesarios para la contabilización en formato JSON.
4. IF el envío de un mensaje a la Cola_Envio_SQS falla, THEN THE SqsProducerService SHALL mantener el estado de la factura como PENDIENTE y registrar el error en el log.

---

### Requisito 3: Lambda contabilizadora — Consumo y procesamiento

**Historia de Usuario:** Como sistema, quiero que la Lambda_Contabilizadora consuma mensajes de la Cola_Envio_SQS y contabilice las facturas en `db_procesos_masivos`, para registrar la contabilización de forma desacoplada.

#### Criterios de Aceptación

1. WHEN la Lambda_Contabilizadora recibe un mensaje de la Cola_Envio_SQS, THE Lambda_Contabilizadora SHALL insertar un registro en la tabla `db_procesos_masivos.facturas_contabilizadas` con los datos de la factura y `estado_contable = CONTABILIZADA`.
2. WHEN la Lambda_Contabilizadora contabiliza exitosamente una factura, THE Lambda_Contabilizadora SHALL enviar un mensaje de respuesta a la Cola_Respuesta_SQS con el identificador de la factura y `status = TERMINADO`.
3. IF la Lambda_Contabilizadora falla al insertar en `db_procesos_masivos.facturas_contabilizadas`, THEN THE Lambda_Contabilizadora SHALL enviar un mensaje de respuesta a la Cola_Respuesta_SQS con el identificador de la factura, `status = ERROR` y un campo `errorMessage` descriptivo.
4. THE Lambda_Contabilizadora SHALL conectarse a la base de datos MySQL utilizando las credenciales configuradas en variables de entorno.

---

### Requisito 4: Listener de respuestas SQS y actualización de estados

**Historia de Usuario:** Como sistema, quiero escuchar la Cola_Respuesta_SQS y actualizar el estado de las facturas según la respuesta de la Lambda, para reflejar el resultado de la contabilización en el Centralizador.

#### Criterios de Aceptación

1. WHILE el Centralizador está en ejecución, THE SqsListenerService SHALL escuchar continuamente la Cola_Respuesta_SQS.
2. WHEN el SqsListenerService recibe un mensaje con `status = TERMINADO`, THE SqsListenerService SHALL actualizar el estado de la factura correspondiente de PROCESO a TERMINADO en la tabla `centralizador.facturas`.
3. WHEN el SqsListenerService recibe un mensaje con `status = ERROR`, THE SqsListenerService SHALL actualizar el estado de la factura correspondiente de PROCESO a ERROR en la tabla `centralizador.facturas`.
4. IF el SqsListenerService recibe un mensaje con un identificador de factura que no existe en la base de datos, THEN THE SqsListenerService SHALL registrar una advertencia en el log y descartar el mensaje.
5. IF el SqsListenerService falla al procesar un mensaje de la Cola_Respuesta_SQS, THEN THE SqsListenerService SHALL registrar el error en el log y permitir que SQS reintente la entrega según su política de reintentos.

---

### Requisito 5: Reintento de facturas en estado ERROR

**Historia de Usuario:** Como operador del Centralizador I&E, quiero poder reintentar el procesamiento de facturas que fallaron, para recuperar facturas que quedaron en estado ERROR.

#### Criterios de Aceptación

1. WHEN el operador envía una solicitud PUT a `/api/facturas/{id}/reintentar`, THE Centralizador SHALL validar que la factura con el identificador proporcionado existe en la base de datos.
2. WHEN la factura existe y su estado es ERROR, THE Centralizador SHALL cambiar el estado de la factura de ERROR a PENDIENTE y enviarla nuevamente a la Cola_Envio_SQS a través del SqsProducerService.
3. IF la factura no existe, THEN THE Centralizador SHALL responder con código HTTP 404 y un mensaje descriptivo.
4. IF la factura existe pero su estado no es ERROR, THEN THE Centralizador SHALL responder con código HTTP 400 indicando que solo facturas en estado ERROR pueden reintentarse.
5. WHEN el reintento se ejecuta exitosamente, THE Centralizador SHALL responder con código HTTP 200 y los datos actualizados de la factura.

---

### Requisito 6: Contrato de mensajes SQS

**Historia de Usuario:** Como desarrollador, quiero un contrato de mensajes definido entre el Centralizador y la Lambda_Contabilizadora, para garantizar la interoperabilidad entre ambos componentes.

#### Criterios de Aceptación

1. THE SqsProducerService SHALL enviar mensajes a la Cola_Envio_SQS en formato JSON con los campos: `facturaId` (Long), `cus` (String), `fechaPago` (String ISO-8601), `responsabilidadFiscal` (String), `tipoDocumento` (String), `numeroDocumento` (String), `nombres` (String), `apellidos` (String), `telefono` (String), `municipio` (String), `direccion` (String), `email` (String), `valorPack` (Decimal), `valorPackIva` (Decimal) y `comentarios` (String).
2. THE Lambda_Contabilizadora SHALL enviar mensajes a la Cola_Respuesta_SQS en formato JSON con los campos: `facturaId` (Long), `status` (String, valores posibles: TERMINADO, ERROR) y `errorMessage` (String, presente solo cuando `status = ERROR`).
3. WHEN el SqsListenerService recibe un mensaje de la Cola_Respuesta_SQS, THE SqsListenerService SHALL deserializar el JSON y validar que los campos `facturaId` y `status` están presentes antes de procesar.
4. IF el SqsListenerService recibe un mensaje con formato JSON inválido o campos obligatorios ausentes, THEN THE SqsListenerService SHALL registrar el error en el log y descartar el mensaje.

---

### Requisito 7: Flujo de estados consistente

**Historia de Usuario:** Como sistema, quiero garantizar que las transiciones de estado de las facturas sigan el flujo definido, para mantener la integridad del ciclo de vida de cada factura.

#### Criterios de Aceptación

1. THE Centralizador SHALL permitir únicamente las siguientes transiciones de estado: PENDIENTE → PROCESO, PROCESO → TERMINADO, PROCESO → ERROR, ERROR → PENDIENTE.
2. IF se intenta una transición de estado no permitida, THEN THE Centralizador SHALL rechazar la operación y registrar una advertencia en el log.
3. WHEN el SqsProducerService actualiza el estado de una factura a PROCESO, THE Centralizador SHALL actualizar el campo `fecha_actualizacion` de la factura.
4. WHEN el SqsListenerService actualiza el estado de una factura a TERMINADO o ERROR, THE Centralizador SHALL actualizar el campo `fecha_actualizacion` de la factura.
