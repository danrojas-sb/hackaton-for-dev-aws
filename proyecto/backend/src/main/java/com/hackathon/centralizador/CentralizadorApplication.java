package com.hackathon.centralizador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación Centralizador de Facturas Packs.
 */
@SpringBootApplication
@EnableScheduling
public class CentralizadorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralizadorApplication.class, args);
    }
}
