package com.practica1.controlador;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;
import com.practica1.modelo.algoritmos.BusquedaConPoda;
import com.practica1.modelo.algoritmos.BusquedaExhaustiva;
import com.practica1.modelo.algoritmos.DyV;
import com.practica1.modelo.algoritmos.DyVMejorada;
import com.practica1.modelo.algoritmos.EstrategiaAlgoritmica;
import com.practica1.utils.GestorFicheros;
import com.practica1.vista.VistaGraficaTiempos;

public class GestorInteracciones {

    private static final int EXPERIMENTOS_POR_TALLA = 10;

    private final GestorFicheros gestorFicheros = new GestorFicheros();
    private final Scanner teclado = new Scanner(System.in);

    // Las 4 estrategias en el orden de la tabla del enunciado
    private final List<EstrategiaAlgoritmica> estrategias = List.of(
            new BusquedaExhaustiva(),
            new BusquedaConPoda(),
            new DyV(),
            new DyVMejorada()
    );

    private List<Punto> datasetCargado;
    private File carpetaActual = new File(".");   // donde se escriben los .tsp

    public void menuPrincipal() {

        int opcion = -1;

        while (opcion != 0) {

            System.out.println("\n===== PRACTICA 1: Punto mas cercano =====");
            System.out.println(" 1) Crear un fichero .tsp aleatorio");
            System.out.println(" 2) Cargar un dataset en memoria");
            System.out.println(" 3) Comprobar Estrategias");
            System.out.println(" 4) Comparar todas las estrategias");
            System.out.println(" 5) Comparar 2 estrategias");
            System.out.println(" 0) Salir");
            System.out.print("   Opcion: ");

            if (!teclado.hasNextInt()) {
                if (!teclado.hasNext()) {          // EOF: cerrar con elegancia
                    break;
                }
                teclado.next();                    // descartar entrada no numerica
                continue;
            }

            opcion = teclado.nextInt();
            teclado.nextLine();            // limpiar buffer

            switch (opcion) {
                case 1 -> crearFicheroAleatorio();
                case 2 -> cargarDataset();
                case 3 -> comprobarEstrategias();
                case 4 -> compararEstrategias(estrategias);
                case 5 -> compararDos();
                case 0 -> System.out.println("Adiós.");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ===== OPCIÓN 1 =====
    private void crearFicheroAleatorio() {

        System.out.print("¿Cuántos puntos? ");
        int n = teclado.nextInt();
        teclado.nextLine();

        System.out.println(" (1) Caso medio — x e y aleatorios");
        System.out.println(" (2) PEOR CASO  — misma x");
        System.out.print("   Modo: ");
        int modo = teclado.nextInt();
        teclado.nextLine();

        gestorFicheros.generarAleatorio(n, modo == 1, carpetaActual);
        System.out.println("Fichero creado: dataset" + n + ".tsp");
    }

    // ===== OPCIÓN 2 =====
    private void cargarDataset() {

        System.out.print("Ruta del fichero .tsp: ");
        String ruta = teclado.nextLine().trim();

        File f = new File(ruta);
        List<Punto> puntos = gestorFicheros.cargarTSP(f);

        if (!puntos.isEmpty()) {
            datasetCargado = puntos;
            carpetaActual = f.getParentFile() != null ? f.getParentFile() : new File(".");
            System.out.println("Cargados " + puntos.size() + " puntos de " + f.getName());
        } else {
            System.out.println("No se ha cargado ningún punto.");
        }
    }

    // ===== OPCIÓN 3 =====
    private void comprobarEstrategias() {

        if (!hayDataset()) return;

        System.out.println();
        System.out.println("Estrategia             Punto1                      Punto2  distancia  dist  tiempo(mseg)");

        for (EstrategiaAlgoritmica estrategia : estrategias) {

            // Copia NUEVA por estrategia: cada una ordena a su aire,
            // siempre desde las mismas condiciones iniciales
            List<Punto> copia = copiar(datasetCargado);

            ResultadoBusqueda r = estrategia.resolver(copia);

            // Fichero de salida con el nombre de la estrategia
            gestorFicheros.guardarTSP(
                    new File(carpetaActual, estrategia.getNombre().replace(" ", "") + ".tsp"),
                    copia);

            mostrarFila(estrategia.getNombre(), r, true);
        }
    }

    // ===== OPCIÓN 4: todas las estrategias =====
    // ===== OPCIÓN 5: solo 2, con distancias mostradas =====
    private void compararEstrategias(List<EstrategiaAlgoritmica> estrs) {

        // Esquema del enunciado (pág. 11): tamaños entre min y max con paso
        System.out.print("Talla mínima: ");
        int tallaMin = teclado.nextInt();
        System.out.print("Talla máxima: ");
        int tallaMax = teclado.nextInt();
        System.out.print("Incremento:   ");
        int paso = teclado.nextInt();
        teclado.nextLine();

        System.out.print("¿Caso medio (1) o PEOR CASO misma x (2)? ");
        int modo = teclado.nextInt();
        teclado.nextLine();
        boolean casoMedio = (modo == 1);

        // Mostrar distancias además de tiempos (opción 5)
        boolean mostrarDistancias = estrs.size() == 2;

        // Datos acumulados para la gráfica (solo cuando se comparan las 4)
        List<Integer> tallasEjecutadas = new ArrayList<>();
        double[][] mediasAcumuladas = new double[estrs.size()][100]; // máx 100 tallas

        // Cabecera
        System.out.println();
        System.out.println("Estado del experimento: " + EXPERIMENTOS_POR_TALLA
                + " datasets por talla, mismos para todas.");
        if (mostrarDistancias) {
            for (EstrategiaAlgoritmica e : estrs) {
                System.out.printf(Locale.US, "%18s (tiempo %12s | dist %12s)  ",
                        e.getNombre(), "mseg", "calculadas");
            }
            System.out.println();
        } else {
            for (EstrategiaAlgoritmica e : estrs) {
                System.out.printf(Locale.US, "%18s (%12s)  ", e.getNombre(), "mseg");
            }
            System.out.println();
        }

        for (int talla = tallaMin; talla <= tallaMax; talla += paso) {

            long[] sumaTiempo = new long[estrs.size()];
            long[] sumaDist   = new long[estrs.size()];

            for (int exp = 0; exp < EXPERIMENTOS_POR_TALLA; exp++) {
                // Dataset aleatorio de esta talla compartido por TODAS
                gestorFicheros.generarAleatorio(talla, casoMedio, carpetaActual);
                List<Punto> dataset = gestorFicheros.cargarTSP(
                        new File(carpetaActual, "dataset" + talla + ".tsp"));

                for (int e = 0; e < estrs.size(); e++) {

                    // Copia por estrategia y por experimento
                    List<Punto> copia = copiar(dataset);
                    ResultadoBusqueda r = estrs.get(e).resolver(copia);

                    sumaTiempo[e] += r.getTiempoEjecucionMilisegundos();
                    sumaDist[e]   += r.getComparacionesRealizadas();
                }
            }

            // Media por talla
            int indiceTalla = tallasEjecutadas.size();
            tallasEjecutadas.add(talla);

            for (int e = 0; e < estrs.size(); e++) {
                double mediaMs = sumaTiempo[e] / 1e6 / EXPERIMENTOS_POR_TALLA;
                mediasAcumuladas[e][indiceTalla] = mediaMs;
                double mediaDist = sumaDist[e] / (double) EXPERIMENTOS_POR_TALLA;

                if (mostrarDistancias) {
                    System.out.printf(Locale.US, "%8.6f (%12.0f)  ", mediaMs, mediaDist);
                } else {
                    System.out.printf(Locale.US, "%18.6f  ", mediaMs);
                }
            }
            System.out.println();
        }

        // === Gráfica de tiempos (requisito de nota de la práctica) ===
        if (estrs.size() == 4 && tallasEjecutadas.size() >= 2) {
            lanzarGraficaTiempos(estrs, tallasEjecutadas, mediasAcumuladas,
                                 casoMedio);
        }
    }

    private void lanzarGraficaTiempos(List<EstrategiaAlgoritmica> estrs,
                                      List<Integer> tallas,
                                      double[][] medias,
                                      boolean casoMedio) {
        int n = tallas.size();
        int[] tallasArr = new int[n];
        for (int i = 0; i < n; i++) tallasArr[i] = tallas.get(i);

        double[][] tiempos = new double[estrs.size()][n];
        for (int e = 0; e < estrs.size(); e++) {
            System.arraycopy(medias[e], 0, tiempos[e], 0, n);
        }

        java.awt.Color[] colores = {
                java.awt.Color.BLACK, java.awt.Color.RED,
                java.awt.Color.BLUE, java.awt.Color.GREEN.darker()
        };

        String tipo = casoMedio ? "caso medio" : "PEOR CASO (misma x)";
        new VistaGraficaTiempos().mostrar(
                new VistaGraficaTiempos.Datos(
                        tallasArr, tiempos, nombresDe(estrs), colores),
                tipo);
    }

    private String[] nombresDe(List<EstrategiaAlgoritmica> estrs) {
        String[] nombres = new String[estrs.size()];
        for (int i = 0; i < estrs.size(); i++) {
            nombres[i] = estrs.get(i).getNombre();
        }
        return nombres;
    }

    /**
     * MODO DEMO: without keyboard input, run some tallas, show the
     * time graph and wait until the user closes the window (the JVM
     * does not terminate while it is visible).
     */
    public void demoConVentana() {

        List<Integer> tallas = new ArrayList<>();
        double[][] medias = new double[estrategias.size()][100];
        long[] suma = new long[estrategias.size()];

        for (int talla = 200; talla <= 600; talla += 200) {

            for (int e = 0; e < estrategias.size(); e++) suma[e] = 0;

            for (int exp = 0; exp < EXPERIMENTOS_POR_TALLA; exp++) {

                gestorFicheros.generarAleatorio(talla, true, carpetaActual);
                List<Punto> dataset = gestorFicheros.cargarTSP(
                        new File(carpetaActual, "dataset" + talla + ".tsp"));

                for (int e = 0; e < estrategias.size(); e++) {
                    List<Punto> copia = copiar(dataset);
                    ResultadoBusqueda r = estrategias.get(e).resolver(copia);
                    suma[e] += r.getTiempoEjecucionMilisegundos();
                }
            }

            int indice = tallas.size();
            tallas.add(talla);
            for (int e = 0; e < estrategias.size(); e++) {
                medias[e][indice] = suma[e] / 1e6 / EXPERIMENTOS_POR_TALLA;
            }
        }

        int n = tallas.size();
        int[] tallasArr = new int[n];
        double[][] datos = new double[estrategias.size()][n];
        for (int i = 0; i < n; i++) tallasArr[i] = tallas.get(i);
        for (int e = 0; e < estrategias.size(); e++) {
            System.arraycopy(medias[e], 0, datos[e], 0, n);
        }

        // Wait until the window close (so the JVM lives and the user sees it)
        java.util.concurrent.CountDownLatch cierre = new java.util.concurrent.CountDownLatch(1);

        javax.swing.SwingUtilities.invokeLater(() -> {
            VistaGraficaTiempos v = new VistaGraficaTiempos();
            v.setPreferredSize(new java.awt.Dimension(950, 620));
            v.setDatos(
                    new VistaGraficaTiempos.Datos(
                            tallasArr, datos, nombresDe(estrategias),
                            new java.awt.Color[]{ java.awt.Color.BLACK, java.awt.Color.RED,
                                    java.awt.Color.BLUE, java.awt.Color.GREEN.darker() }),
                    "caso medio");

            javax.swing.JFrame frame = new javax.swing.JFrame("Gráfica de tiempos (demo): caso medio");
            frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            frame.add(v);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    cierre.countDown();
                }
            });
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        try {
            cierre.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Demo terminada.");
    }

    private void compararDos() {

        System.out.println("Estrategias disponibles:");
        for (int i = 0; i < estrategias.size(); i++) {
            System.out.println(" " + (i + 1) + ") " + estrategias.get(i).getNombre());
        }

        System.out.print("Estrategia A (1-4): ");
        int a = teclado.nextInt();
        System.out.print("Estrategia B (1-4): ");
        int b = teclado.nextInt();
        teclado.nextLine();

        List<EstrategiaAlgoritmica> par = List.of(
                estrategias.get(a - 1),
                estrategias.get(b - 1)
        );

        compararEstrategias(par);
    }

    // ===== utilidades =====

    private List<Punto> copiar(List<Punto> original) {
        List<Punto> copia = new ArrayList<>(original.size());
        for (Punto p : original) {
            copia.add(new Punto(p));
        }
        return copia;
    }

    private boolean hayDataset() {
        if (datasetCargado == null) {
            System.out.println("Carga primero un dataset (opción 2).");
            return false;
        }
        return true;
    }

    private void mostrarFila(String nombre, ResultadoBusqueda r, boolean columnaDistancias) {

        String p1 = r.getPunto1() == null ? "-" : r.getPunto1().toString();
        String p2 = r.getPunto2() == null ? "-" : r.getPunto2().toString();

        System.out.printf(Locale.US, "%-22s %-26s %-26s %.8f  %6d  %9.4f%n",
                nombre, p1, p2,
                r.getDistanciaMinima(),
                r.getComparacionesRealizadas(),
                r.getTiempoEjecucionMilisegundos() / 1e6   // nanos -> ms
        );
    }
}
