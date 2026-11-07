package com.practica1.modelo.algoritmos;

import java.util.ArrayList;
import java.util.List;

import com.practica1.modelo.Punto;
import com.practica1.modelo.ResultadoBusqueda;
import com.practica1.utils.Ordenacion;

/**
 * Estrategia Divide y Vencerás: divide por x en el índice medio,
 * resuelve recursivamente cada mitad con búsqueda con poda.
 */
public class DyV extends EstrategiaAlgoritmica {

    @Override
    public String getNombre() {
        return "DivideVenceras";
    }

    private static final ResultadoBusqueda VACIO =
            new ResultadoBusqueda(null, null, Double.MAX_VALUE, 0, 0);

    @Override
    public ResultadoBusqueda resolver(List<Punto> puntos) {
        List<Punto> copia = new ArrayList<>(puntos);
        Ordenacion.ordenar(copia, Punto.POR_X);

        long inicio = System.nanoTime();
        ResultadoBusqueda r = dyvRec(copia, 0, copia.size() - 1);
        long fin = System.nanoTime();

        return new ResultadoBusqueda(r.getPunto1(), r.getPunto2(),
                r.getDistanciaMinima(), r.getComparacionesRealizadas(),
                fin - inicio);
    }

    private ResultadoBusqueda dyvRec(List<Punto> p, int izq, int der) {

        // caso base: 0 o 1 punto, par imposible
        if (der - izq < 1) {
            return VACIO;
        }

        // caso base: 2 puntos, par directo
        if (der - izq == 1) {
            double d = calcularDistancia(p.get(izq), p.get(der));
            return new ResultadoBusqueda(p.get(izq), p.get(der), d, 1, 0);
        }

        // DIVIDE
        int medio = izq + (der - izq) / 2;
        double xm = p.get(medio).getX();

        ResultadoBusqueda ri = dyvRec(p, izq, medio);
        ResultadoBusqueda rd = dyvRec(p, medio + 1, der);
        ResultadoBusqueda min = combinar(ri, rd);

        // VENCERAS: franja central con los puntos a menos de la menor distancia de la línea.
        // Se construye con dos punteros desde el medio, sin recorrer toda la lista.
        double dMin = min.getDistanciaMinima();

        int li = medio;           // descender por la izquierda
        while (li >= izq && xm - p.get(li).getX() < dMin) {
            li--;
        }
        li++;

        int ld = medio + 1;       // ascender por la derecha
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

        // búsqueda con poda dentro de la franja
        int contador = min.getComparacionesRealizadas();
        Punto mejorP1 = min.getPunto1();
        Punto mejorP2 = min.getPunto2();

        for (int a = 0; a < franja.size() - 1; a++) {
            for (int b = a + 1; b < franja.size(); b++) {
                if (Math.abs(franja.get(a).getX() - franja.get(b).getX()) >= dMin) {
                    break;   // descarte por la «x»: no es una distancia euclídea
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

    // se queda el de menor distancia y se suman los contadores
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
