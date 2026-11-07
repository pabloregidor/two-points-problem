package com.practica1.vista;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;

/**
 * Panel que dibuja el dataset a escala (respetando la proporción de
 * los ejes) y la solucion: el par de puntos más cercanos con la línea
 * que los une. Ventana de dibujo de opción 3 (nota de la práctica).
 */
public class PanelGrafico extends JPanel {

    private static final int MARGEN = 40;
    private static final int TAM_PUNTO = 5;

    private List<Punto> puntos;
    private ResultadoBusqueda solucion;
    private String titulo = "";

    public PanelGrafico() {
        setPreferredSize(new Dimension(850, 700));
        setBackground(Color.WHITE);
    }

    /** Datos sin abrir ventana propia (útil en un JFrame externo). */
    public void setDatos(List<Punto> puntos, ResultadoBusqueda solucion,
                         String tituloVentana) {
        this.puntos = puntos;
        this.solucion = solucion;
        this.titulo = tituloVentana;
        repaint();
    }

    /**
     * Renderiza el panel de solución a un PNG SIN abrir ventana
     * (capturas de la memoria, al estilo de VistaGraficaTiempos).
     */
    public static void guardarComoImagen(List<Punto> puntos, ResultadoBusqueda solucion,
                                         String titulo, File destino) {
        try {
            PanelGrafico panel = new PanelGrafico();
            panel.setPreferredSize(new Dimension(900, 700));
            panel.setSize(900, 700);
            panel.setDatos(puntos, solucion, titulo);

            java.awt.image.BufferedImage img =
                    new java.awt.image.BufferedImage(900, 700,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            panel.printAll(g);
            g.dispose();
            javax.imageio.ImageIO.write(img, "png", destino);
        } catch (Exception e) {
            System.err.println("Error generando imagen " + destino + ": " + e.getMessage());
        }
    }

    /**
     * Abre la ventana con la gráfica.
     * bloquear=true: NO devuelve el control hasta que el usuario la cierra
     * (uso desde menú CLI); bloquear=false: dispara la ventana y sigue
     * (uso desde la VentanaPrincipal).
     */
    public static void mostrar(List<Punto> puntos, ResultadoBusqueda solucion,
                               String titulo, boolean bloquear) {

        CountDownLatch cierre = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            PanelGrafico panel = new PanelGrafico();
            panel.setDatos(puntos, solucion, titulo);

            JFrame frame = new JFrame("Panel de solución: " + titulo);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
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
            if (bloquear) cierre.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (puntos == null || puntos.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Extremos del mundo
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Punto p : puntos) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        double anchoMundo = Math.max(maxX - minX, 1e-9);
        double altoMundo = Math.max(maxY - minY, 1e-9);

        // Escalado UNIFORME (misma proporción en ambos ejes) y centrado
        double escala = Math.min((w - 2.0 * MARGEN) / anchoMundo,
                                 (h - 2.0 * MARGEN) / altoMundo);
        double offX = (w - anchoMundo * escala) / 2.0;
        double offY = (h - altoMundo * escala) / 2.0;

        // La línea de la solción DEBAJO de los puntos
        if (solucion != null && solucion.getPunto1() != null) {
            Punto p1 = solucion.getPunto1();
            Punto p2 = solucion.getPunto2();

            int x1 = (int) (offX + (p1.getX() - minX) * escala);
            int y1 = (int) (offY + (p1.getY() - minY) * escala);
            int x2 = (int) (offX + (p2.getX() - minX) * escala);
            int y2 = (int) (offY + (p2.getY() - minY) * escala);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(x1, y1, x2, y2);

            // etiquetas del par
            g2.setColor(new Color(200, 0, 0));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(p1.toString(), x1 - 20, y1 - 10);
            g2.drawString(p2.toString(), x2 - 20, y2 + 20);
        }

        // Todos los puntos
        for (Punto p : puntos) {
            int x = (int) (offX + (p.getX() - minX) * escala) - TAM_PUNTO / 2;
            int y = (int) (offY + (p.getY() - minY) * escala) - TAM_PUNTO / 2;

            boolean esResultado = solucion != null
                    && (p.equals(solucion.getPunto1()) || p.equals(solucion.getPunto2()));

            if (esResultado) {
                // Punto resultado: mismo tamaño, relleno rojo
                g2.setColor(Color.RED);
                g2.fillOval(x, y, TAM_PUNTO, TAM_PUNTO);
            } else {
                g2.setColor(Color.BLUE);
                g2.fillOval(x, y, TAM_PUNTO, TAM_PUNTO);
            }
        }

        // Pie: identificación
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String pie = puntos.size() + " puntos  |  "
                + (solucion == null || solucion.getPunto1() == null
                        ? "sin solución"
                        : String.format(java.util.Locale.US,
                                "min = %.8f", solucion.getDistanciaMinima()));
        g2.drawString(pie, MARGEN, h - 12);
    }
}
