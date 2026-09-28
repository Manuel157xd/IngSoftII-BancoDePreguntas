# Banco de Preguntas - Arquitectura

## Descripción
Aplicación Java Desktop monolítica organizada en tres capas: presentación, negocio y persistencia. MVC se aplica dentro de presentación; no es una capa adicional.

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

Todos los paquetes Java usan el espacio de nombres `com.taller2`. La separación organiza responsabilidades dentro de un único programa de escritorio, sin convertirlo en varios servicios o aplicaciones.
