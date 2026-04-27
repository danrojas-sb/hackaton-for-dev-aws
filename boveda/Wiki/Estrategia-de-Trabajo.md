# Estrategia de Trabajo en Equipo — 3 Personas con Kiro

## Principio Clave

Cada persona trabaja en **carpetas y archivos completamente separados**. Si nadie toca los mismos archivos, no hay conflictos de merge.

## Asignación por Persona

| Persona | Rol | Carpeta exclusiva | Tasks |
|---------|-----|-------------------|-------|
| **Persona 1** | Backend - Entidad y API | `proyecto/backend/` | Task 2, 3, 4 |
| **Persona 2** | Backend - SQS + Lambda + Docker | `proyecto/lambda/`, `docker-compose.yml`, `proyecto/backend/src/.../sqs/` | Task 5, 6, 7 |
| **Persona 3** | Frontend | `proyecto/frontend/` | Task 8, 9 |

## Reglas para Evitar Conflictos

### 1. Cada persona trabaja en su propia rama

```bash
# Persona 1
git checkout -b feature/backend-api

# Persona 2
git checkout -b feature/backend-sqs-lambda

# Persona 3
git checkout -b feature/frontend-dashboard
```

### 2. Orden de merge obligatorio

Las ramas se mergean en este orden porque hay dependencias:

```
1° → Persona 1 (feature/backend-api)              → merge a main
2° → Persona 2 (feature/backend-sqs-lambda)        → pull main, merge a main
3° → Persona 3 (feature/frontend-dashboard)         → pull main, merge a main
```

Persona 2 depende de la entidad `Factura` y el `FacturaService` que crea Persona 1.
Persona 3 depende de los endpoints del API que crean Persona 1 y 2.

### 3. Archivos que NO debe tocar cada persona

| Archivo | Solo lo toca |
|---------|-------------|
| `pom.xml` | Persona 1 lo crea. Persona 2 agrega solo la dependencia SQS. |
| `application.yml` | Persona 1 lo crea. Persona 2 agrega solo la sección SQS. |
| `CentralizadorApplication.java` | Solo Persona 1 |
| `boveda/Wiki/*` | Solo quien tenga asignada la documentación |

### 4. Estructura de paquetes separados

```
src/main/java/com/hackathon/centralizador/
├── CentralizadorApplication.java          ← Persona 1
├── config/
│   └── CorsConfig.java                   ← Persona 1
├── model/
│   ├── Factura.java                       ← Persona 1
│   └── EstadoFactura.java                 ← Persona 1
├── dto/
│   ├── FacturaRequest.java                ← Persona 1
│   ├── FacturaResponse.java               ← Persona 1
│   └── EstadoResumenResponse.java         ← Persona 1
├── repository/
│   └── FacturaRepository.java             ← Persona 1
├── service/
│   ├── FacturaService.java                ← Persona 1
│   ├── SqsProducerService.java            ← Persona 2
│   └── SqsListenerService.java            ← Persona 2
└── controller/
    └── FacturaController.java             ← Persona 1 (endpoints base), Persona 2 (endpoint reintentar)
```

**Punto de conflicto potencial**: `FacturaController.java`. Para evitarlo:
- Persona 2 crea un controller separado: `FacturaReintentarController.java`
- O Persona 2 espera a que Persona 1 mergee y luego agrega su endpoint

### 5. Antes de cada push

```bash
# SIEMPRE hacer pull de main antes de push
git checkout main
git pull origin main
git checkout mi-rama
git rebase main
# Resolver conflictos si los hay
git push origin mi-rama
```

## Flujo de Trabajo Diario

```
1. git pull origin main                    → Traer últimos cambios
2. git checkout mi-rama                    → Ir a mi rama
3. git rebase main                         → Actualizar mi rama con main
4. ... trabajar con Kiro ...               → Hacer cambios
5. git add archivos-especificos            → Stage solo MIS archivos
6. git commit -m "feat: descripcion"       → Commit
7. git push origin mi-rama                 → Push
8. Crear PR cuando esté listo              → PR a main
```

## Comunicación

- Antes de tocar un archivo compartido (`pom.xml`, `application.yml`), avisar al equipo
- Si Kiro sugiere modificar un archivo de otra persona, **no hacerlo** — pedirle que cree un archivo nuevo
- Hacer PRs pequeños y frecuentes en lugar de un PR gigante al final
