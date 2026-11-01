package com.practica1.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Imagen con los estados de un HeapSort (ejemplos límite de la memoria):
 * dibuja en filas el estado inicial, el max-heap construido y el ordenado.
 */
public final class ImagenEstadosArray {

    private ImagenEstadosArray() {
    }

    public static void guardar(List<String> etiquetas, List<List<Integer>> estados,
                               File destino) {
        try {
            int ancho = 1000;
            int altoFila = 64;
            int alto = 90 + altoFila * estados.size();

            java.awt.image.BufferedImage img =
                    new java.awt.image.BufferedImage(ancho, alto,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, ancho, alto);

            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(Color.DARK_GRAY);

            int y = 40;
            for (int s = 0; s < estados.size(); s++) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                g.drawString(etiquetas.get(s), 20, y - 18);

                List<Integer> estado = estados.get(s);
                int cajaMida = 62;
                int x = 20;
                for (Integer v : estado) {
                    g.setColor(new Color(240, 244, 250));
                    g.fillRect(x, y, cajaMida, 40);
                    g.setColor(new Color(90, 120, 160));
                    g.drawRect(x, y, cajaMida, 40);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Monospaced", Font.BOLD, 14));
                    String num = String.valueOf(v);
                    g.drawString(num, x + (cajaMida - num.length() * 9) / 2, y + 25);
                    x += cajaMida + 4;
                }
                y += altoFila;
            }

            g.dispose();
            ImageIO.write(img, "png", destino);
        } catch (Exception e) {
            System.err.println("Error generando imagen " + destino + ": " + e.getMessage());
        }
    }

    /** Utilidad: lista de enteros aleatorios sin repetir (caso 'mejor'). */
    public static List<Integer> aleatorios(int n, java.util.Random r) {
        List<Integer> lista = new ArrayList<>();
        while (lista.size() < n) {
            int v = r.nextInt(100) + 1;
            if (!lista.contains(v)) {
                lista.add(v);
            }
        }
        return lista;
    }
}
