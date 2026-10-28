package com.practica1.modelo.algoritmos;

import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;

/**
 * Estrategia exhaustiva: comprueba todos los pares posibles.
 * Complejidad: O(n^2) — n·(n-1)/2 distancias euclídeas siempre.
 */
public class BusquedaExhaustiva extends EstrategiaAlgoritmica {

    @Override
    public String getNombre() {
        return "Exhaustivo";
    }

    @Override
    public ResultadoBusqueda resolver(List<Punto> puntos) {
        Punto mejorP1 = null;
        Punto mejorP2 = null;
        double mejorDistancia = Double.MAX_VALUE;
        int contador = 0;

        long inicio = System.nanoTime();

        for (int i = 0; i < puntos.size() - 1; i++) {
            for (int j = i + 1; j < puntos.size(); j++) {
                double distancia = calcularDistancia(puntos.get(i), puntos.get(j));
                contador++;
                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    mejorP1 = puntos.get(i);
                    mejorP2 = puntos.get(j);
                }
            }
        }

        long fin = System.nanoTime();

        return new ResultadoBusqueda(mejorP1, mejorP2, mejorDistancia,
                                     contador, fin - inicio);
    }
}
