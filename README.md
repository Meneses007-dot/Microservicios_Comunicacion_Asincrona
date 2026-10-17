# Taller 09 — Comunicación síncrona y asíncrona de microservicios

**Proyecto Banco de Preguntas Saber PRO — Laboratorio de Ingeniería de Software II — Universidad del Cauca — 2026.2**

Arquitectura de **seis microservicios Spring Boot** que se comunican por **REST (síncrono)** y por **RabbitMQ (asíncrono)** para el ciclo de vida de una pregunta: crear → validar → enviar a revisión → asignar revisor → aprobar/rechazar → corregir.

---

## 1. Diagrama de servicios y puertos

| Microservicio | Puerto | Responsabilidad única | Comunicación |
| --- | --- | --- | --- |
| **User Service** | 8081 | Autenticar usuarios y verificar roles | REST síncrono |
| **Validation Service** | 8082 | Validar enunciado, 4 opciones y clave | REST síncrono (sin estado) |
| **Question Service** | 8083 | Dueño del contenido; coordina el flujo | REST + **publica** comando |
| **Lifecycle Service** | 8084 | **Única autoridad** del estado y las transiciones | **Consume** comando + REST |
| **Review Service** | 8085 | Asignaciones y resultados de revisión | REST + **publica** evento |
| **Notification Service** | 8086 | Notificaciones sin bloquear el flujo | **Solo consume** (asíncrono puro) |

```
                 REST síncrono                                    RabbitMQ (asíncrono)
 Cliente ──┬──► User :8081                                                   ┌────────────────────────┐
           │                                                               │  saberpro.direct.exchange │
           ├──► Validation :8082 ◄──── Question :8083 ──question.submit──► │  (Direct, durable)        │
           │                                   │                           └────────────────────────┘
           ├──► Lifecycle :8084 ◄──────────────┘                              │              ▲
           │        ▲                                                         │              │
           ├──► Review :8085 ────────────reviewer.assigned──────────────────►│              │
           │        │                                                        ▼              │
           └──► Notification :8086 ◄──────────────────────────────── notification.reviewer.assigned.queue
```

**Flujo del Escenario 1**

1. Login síncrono → `POST /auth/login` (200/401).
2. Creación → `POST /questions` en Question Service.
3. Validación síncrona → Question Service llama a `POST /validate`; válida ⇒ persiste `DRAFT` (201); inválida ⇒ 400 sin persistir.
4. Envío → `POST /questions/{id}/submit`; Question Service publica `SubmitCommandDTO` con `question.submit`.
5. Confirmación inmediata → **202** con `operationId` y `status:"PROCESSING"` (no se espera a Lifecycle).
6. Procesamiento → `@RabbitListener` en Lifecycle: idempotente por `operationId`; aplica `DRAFT → PENDING_REVIEW` y guarda `OperationResult` (`COMPLETED` o `FAILED`).
7. Asignación → `POST /reviews/assign`; Review exige `PENDING_REVIEW`, valida al revisor, guarda `PENDING`, pide `IN_REVIEW` a Lifecycle y confirma `ASSIGNED` (o marca `FAILED`).
8. Notificación → Review publica `ReviewerAssigned`; Notification lo consume y simula el envío sin bloquear la asignación.

---

## 2. Stack

- **Java 17** y **Maven** (cada servicio tiene su propio `pom.xml` autónomo).
- **Spring Boot 3.3.5** con `spring-boot-starter-web`, `-amqp`, `-data-jpa`, `-validation`.
- **H2 en memoria** (una base por servicio) para no depender de instalar un motor.
- **RabbitMQ 3 (management)** vía `docker-compose.yml`.

---

## 3. Cómo levantar el sistema

### 3.1 Broker de mensajería

```bash
docker compose up -d          # levanta saberpro-rabbitmq
# consola: http://localhost:15672  (guest / guest)
```

### 3.2 Los seis servicios

Cada servicio es un proyecto Maven independiente. En una terminal por servicio:

```bash
cd user-service        && mvn spring-boot:run     # :8081
cd validation-service  && mvn spring-boot:run     # :8082
cd lifecycle-service   && mvn spring-boot:run     # :8084
cd question-service    && mvn spring-boot:run     # :8083   (depende de :8082 y :8084)
cd review-service      && mvn spring-boot:run     # :8085   (depende de :8081 y :8084)
cd notification-service && mvn spring-boot:run    # :8086
```

