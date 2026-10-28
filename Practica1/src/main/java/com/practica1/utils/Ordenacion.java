package com.practica1.utils;

import java.util.Comparator;
import java.util.List;

/**
 * HeapSort propio (requisito del enunciado: implementar HeapSort o QuickSort).
 *
 * Version in-place e ITERATIVA sobre la propia lista, parametrizada por un
 * Comparator para poder ordenar por x, por y o por cualquier criterio sin
 * duplicar el algoritmo.
 *
 * Complejidad: O(n log n) en MEJOR, MEDIO y PEOR caso.
 *  - Fase 1 (heapify bottom-up): O(n)
 *  - Fase 2 (n-1 extracciones):  O(n log n), cribar cuesta O(log n)
 */
public final class Ordenacion {

    private Ordenacion() {
        // clase de utilidad: solo metodos estaticos
    }

    /**
     * Ordena la lista IN-PLACE (modifica la lista que recibe) con HeapSort.
     *
     * @param lista lista de puntos (se modifica)
     * @param comp  criterio de ordenacion: Punto.POR_X, Punto.POR_Y, ...
     */
    public static <T> void ordenar(List<T> lista, Comparator<? super T> comp) {

        int n = lista.size();

        if (n < 2) {
            return;   // 0 o 1 elemento: ya esta ordenado
        }

        // ===== FASE 1: CONSTRUIR el max-heap =====
        // Las hojas ya son heaps de 1 elemento: se criba solo desde el
        // ULTIMO padre (n/2 - 1) hacia atras, hasta la raiz.
        for (int i = n / 2 - 1; i >= 0; i--) {
            cribar(lista, i, n, comp);
        }

        // ===== FASE 2: EXTRAER el maximo n-1 veces =====
        // Cada maximo se intercambia con la ultima posicion del heap y se
        // criba la nueva raiz. La parte ya ordenada del final NO se toca.
        for (int fin = n - 1; fin > 0; fin--) {
            intercambiar(lista, 0, fin);
            cribar(lista, 0, fin, comp);
        }
    }

    /**
     * Hundir (sift-down) el elemento en la posicion i hasta que se respete
     * el invariante de max-heap en el subrango [0..fin). Iterativo.
     */
    private static <T> void cribar(List<T> lista, int i, int fin,
                                   Comparator<? super T> comp) {

        boolean terminado = false;

        while (!terminado && hayHijos(i, fin)) {

            int hijoIzq  = 2 * i + 1;
            int hijoDer  = 2 * i + 2;

            // Elegir el hijo con la clave MAYOR (segun el comparator)
            int hijoGrande = hijoIzq;
            if (hijoDer < fin && comp.compare(lista.get(hijoDer),
                                              lista.get(hijoIzq)) > 0) {
                hijoGrande = hijoDer;
            }

            // El padre gana a su mayor hijo: propiedad ya restaurada
            if (comp.compare(lista.get(i), lista.get(hijoGrande)) >= 0) {
                terminado = true;
            } else {
                intercambiar(lista, i, hijoGrande);
                i = hijoGrande;   // seguir hundiendolo un nivel
            }
        }
    }

    private static boolean hayHijos(int i, int fin) {
        return 2 * i + 1 < fin;   // basta con que exista el hijo izquierdo
    }

    private static <T> void intercambiar(List<T> lista, int i, int j) {
        T aux = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, aux);
    }
}
