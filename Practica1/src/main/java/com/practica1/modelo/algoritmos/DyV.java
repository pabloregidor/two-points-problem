package com.practica1.modelo.algoritmos;

import java.util.ArrayList;
import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;

/**
 * Estrategia Divide y Vencerás: divide por x en el índice medio,
 * resuelve recursivamente cada mitad y resuelve la franja central
 * con búsqueda con poda.
 *
 * Casos base usan un resultado neutro con distancia infinita
 * (elemento neutro del min), lo que evita propagar null.
 */
public class DyV extends EstrategiaAlgoritmica {

    private static final ResultadoBusqueda VACIO =
            new ResultadoBusqueda(null, null, Double.MAX_VALUE, 0, 0);

    @Override
    public ResultadoBusqueda resolver(List<Punto> puntos) {
        // Copia defensiva: el dataset original NO debe quedar ordenado
        List<Punto> copia = new ArrayList<>(puntos);
        copia.sort(Punto.POR_X);   // TODO: sustituir por QuickSort/HeapSort propio

        long inicio = System.currentTimeMillis();
        ResultadoBusqueda r = dyvRec(copia, 0, copia.size() - 1);
        long fin = System.currentTimeMillis();

        // Reconstruimos con el tiempo TOTAL (ordenación + recursión)
        return new ResultadoBusqueda(r.getPunto1(), r.getPunto2(),
                r.getDistanciaMinima(), r.getComparacionesRealizadas(),
                fin - inicio);
    }

    private ResultadoBusqueda dyvRec(List<Punto> p, int izq, int der) {

        // Caso base 1: 0 o 1 punto -> par imposible (sentinela infinito)
        if (der - izq < 1) {
            return VACIO;
        }

        // Caso base 2: 2 puntos -> par directo
        if (der - izq == 1) {
            double d = calcularDistancia(p.get(izq), p.get(der));
            return new ResultadoBusqueda(p.get(izq), p.get(der), d, 1, 0);
        }

        // DIVIDE (medio es INDICE; la vertical es la x de ese punto)
        int medio = izq + (der - izq) / 2;
        double xm = p.get(medio).getX();

        ResultadoBusqueda ri = dyvRec(p, izq, medio);
        ResultadoBusqueda rd = dyvRec(p, medio + 1, der);
        ResultadoBusqueda min = combinar(ri, rd);

        // VENCERAS: franja central |x - xm| < d_min
        double dMin = min.getDistanciaMinima();
        List<Punto> franja = new ArrayList<>();
        for (Punto punto : p) {
            if (Math.abs(punto.getX() - xm) < dMin) {
                franja.add(punto);
            }
        }

        // Busqueda con poda dentro de la franja
        int contador = min.getComparacionesRealizadas();
        Punto mejorP1 = min.getPunto1();
        Punto mejorP2 = min.getPunto2();

        for (int a = 0; a < franja.size() - 1; a++) {
            for (int b = a + 1; b < franja.size(); b++) {
                if (Math.abs(franja.get(a).getX() - franja.get(b).getX()) >= dMin) {
                    break;   // poda: comparacion de x, NO se contabiliza
                }
                double d = calcularDistancia(franja.get(a), franja.get(b));
                contador++;
                if (d < dMin) {
                    dMin = d;
                    mejorP1 = franja.get(a);
                    mejorP2 = franja.get(b);
                }
            }
        }

        return new ResultadoBusqueda(mejorP1, mejorP2, dMin, contador, 0);
    }

    // Combina dos resultados: gana la menor distancia, se SUMAN los contadores
    private ResultadoBusqueda combinar(ResultadoBusqueda a, ResultadoBusqueda b) {
        if (a.getDistanciaMinima() <= b.getDistanciaMinima()) {
            return new ResultadoBusqueda(a.getPunto1(), a.getPunto2(),
                    a.getDistanciaMinima(),
                    a.getComparacionesRealizadas() + b.getComparacionesRealizadas(), 0);
        }
        return new ResultadoBusqueda(b.getPunto1(), b.getPunto2(),
                b.getDistanciaMinima(),
                a.getComparacionesRealizadas() + b.getComparacionesRealizadas(), 0);
    }
}
