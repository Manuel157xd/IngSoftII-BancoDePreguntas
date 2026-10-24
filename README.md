# Banco de Preguntas - Arquitectura

## Descripción
El proyecto conserva la aplicación Java Desktop de la primera iteración y agrega una demo REST distribuida para la segunda. Los servicios se ejecutan como procesos Spring Boot independientes, sin Docker ni broker externo:

- **Preguntas**: creación, validación, estados y persistencia en `questions.db`.
- **Revisión**: asignaciones y evaluaciones; conserva sus datos en `reviews.db` y consulta/actualiza preguntas mediante HTTP.
- **Notificaciones**: recibe eventos HTTP, guarda avisos en `notifications.db` y los muestra en la bandeja del escritorio. También imprime en consola una simulación del correo; no envía correo real.
- **Identidad**: autenticación y usuarios en `identity.db`.

La comunicación entre Preguntas y Revisión es HTTP REST. Los eventos se envían por HTTP al servicio de Notificaciones; es una implementación didáctica para ejecutar localmente, no un broker durable ni una garantía de entrega de producción. `Main` sigue iniciando la aplicación Swing de la iteración anterior. `MicroservicesApplication` conserva el modo combinado de desarrollo; para mostrar la separación, iniciar las aplicaciones de servicio descritas abajo.

## Ejecutar la demo distribuida

Desde la carpeta `IngSoftII-BancoDePreguntas`, abre cuatro terminales PowerShell y ejecuta un comando en cada una. No cierres las terminales mientras uses la demo:

```powershell
mvn '-Dspring-boot.run.arguments=--app.service=notifications --spring.profiles.active=distributed --server.port=8083' spring-boot:run
```

```powershell
mvn '-Dspring-boot.run.arguments=--app.service=questions --spring.profiles.active=distributed --server.port=8081' spring-boot:run
```

```powershell
mvn '-Dspring-boot.run.arguments=--app.service=reviews --spring.profiles.active=distributed --server.port=8082' spring-boot:run
```

```powershell
mvn '-Dspring-boot.run.arguments=--app.service=identity --spring.profiles.active=distributed --server.port=8084' spring-boot:run
```

Inicia primero Notificaciones y Preguntas, después Revisión e Identidad. La documentación OpenAPI está en:

- Preguntas: `http://localhost:8081/swagger-ui/index.html`
- Revisión: `http://localhost:8082/swagger-ui/index.html`
- Identidad: `http://localhost:8084/swagger-ui/index.html`

Con los cuatro servicios arriba, inicia la interfaz Swing reutilizada desde una quinta terminal:

```powershell
mvn '-Dspring-boot.run.arguments=--app.service=desktop' spring-boot:run
```

La interfaz Swing consume Identidad, Preguntas, Revisión y Notificaciones por HTTP a través de `persistencia.remote`; no inicializa ni accede a las bases SQLite de la aplicación monolítica. En el login puedes usar las cuentas de demostración. En el panel principal, el botón **Notificaciones** abre la bandeja del usuario: las asignaciones aparecen al revisor correspondiente y el resultado de una evaluación aparece al autor. Los avisos se conservan en `notifications.db` y se pueden marcar como leídos. Un usuario con rol `REVISOR` también verá el botón “Mis preguntas asignadas” para abrir la vista de evaluación. Las URLs se pueden cambiar con propiedades `banco.services.questions.url`, `banco.services.identity.url`, `banco.services.reviews.url`, `banco.services.notifications.url` o con variables de entorno `BANCO_QUESTIONS_URL`, `BANCO_IDENTITY_URL`, `BANCO_REVIEWS_URL`, `BANCO_NOTIFICATIONS_URL`.

Esta bandeja es un mecanismo demostrativo para el entorno local: las API no validan una sesión/token antes de consultar el destinatario, así que no debe exponerse como está a una red pública.

Para la demostración: crea una pregunta en Preguntas; envíala a revisión con `PATCH /api/questions/{id}/enviar-revision`; asígnale de 1 a 3 revisores desde Revisión con `POST /api/reviews/assign`; consulta `GET /api/reviews/assigned/{revisorEmail}` y evalúa con `POST /api/reviews/evaluate`. La bandeja obtiene los avisos con `GET /api/notifications/inbox?recipient=<correo-del-revisor>` para revisores o `GET /api/notifications/inbox?recipient=USER_ID:<id>` para autores; `PATCH /api/notifications/{id}/read` con `{"recipient":"..."}` marca un aviso como leído. Las notificaciones simuladas también se imprimen en la terminal del servicio Notificaciones. Las preguntas de ejemplo están en `questions.db`; usa una que esté en estado `PENDIENTE_REVISION` para probar la asignación.

## Estructura de paquetes

```text
com.taller2
├── Main.java
├── microservices
│   ├── identity
│   ├── questions
│   ├── review
│   ├── notification
│   └── runtime
├── presentacion
│   ├── controller
│   │   ├── QuestionController.java
│   │   └── UsuarioController.java
│   └── swing
├── negocio
│   ├── model
│   ├── service
│   └── validation
│       ├── PasswordPolicy.java
│       ├── PasswordValidator.java
│       ├── QuestionPolicy.java
│       └── QuestionValidator.java
├── persistencia
│   ├── database
│   ├── repository
│   └── remote
└── seguridad
```

