# Gestión de Turnos – código inicial

**EIF206 – Programación III** · Guía: *JDBC, patrón Repository y migración MySQL → PostgreSQL*

Este proyecto es el **punto de partida** de la sesión. La interfaz gráfica (JavaFX + CSS), el controlador y las reglas del negocio ya funcionan, pero los turnos se guardan **en memoria** y se pierden al cerrar la aplicación. Durante la sesión se agregará la persistencia con JDBC.

## Requisitos

- Java 21 o superior
- Maven (incluido en NetBeans)
- NetBeans 21 o superior (recomendado)

JavaFX y los drivers JDBC de MySQL y PostgreSQL se descargan automáticamente con Maven.

## Cómo ejecutar

**NetBeans:** *Open Project* → seleccionar la carpeta `gestion-turnos` → *Clean and Build* → *Run*.

**Terminal:**

```bash
mvn clean javafx:run
```

> Ejecute siempre la clase `turnos.Main`, no `AppTurnos`.
> Si en consola aparece *"Unsupported JavaFX configuration: classes were loaded from 'unnamed module'"*, es solo una advertencia: la aplicación funciona normalmente.

## Estructura

```text
gestion-turnos/
├── pom.xml
├── sql/
│   ├── mysql_turnos.sql          ← script de la base MySQL
│   └── postgresql_turnos.sql     ← script de la base PostgreSQL
└── src/main/
    ├── java/turnos/
    │   ├── Main.java             ← arranque (ejecutar esta clase)
    │   ├── AppTurnos.java        ← PUNTO DE ENSAMBLAJE
    │   ├── modelo/
    │   │   ├── EstadoTurno.java
    │   │   └── Turno.java
    │   ├── servicio/
    │   │   └── TurnoService.java ← reglas del negocio + acceso a datos en memoria (temporal)
    │   ├── controlador/
    │   │   └── TurnoController.java
    │   ├── vista/
    │   │   └── VentanaPrincipal.java
    │   └── persistencia/         ← vacío: aquí se trabajará en la sesión
    └── resources/turnos/vista/
        └── estilos.css           ← toda la apariencia de la interfaz
```

## Responsabilidades

| Clase | Responsabilidad | ¿Conoce la base de datos? |
|---|---|---|
| `VentanaPrincipal` | Mostrar datos y avisar de los clics del usuario. | No |
| `TurnoController` | Traducir eventos de la vista en llamadas al servicio y mostrar resultados o mensajes. | No |
| `TurnoService` | Reglas del negocio (crear, llamar, atender, cancelar, contar). | No (y no debe llegar a conocerla) |
| `AppTurnos` | Crear los objetos y conectarlos (punto de ensamblaje). | Solo elige qué implementación usar |
| `persistencia/*` | Guardar y recuperar turnos. | Sí (se crea en la sesión) |

## Reglas del negocio implementadas

- Un turno nuevo recibe el siguiente número disponible y queda **Pendiente**.
- *Llamar siguiente* llama al turno pendiente con el número más bajo.
- Solo un turno **Pendiente** puede pasar a **Llamado**.
- Solo un turno **Llamado** puede pasar a **Atendido**.
- Solo se cancelan turnos **Pendientes** o **Llamados**.

## Credenciales

Las credenciales de la base de datos se leerán desde variables de entorno. En NetBeans se configuran en *Project Properties → Actions → Run project* y *Run file via main()* → *Set Properties* (por ejemplo `Env.MYSQL_PASSWORD=...`). Ese archivo (`nbactions.xml`) está en `.gitignore`: **no lo suba al repositorio.**
