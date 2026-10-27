package com.practica1.modelo;

/**
 * Objeto inmutable que encapsula el resultado de una estrategia:
 * el par de puntos más cercanos, la distancia mínima y las métricas
 * de esfuerzo (distancias euclídeas calculadas y tiempo de ejecución).
 */
public class ResultadoBusqueda {

    private final Punto punto1;
    private final Punto punto2;

    private final double distanciaMinima;

    // Métricas para el análisis de complejidad
    private final int comparacionesRealizadas;
    private final long tiempoEjecucionMilisegundos;

    public ResultadoBusqueda(Punto p1, Punto p2, double distancia, int comparaciones, long tiempo) {
        this.punto1 = p1;
        this.punto2 = p2;
        this.distanciaMinima = distancia;
        this.comparacionesRealizadas = comparaciones;
        this.tiempoEjecucionMilisegundos = tiempo;
    }

    public Punto getPunto1() {
        return punto1;
    }

    public Punto getPunto2() {
        return punto2;
    }

    public double getDistanciaMinima() {
        return distanciaMinima;
    }

    public int getComparacionesRealizadas() {
        return comparacionesRealizadas;
    }

    public long getTiempoEjecucionMilisegundos() {
        return tiempoEjecucionMilisegundos;
    }
}
