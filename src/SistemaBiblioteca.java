import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// ========================================
// 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP)
// ========================================
// Una clase debe tener una sola responsabilidad

/**
 * Clase que solo maneja información del libro
 */
class Libro {
    private String titulo;
    private String autor;
    private String isbn;
    private boolean disponible;

    public Libro(String titulo, String autor, String isbn) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.disponible = true;
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}

/**
 * Clase que solo maneja información del usuario
 */
class Usuario {
    private String nombre;
    private String idUsuario;

    public Usuario(String nombre, String idUsuario) {
        this.nombre = nombre;
        this.idUsuario = idUsuario;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getIdUsuario() { return idUsuario; }
}

/**
 * Clase que solo maneja información del préstamo
 */
class Prestamo {
    private Libro libro;
    private Usuario usuario;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucion;
    private boolean devuelto;

    public Prestamo(Libro libro, Usuario usuario) {
        this.libro = libro;
        this.usuario = usuario;
        this.fechaPrestamo = LocalDateTime.now();
        this.fechaDevolucion = LocalDateTime.now().plusDays(14);
        this.devuelto = false;
    }

    // Getters y Setters
    public Libro getLibro() { return libro; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }
}

// ========================================
// 2. OPEN/CLOSED PRINCIPLE (OCP)
// ========================================
// Abierto para extensión, cerrado para modificación

/**
 * Interfaz base para calcular multas
 */
interface CalculadoraMulta {
    double calcular(long diasRetraso);
}

/**
 * Multa estándar de $10 por día
 */
class MultaEstandar implements CalculadoraMulta {
    @Override
    public double calcular(long diasRetraso) {
        return diasRetraso * 10.0;
    }
}

/**
 * Multa reducida para estudiantes: $5 por día
 */
class MultaEstudiante implements CalculadoraMulta {
    @Override
    public double calcular(long diasRetraso) {
        return diasRetraso * 5.0;
    }
}

/**
 * Sin multa para usuarios VIP
 */
class MultaVIP implements CalculadoraMulta {
    @Override
    public double calcular(long diasRetraso) {
        return 0.0;
    }
}

// ========================================
// 3. LISKOV SUBSTITUTION PRINCIPLE (LSP)
// ========================================
// Los objetos derivados deben poder sustituir a la clase base

/**
 * Interfaz base para notificaciones
 */
interface Notificador {
    boolean enviar(String mensaje, String destinatario);
}

/**
 * Notifica por email
 */
class NotificadorEmail implements Notificador {
    @Override
    public boolean enviar(String mensaje, String destinatario) {
        System.out.println("📧 Email a " + destinatario + ": " + mensaje);
        return true;
    }
}

/**
 * Notifica por SMS
 */
class NotificadorSMS implements Notificador {
    @Override
    public boolean enviar(String mensaje, String destinatario) {
        System.out.println("📱 SMS a " + destinatario + ": " + mensaje);
        return true;
    }
}

// ========================================
// 4. INTERFACE SEGREGATION PRINCIPLE (ISP)
// ========================================
// Los clientes no deben depender de interfaces que no usan

/**
 * Interface solo para reservar
 */
interface Reservable {
    boolean reservar(Usuario usuario);
}

/**
 * Interface solo para prestar
 */
interface Prestable {
    boolean prestar(Usuario usuario);
}

/**
 * Interface solo para renovar
 */
interface Renovable {
    boolean renovar(Prestamo prestamo);
}

// ========================================
// 5. DEPENDENCY INVERSION PRINCIPLE (DIP)
// ========================================
// Depender de abstracciones, no de implementaciones concretas

/**
 * Gestor que depende de abstracciones
 */
class GestorPrestamos {
    private CalculadoraMulta calculadoraMulta;
    private Notificador notificador;
    private List<Prestamo> prestamos;

    public GestorPrestamos(CalculadoraMulta calculadoraMulta, Notificador notificador) {
        this.calculadoraMulta = calculadoraMulta;
        this.notificador = notificador;
        this.prestamos = new ArrayList<>();
    }