También se puede empaquetar y ejecutar el jar:

```bash
mvn package -DskipTests
java -jar target/<servicio>-1.0.0.jar
```

### 3.3 Pruebas automatizadas

```bash
cd validation-service  && mvn test   # reglas de validación
cd lifecycle-service   && mvn test   # máquina de estados + comando asíncrono (idempotencia, FAILED)
```

### 3.4 Colección de Postman

1. Importar `postman/SaberPro_Microservices.postman_collection.json`.
2. Importar el entorno `postman/SaberPro_local.postman_environment.json` y seleccionarlo.
3. Ejecutar la carpeta **7. Flujo E2E (ejecutar en orden)** en el *Collection Runner* para el recorrido completo, o las carpetas por microservicio para los casos puntuales.

**Usuarios de prueba** (todos con clave `secret123`):

| id | username | rol |
| --- | --- | --- |
| 1 | `teacher` | TEACHER |
| 2 | `admin` | ADMIN |
| 3 | `reviewer` | REVIEWER |
| 4 | `reviewer2` | REVIEWER |

---

## 4. Tabla de contratos REST

### User Service (:8081)

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| POST | `/auth/login` | `{username, password}` | **200** `{authenticated:true, user:{id,username,fullName,email,roles}, message}` / **401** `{authenticated:false, user:null, message}` |
| GET | `/users/{id}` | — | **200** `UserDTO` / **404** |

### Validation Service (:8082)

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| POST | `/validate` | `{text, options:[{id,text}], correctOptionId}` | **200** `{valid, errors:[...]}` (fallo de negocio) / **400** solo JSON malformado |

### Question Service (:8083)

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| POST | `/questions` | `{title, text, options, correctOptionId, authorId}` + `Authorization` opcional | **201** `{id, status:"DRAFT", message}` / **400** `{message, errors}` / **503** |
| GET | `/questions/{id}` | — | **200** pregunta / **404** |
| PUT | `/questions/{id}` | igual que POST | **200** actualizada (revalida; si Lifecycle dice `REJECTED`, pide la transición a `DRAFT`) |
| POST | `/questions/{questionId}/submit` | — | **202** `{message, operationId, status:"PROCESSING"}` / **404** / **503** |
| GET | `/operations/{operationId}` | — | `{operationId, questionId, type, status: PENDING\|COMPLETED\|FAILED, message}`; **503** si Lifecycle no responde |

### Lifecycle Service (:8084)

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| GET | `/lifecycle/{questionId}` | — | **200** `{questionId, state, changedAt, changedBy, comment}` / **404** |
| POST | `/lifecycle/transition` | `{questionId, targetState, requestedBy, comment}` | **200** `{questionId, previousState, currentState}` / **409** transición no permitida |
| GET | `/lifecycle/operations/{operationId}` | — | **200** `OperationResult {operationId, questionId, type, status, message, createdAt, completedAt}` / **404** |

### Review Service (:8085)

| Método | Ruta | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| POST | `/reviews/assign` | `{questionId, reviewerId, assignedBy}` | **201** `{assignmentId, questionId, reviewerId, status:"ASSIGNED"}` / **409** la pregunta no está en `PENDING_REVIEW` / **404** revisor inexistente / **422** sin rol `REVIEWER` / **503** Lifecycle falla (asignación `FAILED`, sin evento) |
| POST | `/reviews/{questionId}/result` | `{reviewerId, result: APPROVED\|REJECTED, comments}` | **200** con el resultado registrado / **400** rechazo sin motivo / **409** la pregunta no está en `IN_REVIEW` |

### Notification Service (:8086)

