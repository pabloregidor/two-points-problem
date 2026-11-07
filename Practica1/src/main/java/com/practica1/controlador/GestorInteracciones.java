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
import com.practica1.utils.Ordenacion;
import com.practica1.vista.ImagenEstadosArray;
import com.practica1.vista.PanelGrafico;
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

    // Acceso de la vista (solo lectura)
    public List<EstrategiaAlgoritmica> getEstrategias() {
        return estrategias;
    }

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
                case 4 -> compararEstrategiasDesdeMenu(estrategias);
                case 5 -> compararDos();
                case 0 -> System.out.println("Adiós.");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ===== ACCIÓN NÚCLEO 1 (pública: CLI y GUI) =====
    public void crearFicheroAleatorio(int n, boolean casoMedio) {
        gestorFicheros.generarAleatorio(n, casoMedio, carpetaActual);
        System.out.println("Fichero creado: dataset" + n + ".tsp");
    }

    // (envoltorio CLI que pide datos por consola)
    private void crearFicheroAleatorio() {

        System.out.print("¿Cuántos puntos? ");
        int n = teclado.nextInt();
        teclado.nextLine();

        System.out.println(" (1) Caso medio — x e y aleatorios");
        System.out.println(" (2) PEOR CASO  — misma x");
        System.out.print("   Modo: ");
        int modo = teclado.nextInt();
        teclado.nextLine();

        crearFicheroAleatorio(n, modo == 1);
    }

    // ===== ACCIÓN NÚCLEO 2 (pública: CLI y GUI) =====
    public List<Punto> cargarFichero(File f) {

        List<Punto> puntos = gestorFicheros.cargarTSP(f);
        if (!puntos.isEmpty()) {
            datasetCargado = puntos;
            carpetaActual = f.getParentFile() != null ? f.getParentFile() : new File(".");
            System.out.println("Cargados " + puntos.size() + " puntos de " + f.getName());
            return puntos;
        }
        System.out.println("No se ha cargado ningún punto.");
        return null;
    }

    // (envoltorio CLI)
    private void cargarDataset() {

        System.out.print("Ruta del fichero .tsp: ");
        String ruta = teclado.nextLine().trim();
        cargarFichero(new File(ruta));
    }

    // ===== ACCIÓN NÚCLEO 3 (pública): la opción 3 del enunciado =====
    public void comprobarEstrategias() {

        if (!hayDataset()) return;

        System.out.println();
        System.out.println("Estrategia             Punto1                      Punto2  distancia  dist  tiempo(mseg)");

        ResultadoBusqueda mejorResultado = null;
        for (EstrategiaAlgoritmica estrategia : estrategias) {

            // Copia NUEVA por estrategia: cada una ordena a su aire,
            // siempre desde las mismas condiciones iniciales
            List<Punto> copia = copiar(datasetCargado);

            ResultadoBusqueda r = estrategia.resolver(copia);

            // para el panel: nos quedamos con la mejor de las 4
            if (mejorResultado == null
                    || r.getDistanciaMinima() < mejorResultado.getDistanciaMinima()) {
                mejorResultado = r;
            }

            // Fichero de salida con el nombre de la estrategia
            gestorFicheros.guardarTSP(
                    new File(carpetaActual, estrategia.getNombre().replace(" ", "") + ".tsp"),
                    copia);

            mostrarFila(estrategia.getNombre(), r, true);
        }

        // Ventana con la solución dibujada (nota de la práctica);
        // desde el menú CLI: se espera a que la cierres para volver al menú.
        PanelGrafico.mostrar(datasetCargado, mejorResultado,
                             datasetCargado.size() + " puntos", true);
    }

    // ===== OPCIÓN 4: todas las estrategias =====
    // ===== OPCIÓN 5: solo 2, con distancias mostradas =====
    // ===== ACCIÓN NÚCLEO 4/5 (pública) =====
    private void compararEstrategiasDesdeMenu(List<EstrategiaAlgoritmica> estrs) {

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

        compararEstrategias(estrs, tallaMin, tallaMax, paso, modo == 1);
    }

    // ===== OPCIÓN 5: solo 2, con distancias mostradas =====
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

        compararEstrategiasDesdeMenu(par);
    }

    // ===== NÚCLEO de comparación (pública: CLI y GUI) =====
    public void compararEstrategias(List<EstrategiaAlgoritmica> estrs,
                                    int tallaMin, int tallaMax, int paso,
                                    boolean casoMedio) {

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

        // === Gráfica de tiempos ===
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
     * MODO CAPTURAS: ejecuta los experimentos OFICIALES de la memoria
     * (tallas 200..5000, 10 experimentos por talla) en caso medio y
     * peor caso, imprime las tablas y guarda las 2 gráficas como PNG
     * SIN interacción. Lanzado con `App capturas`.
     */
    public void capturasParaMemoria() {


        int[] tallas = {200, 500, 1000, 1500, 2000, 3000, 4000, 5000};
        String[] casos = {"caso_medio", "peor_caso"};
        boolean[] medios = {true, false};

        for (int c = 0; c < casos.length; c++) {

            System.out.println("\n===== EXPERIMENTO " + casos[c].toUpperCase()
                    + " =====");
            System.out.println();

            double[][] medias = new double[estrategias.size()][tallas.length];
            int[][] dist = new int[estrategias.size()][tallas.length];

            for (int i = 0; i < tallas.length; i++) {
                int talla = tallas[i];
                long[] sumaTiempo = new long[estrategias.size()];
                long[] sumaDist = new long[estrategias.size()];

                for (int exp = 0; exp < EXPERIMENTOS_POR_TALLA; exp++) {

                    gestorFicheros.generarAleatorio(talla, medios[c], carpetaActual);
                    List<Punto> dataset = gestorFicheros.cargarTSP(
                            new File(carpetaActual, "dataset" + talla + ".tsp"));

                    for (int e = 0; e < estrategias.size(); e++) {
                        List<Punto> copia = copiar(dataset);
                        ResultadoBusqueda r = estrategias.get(e).resolver(copia);
                        sumaTiempo[e] += r.getTiempoEjecucionMilisegundos();
                        sumaDist[e] += r.getComparacionesRealizadas();
                    }
                }

                System.out.print("Talla " + talla + ": ");
                for (int e = 0; e < estrategias.size(); e++) {
                    medias[e][i] = sumaTiempo[e] / 1e6 / EXPERIMENTOS_POR_TALLA;
                    dist[e][i] = (int) (sumaDist[e] / EXPERIMENTOS_POR_TALLA);
                    System.out.printf(Locale.US, "%s=%.6fms(%d dd)  ",
                            estrategias.get(e).getNombre(), medias[e][i], dist[e][i]);
                }
                System.out.println();
            }

            // Tablas CSV para la memoria (tiempo y distancias, mismos datasets)
            try {
                java.io.PrintWriter pw = new java.io.PrintWriter(
                        new File(carpetaActual, "tabla_" + casos[c] + ".csv"));
                pw.println("Talla;" + String.join(";", nombresDe(estrategias)));
                for (int i = 0; i < tallas.length; i++) {
                    StringBuilder sb = new StringBuilder(String.valueOf(tallas[i]));
                    for (int e = 0; e < estrategias.size(); e++) {
                        sb.append(';').append(String.format(Locale.US, "%.6f", medias[e][i]));
                    }
                    pw.println(sb);
                }
                pw.close();

                java.io.PrintWriter pd = new java.io.PrintWriter(
                        new File(carpetaActual, "tabla_" + casos[c] + "_dist.csv"));
                pd.println("Talla;" + String.join(";", nombresDe(estrategias)));
                for (int i = 0; i < tallas.length; i++) {
                    StringBuilder sb = new StringBuilder(String.valueOf(tallas[i]));
                    for (int e = 0; e < estrategias.size(); e++) {
                        sb.append(';').append(dist[e][i]);
                    }
                    pd.println(sb);
                }
                pd.close();
            } catch (java.io.IOException e) {
                System.err.println("Error escribiendo CSV de " + casos[c] + ": " + e.getMessage());
            }

            // Gráfica como PNG para la memoria
            java.awt.Color[] colores = {
                    java.awt.Color.BLACK, java.awt.Color.RED,
                    java.awt.Color.BLUE, java.awt.Color.GREEN.darker()
            };
            new VistaGraficaTiempos().guardarComoImagen(
                    new VistaGraficaTiempos.Datos(tallas, medias,
                            nombresDe(estrategias), colores),
                    casos[c],
                    new File(carpetaActual, "grafica_" + casos[c] + ".png"),
                    !casos[c].equals("peor_caso"));
        }

        System.out.println("\nCAPTURAS PARA LA MEMORIA LISTAS (PNG + CSV en "
                + carpetaActual.getAbsolutePath() + ")");
        // modo sin ventanas: la JVM puede quedarse viva por los hilos AWT
        System.exit(0);
    }

    /**
     * MODO CAPTURAS AUX: genera las imágenes complementarias de la memoria
     * SIN repetir los experimentos largos:
     *  - dibujo de la solución (PanelGrafico) en caso medio y peor caso
     *  - ejemplos límite del HeapSort (estados inicial / heap / ordenado)
     * Lanzado con `App capturas_aux`.
     */
    public void capturasAuxiliaresParaMemoria() {

        com.practica1.modelo.algoritmos.EstrategiaAlgoritmica poda =
                new com.practica1.modelo.algoritmos.BusquedaConPoda();

        // ===== 1. Dibujo de la solución — CASO MEDIO =====
        List<Punto> datasetMedio = gestorFicheros.generarAleatorio(600, true, carpetaActual);
        ResultadoBusqueda rMedio = poda.resolver(copiar(datasetMedio));
        PanelGrafico.guardarComoImagen(datasetMedio, rMedio, "caso medio",
                new File(carpetaActual, "dibujo_solucion_caso_medio.png"));
        System.out.println("OK dibujo_solucion_caso_medio.png");

        // ===== 2. Dibujo de la solución — PEOR CASO (misma x: la vertical) =====
        List<Punto> datasetPeor = gestorFicheros.generarAleatorio(600, false, carpetaActual);
        ResultadoBusqueda rPeor = poda.resolver(copiar(datasetPeor));
        PanelGrafico.guardarComoImagen(datasetPeor, rPeor, "peor caso (misma x)",
                new File(carpetaActual, "dibujo_solucion_peor_caso.png"));
        System.out.println("OK dibujo_solucion_peor_caso.png");

        // ===== 3. Ejemplos límite del HeapSort =====
        java.util.Random r = new java.util.Random();
        int n = 12;

        // CASO 'MEJOR' (datos aleatorios)
        List<Integer> mejor = ImagenEstadosArray.aleatorios(n, r);
        List<String> etiquetas = new java.util.ArrayList<>();
        List<List<Integer>> estados = new java.util.ArrayList<>();
        Ordenacion.ordenar(mejor, java.util.Comparator.naturalOrder(),
                (etiqueta, instantanea) -> {
                    etiquetas.add(etiqueta);
                    estados.add(new java.util.ArrayList<>());
                    for (Object o : instantanea) {
                        estados.get(estados.size() - 1)
                               .add(((Number) o).intValue());
                    }
                });
        ImagenEstadosArray.guardar(etiquetas, estados,
                new File(carpetaActual, "heapsort_datos_aleatorios.png"));
        System.out.println("OK heapsort_datos_aleatorios.png");

        // CASO LÍMITE (ordenado ascendente: caso clásico de estudio del HeapSort)
        List<Integer> ascendente = new java.util.ArrayList<>();
        for (int i = 1; i <= n; i++) {
            ascendente.add(i);
        }
        List<String> etiquetas2 = new java.util.ArrayList<>();
        List<List<Integer>> estados2 = new java.util.ArrayList<>();
        Ordenacion.ordenar(ascendente, java.util.Comparator.naturalOrder(),
                (etiqueta, instantanea) -> {
                    etiquetas2.add(etiqueta);
                    estados2.add(new java.util.ArrayList<>());
                    for (Object o : instantanea) {
                        estados2.get(estados2.size() - 1)
                               .add(((Number) o).intValue());
                    }
                });
        ImagenEstadosArray.guardar(etiquetas2, estados2,
                new File(carpetaActual, "heapsort_ordenado_ascendente.png"));
        System.out.println("OK heapsort_ordenado_ascendente.png");

        System.out.println("CAPTURAS AUXILIARES LISTAS en " + carpetaActual.getAbsolutePath());
        System.exit(0);
    }

    /**
     * MODO GRAFICAS: vuelve a dibujar las 2 gráficas de tiempos leyendo
     * LOS CSV GUARDADOS (sin repetir experimentos: mismos datos).
     * Caso medio en escala logarítmica; peor caso en lineal.
     */
    public void graficasDesdeCsv() {

        String[][] archivos = {
                {"tabla_caso_medio.csv", "grafica_caso_medio.png", "caso medio", "true"},
                {"tabla_peor_caso.csv", "grafica_peor_caso.png", "peor caso", "false"}
        };

        for (String[] cfg : archivos) {
            List<String[]> filas = new java.util.ArrayList<>();
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.FileReader(new File(carpetaActual, cfg[0])));
                String linea;
                while ((linea = br.readLine()) != null) {
                    filas.add(linea.split(";"));
                }
                br.close();
            } catch (java.io.IOException e) {
                System.err.println("No se pudo leer " + cfg[0]);
                continue;
            }

            int n = filas.size() - 1;
            int[] tallas = new int[n];
            for (int i = 0; i < n; i++) {
                tallas[i] = Integer.parseInt(filas.get(i + 1)[0].trim());
            }

            double[][] medias = new double[estrategias.size()][n];
            for (int e = 0; e < estrategias.size(); e++) {
                for (int i = 0; i < n; i++) {
                    medias[e][i] = Double.parseDouble(
                            filas.get(i + 1)[e + 1].replace(',', '.'));
                }
            }

            java.awt.Color[] colores = {
                    java.awt.Color.BLACK, java.awt.Color.RED,
                    java.awt.Color.BLUE, java.awt.Color.GREEN.darker()
            };
            new VistaGraficaTiempos().guardarComoImagen(
                    new VistaGraficaTiempos.Datos(tallas, medias,
                            nombresDe(estrategias), colores),
                    cfg[2],
                    new File(carpetaActual, cfg[1]),
                    Boolean.parseBoolean(cfg[3]));
            System.out.println("OK grafica_" + cfg[2].replace(" ", "_") + ".png");
        }
        System.exit(0);
    }

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
