# API REST de Gestión de Eventos Académicos

Backend desarrollado con **Java 17**, **Spring Boot 3.3.4**, **PostgreSQL** y **Redis** para administrar eventos académicos, categorías, sesiones, inscripciones, auditoría, seguridad y reportes descargables.

## Integrantes

| Integrante | Correo institucional | GitHub |
|---|---|---|
| Cinthya Ramón | cramonm1@est.ups.edu.ec | [CinthyLu](https://github.com/CinthyLu) |
| Domenica Uyunkar | duyunkar@est.ups.edu.ec | [dome323](https://github.com/dome323) |
| Josué Valdez | jvaldezl1@est.ups.edu.ec | [Josuelv14](https://github.com/Josuelv14) |

## Enlaces del proyecto

- **Repositorio:** https://github.com/CinthyLu/icc-ppw-proyecto-final
- **Backend desplegado en Render:** https://academic-events-api-h1kf.onrender.com
- **Swagger UI:** https://academic-events-api-h1kf.onrender.com/api/swagger-ui/index.html#/
- **Health Check:** https://academic-events-api-h1kf.onrender.com/api/actuator/health
- **Video de presentación:** [Ver video](https://www.youtube.com/watch?v=4mKnACQPmYs)

## Credenciales de evaluación

> Las siguientes cuentas fueron creadas exclusivamente para las pruebas y la evaluación académica del proyecto.

### Acceso a Swagger — Basic Auth

```text
Usuario: evaluador

```

### Inicio de sesión con usuario administrador

```text
Usuario: admin@ups.edu.ec

```

### Inicio de sesión con usuario participante

```text
Usuario: domenica.demo@ups.edu.ec

```

El acceso Basic Auth de Swagger y el inicio de sesión de la API son autenticaciones diferentes.

Primero se debe ingresar a Swagger con las credenciales Basic Auth. Después, para ejecutar los endpoints protegidos, se debe iniciar sesión mediante:

```text
POST /api/auth/login
```

El endpoint de login devuelve un token JWT, el cual debe colocarse en el botón **Authorize** de Swagger.

> Después de finalizar la evaluación, se recomienda cambiar o eliminar las credenciales de prueba publicadas en este documento.

## Funcionalidades principales

- Registro e inicio de sesión con JWT.
- Renovación de tokens mediante Refresh Token.
- Cierre de sesión con invalidación de tokens en Redis.
- Control de acceso por roles: `ADMIN`, `ORGANIZER` y `PARTICIPANT`.
- Gestión de categorías, eventos y sesiones.
- Validación de propiedad de eventos para organizadores.
- Inscripciones con control transaccional de cupos.
- Prevención de inscripciones duplicadas.
- Prevención de sobreventa de cupos.
- Auditoría de operaciones relevantes.
- Rate limiting y protección frente a intentos repetidos.
- Reportes de inscritos en Excel y PDF.
- Certificados individuales en PDF.
- Documentación OpenAPI mediante Swagger UI.
- Despliegue mediante Docker y Render.

## Arquitectura

El proyecto utiliza una arquitectura monolítica modular organizada por dominios para asegurar una alta cohesión y bajo acoplamiento.

### Estructura de Módulos y Paquetes

```text
src/main/java/ec/edu/ups/icc/events/
├── audit/                    # Módulo de Auditoría (AOP)
│   ├── annotations/          # @Auditable para marcar métodos a registrar
│   ├── aspects/              # Aspectos que interceptan operaciones y registran bitácoras
│   ├── entities/             # Entidad AuditLogEntity para persistencia en base de datos
│   ├── repositories/         # Repositorio AuditLogRepository
│   └── services/             # Servicios para manejo de bitácoras de auditoría
├── auth/                     # Módulo de Autenticación de Usuarios y JWT
│   ├── controllers/          # Controladores (registro, login, refresh, logout)
│   ├── dtos/                 # DTOs de entrada y salida (LoginRequestDto, RegisterRequestDto)
│   ├── mappers/              # Mapeadores de entidades y DTOs
│   └── services/             # AuthService y AuthServiceImpl
├── categories/               # Módulo de Categorías de Eventos
│   ├── controllers/          # CategoryController
│   ├── dtos/                 # CreateCategoryDto, CategoryResponseDto
│   ├── entities/             # CategoryEntity
│   ├── mappers/              # CategoryMapper
│   ├── repositories/         # CategoryRepository
│   └── services/             # CategoryService y CategoryServiceImpl
├── core/                     # Capa transversal y Manejo Centralizado de Excepciones
│   ├── dtos/                 # Formatos comunes de respuesta (ApiErrorResponse)
│   ├── entities/             # BaseEntity
│   ├── exceptions/           # Excepciones personalizadas y GlobalExceptionHandler (@RestControllerAdvice)
│   └── security/             # OpenApiConfig (configuración de Swagger y seguridad JWT)
├── events/                   # Módulo de Eventos Académicos
│   ├── controllers/          # EventController (búsqueda, filtros, CRUD)
│   ├── dtos/                 # DTOs de eventos (CreateEventDto, EventFilterDTO)
│   ├── entities/             # EventEntity, EventModality (ONLINE, PRESENTIAL, HYBRID), EventStatus
│   ├── mappers/              # EventMapper
│   ├── repositories/         # EventRepository (búsqueda pesimista e índices)
│   └── services/             # EventService y EventServiceImpl
├── registrations/            # Módulo de Inscripciones y Control Transaccional de Cupos
│   ├── controllers/          # RegistrationController (inscripción, cancelación, consultas)
│   ├── dtos/                 # DTOs de inscripciones (RegistrationResponseDto)
│   ├── entities/             # RegistrationEntity, RegistrationStatus (CONFIRMED, CANCELLED)
│   ├── mappers/              # RegistrationMapper
│   ├── repositories/         # RegistrationRepository
│   └── services/             # RegistrationService y RegistrationServiceImpl
├── reports/                  # Módulo de Reportes Descargables en Memoria
│   ├── controllers/          # ReportController (Excel, PDF y certificados)
│   ├── services/             # ExcelReportService, PdfReportService y ReportAccessService
│   └── utils/                # ReportDateTimeUtils (zona horaria America/Guayaquil)
├── security/                 # Seguridad de la API, Filtros y Rate Limiting
│   ├── config/               # SecurityConfig, SecurityBeansConfig, RateLimitingProperties
│   ├── filters/              # JwtAuthenticationFilter (validación de tokens), RateLimitingFilter (Redis)
│   └── services/             # JwtService (generación, validación y expiración)
├── sessions/                 # Módulo de Sesiones y Cronogramas de Eventos
│   ├── controllers/          # SessionController (sesiones por evento)
│   ├── dtos/                 # CreateSessionDto, SessionResponseDto
│   ├── entities/             # SessionEntity
│   ├── mappers/              # SessionMapper
│   ├── repositories/         # SessionRepository
│   └── services/             # SessionService y SessionServiceImpl
└── users/                    # Módulo de Usuarios y Roles
    ├── entities/             # UserEntity, RoleEntity (Roles ADMIN, ORGANIZER, PARTICIPANT)
    ├── repositories/         # UserRepository, RoleRepository
    └── services/             # CustomUserDetailsService (cargado de roles por Spring Security)
```

Los servicios se definen mediante interfaces y clases `*ServiceImpl`, reduciendo el acoplamiento y facilitando las pruebas y el mantenimiento del sistema.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 17 | Lenguaje principal |
| Spring Boot 3.3.4 | Framework backend |
| Spring Security | Autenticación y autorización |
| JWT | Access Token y Refresh Token |
| Spring Data JPA / Hibernate | Persistencia |
| PostgreSQL | Base de datos relacional |
| Redis | Blacklist de tokens, bloqueos y rate limiting |
| Apache POI | Generación de reportes Excel |
| OpenPDF | Generación de reportes y certificados PDF |
| Gradle Kotlin DSL | Gestión de dependencias |
| Docker / Docker Compose | Contenedores locales |
| Render | Despliegue en la nube |
| Swagger / OpenAPI | Documentación interactiva |
| Postman | Pruebas manuales de endpoints |

## Modelo de base de datos

El siguiente diagrama fue generado a partir del esquema PostgreSQL del proyecto:

![Diagrama entidad-relación](./docs/database/diagrama-er.png)

### Entidades y Tablas Principales:

* `users`: Almacena la información de los usuarios registrados (nombre, correo institucional, contraseña cifrada con BCrypt, estado habilitado/bloqueado, fechas de creación/actualización).
* `roles`: Registra los roles del sistema (`ROLE_ADMIN`, `ROLE_ORGANIZER`, `ROLE_PARTICIPANT`).
* `user_roles`: Tabla intermedia de relación muchos a muchos entre usuarios y roles.
* `categories`: Categorías temáticas de los eventos (ej. Inteligencia Artificial, Ciberseguridad, Desarrollo de Software).
* `events`: Datos principales de los eventos académicos (título, descripción, modalidad ONLINE/PRESENTIAL/HYBRID, ubicación, cupo inicial, asientos disponibles, fechas de inicio y fin, estado DRAFT/PUBLISHED/CANCELLED/FINISHED, categoría y organizador).
* `sessions`: Sesiones o conferencias individuales programadas dentro de un evento específico (título, descripción, horarios, salón).
* `registrations`: Registro de las inscripciones de los participantes en los eventos con su estado (CONFIRMED/CANCELLED) y fecha de inscripción. Posee restricción única de par (user_id, event_id).
* `audit_logs`: Registros de auditoría automáticos de operaciones del sistema (usuario ejecutor, acción realizada, recurso, identificador, dirección IP, User-Agent, detalles, fecha).

---

## Reporte de Actividades Desarrolladas por Rubro

A continuación se detalla punto por punto el estado de cumplimiento de los requisitos obligatorios especificados en la rúbrica del proyecto integrador:

1. **Arquitectura general**: Cumplido. La API cuenta con una arquitectura monolítica modular organizada por dominio. Cada paquete separa controladores, DTOs, mapeadores, repositorios, entidades, servicios e implementaciones.
2. **Modelo de datos**: Cumplido. Se definen las entidades correspondientes al script SQL y se configura Hibernate con `ddl-auto=validate` para validar la correspondencia sin modificar la base de datos de producción.
3. **Roles y permisos**: Cumplido. Se implementaron los roles `ADMIN`, `ORGANIZER` y `PARTICIPANT`. La pertenencia de eventos se valida en `EventServiceImpl` y la propiedad de reportes/certificados en `ReportAccessServiceImpl`.
4. **Flujo funcional**: Cumplido. Se implementan los flujos CRUD mínimos de cada módulo.
5. **Autenticación y autorización**: Cumplido. Contraseñas cifradas con BCrypt, renovación mediante Refresh Token, cierre de sesión con invalidación en Redis mediante blacklist, y protección con `@PreAuthorize`.
6. **Redis y rate limiting**: Cumplido. Se implementaron contadores atómicos para límites por IP/usuario, bloqueos temporales por intentos fallidos de login y lista negra de tokens en Redis.
7. **Límites de solicitudes**: Cumplido. El filtro intercepta peticiones retornando `429 Too Many Requests` y cabeceras `Retry-After`.
8. **CORS restringido**: Cumplido. Lee el origen desde variables de entorno, restringe métodos a `GET, POST, PUT, PATCH, DELETE, OPTIONS` y restringe cabeceras.
9. **Reglas de negocio y transacciones**: Cumplido. Validación de duplicados, fechas y cupos. El control de concurrencia para evitar la sobreventa de asientos se realiza utilizando bloqueo pesimista (`SELECT FOR UPDATE`). Se aplica soft-delete a los eventos publicados.
10. **Manejo centralizado de excepciones**: Cumplido. Implementa `@RestControllerAdvice` retornando respuestas de error estructuradas (`ApiErrorResponse`) con desglose de validaciones por campo.
11. **Swagger y OpenAPI protegidos**: Cumplido. Swagger UI está documentado completamente y se encuentra protegido en producción (`prod`) bajo autenticación básica (Basic Auth), mientras que en desarrollo es público.
12. **Actuator y observabilidad**: Cumplido. Expone únicamente `/actuator/health` ocultando detalles internos.
13. **Reportes y descargas**: Cumplido. Exporta en memoria la lista de inscritos a Excel (Apache POI) y PDF (OpenPDF), además de generar certificados en PDF con código de verificación.
15. **Despliegue**: Cumplido. Desplegado en contenedores Docker y configurado en Render usando `render.yaml` y optimizaciones de memoria para la JVM.
16. **Variables de entorno**: Cumplido. Toda la información sensible y de conexión a bases de datos se encuentra parametrizada mediante variables de entorno.
17. **Zona Horaria**: Cumplido. Persistencia de instantes en base de datos en UTC y conversión al huso horario de negocio (`America/Guayaquil`) para visualización y reportes.

---

## Configuración

Las credenciales de infraestructura y los datos sensibles deben configurarse mediante variables de entorno.

Variables relevantes para Swagger en producción:

```env
SPRING_PROFILES_ACTIVE=prod
SWAGGER_USER=usuario_configurado
SWAGGER_PASSWORD=contraseña_configurada
```

También se utilizan variables de entorno para configurar:

- PostgreSQL.
- Redis.
- CORS.
- Seguridad JWT.
- Perfil activo de Spring.

No se deben publicar en el repositorio:

- Credenciales reales de PostgreSQL.
- Credenciales de Redis.
- Claves privadas JWT.
- Tokens activos.
- Contraseñas de infraestructura.
- Variables privadas del servicio de Render.

Las credenciales incluidas en este README corresponden únicamente a cuentas de demostración para la evaluación.

## Ejecución local

### Requisitos

- Java 17.
- Docker Desktop.
- Git.

### 1. Clonar el repositorio

```bash
git clone https://github.com/CinthyLu/icc-ppw-proyecto-final.git
cd icc-ppw-proyecto-final
```

### 2. Levantar PostgreSQL y Redis

```bash
docker compose up -d postgres redis
```

### 3. Ejecutar las pruebas

En Windows:

```powershell
.\gradlew.bat clean test
```

En Linux o macOS:

```bash
./gradlew clean test
```

### 4. Iniciar la API

En Windows:

```powershell
.\gradlew.bat bootRun
```

En Linux o macOS:

```bash
./gradlew bootRun
```

### 5. Abrir Swagger local

```text
http://localhost:8080/api/swagger-ui/index.html#/
```

## Seguridad de Swagger

En el entorno de producción, Swagger está protegido mediante autenticación Basic.

Comportamiento esperado:

- Sin credenciales válidas: `HTTP 401 Unauthorized`.
- Con credenciales válidas: `HTTP 200 OK`.

Las credenciales se obtienen de las siguientes variables de entorno:

```env
SWAGGER_USER
SWAGGER_PASSWORD
```

La protección de Swagger es diferente a la autenticación JWT utilizada por los usuarios de la API.

## Autenticación de usuarios

El sistema utiliza JWT para proteger los endpoints privados.

Endpoints principales:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

Proceso de autenticación:

1. El usuario inicia sesión mediante `POST /api/auth/login`.
2. El sistema valida las credenciales.
3. Se genera un `accessToken`.
4. También se genera un `refreshToken`.
5. El `accessToken` se utiliza para acceder a los endpoints protegidos.
6. El cierre de sesión invalida el token mediante Redis.

## Roles y permisos

### ADMIN

Puede administrar los recursos del sistema y acceder a los reportes completos.

### ORGANIZER

Puede administrar sus propios eventos y descargar los reportes de los eventos que le pertenecen.

### PARTICIPANT

Puede consultar eventos, inscribirse y descargar el certificado correspondiente a su propia inscripción.

## Control de inscripciones

El sistema implementa control transaccional para prevenir errores de concurrencia.

Antes de registrar una inscripción se verifica:

- Que el evento exista.
- Que el evento se encuentre publicado.
- Que existan cupos disponibles.
- Que el participante no esté inscrito previamente.
- Que la operación se ejecute dentro de una transacción.

Se utiliza bloqueo pesimista para evitar que varios participantes ocupen simultáneamente el último cupo disponible.

## Reportes descargables

Endpoints principales:

```text
GET /api/reports/events/{eventId}/registrations.xlsx
GET /api/reports/events/{eventId}/registrations.pdf
GET /api/registrations/{id}/certificate.pdf
```

### Reporte Excel

Permite descargar la lista de inscritos de un evento en formato `.xlsx`.

El archivo es generado mediante Apache POI e incluye:

- Información del evento.
- Datos de los participantes.
- Estado de las inscripciones.
- Fechas formateadas.
- Encabezados y estilos.
- Ajuste automático de columnas.

### Reporte PDF

Permite descargar la lista de inscritos de un evento en formato PDF.

### Certificado individual

Permite que el participante descargue el certificado correspondiente a su inscripción.

El certificado incluye:

- Logo institucional.
- Datos del participante.
- Nombre del evento.
- Estado de la inscripción.
- Código de verificación.
- Fecha de emisión.
- Zona horaria de Ecuador.

Los documentos se generan directamente en memoria mediante `ByteArrayOutputStream`, evitando almacenar archivos temporales en el servidor.

Las fechas se presentan utilizando la zona horaria:

```text
America/Guayaquil
```

Los reportes completos de inscritos requieren un usuario con rol `ADMIN` o un `ORGANIZER` autorizado.

El certificado individual puede ser descargado por el participante propietario de la inscripción.

## Colección de Postman

La colección se encuentra en:

`docs/postman/academic-events-api.postman_collection.json`

### Importación

1. Abrir Postman.
2. Seleccionar **Import**.
3. Elegir el archivo JSON.
4. Verificar la variable `baseUrl`.
5. Ejecutar el login para almacenar automáticamente los tokens de acceso.

La colección permite probar:

- Autenticación.
- Categorías.
- Eventos.
- Inscripciones.

La variable principal de la colección apunta al servidor desplegado:

```text
https://academic-events-api-h1kf.onrender.com/api
```

## Evidencias

### Pruebas automatizadas

![Pruebas exitosas](./evidences/01-pruebas-exitosas.png)

### Swagger protegido en producción

![Swagger Basic Auth](./evidences/02-swagger-basic-auth.png)

### Endpoints documentados

![Swagger endpoints](./evidences/03-swagger-endpoints.png)

### Reporte Excel

![Reporte Excel](./evidences/04-reporte-excel.png)

### Reporte PDF

![Reporte PDF](./evidences/05-reporte-pdf.png)

### Despliegue en Render

![Despliegue Render](./evidences/06-render-deploy.png)

### Colección de pruebas en Postman

![Colección Postman](./evidences/07-postman-collection.png)

## Video de presentación

El video presenta:

- Arquitectura del proyecto.
- Autenticación y seguridad.
- Reglas de negocio.
- Control transaccional de cupos.
- Gestión de eventos e inscripciones.
- Generación de reportes.
- Protección de Swagger.
- Demostración del sistema desplegado en Render.

**Enlace del video:**

[Ver presentación del proyecto](PEGAR_AQUI_EL_ENLACE_DEL_VIDEO)

## Uso académico

Proyecto desarrollado con fines académicos para la asignatura de Programación Web.