    /**
     * Realiza un préstamo de libro
     */
    public Prestamo realizarPrestamo(Libro libro, Usuario usuario) {
        if (!libro.isDisponible()) {
            System.out.println("❌ El libro '" + libro.getTitulo() + "' no está disponible");
            return null;
        }

        libro.setDisponible(false);
        Prestamo prestamo = new Prestamo(libro, usuario);
        prestamos.add(prestamo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String mensaje = "Has prestado '" + libro.getTitulo() +
                "'. Fecha de devolución: " + prestamo.getFechaDevolucion().format(formatter);
        notificador.enviar(mensaje, usuario.getNombre());

        System.out.println("✅ Préstamo realizado: '" + libro.getTitulo() +
                "' para " + usuario.getNombre());
        return prestamo;
    }

    /**
     * Devuelve un libro y calcula multa si hay retraso
     */
    public void devolverLibro(Prestamo prestamo) {
        if (prestamo.isDevuelto()) {
            System.out.println("❌ Este libro ya fue devuelto");
            return;
        }

        LocalDateTime fechaActual = LocalDateTime.now();
        if (fechaActual.isAfter(prestamo.getFechaDevolucion())) {
            long diasRetraso = ChronoUnit.DAYS.between(prestamo.getFechaDevolucion(), fechaActual);
            double multa = calculadoraMulta.calcular(diasRetraso);
            System.out.println("⚠️  Retraso de " + diasRetraso + " días. Multa: $" + multa);
        } else {
            System.out.println("✅ Libro devuelto a tiempo");
        }

        prestamo.setDevuelto(true);
        prestamo.getLibro().setDisponible(true);
        System.out.println("📚 '" + prestamo.getLibro().getTitulo() +
                "' devuelto por " + prestamo.getUsuario().getNombre());
    }
}

// ========================================
// CLASE PRINCIPAL - EJEMPLO DE USO
// ========================================

public class SistemaBiblioteca {

    public static void main(String[] args) {
        System.out.println("🏛️  SISTEMA DE BIBLIOTECA - PRINCIPIOS SOLID");
        System.out.println("=".repeat(50));

        // Crear libros
        Libro libro1 = new Libro("1984", "George Orwell", "978-0-452-28423-4");
        Libro libro2 = new Libro("Cien años de soledad", "Gabriel García Márquez", "978-84-376-0494-7");

        // Crear usuarios
        Usuario usuario1 = new Usuario("Ana García", "U001");
        Usuario usuario2 = new Usuario("Carlos López", "U002");

        // Crear diferentes gestores con distintas configuraciones
        System.out.println("\n📋 GESTOR PARA USUARIOS REGULARES:");
        GestorPrestamos gestorRegular = new GestorPrestamos(
                new MultaEstandar(),
                new NotificadorEmail()
        );

        System.out.println("\n📋 GESTOR PARA ESTUDIANTES:");
        GestorPrestamos gestorEstudiante = new GestorPrestamos(
                new MultaEstudiante(),
                new NotificadorSMS()
        );

        // Realizar préstamos
        System.out.println("\n🔄 REALIZANDO PRÉSTAMOS:");
        Prestamo prestamo1 = gestorRegular.realizarPrestamo(libro1, usuario1);
        Prestamo prestamo2 = gestorEstudiante.realizarPrestamo(libro2, usuario2);

        // Simular devolución tardía
        System.out.println("\n📅 SIMULANDO DEVOLUCIÓN TARDÍA:");
        // Modificamos la fecha para simular retraso
        if (prestamo1 != null) {
            prestamo1.setFechaDevolucion(LocalDateTime.now().minusDays(3));
            gestorRegular.devolverLibro(prestamo1);
        }

        // Devolución a tiempo
        System.out.println("\n📅 DEVOLUCIÓN A TIEMPO:");
        if (prestamo2 != null) {
            gestorEstudiante.devolverLibro(prestamo2);
        }
    }
}