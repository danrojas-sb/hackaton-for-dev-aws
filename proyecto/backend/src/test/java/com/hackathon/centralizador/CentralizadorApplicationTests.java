package com.hackathon.centralizador;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de contexto de la aplicación.
 * Usa perfil 'test' para evitar dependencia de MySQL en CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class CentralizadorApplicationTests {

    @Test
    void contextLoads() {
    }
}
