package com.practica1.modelo.algoritmos;

import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;

/**
 * Clase abstracta que define el contrato común para las estrategias
 * de búsqueda del par de puntos más cercano.
 */
public abstract class EstrategiaAlgoritmica {

    /**
     * Nombre de la estrategia según el enunciado
     * (Exhaustivo, ExhaustivoPoda, DivideVenceras, DyV Mejorado).
     */
    public abstract String getNombre();

    /**
     * Distancia euclídea entre dos puntos (método común a todas las estrategias).
     */
    protected double calcularDistancia(Punto p1, Punto p2) {
        return Punto.distancia(p1, p2);
    }

    /**
     * Cada estrategia concreta implementa su método de resolución.
     *
     * @param puntos Copia del dataset (el ORIGINAL no debe modificarse).
     * @return ResultadoBusqueda con solución y métricas.
     */
    public abstract ResultadoBusqueda resolver(List<Punto> puntos);
}
