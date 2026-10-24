# Bacteria — Simulador de ecosistema bacteriano 2D

## 1. Descripción del proyecto

**Bacteria** es un proyecto educativo en Java para construir una simulación bidimensional de un ecosistema bacteriano. El objetivo es representar organismos que se desplazan por un entorno, buscan nutrientes, consumen energía, se reproducen, mutan y mueren.

El proyecto también servirá para aprender y experimentar con:

- Gráficos 2D y transformaciones de cámara.
- Programación orientada a objetos.
- Simulación basada en pasos de tiempo.
- Concurrencia y sincronización en Java.
- Algoritmos de detección de proximidad.
- Pruebas automatizadas y medición del rendimiento.

La simulación será una **aproximación educativa**, no un modelo biológico clínico ni una reproducción exacta de todos los procesos de una bacteria real.

## 2. Objetivos

### Objetivo general

Desarrollar una aplicación de escritorio que permita observar y controlar un ecosistema artificial de bacterias en un mundo 2D.

### Objetivos específicos

1. Crear una ventana y un área de dibujo.
2. Implementar una cámara con desplazamiento y zoom.
3. Dibujar bacterias, nutrientes y una cuadrícula de referencia.
4. Implementar movimiento y reglas básicas de supervivencia.
5. Añadir consumo de nutrientes, gasto de energía y muerte.
6. Implementar reproducción y herencia de características.
7. Introducir mutaciones y variación entre generaciones.
8. Incorporar procesamiento paralelo de forma segura.
9. Mostrar estadísticas de la población y el rendimiento.
10. Crear pruebas para validar las reglas de la simulación.

## 3. Tecnologías y herramientas

| Herramienta | Uso |
|---|---|
| Java 21 LTS | Lenguaje y entorno de ejecución |
| IntelliJ IDEA | Desarrollo, depuración y refactorización |
| Swing | Ventana, paneles y eventos de entrada |
| Java 2D (`Graphics2D`) | Dibujo y transformaciones gráficas |
| Maven | Compilación, gestión del proyecto y dependencias |
| Git y GitHub | Historial de cambios y respaldo del código |
| JUnit 5 | Pruebas automatizadas |
| Java Flight Recorder / VisualVM | Análisis de memoria, CPU e hilos cuando sea necesario |

No se necesita una biblioteca gráfica externa en la primera etapa. Swing y Java 2D forman parte del JDK.

## 4. Alcance de la primera versión

La primera versión se concentrará en la base visual. **No se implementará todavía la biología completa ni el multihilo.**

Debe incluir:

- Una ventana redimensionable.
- Un panel de dibujo (`WorldPanel`).
- Un mundo lógico más grande que la zona visible.
- Una cuadrícula para visualizar la escala del mundo.
- Una bacteria de prueba con posición y tamaño.
- Una cámara que permita desplazarse por el mundo.
- Zoom mediante la rueda del ratón.
- Desplazamiento de la cámara mediante arrastre del ratón.
- Separación entre las coordenadas del mundo y las coordenadas de pantalla.

### Criterios de aceptación

- La aplicación inicia sin errores.
- La cuadrícula y la bacteria se dibujan correctamente.
- El zoom tiene límites mínimos y máximos razonables.
- El zoom centrado en el cursor conserva el punto del mundo que está bajo este.
- El movimiento de cámara no altera las coordenadas reales de la bacteria.
- La ventana puede redimensionarse sin perder el estado del mundo.

## 5. Arquitectura inicial

Estructura sugerida:

```text
bacteria-simulator/
├── pom.xml
├── README.md
├── docs/
│   └── PROJECT.md
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/bacteria/
│   │           ├── Main.java
│   │           ├── WorldPanel.java
│   │           ├── Camera.java
│   │           └── Bacterium.java
│   └── test/
│       └── java/
│           └── com/bacteria/
```

### Responsabilidades de las clases

#### `Main`

- Crear la ventana principal (`JFrame`).
- Configurar el tamaño inicial, el cierre y la ubicación de la ventana.
- Crear e instalar `WorldPanel`.
- Inicializar la interfaz desde el Event Dispatch Thread de Swing.

#### `WorldPanel`

- Extender `JPanel`.
- Sobrescribir `paintComponent(Graphics g)`.
- Crear una copia de `Graphics2D` y liberarla al terminar con `dispose()`.
- Aplicar la transformación de cámara.
- Dibujar la cuadrícula y los objetos del mundo.
- Recibir eventos de ratón para zoom y desplazamiento.
- Solicitar repintados mediante `repaint()`.