### 1. Capa de presentación
`presentacion.swing` contiene las vistas implementadas con Swing. `presentacion.controller` contiene controladores que coordinan acciones de interfaz con los servicios. El flujo de preguntas pasa por `QuestionController`; el de usuarios usa `UsuarioController`.

### 2. Capa de negocio
`negocio.model` representa usuarios, preguntas y sus estados. `negocio.service` implementa casos de uso y reglas del negocio. `negocio.validation` contiene políticas de validación inyectadas en los servicios. Las preguntas nuevas quedan asociadas al ID del usuario autenticado; las preguntas preexistentes se conservan con autor desconocido. Los autores ven y editan solo las propias, y pueden dejarlas en borrador o enviarlas a revisión; administradores y revisores gestionan los estados de revisión.

### 3. Capa de persistencia
`persistencia.repository` define los contratos y las implementaciones SQLite. `persistencia.database` configura conexiones e inicializa las tablas.

`persistencia.remote` contiene los adaptadores HTTP que implementan los contratos de repositorio del frontend y delegan las operaciones en Identidad, Preguntas y Revisión. En el modo de escritorio, `Main` ensambla esos adaptadores y abre la vista de login; no inicializa las bases SQLite locales de la aplicación monolítica. Cada microservicio conserva su propia persistencia.

`persistencia.repository` y `persistencia.database` mantienen las implementaciones SQLite de la primera iteración, utilizadas por las pruebas y el modo monolítico. `seguridad` contiene abstracciones e implementaciones para el hashing de contraseñas.

## Patrón MVC
La aplicación usa el micro-patrón MVC de forma parcial:

- Modelo: entidades del dominio (`com.taller2.negocio.model`)
- Vista: ventanas Swing (`com.taller2.presentacion.swing`)
- Controlador: `QuestionController` y `UsuarioController` en `com.taller2.presentacion.controller`

En gestión de usuarios algunas vistas todavía llaman directamente a servicios; ese flujo puede migrarse gradualmente al controlador. Las reglas y validaciones de negocio permanecen en los servicios/políticas, no en las vistas.

## Principios SOLID

### SRP (Single Responsibility Principle)
La estructura separa las responsabilidades principales por clase:
- `UsuarioService`: gestión de usuarios.
- `QuestionService`: gestión de preguntas.
- `UsuarioRepositorySQLite`: acceso a usuarios en SQLite.
- `LoginView`: render de la vista de login.

### OCP (Open/Closed Principle)
La lógica está abstraída con interfaces como:
- `UsuarioRepository` y `QuestionRepository`, implementados por SQLite en el modo local y por adaptadores HTTP en el escritorio conectado a microservicios
- `PasswordPolicy`
- `PasswordHasher`
- `QuestionPolicy`

Esto permite cambiar implementaciones sin tocar la lógica central del servicio.

### LSP (Liskov Substitution Principle)
Las implementaciones deben respetar los contratos de sus interfaces. La existencia de interfaces por sí sola no garantiza este principio; sus comportamientos deben verificarse con pruebas.

### ISP (Interface Segregation Principle)
Se usan interfaces pequeñas y específicas para repositorios y políticas de validación.

### DIP (Dependency Inversion Principle)
Los servicios reciben abstracciones como `UsuarioRepository`, `PasswordPolicy` y `PasswordHasher`. Conviene que las vistas tampoco creen repositorios concretos; `Main` es el lugar apropiado para ensamblar esas dependencias.

## Patrones de diseño GoF usados

La parte de microservicios incluye al menos seis patrones GoF de las tres categorías solicitadas:

- **Creacionales**: Builder (`QuestionBuilder`) y Factory (`NotificationFactory`).
- **Estructurales**: Facade (`StructuralValidationFacade`) y Adapter (`EmailSenderAdapter` / `LogEmailSenderAdapter`).
- **De comportamiento**: State (`QuestionState`) y Observer (`DomainEventBus` en modo combinado; el modo distribuido publica eventos al servicio de notificaciones).

### Strategy
- `PasswordHasher` y `QuestionPolicy` permiten variar hashing y validación sin cambiar los servicios.

### Observer
- `QuestionService` usa `PropertyChangeSupport` para actualizar estadísticas cuando cambian las preguntas.

### Centralización de conexiones
- `DatabaseConnection` centraliza la creación de conexiones SQLite; no mantiene una única conexión compartida.

## Ventajas de esta estructura
- Código más mantenible.
- Menor acoplamiento entre UI y negocio.
- Posible extensión de nuevas funcionalidades.
- Mejor testabilidad por capas.

Todos los paquetes Java usan el espacio de nombres `com.taller2`. La interfaz Swing se ejecuta como un proceso de escritorio separado y accede a los servicios por HTTP; la demo REST muestra los contextos de servicio en procesos separados y mantiene bases SQLite separadas para preguntas, revisiones e identidad.
