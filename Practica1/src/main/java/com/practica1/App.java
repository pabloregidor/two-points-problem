package com.practica1;

import com.practica1.controlador.GestorInteracciones;

public class App {

    public static void main(String[] args) {
        if (args.length > 0 && "demo".equals(args[0])) {
            new GestorInteracciones().demoConVentana();
        } else {
            new GestorInteracciones().menuPrincipal();
        }
    }
}
