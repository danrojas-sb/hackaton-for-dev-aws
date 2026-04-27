package com.hackathon.centralizador.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.centralizador.dto.SqsMessageDto;
import com.hackathon.centralizador.model.Factura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Servicio encargado de enviar mensajes a la cola SQS de envío (facturas-envio).
 * Mapea la entidad {@link Factura} a {@link SqsMessageDto}, serializa a JSON
 * y envía el mensaje mediante el cliente SQS.
 */
@Service
public class SqsProducerService {

    private static final Logger log = LoggerFactory.getLogger(SqsProducerService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueName;

    /**
     * Construye el servicio con las dependencias necesarias.
     *
     * @param sqsClient    cliente SQS configurado para LocalStack
     * @param objectMapper mapper JSON de Jackson
     * @param queueName    nombre de la cola SQS de envío
     */
    public SqsProducerService(SqsClient sqsClient,
                               ObjectMapper objectMapper,
                               @Value("${aws.sqs.queue.envio}") String queueName) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    /**
     * Envía un mensaje SQS con los datos de la factura a la cola de envío.
     * Mapea la factura a {@link SqsMessageDto}, serializa a JSON y lo envía.
     *
     * @param factura entidad factura a enviar
     * @return {@code true} si el envío fue exitoso, {@code false} si falló
     */
    public boolean enviarMensaje(Factura factura) {
        try {
            SqsMessageDto mensaje = mapToSqsMessage(factura);
            String json = objectMapper.writeValueAsString(mensaje);

            String queueUrl = resolverQueueUrl();

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(json)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Mensaje enviado a SQS para factura id={}", factura.getId());
            return true;

        } catch (JsonProcessingException e) {
            log.error("Error al serializar factura id={} a JSON: {}", factura.getId(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error al enviar mensaje SQS para factura id={}: {}", factura.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Resuelve la URL de la cola SQS a partir del nombre configurado.
     *
     * @return URL de la cola SQS
     */
    private String resolverQueueUrl() {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        return sqsClient.getQueueUrl(request).queueUrl();
    }

    /**
     * Mapea una entidad {@link Factura} a {@link SqsMessageDto}.
     * Convierte {@code fechaPago} de {@code LocalDate} a {@code String} en formato ISO-8601.
     *
     * @param factura entidad fuente
     * @return DTO con los datos para el mensaje SQS
     */
    private SqsMessageDto mapToSqsMessage(Factura factura) {
        return new SqsMessageDto(
                factura.getId(),
                factura.getCus(),
                factura.getFechaPago() != null ? factura.getFechaPago().toString() : null,
                factura.getResponsabilidadFiscal(),
                factura.getTipoDocumento(),
                factura.getNumeroDocumento(),
                factura.getNombres(),
                factura.getApellidos(),
                factura.getTelefono(),
                factura.getMunicipio(),
                factura.getDireccion(),
                factura.getEmail(),
                factura.getValorPack(),
                factura.getValorPackIva(),
                factura.getComentarios()
        );
    }
}
