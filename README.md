# API REST de Gestión de Eventos Académicos

Esta es una API REST robusta, segura y escalable desarrollada con **Spring Boot 3.x** y **PostgreSQL** para la gestión de usuarios, eventos académicos, sesiones de conferencias, inscripciones concurrentes y generación de reportes bajo demanda.

---

## 👥 Integrantes del Grupo
* **Josué Valdez** — [jvaldez10@est.ups.edu.ec](mailto:[EMAIL_ADDRESS]) | GitHub: [jvaldez10](https://github.com/jvaldez10)
* **Domenica Uyunkar** — [[Correo ]] | GitHub: [[Usuario ]]
* **Cinthya Ramon** — [cramonm1@est.ups.edu.ec](mailto:cramonm1@est.ups.edu.ec)| GitHub: [CinthyLu]

---

## 🔗 Enlaces Públicos
* **URL Backend (Despliegue):** `https://academic-events-api.onrender.com`
* **Swagger UI (Producción):** `https://academic-events-api.onrender.com/api/swagger-ui.html`
* **Health Check:** `https://academic-events-api.onrender.com/api/actuator/health`

---


# Academic Events API

## Variables de entorno

Para habilitar la autenticación básica de Swagger en producción se requieren:

- `SWAGGER_USER`
- `SWAGGER_PASSWORD`

Ejemplo:

```bash
export SWAGGER_USER=swagger
export SWAGGER_PASSWORD=change-me
```