| Método | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/notifications` | **200** lista de notificaciones (solo lectura, para evidencia) |

---

## 5. Mensajería RabbitMQ

| Elemento | Valor |
| --- | --- |
| Exchange | `saberpro.direct.exchange` (Direct, durable) |
| Cola de comandos | `question.submit.review.queue` (durable) ← routing key `question.submit` |
| Cola de eventos | `notification.reviewer.assigned.queue` (durable) ← routing key `reviewer.assigned` |
| Convertidor | `Jackson2JsonMessageConverter` como `@Bean` en cada servicio |

- **Comando `SubmitCommandDTO`**: `{questionId, operationId, requestedBy, requestedAt}` (publica Question, consume Lifecycle).
- **Evento `ReviewerAssigned`**: `{questionId, reviewerId, assignedBy, occurredAt}` (publica Review, consume Notification).
- Cada servicio declara en su `RabbitMQConfig` el exchange y las colas/bindings que usa (declaración idempotente ⇒ arranque en cualquier orden).
- Constantes centralizadas por servicio (`MessagingConstants`), sin literales repetidos.
- Los DTO se **duplican por servicio** a propósito (no se comparte una librería de DTO).
- Consumidores **idempotentes** (Lifecycle usa `operationId`) y `spring.rabbitmq.listener.simple.default-requeue-rejected=false` para no relanzar en bucle; las excepciones de negocio se capturan y se registran como `FAILED`.

---

## 6. Máquina de estados (Lifecycle Service)

Enum `QuestionStatus`: `DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED, REJECTED`.

| Desde | Hacia | Quién lo solicita |
| --- | --- | --- |
| DRAFT | PENDING_REVIEW | Comando `SubmitQuestionForReview` (Question Service) |
| PENDING_REVIEW | IN_REVIEW | Review Service, al asignar revisor |
| IN_REVIEW | APPROVED | Review Service, con resultado aprobado |
| IN_REVIEW | REJECTED | Review Service, con resultado rechazado y motivo |
| REJECTED | DRAFT | Question Service, tras corregir y revalidar la pregunta |

Cualquier otra transición se rechaza con **409** (p. ej. `DRAFT → APPROVED`) y queda registrada. El **estado inicial implícito** es `DRAFT`: si Lifecycle no tiene registro de un `questionId`, lo trata como `DRAFT`.

---

## 7. Decisiones de diseño

Resolución de las incongruencias entre la Guía (GP) y el Enunciado (ET), aplicando la regla de precedencia (prevalece GP; si GP calla, ET):

| # | Tema | Decisión adoptada |
| --- | --- | --- |
| 1 | Nombre del exchange | `saberpro.direct.exchange` (Direct, durable), el del código. |
| 2 | DTO del comando | `SubmitCommandDTO` en Question y Lifecycle. «SubmitQuestionForReview» es el nombre lógico. |
| 3 | Cola del comando | `question.submit.review.queue` con routing key `question.submit`. |
| 4 | Endpoint de transición | `POST /lifecycle/transition` con `questionId` en el **cuerpo**. |
| 5 | Conjunto de estados | Enum de GP + `REJECTED`. No se implementan `PUBLISHED` ni `ARCHIVED`. |
| 6 | Tokens | **No hay JWT.** El encabezado `Authorization` se acepta de forma opcional y no se valida. |
| 7 | Cantidad de opciones | **Exactamente 4** en total y una sola correcta; el número es configurable (`validation.required-options`). |
| 8 | Respuesta de Validation ante pregunta inválida | **200** con `valid:false` y `errors`; **400** solo para JSON malformado. Question traduce `valid:false` en **400**. |
| 9 | Consulta de operaciones asíncronas | Se implementa `GET /operations/{operationId}` (delega en Lifecycle; `PENDING` si aún no hay registro). |
| 10 | Postman o Bruno | **Postman**. |
| 11 | Mensajería Review → Notification | Routing key `reviewer.assigned`, cola `notification.reviewer.assigned.queue`. |

### Decisiones propias (no fijadas por las fuentes)

- **404 en `GET /lifecycle/{questionId}` cuando no hay registro** (el ET lo pide); el estado implícito `DRAFT` se usa solo al recibir un comando o una transición, para no acoplar la creación de preguntas a Lifecycle.
- **409 para transición no permitida** y para «la pregunta no está en el estado requerido» en Review.
- **`operationId` como UUID** generado en Question Service; también es la clave de idempotencia en Lifecycle.
- **`PUT /questions/{id}`** consulta a Lifecycle y, si el estado es `REJECTED`, solicita `REJECTED → DRAFT`: así una pregunta rechazada vuelve al proceso de revisión por el flujo normal.
- **`status` de Question Service es informativo**: se fija en `DRAFT` al crear y **nunca** se actualiza (la verdad está en Lifecycle). `GET /lifecycle/{id}` es la fuente autoritativa.
- **H2 en memoria** por servicio para que las pruebas sean reproducibles desde cero (sin datos de ejecuciones previas).
- **RestTemplate con timeouts** (2 s conexión / 3 s lectura) para que un servicio caído produzca **503** en lugar de colgar la petición.
- **Resiliencia del broker**: si al publicar el comando el broker no está disponible, `submit` responde **503**; si falla la publicación del evento `ReviewerAssigned`, la asignación **no** se cae (la notificación es asíncrona y no debe bloquear el flujo).
- **Sin `@Transactional` en `ReviewService.assign`**: cada `save` confirma por separado para que el registro `FAILED` sobreviva a la excepción lanzada.

### Extensiones requeridas por la matriz de pruebas

- `GET /users/{id}` (User) para la elegibilidad del revisor.
- `GET /questions/{id}` y `PUT /questions/{id}` (Question).
- `POST /reviews/{questionId}/result` (Review).
- `GET /lifecycle/operations/{operationId}` (Lifecycle) y `GET /operations/{operationId}` (Question).
- `GET /notifications` (Notification).

---

## 8. Matriz de pruebas (Postman)

| Caso | Petición | Resultado esperado | Carpeta |
| --- | --- | --- | --- |
| Login válido | `POST /auth/login` | 200, `authenticated:true` | 1. User |
| Login incorrecto | `POST /auth/login` | 401 | 1. User |
| Crear pregunta válida | `POST /questions` | 201, `DRAFT` | 3. Question |
| Crear pregunta inválida | `POST /questions` | 400 con errores | 3. Question |
| Enviar a revisión | `submit` + `GET /operations` + `GET /lifecycle` | 202 → `COMPLETED` → `PENDING_REVIEW` | 3. Question / 4. Lifecycle |
| Aprobar en DRAFT | `POST /lifecycle/transition` (`APPROVED`) | 409 | 4. Lifecycle |
| Asignar revisor elegible | `POST /reviews/assign` | 201, `IN_REVIEW`, notificación | 5. Review / 6. Notification |
| Rechazar en `IN_REVIEW` | `POST /reviews/{id}/result` (`REJECTED`) | `REJECTED` con motivo | 5. Review |
| Corregir rechazada | `PUT /questions/{id}` + transición + nuevo envío | vuelve al proceso | 3. Question |
| Falla de transición asíncrona | `submit` sobre pregunta no `DRAFT` | 202 → `FAILED` con motivo | 3. Question |

---

## 9. Estructura del repositorio

```text
taller_09/
├── docker-compose.yml
├── .gitignore
├── README.md
├── postman/
│   ├── SaberPro_Microservices.postman_collection.json
│   └── SaberPro_local.postman_environment.json
├── user-service/          (:8081)  config/ controller/ dto/ model/ repository/ service/ exception/
├── validation-service/    (:8082)  controller/ dto/ service/ exception/
├── question-service/      (:8083)  config/ client/ controller/ dto/ messaging/ model/ repository/ service/ exception/
├── lifecycle-service/     (:8084)  config/ controller/ dto/ messaging/ model/ repository/ service/ exception/
├── review-service/        (:8085)  config/ client/ controller/ dto/ messaging/ model/ repository/ service/ exception/
└── notification-service/  (:8086)  config/ controller/ messaging/ model/ repository/ service/
```

---

## 10. Respuestas de sustentación

- **¿Por qué cada llamada es síncrona o asíncrona?** Login, creación y validación necesitan respuesta inmediata para decidir si la operación continúa (síncronas). El envío a revisión y la notificación son efectos que el cliente no necesita esperar (asíncronas).
- **¿Qué garantiza el 202?** Solo que el comando fue **aceptado** y publicado; el resultado real se consulta en `GET /operations/{operationId}`. Nunca se reporta `COMPLETED` sin que Lifecycle lo haya registrado.
- **¿Quién es dueño del estado?** Exclusivamente Lifecycle Service. Ningún otro servicio escribe el estado; los demás lo consultan o solicitan transiciones.
- **¿Qué pasa cuando un paso falla?** Validación inválida ⇒ 400 y no se persiste; servicio síncrono caído ⇒ 503; transición asíncrona inválida ⇒ `OperationResult` `FAILED` sin cambiar el estado; Lifecycle caído durante la asignación ⇒ asignación `FAILED` y sin evento.
