package com.practica1.modelo.algoritmos;

import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;

/**
 * Clase abstracta que define la parte común para las estrategias de búsqueda
 */
public abstract class EstrategiaAlgoritmica {

    /**
     * Nombre de la estrategia según el enunciado
     */
    public abstract String getNombre();

    /**
     * Distancia euclídea entre dos puntos
     */
    protected double calcularDistancia(Punto p1, Punto p2) {
        return Punto.distancia(p1, p2);
    }

    /**
     * Cada estrategia concreta implementa su método de resolución
     *
     * @param puntos Copia del dataset
     * @return ResultadoBusqueda con solución y métricas
     */
    public abstract ResultadoBusqueda resolver(List<Punto> puntos);
}
