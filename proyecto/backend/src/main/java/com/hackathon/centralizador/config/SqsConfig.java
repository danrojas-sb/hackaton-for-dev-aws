package com.hackathon.centralizador.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Configuración del cliente SQS para conectarse a LocalStack.
 */
@Configuration
public class SqsConfig {

    /**
     * Crea un bean SqsClient configurado para apuntar a LocalStack
     * con credenciales estáticas de prueba.
     *
     * @param endpoint URL del endpoint SQS (LocalStack)
     * @param region   región AWS configurada
     * @return cliente SQS configurado
     */
    @Bean
    public SqsClient sqsClient(
            @Value("${aws.sqs.endpoint}") String endpoint,
            @Value("${aws.region}") String region) {
        return SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }
}
