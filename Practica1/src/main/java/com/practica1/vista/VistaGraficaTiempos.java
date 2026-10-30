package com.practica1.vista;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel que dibuja la gráfica de tiempos (una línea por estrategia)
 * con escala LOGARÍTMICA en el eje Y, sin librerías externas (Swing puro).
 */
public class VistaGraficaTiempos extends JPanel {

    /** Datos mínimos que necesita el panel (inmutables). */
    public record Datos(int[] tallas,
                        double[][] tiempos,   // [estrategia][talla] en ms
                        String[] nombres,
                        Color[] colores) {
    }

    private static final int MARGEN_IZQ = 70, MARGEN_ARR = 25;
    private static final int MARGEN_AB = 45, MARGEN_DER = 170;

    private Datos datos;
    private String titulo = "";

    public VistaGraficaTiempos() {
        setPreferredSize(new Dimension(850, 550));
        setBackground(Color.WHITE);
    }

    /** Datos + repintado sin abrir ventana propia (útil en un JFrame externo). */
    public void setDatos(Datos d, String tituloVentana) {
        this.datos = d;
        this.titulo = tituloVentana;
        repaint();
    }

    /** Crea y muestra la ventana con esta gráfica ya dibujada. */
    public void mostrar(Datos d, String titulo) {
        setDatos(d, titulo);
        repintarEnVentana();
    }

    /** Reutilizable: abre una ventana nueva con los datos dados. */
    public void repintarEnVentana() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gráfica de tiempos: " + titulo);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(this);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datos == null || datos.tallas().length < 2) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int[] tallas = datos.tallas();
        double[][] tiempos = datos.tiempos();
        int w = getWidth(), h = getHeight();

        int minX = MARGEN_IZQ, maxY = h - MARGEN_AB;
        int maxX = w - MARGEN_DER, minY = MARGEN_ARR;

        // Calcular rango del eje Y en log10
        double maxLogY = Double.NEGATIVE_INFINITY;
        double minLogY = Double.POSITIVE_INFINITY;
        for (double[] porEstrategia : tiempos) {
            for (double v : porEstrategia) {
                double logv = Math.log10(Math.max(v, 0.001));
                maxLogY = Math.max(maxLogY, logv);
                minLogY = Math.min(minLogY, logv);
            }
        }
        if (maxLogY - minLogY < 0.5) {
            maxLogY = Math.ceil(maxLogY) + 0.5;   // evitar rango degenerado
            minLogY = maxLogY - 1;
        }

        double minTalla = tallas[0], maxTalla = tallas[tallas.length - 1];

        // ===== Ejes + rejilla =====
        g2.setColor(Color.GRAY);
        g2.drawLine(minX, maxY, maxX + 10, maxY);              // eje X
        g2.drawLine(minX, minY - 5, minX, maxY);               // eje Y

        // marcas Y: cada potencia de 10
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int exp = (int) Math.floor(minLogY); exp <= (int) Math.ceil(maxLogY); exp++) {
            double fraccion = (maxLogY - exp) / (maxLogY - minLogY);
            int yPantalla = (int) (minY + fraccion * (maxY - minY));
            if (yPantalla < minY - 5 || yPantalla > maxY + 5) continue;

            g2.setColor(new Color(225, 225, 225));
            g2.drawLine(minX, yPantalla, maxX, yPantalla);
            g2.setColor(Color.GRAY);
            String etiqueta = exp == 0 ? "1 ms" : exp == -3 ? "1 micros" : "10^" + exp;
            g2.drawString(etiqueta, 8, yPantalla + 4);
            g2.drawLine(minX - 3, yPantalla, minX, yPantalla);
        }

        // marcas X: cada talla
        for (int i = 0; i < tallas.length; i++) {
            int xPantalla = xDe(tallas[i], minTalla, maxTalla, minX, maxX);
            g2.setColor(new Color(225, 225, 225));
            g2.drawLine(xPantalla, minY, xPantalla, maxY);
            g2.setColor(Color.GRAY);
            g2.drawLine(xPantalla, maxY, xPantalla, maxY + 3);
            String marca = String.valueOf(tallas[i]);
            g2.drawString(marca, xPantalla - marca.length() * 4, maxY + 18);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("talla (n)", minX + (maxX - minX) / 2 - 25, maxY + 35);

        // ===== Las 4 polilíneas =====
        for (int e = 0; e < tiempos.length; e++) {
            g2.setColor(datos.colores()[e]);
            g2.setStroke(new BasicStroke(2.0f));

            Point anterior = null;
            for (int i = 0; i < tallas.length; i++) {
                Point actual = new Point(
                        xDe(tallas[i], minTalla, maxTalla, minX, maxX),
                        yDe(Math.log10(Math.max(tiempos[e][i], 0.001)),
                            minLogY, maxLogY, minY, maxY));
                if (anterior != null) {
                    g2.drawLine(anterior.x, anterior.y, actual.x, actual.y);
                }
                anterior = actual;
            }

            // marca de punto final
            if (anterior != null) {
                g2.fillOval(anterior.x - 3, anterior.y - 3, 6, 6);
            }
        }

        // ===== Leyenda =====
        int leyendaX = maxX + 18, leyendaY = minY;
        g2.setStroke(new BasicStroke(1.0f));
        for (int e = 0; e < datos.nombres().length; e++) {
            g2.setColor(datos.colores()[e]);
            g2.fillRect(leyendaX, leyendaY, 22, 4);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(datos.nombres()[e], leyendaX + 30, leyendaY + 7);
            leyendaY += 22;
        }
    }

    private int xDe(int talla, double minT, double maxT, int minX, int maxX) {
        if (maxT == minT) return minX + (maxX - minX) / 2;
        return (int) (minX + (talla - minT) / (maxT - minT) * (maxX - minX));
    }

    private int yDe(double logv, double minL, double maxL, int minY, int maxY) {
        double fraccion = (logv - minL) / (maxL - minL);
        return (int) (maxY - fraccion * (maxY - minY));
    }
}
