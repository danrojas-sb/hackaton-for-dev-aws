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
  