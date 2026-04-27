# Documento de Requisitos: HU-04 — Dashboard Angular

## Introducción

Esta historia cubre la creación del frontend Angular que consume los endpoints del microservicio Centralizador. El dashboard presenta una tabla paginada de facturas, un diagrama de torta con el indicador de estados y la funcionalidad de carga masiva de archivos CSV.

## Requisitos

### Requisito 1: Tabla de facturas con paginación

**Historia de Usuario:** Como operador del Centralizador I&E, quiero ver una tabla con las facturas cargadas para monitorear su estado.

#### Criterios de Aceptación

1. WHEN el dashboard se carga, THE tabla SHALL mostrar las columnas: numeroDocumento, nombres, apellidos, municipio, valorPack, valorPackIva, estado, fechaPago.
2. WHEN hay más de 20 facturas, THE tabla SHALL mostrar paginación con controles de página.
3. WHEN se cambia de página, THE tabla SHALL consultar GET /api/facturas?page=N&size=20.
4. WHEN el estado es PENDIENTE, THE celda SHALL mostrar badge amarillo (#FFC107).
5. WHEN el estado es PROCESO, THE celda SHALL mostrar badge azul (#2196F3).
6. WHEN el estado es TERMINADO, THE celda SHALL mostrar badge verde (#4CAF50).
7. WHEN el estado es ERROR, THE celda SHALL mostrar badge rojo (#F44336).

### Requisito 2: Diagrama de torta con indicador de estados

**Historia de Usuario:** Como operador del Centralizador I&E, quiero ver un diagrama de torta con el porcentaje de facturas por estado.

#### Criterios de Aceptación

1. WHEN el dashboard se carga, THE diagrama SHALL consultar GET /api/facturas/estados/resumen.
2. THE diagrama SHALL mostrar un sector por cada estado con colores: Pendiente=#FFC107, Proceso=#2196F3, Terminado=#4CAF50, Error=#F44336.
3. THE diagrama SHALL mostrar el porcentaje y conteo de cada estado.
4. WHEN no hay facturas, THE diagrama SHALL mostrar todos los sectores en 0.

### Requisito 3: Carga masiva de archivos CSV

**Historia de Usuario:** Como operador del Centralizador I&E, quiero cargar un archivo CSV con facturas para que se procesen masivamente.

#### Criterios de Aceptación

1. WHEN el usuario selecciona un archivo CSV, THE componente SHALL parsear el CSV a JSON usando los headers como nombres de campo.
2. WHEN el usuario confirma la carga, THE componente SHALL enviar POST /api/facturas/bulk con el JSON parseado.
3. WHEN la carga es exitosa, THE dashboard SHALL actualizar la tabla y el diagrama de torta automaticamente.
4. WHEN la carga falla por validacion, THE componente SHALL mostrar los errores retornados por el API.

### Requisito 4: Dashboard integrado

#### Criterios de Aceptación

1. THE dashboard SHALL mostrar el componente de carga en la parte superior.
2. THE dashboard SHALL mostrar el diagrama de torta y la tabla lado a lado debajo de la carga.
3. WHEN se realiza una carga exitosa, THE dashboard SHALL refrescar tanto la tabla como el diagrama.