#### `Camera`

- Mantener la posición de la cámara en coordenadas del mundo.
- Mantener el factor de zoom.
- Definir límites de zoom.
- Convertir coordenadas de pantalla a coordenadas del mundo y viceversa.
- Aplicar la transformación de cámara a `Graphics2D`.
- Implementar el zoom centrado en el cursor.

#### `Bacterium`

En la primera versión será un objeto de prueba sencillo:

- Identificador, si resulta útil.
- Posición `x` e `y` en coordenadas del mundo.
- Radio o dimensiones.
- Método de dibujo, o datos que `WorldPanel` utilice para dibujarlo.

No debe depender de la cámara: la posición de la bacteria pertenece al mundo, no a la pantalla.

## 6. Modelo de coordenadas y cámara

Se utilizarán dos sistemas de coordenadas:

- **Coordenadas del mundo:** posición real de los objetos.
- **Coordenadas de pantalla:** píxeles donde aparecen dibujados.

La transformación conceptual será:

```text
xPantalla = (xMundo - cameraX) * zoom
yPantalla = (yMundo - cameraY) * zoom
```

La implementación podrá usar `AffineTransform` para trasladar y escalar el contexto gráfico. El orden de las transformaciones debe comprobarse con pruebas visuales.

### Reglas de cámara

- El zoom debe mantenerse dentro de un rango configurable.
- Al hacer zoom sobre el cursor, el punto del mundo situado bajo él debe permanecer en la misma posición de pantalla.
- El desplazamiento debe modificar la cámara, no los objetos.
- Los objetos deben conservar sus coordenadas cuando cambia el tamaño de la ventana.
- La cuadrícula debe seguir siendo útil a diferentes niveles de zoom; más adelante puede ajustarse su separación visual.

## 7. Evolución de la simulación

Las fases siguientes se implementarán solo después de completar y probar la fase visual.

### Fase 1 — Ventana y cámara

- Crear `Main`, `WorldPanel`, `Camera` y `Bacterium`.
- Dibujar fondo, cuadrícula y bacteria de prueba.
- Añadir zoom y desplazamiento.
- Validar coordenadas y límites.

**Resultado:** un visor 2D navegable.

### Fase 2 — Mundo y entidades

- Definir las dimensiones del mundo.
- Crear una colección de bacterias.
- Crear nutrientes ubicados en el mapa.
- Separar los datos del mundo de su representación gráfica.

**Resultado:** múltiples objetos visibles en un mismo mundo.

### Fase 3 — Ciclo de simulación

- Crear un `SimulationEngine`.
- Definir un paso de simulación o *tick*.
- Implementar movimiento inicial.
- Mantener el renderizado separado de la lógica.
- Añadir controles para iniciar, pausar y reiniciar.

**Resultado:** organismos que cambian de estado con el tiempo.

### Fase 4 — Supervivencia y recursos

- Dar energía y edad a cada bacteria.
- Consumir energía al moverse.
- Recuperar energía al consumir nutrientes.
- Eliminar bacterias que mueren.
- Definir reglas para regenerar nutrientes, si corresponde.

**Resultado:** una población dependiente de los recursos disponibles.

### Fase 5 — Reproducción y mutación

- Reproducir bacterias cuando cumplan condiciones definidas.
- Transmitir características a la descendencia.
- Introducir variaciones aleatorias configurables.
- Registrar generaciones y cambios de población.

**Resultado:** evolución artificial observable a través del tiempo.

### Fase 6 — Detección eficiente

- Implementar una cuadrícula espacial o partición del mapa.
- Buscar vecinos y nutrientes cercanos sin recorrer todos los objetos en cada consulta.
- Comparar el algoritmo espacial con una búsqueda exhaustiva.
- Medir tiempos con diferentes tamaños de población.

**Resultado:** detección de proximidad más escalable.

### Fase 7 — Concurrencia

- Crear un `SimulationWorkerPool` basado en `ExecutorService`.
- Dividir el trabajo en bloques de bacterias.
- Hacer que los trabajadores calculen resultados sin modificar simultáneamente estructuras compartidas.
- Esperar a que terminen los trabajos antes de resolver el siguiente paso.
- Resolver nacimientos, muertes y consumo de recursos de forma consistente.
- Comparar los resultados y el rendimiento frente a la versión secuencial.

**Resultado:** simulación paralela controlada y medible.

### Fase 8 — Interfaz de laboratorio

