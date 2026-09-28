# Estructura actual del proyecto

La aplicación es un monolito Java Desktop. Los paquetes de producción y pruebas siguen el mismo espacio de nombres `com.taller2`.

```text
src/
├── main/
│   ├── java/com/taller2/
│   │   ├── Main.java
│   │   ├── negocio/
│   │   │   ├── model/
│   │   │   │   ├── EstadoUsuario.java
│   │   │   │   ├── Question.java
│   │   │   │   ├── QuestionStatus.java
│   │   │   │   ├── Rol.java
│   │   │   │   └── Usuario.java
│   │   │   ├── service/
│   │   │   │   ├── QuestionService.java
│   │   │   │   └── UsuarioService.java
│   │   │   └── validation/
│   │   │       ├── PasswordPolicy.java
│   │   │       ├── PasswordValidator.java
│   │   │       ├── QuestionPolicy.java
│   │   │       └── QuestionValidator.java
│   │   ├── persistencia/
│   │   │   ├── database/
│   │   │   │   ├── ConnectionProvider.java
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── DatabaseInitializer.java
│   │   │   │   └── SQLiteConnectionProvider.java
│   │   │   └── repository/
│   │   │   │       ├── QuestionRepository.java
│   │   │   │       ├── QuestionRepositorySQLite.java
│   │   │   │       ├── UsuarioRepository.java
│   │   │   │       └── UsuarioRepositorySQLite.java
│   │   ├── presentacion/
│   │   │   ├── controller/
│   │   │   │   ├── QuestionController.java
│   │   │   │   └── UsuarioController.java
│   │   │   └── swing/
│   │   │   │       ├── DashboardView.java
│   │   │   │       ├── EditarUsuarioView.java
│   │   │   │       ├── GestionUsuariosView.java
│   │   │   │       ├── ListaUsuariosView.java
│   │   │   │       ├── LoginView.java
│   │   │   │       ├── PieChartView.java
│   │   │   │       ├── PreguntasView.java
│   │   │   │       ├── RegistroView.java
│   │   │   │       └── StatisticsView.java
│   │   └── seguridad/
│   │       ├── Argon2PasswordHasher.java
│   │       └── PasswordHasher.java
│   └── resources/  (vacío; ubicación estándar de recursos Maven)
└── test/java/com/taller2/
    ├── negocio/
    │   ├── service/
    │   │   ├── QuestionServiceTest.java
    │   │   └── UsuarioServiceTest.java
    │   └── validation/
    │       ├── PasswordValidatorTest.java
    │       └── QuestionValidatorTest.java
    ├── persistencia/database/DatabaseInitializerTest.java
    ├── persistencia/repository/
    │   ├── QuestionRepositorySQLiteTest.java
    │   └── UsuarioRepositorySQLiteTest.java
    ├── presentacion/controller/QuestionControllerTest.java
    └── presentacion/swing/PreguntasViewTest.java
```

Las vistas Swing pertenecen a `presentacion.swing`; los controladores de interfaz, a `presentacion.controller`. Los servicios y modelos forman parte de la capa de negocio, y los repositorios y conexiones de la capa de persistencia. `QuestionValidator` concentra las reglas del formulario fuera de Swing; la migración agrega `autor_id` sin descartar preguntas anteriores.