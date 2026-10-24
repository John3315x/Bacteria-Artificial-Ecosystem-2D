package com.ecosystem.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NutrientManager {

    private final List<Nutrient> nutrients = new ArrayList<>();
    private final List<NutrientSource> sources = new ArrayList<>();

    private static final int INITIAL_NUTRIENTS = 1000;

    public NutrientManager(
            double worldWidth,
            double worldHeight
    ) {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < INITIAL_NUTRIENTS; i++) {

            double radius = 4.0;
            double x = random.nextDouble(radius, worldWidth - radius);
            double y = random.nextDouble(radius, worldHeight - radius);
            double energy = random.nextDouble(10.0, 21.0);
            nutrients.add(new Nutrient(x, y, radius, energy));
        }

        sources.add(new NutrientSource(
                worldWidth * 0.25,
                worldHeight * 0.30,
                70.0,
                1.0
        ));

        sources.add(new NutrientSource(
                worldWidth * 0.50,
                worldHeight * 0.70,
                70.0,
                1.0
        ));

        sources.add(new NutrientSource(
                worldWidth * 0.75,
                worldHeight * 0.35,
                70.0,
                1.0
        ));
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

                double energy = random.nextDouble(20.0, 40.0);

                nutrients.add(
                        new Nutrient(x, y, nutrientRadius, energy)
                );
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

    public boolean removeNutrient(Nutrient nutrient) {
        return nutrients.remove(nutrient);
    }

    public List<Nutrient> getNutrients() {
        return Collections.unmodifiableList(nutrients);
    }

    public int getPopulation() {
        return nutrients.size();
    }
}
