package com.ecosystem.simulation;

public class Nutrient {

    private final double x;
    private final double y;
    private final double radius;
    private final double energy;

    public Nutrient(
            double x,
            double y,
            double radius,
            double energy
    ) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.energy = energy;
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
}
