package com.ecosystem.simulation;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                    "Bacteria Simulator"
            );

            WorldPanel worldPanel = new WorldPanel();

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setContentPane(worldPanel);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setVisible(true);
        });
    }
}