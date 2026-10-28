package com.practica1.modelo.algoritmos;

import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;
import com.practica1.utils.Ordenacion;

/**
 * Estrategia exhaustiva con poda: ordena por x y aborta el bucle interno
 * cuando la separación en x supera la mejor distancia conocida.
 *
 * TODO: sustituir el Collections.sort por QuickSort/HeapSort propio.
 */
public class BusquedaConPoda extends EstrategiaAlgoritmica {

    @Override
    public ResultadoBusqueda resolver(List<Punto> puntos) {

        // Ordenacion propia del alumno (HeapSort) — requisito del enunciado
        Ordenacion.ordenar(puntos, Punto.POR_X);

        Punto mejorP1 = null;
        Punto mejorP2 = null;
        double mejorDistancia = Double.MAX_VALUE;
        int contador = 0;

        long inicio = System.currentTimeMillis();

        for (int i = 0; i < puntos.size() - 1; i++) {

            Punto pi = puntos.get(i);

            // Aborto: si x_j - x_i >= mejorDistancia, ningún punto posterior
            // puede mejorar; se descartan todos los p_k con k > j.
            int j = i + 1;
            while (j < puntos.size()
                   && puntos.get(j).getX() - pi.getX() < mejorDistancia) {

                double distancia = calcularDistancia(pi, puntos.get(j));
                contador++;   // solo distancias EUCLÍDEAS, no comparaciones de x

                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    mejorP1 = pi;
                    mejorP2 = puntos.get(j);
                }
                j++;
            }
        }

        long fin = System.currentTimeMillis();

        return new ResultadoBusqueda(mejorP1, mejorP2, mejorDistancia,
                                     contador, fin - inicio);
    }
}
