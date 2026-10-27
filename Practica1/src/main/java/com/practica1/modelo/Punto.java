package com.practica1.modelo;

import java.util.Comparator;

public class Punto {

    private double x;
    private double y;
    private int id;

    // Constructores
    public Punto() {
    }

    public Punto(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public Punto(Punto p) {
        this.x = p.x;
        this.y = p.y;
        this.id = p.id;
    }

    // Getters / Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Distancia euclidea a otro punto (instancia)
    public double distanciaA(Punto o) {
        double dx = this.x - o.x;
        double dy = this.y - o.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Distancia estatica (util para contar operaciones en los algoritmos)
    public static double distancia(Punto a, Punto b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return id + " (" + String.format("%.3f", x) + ", " + String.format("%.3f", y) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Punto)) return false;
        Punto p = (Punto) o;
        return this.id == p.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Comparadores para ordenar por x / y
    public static final Comparator<Punto> POR_X =
            Comparator.comparingDouble(Punto::getX)
                      .thenComparingDouble(Punto::getY);

    public static final Comparator<Punto> POR_Y =
            Comparator.comparingDouble(Punto::getY)
                      .thenComparingDouble(Punto::getX);
}
