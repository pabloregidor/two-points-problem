# AGENTS.md — Metodología de trabajo en la memoria (Práctica 1 AMC)

Protocolo acordado entre el alumno y el asistente para redactar
`AMC-memoria.docx` punto a punto.

## Regla de oro
NO se escribe NADA en el documento hasta que el alumno dé el OK
explícito sobre el texto propuesto.

## Ciclo de trabajo (task = un punto del índice)
0. FORMATEO: cuando el alumno aplique formato a las partes ya
   rellenadas, el asistente LO COMPRUEBA en el .docx (fuentes,
   tamaños, márgenes de los párrafos escritos) y repite ese MISMO
   formato en el siguiente texto que escriba. Nunca se escribe con un
   formato distinto al que el alumno haya fijado.
0b. ANCLAJES: el texto de los títulos aparece dos veces (entrada del
    índice y título real). Al insertar texto bajo un apartado, el
    asistente SIEMPRE se ancla a la ÚLTIMA ocurrencia del título y
    verifica por adyacencia (párrafo anterior y siguiente) antes de
    guardar. Después de escribir, imprime la vecindad del nuevo
    párrafo para que la estructura sea comprobable.
0c. IMÁGENES: los párrafos que contienen dibujos tienen texto vacío.
    NINGÚN borrado o limpieza puede eliminarlos: antes de quitar un
    párrafo sin texto se comprueba si contiene <w:drawing>. Cada pie
    (Imagen n / Gráfica n) debe tener exactamente un dibujo adyacente;
    tras cualquier operación se contan contenidos "draws" y se publican
    junto con las vecindades.
1. El alumno da paso (o el asistente pregunta si es el inicio).
2. El asistente **plantea el siguiente punto del índice**:
   - Ofrece un PROYECTO DE PÁRRAFO (solo en el chat, sin escribirlo
     en el documento).
   - Si el punto requiere datos o decisiones del alumno, FORMULA
     LAS PREGUNTAS necesarias antes de proponer el texto.
3. El alumno responde:
   - "Paso" → se aplica tal cual el texto propuesto.
   - Correcciones → se revisa el borrador y se re-presenta.
4. Solo tras el OK explícito se ESCRIBE el texto en
   `AMC-memoria.docx` (completando el punto correspondiente del índice)
   y el ciclo vuelve a 1 para el siguiente punto.

## Índice en vigor (tasks)
1. Objetivo y descripción del problema
2. Estrategias implementadas (2.1 exhaustiva, 2.2 poda, 2.3 DyV, 2.4 DyV mejorada)
3. Ordenación: HeapSort (+ 3.1 ejemplos límite: imágenes ya generadas)
4. Estudio experimental (4.1 metodología, 4.2 caso medio, 4.3 peor caso
   — imágenes ya pegadas en el fichero de índice —, 4.4 métrica de distancias)
5. Conclusiones
6. Anexo: decisiones de diseño (resumen → fuente: DecisionesPractica1.md)

## Materiales de apoyo
- `DecisionesPractica1.md` — registro de las 40 decisiones (A..L), con
  su justificación de defensa.
- `Practica1/tabla_caso_medio.csv` y `tabla_peor_caso.csv` — datos reales.
- Imágenes ya generadas: grafica_caso_medio/peor_caso.png,
  dibujo_solucion_caso_medio/peor_caso.png,
  heapsort_datos_aleatorios/ordenado_ascendente.png.
- Enunciado: Practica1AMC_v24_10.pdf.

## Estilo de redacción aprobado (reglas del alumno, aplican a TODO el texto)
- **Voz impersonal**, nunca primera persona: "se comprueba", "se genera",
  "se mide" (no "compruebo", "implemento").
- **Directo y conciso, sin florituras ni metalenguaje**: nada de "el
  algoritmo funciona así…". Para procesos, introducir con una frase
  tipo «El proceso utilizado … ha sido el siguiente:» seguida de una
  LISTA CON VIÑETAS que explique el método.
- **Sin símbolos para conceptos verbales**: nada de δ; escribir "la
  menor distancia", "0,7 veces esa distancia". Los únicos símbolos son
  los de órdenes de complejidad O(n·log n) y fórmulas aritméticas.
- **Sin paréntesis redundantes**: no "(peor caso)", usar "es decir, en
  el peor caso" o integrarlo en la frase.
- **Coordenadas entre comillas latinas**: «x», «y» siempre que se
  citen como coordenadas ("la coordenada «x»", "las «x» de dos
  puntos").
- **Datos citados por su tabla/imagen**: "Según la Tabla 1…", y los
  números decimales con coma ("0,03 ms") al citarlos en el texto.
- **Cualidad máxima**: "el más rápido" (no "el mejor") al comparar
  estrategias por tiempos.
- **Variación de la JVM**: en medias sub-milisegundo se admite una
  línea que atribuya las fluctuaciones al ajuste interno de la máquina
  virtual Java, y remitir a aumentar el número de experimentos.
- **Pseudocódigo**: SIEMPRE en texto (nunca imagen), Consolas 10,
  espaciado sencillo, keywords del italiano de la práctica en
  mayúsculas (PARA/MIENTRAS/SI).
- **Lo que cuenta es la explicación básica**: el lector objetivo es un
  compañero de carrera; sin jerga no explicada (JVM se explicó en el
  chat antes de usarla en el texto).

## Estado actual del documento (contexto vivo)
- Base de trabajo: `AMC-memoria.docx` (con las 6 imágenes y 4 tablas).
- Escrito y verificado: índice (completo, con 3.2), punto 1, punto 2
  (2.1–2.4), punto 3 + 3.1 (imágenes) + 3.2 (pseudo + conteo + cierre),
  4.1 (metodología con viñetas), 4.2 (análisis + nota JVM), 4.3
  (análisis), tablas de tiempos y de distancias (4.4 con subtítulos).
- Pendiente de paso: **4.4** (borrador redactado en el chat: lectura de
  las tablas de distancias, factor 74 del DyV normal vs mejorado).
- Después: 5. Conclusiones y 6. Anexo; portada y repaso del alumno.
- Fuente de los datos: Practica1/tabla_caso_medio.csv,
  tabla_peor_caso.csv y las 4 corridas de la opción 5 (distancias).
