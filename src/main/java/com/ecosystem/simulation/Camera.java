package com.ecosystem.simulation;

import java.awt.*;
import java.awt.geom.Point2D;

public class Camera {

    private double x = 0;
    private double y = 0;
    private double zoom = 1.0;

    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 4.0;

    public double getZoom() {
        return zoom;
    }

    // Convierte coordenadas del mundo a coordenadas de pantalla.
    public Point2D.Double worldToScreen(double worldX, double worldY) {
        double screenX = (worldX - x) * zoom;
        double screenY = (worldY - y) * zoom;

        return new Point2D.Double(screenX, screenY);
    }

    // Convierte coordenadas de pantalla a coordenadas del mundo.
    public Point2D.Double screenToWorld(double screenX, double screenY) {
        double worldX = screenX / zoom + x;
        double worldY = screenY / zoom + y;

        return new Point2D.Double(worldX, worldY);
    }

    // Aplica la transformación de la cámara al dibujo.
    public void applyTransform(Graphics2D g2) {
        g2.scale(zoom, zoom);
        g2.translate(-x, -y);
    }

    // Desplaza la cámara usando el movimiento del ratón en pantalla.
    public void pan(double deltaX, double deltaY) {
        x -= deltaX / zoom;
        y -= deltaY / zoom;
    }

    // Acerca o aleja la cámara manteniendo fijo el punto bajo el cursor.
    public void zoomAt(double screenX, double screenY, double wheelRotation) {

        Point2D.Double worldBefore = screenToWorld(screenX, screenY);

        double factor = Math.pow(1.1, -wheelRotation);
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));

        x = worldBefore.x - screenX / zoom;
        y = worldBefore.y - screenY / zoom;
    }
}
