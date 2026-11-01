package com.practica1.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Ventana principal (formulario con botones de comando, 0.75 puntos).
 * Usa exactamente los mismos métodos núcleo del controlador que el menú
 * de consola; los mensajes del controlador (System.out) se redirigen
 * a un área de texto al estilo terminal.
 */
public class VentanaPrincipal extends JFrame {

    private final transient com.practica1.controlador.GestorInteracciones controlador =
            new com.practica1.controlador.GestorInteracciones();

    private JTextArea consola;
    private JScrollPane scroll;

    public VentanaPrincipal() {

        super("Práctica 1 AMC — Par de puntos más cercano");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // ===== consola gráfica =====
        consola = new JTextArea();
        consola.setEditable(false);
        consola.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        consola.setBackground(new Color(30, 30, 30));
        consola.setForeground(new Color(220, 220, 220));
        scroll = new JScrollPane(consola);

        // ===== panel de botones =====
        JPanel botones = new JPanel(new java.awt.GridLayout(1, 3));
        JButton bCrear = new JButton("1. Crear .tsp aleatorio");
        JButton bCargar = new JButton("2. Cargar dataset");
        JButton bEstrategias = new JButton("3. Comprobar estrategias");
        JButton bTodas = new JButton("4. Comparar todas");
        JButton bDos = new JButton("5. Comparar 2");
        JButton bLimpiar = new JButton("Limpiar consola");

        botones.add(bCrear);
        botones.add(bCargar);
        botones.add(bEstrategias);
        botones.add(bTodas);
        botones.add(bDos);
        botones.add(bLimpiar);

        add(botones, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Los System.out del controlador van a la consola gráfica
        System.setOut(new PrintStream(new OutputStreamConsola(consola, scroll), true));

        // ===== acciones =====
        bCrear.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(this, "¿Cuántos puntos?");
            if (s == null) return;
            try {
                int n = Integer.parseInt(s.trim());
                int modo = JOptionPane.showConfirmDialog(this,
                        "¿Caso medio?\n  Sí = x e y aleatorios\n  No = PEOR CASO (misma x)",
                        "Modo del dataset", JOptionPane.YES_NO_CANCEL_OPTION);
                if (modo == JOptionPane.CANCEL_OPTION || modo == -1) return;
                controlador.crearFicheroAleatorio(n, modo == JOptionPane.YES_OPTION);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido.");
            }
        });

        bCargar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                new Thread(() -> controlador.cargarFichero(f)).start();
            }
        });

        // Ejecutar experimentos EN UN HILO aparte para no congelar la ventana
        java.awt.event.ActionListener enSegundoPlano = e -> new Thread(() -> {
            try {
                controlador.comprobarEstrategias();
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }).start();

        bEstrategias.addActionListener(e -> {
            // El panel gráfico de bloqueo true dejaría el hilo de Swing
            // congelado; el accion de comprobar abre su ventana por sí mismo.
            enSegundoPlano.actionPerformed(e);
        });

        bTodas.addActionListener(e -> pedirYComparar(true));
        bDos.addActionListener(e -> pedirYComparar(false));

        bLimpiar.addActionListener(e -> consola.setText(""));
    }

    private void pedirYComparar(boolean todas) {
        String minS = JOptionPane.showInputDialog(this, "Talla mínima:");
        String maxS = JOptionPane.showInputDialog(this, "Talla máxima:");
        String pasoS = JOptionPane.showInputDialog(this, "Incremento:");
        if (minS == null || maxS == null || pasoS == null) return;
        try {
            int min = Integer.parseInt(minS.trim());
            int max = Integer.parseInt(maxS.trim());
            int paso = Integer.parseInt(pasoS.trim());

            int modo = JOptionPane.showConfirmDialog(this,
                    "¿Caso medio?  No = PEOR CASO (misma x)",
                    "Modo", JOptionPane.YES_NO_CANCEL_OPTION);
            if (modo == -1) return;

            if (todas) {
                new Thread(() ->
                    controlador.compararEstrategias(
                            controlador.getEstrategias(), min, max, paso, modo == 0)
                ).start();
            } else {
                String aS = JOptionPane.showInputDialog(this,
                        "Estrategia A (1-4):\n" + listaEstrategias());
                String bS = JOptionPane.showInputDialog(this,
                        "Estrategia B (1-4):\n" + listaEstrategias());
                if (aS == null || bS == null) return;
                int a = Integer.parseInt(aS.trim()) - 1;
                int b = Integer.parseInt(bS.trim()) - 1;

                new Thread(() ->
                    controlador.compararEstrategias(
                            java.util.List.of(
                                    controlador.getEstrategias().get(a),
                                    controlador.getEstrategias().get(b)),
                            min, max, paso, modo == 0)
                ).start();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Números inválidos.");
        }
    }

    private String listaEstrategias() {
        StringBuilder sb = new StringBuilder();
        for (com.practica1.modelo.algoritmos.EstrategiaAlgoritmica e :
                controlador.getEstrategias()) {
            sb.append("- ").append(e.getNombre()).append("\n");
        }
        return sb.toString();
    }

    /**
     * OutputStream que escribe en el JTextArea de la ventana.
     */
    private static class OutputStreamConsola extends OutputStream {
        private final JTextArea area;
        private final JScrollPane contenedor;

        OutputStreamConsola(JTextArea area, JScrollPane contenedor) {
            this.area = area;
            this.contenedor = contenedor;
        }

        @Override
        public void write(int b) {
            SwingUtilities.invokeLater(() -> {
                area.append(String.valueOf((char) b));
                contenedor.getVerticalScrollBar()
                          .setValue(contenedor.getVerticalScrollBar().getMaximum());
            });
        }
    }
}
