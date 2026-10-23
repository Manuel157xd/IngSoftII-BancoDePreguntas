# Banco de Preguntas - Arquitectura

## Descripción
El proyecto conserva la aplicación Java Desktop de la primera iteración y agrega una demo REST distribuida para la segunda. Los servicios se ejecutan como procesos Spring Boot independientes, sin Docker ni broker externo:

- **Preguntas**: creación, validación, estados y persistencia en `questions.db`.
- **Revisión**: asignaciones y evaluaciones; conserva sus datos en `reviews.db` y consulta/actualiza preguntas mediante HTTP.
- **Notificaciones**: recibe eventos HTTP y simula el envío de correo escribiéndolo en la consola.
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

Para la demostración: crea una pregunta en Preguntas; envíala a revisión con `PATCH /api/questions/{id}/enviar-revision`; asígnale de 1 a 3 revisores desde Revisión con `POST /api/reviews/assign`; consulta `GET /api/reviews/assigned/{revisorEmail}` y evalúa con `POST /api/reviews/evaluate`. Las notificaciones simuladas se ven en la terminal del servicio Notificaciones. Las preguntas de ejemplo están en `questions.db`; usa una que esté en estado `PENDIENTE_REVISION` para probar la asignación.

## Estructura de paquetes

```text
com.taller2
├── Main.java
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
│   └── repository
└── seguridad
```

### 1. Capa de presentación
`presentacion.swing` contiene las vistas implementadas con Swing. `presentacion.controller` contiene controladores que coordinan acciones de interfaz con los servicios. El flujo de preguntas pasa por `QuestionController`; el de usuarios usa `UsuarioController`.

### 2. Capa de negocio
`negocio.model` representa usuarios, preguntas y sus estados. `negocio.service` implementa casos de uso y reglas del negocio. `negocio.validation` contiene políticas de validación inyectadas en los servicios. Las preguntas nuevas quedan asociadas al ID del usuario autenticado; las preguntas preexistentes se conservan con autor desconocido. Los autores ven y editan solo las propias, y pueden dejarlas en borrador o enviarlas a revisión; administradores y revisores gestionan los estados de revisión.

### 3. Capa de persistencia
`persistencia.repository` define los contratos y las implementaciones SQLite. `persistencia.database` configura conexiones e inicializa las tablas.

`seguridad` contiene abstracciones e implementaciones para el hashing de contraseñas. `Main` construye las dependencias concretas y abre la primera vista.

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
- `UsuarioRepository`
- `QuestionRepository`
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

Todos los paquetes Java usan el espacio de nombres `com.taller2`. La aplicación Swing continúa como programa de escritorio independiente; la demo REST muestra los contextos de servicio en procesos separados y mantiene bases SQLite separadas para preguntas, revisiones e identidad.
