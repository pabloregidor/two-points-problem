package com.practica1.modelo.algoritmos;

import java.util.ArrayList;
import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;
import com.practica1.utils.Ordenacion;

/**
 * Estrategia Divide y Vencerás MEJORADA: igual que DyV, pero la franja
 * central se ORDENA POR Y y cada punto se compara solo con sus siguientes
 */
public class DyVMejorada extends EstrategiaAlgoritmica {

    @Override
    public String getNombre() {
        return "DyV Mejorado";
    }

    // se comprueba cada punto con los 7 siguientes de la franja ordenada por «y»
    private static final int VENTANA = 7;

    private static final ResultadoBusqueda VACIO =
            new ResultadoBusqueda(null, null, Double.MAX_VALUE, 0, 0);

    @Override
    public ResultadoBusqueda resolver(List<Punto> puntos) {
        // Copia defensiva: el dataset original NO debe quedar ordenado
        List<Punto> copia = new ArrayList<>(puntos);
        // Ordenacion propia del alumno (HeapSort) — requisito del enunciado
        Ordenacion.ordenar(copia, Punto.POR_X);

        long inicio = System.nanoTime();
        ResultadoBusqueda r = dyvRec(copia, 0, copia.size() - 1);
        long fin = System.nanoTime();

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

        // VENCERAS: franja central |x - xm| < d_min.
        // Construcción EFICIENTE con doble puntero sobre la lista
        // ordenada por x: O(tam franja) en vez de O(n) por nodo.
        double dMin = min.getDistanciaMinima();

        int li = medio;
        while (li >= izq && xm - p.get(li).getX() < dMin) {
            li--;
        }
        li++;

        int ld = medio + 1;
        while (ld <= der && p.get(ld).getX() - xm < dMin) {
            ld++;
        }
        ld--;

        List<Punto> franja = new ArrayList<>();
        for (int i = li; i <= medio; i++) {
            franja.add(p.get(i));
        }
        for (int i = medio + 1; i <= ld; i++) {
            franja.add(p.get(i));
        }

        // === MEJORA respecto a DyV ===
        // La franja se ordena por Y (en cada llamada recursiva)
        Ordenacion.ordenar(franja, Punto.POR_Y);

        // Cada punto solo se compara con sus VENTANA vecinos siguientes
        int contador = min.getComparacionesRealizadas();
        Punto mejorP1 = min.getPunto1();
        Punto mejorP2 = min.getPunto2();

        for (int a = 0; a < franja.size() - 1; a++) {
            int limite = Math.min(a + 1 + VENTANA, franja.size());
            for (int b = a + 1; b < limite; b++) {

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
