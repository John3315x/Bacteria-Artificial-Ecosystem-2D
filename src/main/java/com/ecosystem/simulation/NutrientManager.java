package com.ecosystem.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NutrientManager {

    private final List<Nutrient> nutrients = new ArrayList<>();
    private final List<NutrientSource> sources = new ArrayList<>();

    // Número inicial de nutrientes en el mundo.
    private static final int INITIAL_NUTRIENTS = 500;

    // Radio de la zona de producción de nutrientes.
    private static final double ZONE_RADIUS = 370.0;

    // Rango de energía de los nutrientes generados.
    private static final double minEnergy = 10.0;
    private static final double maxEnergy = 21.0;

    public NutrientManager(double worldWidth, double worldHeight) {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < INITIAL_NUTRIENTS; i++) {

            double radius = 4.0;
            double x = random.nextDouble(radius, worldWidth - radius);
            double y = random.nextDouble(radius, worldHeight - radius);
            double energy = random.nextDouble(minEnergy, maxEnergy); // 🪫
            nutrients.add(new Nutrient(x, y, radius, energy));
        }

        // Crear fuentes de nutrientes en posiciones aleatorias dentro del mundo.
        for (int i = 0; i < 3; i++) {
            double x = random.nextDouble(ZONE_RADIUS, worldWidth - ZONE_RADIUS);
            double y = random.nextDouble(ZONE_RADIUS, worldHeight - ZONE_RADIUS);

            sources.add(new NutrientSource(
                    x,
                    y,
                    ZONE_RADIUS,
                    1.0
            ));
        }

    }


    public void update(
            double deltaTime,
            double worldWidth,
            double worldHeight
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (NutrientSource source : sources) {
            int amount = source.update(deltaTime);

            for (int i = 0; i < amount; i++) {
                double angle = random.nextDouble(0, Math.PI * 2);

                // Distribución aleatoria dentro del área circular
                double distance = source.getRadius()
                        * Math.sqrt(random.nextDouble());

                double x = source.getX()
                        + Math.cos(angle) * distance;

                double y = source.getY()
                        + Math.sin(angle) * distance;

                // Evitar que el nutriente salga del mundo
                double nutrientRadius = 4.0;

                x = Math.max(
                        nutrientRadius,
                        Math.min(x, worldWidth - nutrientRadius)
                );

                y = Math.max(
                        nutrientRadius,
                        Math.min(y, worldHeight - nutrientRadius)
                );

                double energy = random.nextDouble(minEnergy, maxEnergy); // 🪫

                nutrients.add(new Nutrient(x, y, nutrientRadius, energy));
            }
        }
    }


    public double consumeAt(double x, double y, double bacteriumRadius) {
        Iterator<Nutrient> iterator = nutrients.iterator();

        while (iterator.hasNext()) {
            Nutrient nutrient = iterator.next();

            double dx = x - nutrient.getX();
            double dy = y - nutrient.getY();

            double contactDistance = bacteriumRadius + nutrient.getRadius();

            if (dx * dx + dy * dy <= contactDistance * contactDistance) {
                iterator.remove();
                return nutrient.getEnergy();
            }
        }

        return 0.0;
    }

    public void addNutrient(double x, double y, double energy) {
        double nutrientRadius = 4.0;

        nutrients.add(new Nutrient(x, y, nutrientRadius, energy));
    }

    public List<Nutrient> getNutrients() {
        return Collections.unmodifiableList(nutrients);
    }

    public int getPopulation() {
        return nutrients.size();
    }
}
