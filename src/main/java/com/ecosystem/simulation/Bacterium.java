package com.ecosystem.simulation;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Bacterium {

    private double x;
    private double y;

    private double radius;
    private final double speed;

    private double angle;
    private double energy = 100.0;
    private boolean alive = true;

    private static final double TURN_RATE = 1.8;
    private final double baseMetabolism = ThreadLocalRandom.current().nextDouble(0.25, 0.46);
    private static final double MOVEMENT_COST = 0.015;

    private static final double REPRODUCTION_THRESHOLD = 80.0;
    private static final double REPRODUCTION_COST = 40.0;
    private static final double CHILD_INITIAL_ENERGY = 40.0;

    private static final double GROWTH_ENERGY_THRESHOLD = 60.0;
    private static final double MAX_RADIUS = 28.0;
    private static final double GROWTH_RATE = 0.50;
    private static final double GROWTH_ENERGY_COST = 5.0;

    private double decompositionTime = 0.0;
    private static final double DECOMPOSITION_DURATION = 16.0;
    private static final double DECOMPOSITION_RATE = 1.0;

    private double nutrientReleaseAccumulator = 0.0;
    private static final double NUTRIENT_RELEASE_INTERVAL = 1.5;


    public Bacterium(double x, double y, double radius, double initialEnergy) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.energy = initialEnergy;
        this.speed = 25.0;

        this.angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
    }

    // Este contructor usa los parametros del constructor principal, pero con energia inicial de 80.0
    public Bacterium(double x, double y, double radius) {
        this(x, y, radius, 80.0);
    }

    public void update(
            double deltaTime,
            double worldWidth,
            double worldHeight,
            NutrientManager nutrientManager,
            List<Bacterium> bacteria
    ) {
        if (!alive || deltaTime <= 0) {
            return;
        }

        // Consumo de energía por metabolismo y movimiento.
        double energyConsumption = baseMetabolism + MOVEMENT_COST * speed;

        energy -= energyConsumption * deltaTime;

        if (energy <= 0) {
            energy = 0;
            alive = false;
            return;
        }

        growGradually(deltaTime);

        // Movimiento aleatorio persistente.
        Nutrient target = findNearestNutrient(
                nutrientManager.getNutrients()
        );

        if (target != null) {
            // Dirección hacia el nutriente
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double targetAngle = Math.atan2(dy, dx);

            // Dirección para apartarse de otras bacterias
            double separationAngle = calculateSeparationAngle(bacteria);

            if (!Double.isNaN(separationAngle)) {
                double combinedX =
                        Math.cos(targetAngle)
                                + 1.5 * Math.cos(separationAngle);

                double combinedY =
                        Math.sin(targetAngle)
                                + 1.5 * Math.sin(separationAngle);

                targetAngle = Math.atan2(combinedY, combinedX);
            }

            // Girar gradualmente hacia la dirección combinada
            double angleDifference = Math.atan2(
                    Math.sin(targetAngle - angle),
                    Math.cos(targetAngle - angle)
            );

            double maxTurn = TURN_RATE * deltaTime;

            angle += Math.max(
                    -maxTurn,
                    Math.min(maxTurn, angleDifference)
            );

        } else {
            // No hay nutrientes: explorar y evitar bacterias cercanas
            ThreadLocalRandom random = ThreadLocalRandom.current();

            double separationAngle = calculateSeparationAngle(bacteria);

            if (!Double.isNaN(separationAngle)) {
                // Combinar exploración aleatoria con separación
                double explorationAngle =
                        angle + random.nextDouble(-TURN_RATE, TURN_RATE)
                                * deltaTime;

                double combinedX =
                        Math.cos(explorationAngle)
                                + 1.5 * Math.cos(separationAngle);

                double combinedY =
                        Math.sin(explorationAngle)
                                + 1.5 * Math.sin(separationAngle);

                double targetAngle = Math.atan2(combinedY, combinedX);

                double angleDifference = Math.atan2(
                        Math.sin(targetAngle - angle),
                        Math.cos(targetAngle - angle)
                );

                double maxTurn = TURN_RATE * deltaTime;

                angle += Math.max(
                        -maxTurn,
                        Math.min(maxTurn, angleDifference)
                );

            } else {
                // Sin bacterias cerca: exploración normal
                angle += random.nextDouble(-TURN_RATE, TURN_RATE)
                        * deltaTime;
            }
        }

        // Moverse en la dirección actual.
        x += Math.cos(angle) * speed * deltaTime;
        y += Math.sin(angle) * speed * deltaTime;

        // Rebote contra los límites del mundo.
        if (x < radius) {
            x = radius;
            angle = Math.PI - angle;
        } else if (x > worldWidth - radius) {
            x = worldWidth - radius;
            angle = Math.PI - angle;
        }

        if (y < radius) {
            y = radius;
            angle = -angle;
        } else if (y > worldHeight - radius) {
            y = worldHeight - radius;
            angle = -angle;
        }
    }


    // Consume nutrientes y aumenta la energía de la bacteria.
    public void consumeNutrient(double energyAmount) {
        if (!alive || energyAmount <= 0) {
            return;
        }

        energy = Math.min(100.0, energy + energyAmount);
    }

    // Encuentra el nutriente más cercano dentro del radio de percepción.
    private Nutrient findNearestNutrient(List<Nutrient> nutrients) {
        Nutrient nearest = null;

        double perceptionRadius = 120.0;
        double nearestDistanceSquared =
                perceptionRadius * perceptionRadius;

        for (Nutrient nutrient : nutrients) {
            double dx = nutrient.getX() - x;
            double dy = nutrient.getY() - y;

            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = nutrient;
            }
        }

        return nearest;
    }

    // Calcula el ángulo de separación para evitar colisiones con otras bacterias.
    private double calculateSeparationAngle(List<Bacterium> bacteria) {
        double separationX = 0.0;
        double separationY = 0.0;

        double separationDistance = radius * 2.5;

        for (Bacterium other : bacteria) {
            if (other == this || !other.isAlive()) {
                continue;
            }

            double dx = x - other.getX();
            double dy = y - other.getY();

            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared == 0.0) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);

            if (distance < separationDistance) {
                // Cuanto más cerca esté la otra bacteria,
                // mayor será la fuerza de separación.
                separationX += dx / distance;
                separationY += dy / distance;
            }
        }

        if (separationX == 0.0 && separationY == 0.0) {
            return Double.NaN;
        }

        return Math.atan2(separationY, separationX);
    }

    public boolean canReproduce() {
        return alive && energy >= REPRODUCTION_THRESHOLD && radius == MAX_RADIUS;
    }

    public boolean reproduce() {
        if (!canReproduce()) {
            return false;
        }

        energy -= REPRODUCTION_COST;
        return true;
    }

    // Crecimiento gradual basado en la energía disponible y el tiempo transcurrido.
    private void growGradually(double deltaTime) {
        if (energy <= GROWTH_ENERGY_THRESHOLD || radius >= MAX_RADIUS) {
            return;
        }

        // Crecimiento gradual basado en el tiempo y la energía disponible.
        double growthAmount = Math.min(
                GROWTH_RATE * deltaTime,
                MAX_RADIUS - radius
        );

        double energyCost = growthAmount * GROWTH_ENERGY_COST;

        // No crecer si no hay energía suficiente para pagarlo.
        if (energy < energyCost) {
            return;
        }

        radius += growthAmount;
        energy -= energyCost;
    }

    // Divide la biomasa de la bacteria, reduciendo su radio a la mitad.
    public void divideBiomass() {
        radius = radius / Math.sqrt(2.0);
    }

    // Actualiza el tiempo de descomposición y reduce el radio de la bacteria si está muerta.
    public int updateDecomposition(double deltaTime) {
        if (alive || deltaTime <= 0) {
            return 0;
        }

        decompositionTime += deltaTime;

        radius = Math.max(0.0, radius - DECOMPOSITION_RATE * deltaTime);

        nutrientReleaseAccumulator += deltaTime;

        int nutrientsReleased = (int) (nutrientReleaseAccumulator / NUTRIENT_RELEASE_INTERVAL);

        nutrientReleaseAccumulator -= nutrientsReleased * NUTRIENT_RELEASE_INTERVAL;

        return nutrientsReleased;
    }

    public boolean isDecomposed() {
        return !alive && decompositionTime >= DECOMPOSITION_DURATION;
    }






    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public double getEnergy() {
        return energy;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getBaseMetabolism() {
        return baseMetabolism;
    }

    public double getSpeed() {
        return speed;
    }

    public static double getChildInitialEnergy() {
        return CHILD_INITIAL_ENERGY;
    }
}
