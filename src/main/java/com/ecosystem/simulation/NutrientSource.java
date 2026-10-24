package com.ecosystem.simulation;

public class NutrientSource {

    private final double x;
    private final double y;
    private final double radius;
    private final double productionRate;

    private double productionAccumulator;

    public NutrientSource(
            double x,
            double y,
            double radius,
            double productionRate
    ) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.productionRate = productionRate;
        this.productionAccumulator = 0.0;
    }

    public int update(double deltaTime) {
        productionAccumulator += productionRate * deltaTime;

        int nutrientsToProduce = (int) productionAccumulator;

        productionAccumulator -= nutrientsToProduce;

        return nutrientsToProduce;
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
}
