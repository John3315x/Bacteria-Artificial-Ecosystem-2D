package com.ecosystem.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.Iterator;
import java.util.List;

public class BacteriaManager {

    private static final int INITIAL_POPULATION = 1;

    private final List<Bacterium> bacteria = new ArrayList<>();

    // Vista de solo lectura de la población.
    private final List<Bacterium> bacteriaView = Collections.unmodifiableList(bacteria);

    public BacteriaManager(
            double worldWidth,
            double worldHeight
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < INITIAL_POPULATION; i++) {

            double radius = random.nextDouble(8, 16);

            double x = random.nextDouble(
                    radius,
                    worldWidth - radius
            );

            double y = random.nextDouble(
                    radius,
                    worldHeight - radius
            );

            bacteria.add(new Bacterium(x, y, radius));
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

        // 4. Eliminar bacterias muertas.
        Iterator<Bacterium> iterator = bacteria.iterator();

        while (iterator.hasNext()) {
            if (!iterator.next().isAlive()) {
                iterator.remove();
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

            if (!mother.reproduce()) {
                continue;
            }

            double radius = random.nextDouble(8, 16);
            //double radius = mother.getRadius() - z;

            // Dirección aleatoria para colocar a la hija.
            double angle = random.nextDouble(0, Math.PI * 2);

            double distance = mother.getRadius() + radius + 2;

            double x = mother.getX()
                    + Math.cos(angle) * distance;

            double y = mother.getY()
                    + Math.sin(angle) * distance;

            // Mantener a la hija dentro del mundo.
            x = Math.max(radius, Math.min(x, worldWidth - radius));
            y = Math.max(radius, Math.min(y, worldHeight - radius));

            newborns.add(new Bacterium(x, y, radius, 40.0));
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
