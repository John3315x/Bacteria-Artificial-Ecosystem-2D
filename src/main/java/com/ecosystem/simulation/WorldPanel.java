package com.ecosystem.simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import javax.swing.Timer;

public class WorldPanel extends JPanel {

    private static final int WORLD_WIDTH = 5000;
    private static final int WORLD_HEIGHT = 3500;
    private static final int GRID_SPACING = 100;

    private final Camera camera = new Camera();

    private final BacteriaManager bacteriaManager = new BacteriaManager(WORLD_WIDTH, WORLD_HEIGHT);
    private final NutrientManager nutrientManager = new NutrientManager(WORLD_WIDTH, WORLD_HEIGHT);

    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging;

    private Bacterium selectedBacterium = null;
    private int pressX;
    private int pressY;
    private boolean draggedDuringPress = false;

    private long lastUpdateTime;
    private final Timer simulationTimer;

    public WorldPanel() {
        setBackground(new Color(35, 39, 45));
        setFocusable(true);

        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragging = true;

                    pressX = e.getX();
                    pressY = e.getY();

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    draggedDuringPress = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragging = false;

                    // Solo seleccionar si fue un clic, no un arrastre.
                    if (!draggedDuringPress) {
                        selectBacteriumAt(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int deltaX = e.getX() - lastMouseX;
                    int deltaY = e.getY() - lastMouseY;

                    // Considerar arrastre si se movió más de 4 píxeles.
                    int totalX = e.getX() - pressX;
                    int totalY = e.getY() - pressY;

                    if (totalX * totalX + totalY * totalY > 16) {
                        draggedDuringPress = true;
                    }

                    camera.pan(deltaX, deltaY);

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                camera.zoomAt(
                        e.getX(),
                        e.getY(),
                        e.getPreciseWheelRotation()
                );

                repaint();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);


        lastUpdateTime = System.nanoTime();

        simulationTimer = new Timer(16, e -> {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;

            lastUpdateTime = currentTime;

            // Evitar saltos enormes si la interfaz se detiene.
            deltaTime = Math.min(deltaTime, 0.05);

            bacteriaManager.update(
                    deltaTime,
                    WORLD_WIDTH,
                    WORLD_HEIGHT,
                    nutrientManager
            );

            nutrientManager.update(
                    deltaTime,
                    WORLD_WIDTH,
                    WORLD_HEIGHT
            );

            repaint();
        });

        simulationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Dibujar el mundo usando la transformación de la cámara.
            camera.applyTransform(g2);

            // Dibujar el fondo del mundo.
            //g2.setColor(new Color(238, 242, 232));
            g2.setColor(new Color(212, 251, 255));
            g2.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            // Dibujar la cuadrícula del mundo.
            drawGrid(g2);

            // Dibujar las bacterias.
            for (Bacterium bacterium : bacteriaManager.getBacteria()) {
                drawBacterium(g2, bacterium);

                if (bacterium == selectedBacterium) {
                    double radius = bacterium.getRadius();
                    double outline = radius + 3;

                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(
                            (float) (2.0 / camera.getZoom())
                    ));

                    g2.drawOval(
                            (int) (bacterium.getX() - outline),
                            (int) (bacterium.getY() - outline),
                            (int) (outline * 2),
                            (int) (outline * 2)
                    );
                }
            }

            // Dibujar los nutrientes.
            g2.setColor(new Color(100, 220, 120));
            for (Nutrient nutrient : nutrientManager.getNutrients()) {

                double radius = nutrient.getRadius();

                double x = nutrient.getX() - radius;
                double y = nutrient.getY() - radius;
                double diameter = radius * 2;

                g2.fillOval(
                        (int) x,
                        (int) y,
                        (int) diameter,
                        (int) diameter
                );
            }

            // Borde del mundo.
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(
                    new java.awt.BasicStroke(
                            (float) (2.0 / camera.getZoom())
                    )
            );

            g2.drawRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        } finally {
            g2.dispose();

            drawSelectedBacteriumInfo(g);
        }
    }

    private void drawGrid(Graphics2D g2) {
        Point2D.Double topLeft =
                camera.screenToWorld(0, 0);

        Point2D.Double bottomRight =
                camera.screenToWorld(getWidth(), getHeight());

        int startX = Math.max(
                0,
                (int) Math.floor(topLeft.x / GRID_SPACING)
                        * GRID_SPACING
        );

        int startY = Math.max(
                0,
                (int) Math.floor(topLeft.y / GRID_SPACING)
                        * GRID_SPACING
        );

        int endX = Math.min(
                WORLD_WIDTH,
                (int) Math.ceil(bottomRight.x / GRID_SPACING)
                        * GRID_SPACING
        );

        int endY = Math.min(
                WORLD_HEIGHT,
                (int) Math.ceil(bottomRight.y / GRID_SPACING)
                        * GRID_SPACING
        );

        g2.setColor(new Color(205, 213, 199));
        g2.setStroke(
                new java.awt.BasicStroke(
                        (float) (1.0 / camera.getZoom())
                )
        );

        for (int x = startX; x <= endX; x += GRID_SPACING) {
            g2.drawLine(x, 0, x, WORLD_HEIGHT);
        }

        for (int y = startY; y <= endY; y += GRID_SPACING) {
            g2.drawLine(0, y, WORLD_WIDTH, y);
        }
    }

    private void drawBacterium(Graphics2D g2, Bacterium bacterium) {
        double x = bacterium.getX();
        double y = bacterium.getY();
        double radius = bacterium.getRadius();


        if (bacterium.isAlive()) {
            //g2.setColor(new Color(68, 166, 91));
            g2.setColor(new Color(210, 237, 38));
        } else {
            g2.setColor(Color.GRAY);
        }


        g2.fillOval(
                (int) (x - radius),
                (int) (y - radius),
                (int) (radius * 2),
                (int) (radius * 2)
        );

        g2.setColor(new Color(31, 95, 49));

        g2.drawOval(
                (int) (x - radius),
                (int) (y - radius),
                (int) (radius * 2),
                (int) (radius * 2)
        );
    }

    private void selectBacteriumAt(int screenX, int screenY) {
        Point2D.Double point = camera.screenToWorld(
                screenX,
                screenY
        );

        selectedBacterium = null;

        var bacteria = bacteriaManager.getBacteria();

        for (int i = bacteria.size() - 1; i >= 0; i--) {
            Bacterium bacterium = bacteria.get(i);

            double dx = point.x - bacterium.getX();
            double dy = point.y - bacterium.getY();
            double radius = bacterium.getRadius();

            if (dx * dx + dy * dy <= radius * radius) {
                selectedBacterium = bacterium;
                break;
            }
        }

        repaint();
    }

    private void drawSelectedBacteriumInfo(Graphics g) {
        if (selectedBacterium == null) {
            return;
        }

        Graphics2D infoG = (Graphics2D) g.create();

        try {
            int panelX = 20;
            int panelY = 20;
            int panelWidth = 240;
            int panelHeight = 195;

            // Fondo del panel.
            infoG.setColor(new Color(20, 24, 30, 230));
            infoG.fillRoundRect(
                    panelX, panelY,
                    panelWidth, panelHeight,
                    15, 15
            );

            // Borde.
            infoG.setColor(Color.YELLOW);
            infoG.setStroke(new BasicStroke(2));
            infoG.drawRoundRect(
                    panelX, panelY,
                    panelWidth, panelHeight,
                    15, 15
            );

            infoG.setFont(new Font("SansSerif", Font.BOLD, 15));
            infoG.drawString(
                    "Informacion de bacteria",
                    panelX + 12, panelY + 25
            );

            infoG.setFont(new Font("SansSerif", Font.PLAIN, 13));
            infoG.setColor(Color.WHITE);

            infoG.drawString(
                    String.format(
                            "Energia: %.1f / 100",
                            selectedBacterium.getEnergy()
                    ),
                    panelX + 12, panelY + 50
            );

            infoG.drawString(
                    String.format(
                            "Metabolismo: %.2f",
                            selectedBacterium.getBaseMetabolism()
                    ),
                    panelX + 12, panelY + 72
            );

            infoG.drawString(
                    String.format(
                            "Velocidad: %.1f",
                            selectedBacterium.getSpeed()
                    ),
                    panelX + 12, panelY + 94
            );

            infoG.drawString(
                    String.format(
                            "Posicion X: %.1f",
                            selectedBacterium.getX()
                    ),
                    panelX + 12, panelY + 116
            );

            infoG.drawString(
                    String.format(
                            "Posicion Y: %.1f",
                            selectedBacterium.getY()
                    ),
                    panelX + 12, panelY + 138
            );

            infoG.drawString(
                    String.format(
                            "Radio: %.1f",
                            selectedBacterium.getRadius()
                    ),
                    panelX + 12, panelY + 160
            );

            infoG.setColor(
                    selectedBacterium.isAlive()
                            ? new Color(100, 230, 130)
                            : Color.RED
            );

            infoG.drawString(
                    selectedBacterium.isAlive() ? "Estado: Viva" : "Estado: Muerta",
                    panelX + 12, panelY + 182
            );

        } finally {
            infoG.dispose();
        }
    }
}
