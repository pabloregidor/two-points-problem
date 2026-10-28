package com.practica1.utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import com.practica1.modelo.Punto;

public class GestorFicheros {

    /**
     * Lee un fichero .tsp de TSPLIB y devuelve la lista de puntos.
     *
     * Valida DIMENSION: el nº de puntos leídos debe
     * coincidir con la cabecera; si no, avisa por stderr.
     */
    public List<Punto> cargarTSP(File fichero) {

        List<Punto> puntos = new ArrayList<>();
        int dimensionEsperada = 0;
        boolean enCoordenadas = false;

        try (Scanner sc = new Scanner(fichero)) {

            while (sc.hasNextLine()) {

                String linea = sc.nextLine().trim();

                if (linea.isEmpty()) {
                    continue;
                }

                // Cabecera: extraer el entero final de "DIMENSION: 14"
                if (linea.startsWith("DIMENSION")) {
                    String[] partes = linea.split(":");
                    dimensionEsperada = Integer.parseInt(partes[1].trim());
                    continue;
                }

                if (linea.equals("EOF")) {
                    break;
                }

                if (linea.startsWith("NODE_COORD_SECTION")) {
                    enCoordenadas = true;
                    continue;
                }

                if (!enCoordenadas) {
                    continue;   // NAME, TYPE, COMMENT, EDGE_WEIGHT... ignorar
                }

                // Trío: id x y
                String[] partes = linea.split("\\s+");
                int id = Integer.parseInt(partes[0]);
                double x = Double.parseDouble(partes[1]);
                double y = Double.parseDouble(partes[2]);
                puntos.add(new Punto(x, y, id));
            }

            // Validación contra la cabecera
            if (dimensionEsperada != 0 && puntos.size() != dimensionEsperada) {
                System.err.println("AVISO en " + fichero.getName()
                        + ": se esperaban " + dimensionEsperada
                        + " puntos y se han leído " + puntos.size());
            }

        } catch (Exception e) {
            System.err.println("Error leyendo " + fichero + ": " + e.getMessage());
        }
        return puntos;
    }

    /**
     * Escribe los puntos en formato .tsp.
     * El nombre del fichero de salida lo decide el Controlador
     * (p. ej. "DivideVenceras.tsp"); aquí solo se escribe.
     *
     * Locale.US: el punto decimal SIEMPRE, nunca la coma.
     */
    public void guardarTSP(File fichero, List<Punto> puntos) {

        try (PrintWriter pw = new PrintWriter(fichero)) {
            pw.println("NAME: " + nombreSinExtension(fichero));
            pw.println("TYPE: TSP");
            pw.println("DIMENSION: " + puntos.size());
            pw.println("NODE_COORD_SECTION");

            for (Punto p : puntos) {
                pw.println(p.getId() + " "
                        + String.format(Locale.US, "%.10f", p.getX()) + " "
                        + String.format(Locale.US, "%.10f", p.getY()));
            }
            pw.println("EOF");

        } catch (Exception e) {
            System.err.println("Error escribiendo " + fichero + ": " + e.getMessage());
        }
    }

    /**
     * Generador aleatorio, TRADUCIDO tal cual del prototipo
     * C++ del enunciado (Punto::rellenarPuntos), connextInt en lugar
     * de rand(). Crea el fichero datasetN.tsp y devuelve
     * la lista ya cargada en memoria.
     *
     * @param casoMedio true -> casos distribuidos en x e y
     *                  false -> PEOR CASO: misma x, solo y varía
     */
    public List<Punto> generarAleatorio(int n, boolean casoMedio, File carpetaSalida) {

        List<Punto> puntos = new ArrayList<>(n);
        Random r = new Random();

        if (casoMedio) {

            // === CASO MEDIO (rama 'else' del C++) ===
            for (int i = 0; i < n; i++) {
                int num = r.nextInt(4000) + 1;          // rand()%4000 + 1
                int den = r.nextInt(11) + 7;            // rand()%11 + 7
                double x = num / ((double) den + 0.37); // división con decimales
                double y = (r.nextInt(4000) + 1)
                         / ((double) (r.nextInt(11) + 7) + 0.37);
                puntos.add(new Punto(x, y, i + 1));
            }

        } else {

            // === PEOR CASO (rama 'mismax' del C++) ===
            for (int i = 0; i < n; i++) {
                int aux1 = r.nextInt(1000) + 7;         // rand()%1000 + 7
                double y = aux1 / ((double) (i + 1) + i * 0.100);
                int num = r.nextInt(3);                 // rand()%3
                y += ((i % 500) - num * (r.nextInt(100)));
                double x = 1;
                puntos.add(new Punto(x, y, i + 1));
            }
        }

        guardarTSP(new File(carpetaSalida, "dataset" + n + ".tsp"), puntos);
        return puntos;
    }

    /**
     * "burma14.tsp" -> "burma14"
     */
    private String nombreSinExtension(File f) {
        String n = f.getName();
        int punto = n.lastIndexOf('.');
        return punto > 0 ? n.substring(0, punto) : n;
    }
}