- Mostrar población total y evolución temporal.
- Mostrar nacimientos, muertes y mutaciones.
- Mostrar velocidad de simulación y duración de cada paso.
- Añadir controles para pausar, reiniciar y ajustar parámetros.
- Permitir cambiar la cantidad inicial de bacterias y nutrientes.

**Resultado:** una herramienta interactiva para experimentar con el ecosistema.

## 8. Principios de concurrencia

La concurrencia se añadirá después de contar con una simulación secuencial correcta y comprobable.

Reglas recomendadas:

1. No crear un hilo nuevo por bacteria.
2. Utilizar un pool de trabajadores reutilizables.
3. No modificar colecciones mientras otros hilos las recorren sin una estrategia de coordinación.
4. Evitar que la lógica de simulación dependa del hilo gráfico de Swing.
5. Actualizar la interfaz desde el Event Dispatch Thread.
6. Usar pasos de simulación definidos y coordinar la finalización de los cálculos.
7. Preferir resultados locales por trabajador y una fase controlada de combinación.
8. Evitar sincronización innecesaria y medir antes de optimizar.
9. Mantener una implementación secuencial de referencia para comparar resultados.
10. Considerar una semilla aleatoria configurable para facilitar la reproducción de experimentos.

Una arquitectura posible para cada paso será:

1. Leer una instantánea del estado actual.
2. Calcular decisiones por bloques en paralelo.
3. Combinar los resultados y resolver conflictos de recursos.
4. Publicar el nuevo estado para su renderizado.

## 9. Reglas biológicas simplificadas

Las reglas se definirán como parámetros ajustables, no como constantes dispersas por el código.

Ejemplos de parámetros:

- Cantidad inicial de bacterias.
- Energía inicial.
- Coste de movimiento.
- Energía obtenida de los nutrientes.
- Umbral de reproducción.
- Edad máxima.
- Probabilidad de mutación.
- Cantidad de nutrientes y tasa de regeneración.
- Velocidad de simulación.

Las reglas deben considerarse hipótesis del modelo. No se afirmará que representan con exactitud una especie bacteriana real sin fuentes y validación científica apropiadas.

## 10. Pruebas

Se deben probar las reglas independientemente de la interfaz gráfica.

Pruebas iniciales:

- La transformación de coordenadas y su inversa son coherentes.
- El zoom no permite superar sus límites.
- El zoom centrado en el cursor mantiene el punto esperado.
- Mover la cámara no cambia la posición de los objetos.
- Una bacteria pierde energía cuando corresponde.
- Una bacteria consume nutrientes sin que la cantidad disponible sea negativa.
- La reproducción respeta el umbral establecido.
- Una bacteria muerta deja de participar en la simulación.
- La mutación respeta las reglas y probabilidades configuradas.
- La versión paralela mantiene invariantes equivalentes a la secuencial cuando se usan las mismas decisiones y entradas.

## 11. Rendimiento y observabilidad

No se establecerá una cantidad máxima de bacterias antes de medir el rendimiento en el equipo de desarrollo.

Registrar progresivamente:

- Número de bacterias.
- Duración media y máxima de cada paso.
- Tiempo dedicado al cálculo y al renderizado.
- Cantidad de trabajadores activos.
- Uso de CPU y memoria.
- Cantidad de consultas espaciales.
- Nacimientos, muertes y mutaciones.

Se optimizará a partir de mediciones, no únicamente por intuición. Para microbenchmarks de algoritmos se podrá incorporar JMH más adelante; JFR o VisualVM ayudarán a investigar el comportamiento de la aplicación completa.

## 12. Flujo de trabajo recomendado

1. Crear el proyecto Maven en IntelliJ IDEA con Java 21.
2. Inicializar Git y realizar el primer commit.
3. Implementar solo la ventana y el panel.
4. Dibujar una cuadrícula y una bacteria estática.
5. Añadir desplazamiento y zoom.
6. Probar los casos límite de la cámara.
7. Registrar los resultados en el README.
8. Avanzar a la siguiente fase solo cuando la anterior sea estable.

Convención sugerida para los commits:

```text
feat: create initial simulation window
feat: draw world grid and test bacterium
feat: add camera zoom
feat: add camera panning
test: verify camera coordinate transforms
```

## 13. Definición de terminado para la primera entrega

La primera entrega estará terminada cuando la aplicación permita abrir una ventana, ver una cuadrícula y una bacteria de prueba, acercar y alejar la vista y desplazarse por el mundo sin alterar las coordenadas de los objetos.

No se añadirán todavía metabolismo, reproducción, mutaciones ni multihilo. El objetivo es construir una base gráfica comprensible, verificable y preparada para crecer.
