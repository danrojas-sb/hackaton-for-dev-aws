package com.hackathon.centralizador.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.centralizador.dto.SqsResponseDto;
import com.hackathon.centralizador.exception.FacturaNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

/**
 * Servicio encargado de escuchar la cola SQS de respuesta (facturas-respuesta).
 * Realiza polling periódico, deserializa los mensajes a {@link SqsResponseDto},
 * valida campos obligatorios y delega la actualización de estado a {@link FacturaService}.
 */
@Service
public class SqsListenerService {

    private static final Logger log = LoggerFactory.getLogger(SqsListenerService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final FacturaService facturaService;
    private final String queueName;

    /**
     * Construye el servicio con las dependencias necesarias.
     *
     * @param sqsClient      cliente SQS configurado para LocalStack
     * @param objectMapper   mapper JSON de Jackson
     * @param facturaService servicio de facturas para actualizar estados
     * @param queueName      nombre de la cola SQS de respuesta
     */
    public SqsListenerService(SqsClient sqsClient,
                               ObjectMapper objectMapper,
                               FacturaService facturaService,
                               @Value("${aws.sqs.queue.respuesta}") String queueName) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.facturaService = facturaService;
        this.queueName = queueName;
    }

    /**
     * Polling periódico (cada 5 segundos) de la cola de respuesta SQS.
     * Recibe hasta 10 mensajes con long polling de 5 segundos, deserializa cada uno
     * a {@link SqsResponseDto}, valida campos obligatorios y delega la actualización
     * de estado a {@link FacturaService#actualizarEstadoDesdeRespuesta(Long, String)}.
     * Los mensajes procesados o inválidos se eliminan de la cola.
     */
    @Scheduled(fixedDelay = 5000)
    public void escucharRespuestas() {
        try {
            String queueUrl = resolverQueueUrl();

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            for (Message message : messages) {
                procesarMensaje(message, queueUrl);
            }
        } catch (Exception e) {
            log.error("Error al realizar polling de la cola de respuesta SQS: {}", e.getMessage());
        }
    }

    /**
     * Procesa un mensaje individual de la cola de respuesta.
     * Deserializa el JSON, valida campos obligatorios y delega la actualización.
     * En caso de JSON inválido o factura inexistente, descarta el mensaje.
     *
     * @param message  mensaje SQS recibido
     * @param queueUrl URL de la cola para eliminar el mensaje
     */
    private void procesarMensaje(Message message, String queueUrl) {
        try {
            SqsResponseDto respuesta = deserializarMensaje(message.body());

            if (respuesta == null) {
                log.error("Mensaje SQS con JSON inválido, descartando. MessageId={}", message.messageId());
                eliminarMensaje(message, queueUrl);
                return;
            }

            if (!validarCamposObligatorios(respuesta)) {
                log.error("Mensaje SQS sin campos obligatorios (facturaId o status), descartando. MessageId={}",
                        message.messageId());
                eliminarMensaje(message, queueUrl);
                return;
            }

            facturaService.actualizarEstadoDesdeRespuesta(respuesta.facturaId(), respuesta.status());
            log.info("Estado actualizado para factura id={} con status={}", respuesta.facturaId(), respuesta.status());
            eliminarMensaje(message, queueUrl);

        } catch (FacturaNotFoundException e) {
            log.warn("Factura no encontrada para mensaje SQS. {}", e.getMessage());
            eliminarMensaje(message, queueUrl);
        } catch (Exception e) {
            log.error("Error al procesar mensaje SQS. MessageId={}: {}", message.messageId(), e.getMessage());
        }
    }

    /**
     * Deserializa el cuerpo del mensaje JSON a {@link SqsResponseDto}.
     *
     * @param body cuerpo del mensaje en formato JSON
     * @return DTO deserializado o {@code null} si el JSON es inválido
     */
    private SqsResponseDto deserializarMensaje(String body) {
        try {
            return objectMapper.readValue(body, SqsResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error al deserializar JSON del mensaje SQS: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valida que los campos obligatorios {@code facturaId} y {@code status} estén presentes.
     *
     * @param respuesta DTO deserializado
     * @return {@code true} si ambos campos están presentes, {@code false} en caso contrario
     */
    private boolean validarCamposObligatorios(SqsResponseDto respuesta) {
        return respuesta.facturaId() != null
                && respuesta.status() != null
                && !respuesta.status().isBlank();
    }

    /**
     * Elimina un mensaje de la cola SQS usando su receipt handle.
     *
     * @param message  mensaje a eliminar
     * @param queueUrl URL de la cola
     */
    private void eliminarMensaje(Message message, String queueUrl) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        } catch (Exception e) {
            log.error("Error al eliminar mensaje SQS. MessageId={}: {}", message.messageId(), e.getMessage());
        }
    }

    /**
     * Resuelve la URL de la cola SQS a partir del nombre configurado.
     *
     * @return URL de la cola SQS de respuesta
     */
    private String resolverQueueUrl() {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        return sqsClient.getQueueUrl(request).queueUrl();
    }
}
