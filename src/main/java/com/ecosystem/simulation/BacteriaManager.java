package com.ecosystem.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.Iterator;
import java.util.List;

public class BacteriaManager {

    private static final int INITIAL_POPULATION = 2;

    private final List<Bacterium> bacteria = new ArrayList<>();

    // Vista de solo lectura de la población.
    private final List<Bacterium> bacteriaView = Collections.unmodifiableList(bacteria);

    public BacteriaManager(double worldWidth, double worldHeight) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < INITIAL_POPULATION; i++) {

            double radius = random.nextDouble(8, 16); // 🔨

            double x = random.nextDouble(radius, worldWidth - radius);

            double y = random.nextDouble(radius, worldHeight - radius);

            bacteria.add(new Bacterium(x, y, radius)); // 🚩
        }
    }

    public void update(
            double deltaTime,
            double worldWidth,
            double worldHeight,
            NutrientManager nutrientManager
    ) {
        // 1. Movimiento y metabolismo.
        for (Bacterium bacterium : bacteria) {
            bacterium.update(
                    deltaTime,
                    worldWidth,
                    worldHeight,
                    nutrientManager,
                    bacteriaView
            );
        }

        // 2. Alimentación.
        for (Bacterium bacterium : bacteria) {
            if (!bacterium.isAlive()) {
                continue;
            }

            double energy = nutrientManager.consumeAt(
                    bacterium.getX(),
                    bacterium.getY(),
                    bacterium.getRadius()
            );

            if (energy > 0) {
                bacterium.consumeNutrient(energy);
            }
        }

        // 3. Reproducción.
        reproduceBacteria(worldWidth, worldHeight);

        // 4. Descomposición, liberación de nutrientes y eliminación.
        Iterator<Bacterium> iterator = bacteria.iterator();

        while (iterator.hasNext()) {
            Bacterium bacterium = iterator.next();

            if (!bacterium.isAlive()) {
                int nutrientsReleased = bacterium.updateDecomposition(deltaTime);

                for (int i = 0; i < nutrientsReleased; i++) {
                    double dist = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * bacterium.getRadius();
                    double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                    nutrientManager.addNutrient(
                            bacterium.getX() + Math.cos(angle) * dist,
                            bacterium.getY() + Math.sin(angle) * dist,
                            21.0 // 🪫
                    );
                }

                if (bacterium.isDecomposed()) {
                    iterator.remove();
                }
            }
        }
    }


    private void reproduceBacteria(double worldWidth, double worldHeight) {
        List<Bacterium> newborns = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Bacterium mother : bacteria) {
            if (!mother.canReproduce()) {
                continue;
            }

            // Guardar el tamaño original antes de la división.
            double originalRadius = mother.getRadius();

            if (!mother.reproduce()) {
                continue;
            }

            // Dividir la biomasa entre madre e hija.
            mother.divideBiomass();

            double childRadius = originalRadius / Math.sqrt(2.0);

            // Dirección aleatoria para colocar a la hija.
            double angle = random.nextDouble(0, Math.PI * 2);
            double randomDistance = random.nextDouble(4, 6);

            double distance = mother.getRadius() + childRadius + randomDistance;

            double x = mother.getX() + Math.cos(angle) * distance;
            double y = mother.getY() + Math.sin(angle) * distance;

            // Mantener a la hija dentro del mundo.
            x = Math.max(childRadius,
                    Math.min(x, worldWidth - childRadius));

            y = Math.max(childRadius,
                    Math.min(y, worldHeight - childRadius));

            newborns.add(new Bacterium(x, y, childRadius, 40.0));
        }

        bacteria.addAll(newborns);
    }




    public List<Bacterium> getBacteria() {
        return bacteriaView;
    }

    public int getPopulation() {
        return bacteria.size();
    }
}
