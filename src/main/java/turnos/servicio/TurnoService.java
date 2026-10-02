package turnos.servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import turnos.modelo.EstadoTurno;
import turnos.modelo.Turno;

/**
 * Reglas del negocio para la gestión de turnos.
 *
 * Esta clase NO conoce la interfaz gráfica.
 *
 * ESTADO ACTUAL DEL CÓDIGO INICIAL:
 * los turnos se guardan en una lista en memoria y se pierden al cerrar la
 * aplicación. Durante la sesión, el bloque "ACCESO A DATOS" (al final de la
 * clase) será reemplazado por un TurnoRepository.
 */
public class TurnoService {

    public TurnoService() {
        // Datos de ejemplo: los mismos que insertan los scripts de la carpeta sql/
        insertar(new Turno(1, EstadoTurno.PENDIENTE));
        insertar(new Turno(2, EstadoTurno.PENDIENTE));
        insertar(new Turno(3, EstadoTurno.ATENDIDO));
    }

    // =====================================================================
    // REGLAS DEL NEGOCIO
    // =====================================================================

    /** Crea un turno nuevo con el siguiente número disponible. */
    public Turno crearTurno() {
        int siguiente = obtenerTodos().stream()
                .mapToInt(Turno::getNumero)
                .max()
                .orElse(0) + 1;

        Turno turno = new Turno(siguiente, EstadoTurno.PENDIENTE);
        insertar(turno);
        return turno;
    }

    /** Todos los turnos, ordenados por número. */
    public List<Turno> listarTodos() {
        return obtenerTodos().stream()
                .sorted(Comparator.comparingInt(Turno::getNumero))
                .toList();
    }

    /** Turnos que se encuentran en un estado específico. */
    public List<Turno> listarPorEstado(EstadoTurno estado) {
        return listarTodos().stream()
                .filter(t -> t.getEstado() == estado)
                .toList();
    }

    /** Cantidad de turnos en cada estado (incluye los estados con 0). */
    public Map<EstadoTurno, Long> contarPorEstado() {
        Map<EstadoTurno, Long> conteo = obtenerTodos().stream()
                .collect(Collectors.groupingBy(
                        Turno::getEstado,
                        () -> new EnumMap<>(EstadoTurno.class),
                        Collectors.counting()));

        for (EstadoTurno estado : EstadoTurno.values()) {
            conteo.putIfAbsent(estado, 0L);
        }
        return conteo;
    }

    /** Llama al turno pendiente con el número más bajo. */
    public Turno llamarSiguiente() {
        Turno turno = listarPorEstado(EstadoTurno.PENDIENTE).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay turnos pendientes."));
        return llamar(turno.getNumero());
    }

    /** Regla: solo un turno PENDIENTE puede pasar a LLAMADO. */
    public Turno llamar(int numero) {
        Turno turno = buscarObligatorio(numero);
        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            throw new IllegalStateException(
                    "El turno " + numero + " no está pendiente.");
        }
        turno.setEstado(EstadoTurno.LLAMADO);
        guardarCambios(turno);
        return turno;
    }

    /** Regla: solo un turno LLAMADO puede pasar a ATENDIDO. */
    public Turno atender(int numero) {
        Turno turno = buscarObligatorio(numero);
        if (turno.getEstado() != EstadoTurno.LLAMADO) {
            throw new IllegalStateException(
                    "Solo se puede atender un turno que haya sido llamado.");
        }
        turno.setEstado(EstadoTurno.ATENDIDO);
        guardarCambios(turno);
        return turno;
    }

    /** Regla: solo se cancelan turnos PENDIENTES o LLAMADOS. */
    public Turno cancelar(int numero) {
        Turno turno = buscarObligatorio(numero);
        if (turno.getEstado() == EstadoTurno.ATENDIDO
                || turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new IllegalStateException(
                    "El turno " + numero + " ya fue "
                    + turno.getEstado().getEtiqueta().toLowerCase() + ".");
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        guardarCambios(turno);
        return turno;
    }

    private Turno buscarObligatorio(int numero) {
        return buscar(numero)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el turno " + numero + "."));
    }

    // =====================================================================
    // ACCESO A DATOS (TEMPORAL, EN MEMORIA)
    //
    // Todas las reglas del negocio usan ÚNICAMENTE estos cuatro métodos.
    // Pregunta para la clase: ¿qué contrato (interfaz) describen?
    // =====================================================================

    private final List<Turno> turnos = new ArrayList<>();

    private void insertar(Turno turno) {
        turnos.add(turno);
    }

    private List<Turno> obtenerTodos() {
        return new ArrayList<>(turnos);
    }

    private Optional<Turno> buscar(int numero) {
        return turnos.stream()
                .filter(t -> t.getNumero() == numero)
                .findFirst();
    }

    private void guardarCambios(Turno turno) {
        // En memoria no hace falta hacer nada: el objeto modificado
        // ES el mismo que está dentro de la lista.
        // ¿Seguirá siendo cierto cuando los datos estén en una base de datos?
    }
}
