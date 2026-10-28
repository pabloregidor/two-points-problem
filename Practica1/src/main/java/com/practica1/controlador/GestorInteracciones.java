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
                teclado.next();            // descartar entrada no numerica
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
            for (int e = 0; e < estrs.size(); e++) {
                double mediaMs = sumaTiempo[e] / 1e6 / EXPERIMENTOS_POR_TALLA;
                double mediaDist = sumaDist[e] / (double) EXPERIMENTOS_POR_TALLA;

                if (mostrarDistancias) {
                    System.out.printf(Locale.US, "%8.6f (%12.0f)  ", mediaMs, mediaDist);
                } else {
                    System.out.printf(Locale.US, "%18.6f  ", mediaMs);
                }
            }
            System.out.println();
        }
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
