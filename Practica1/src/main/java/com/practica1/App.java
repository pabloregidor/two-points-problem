package com.practica1;

import com.practica1.controlador.GestorInteracciones;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        if (args.length > 0 && "demo".equals(args[0])) {
            new GestorInteracciones().demoConVentana();
        } else if (args.length > 0 && "gui".equals(args[0])) {
            SwingUtilities.invokeLater(() -> new com.practica1.vista.VentanaPrincipal().setVisible(true));
        } else if (args.length > 0 && "capturas".equals(args[0])) {
            new GestorInteracciones().capturasParaMemoria();
        } else if (args.length > 0 && "capturas_aux".equals(args[0])) {
            new GestorInteracciones().capturasAuxiliaresParaMemoria();
        } else {
            new GestorInteracciones().menuPrincipal();
        }
    }
}
