package com.deadzone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Arena = গেমের ময়দান (গাঢ় ব্যাকগ্রাউন্ড + গ্রিড)।
 */
public class Arena {

    private final double width;
    private final double height;

    // প্রতিটি গ্রিড ঘরের মাপ (পিক্সেল)
    private final int cellSize = 50;

    public Arena(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void draw(GraphicsContext gc) {
        // ১) পুরো স্ক্রিন গাঢ় রঙে ভরি
        gc.setFill(Color.rgb(20, 22, 26));
        gc.fillRect(0, 0, width, height);

        // ২) গ্রিডের লাইন (হালকা ধূসর, প্রায় স্বচ্ছ)
        gc.setStroke(Color.rgb(255, 255, 255, 0.07));
        gc.setLineWidth(1);

        // খাড়া লাইন: x = 0, 50, 100, ... 900
        for (double lineX = 0; lineX <= width; lineX += cellSize) {
            gc.strokeLine(lineX, 0, lineX, height);
        }

        // আড়াআড়ি লাইন: y = 0, 50, 100, ... 600
        for (double lineY = 0; lineY <= height; lineY += cellSize) {
            gc.strokeLine(0, lineY, width, lineY);
        }

        // ৩) আরেনার বর্ডার
        gc.setStroke(Color.rgb(180, 40, 40));
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, width - 4, height - 4);
    }
}
