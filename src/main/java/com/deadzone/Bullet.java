package com.deadzone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Bullet = একটি গুলি।
 * জন্মের সময় একটি দিক পায়, তারপর সেই দিকে সোজা উড়ে যায়।
 */
public class Bullet {

    // গুলির বর্তমান অবস্থান
    private double x;
    private double y;

    // গুলির বেগ: প্রতি সেকেন্ডে x ও y দিকে কত পিক্সেল যাবে
    private final double velocityX;
    private final double velocityY;

    // গুলির গতি (প্রতি সেকেন্ডে পিক্সেল) ও আকার
    private static final double SPEED = 700;
    private static final double RADIUS = 4;

    /**
     * startX, startY: গুলি কোথা থেকে বের হবে
     * dirX, dirY: দিক (দৈর্ঘ্য ১ হতে হবে)
     */
    public Bullet(double startX, double startY, double dirX, double dirY) {
        this.x = startX;
        this.y = startY;
        // বেগ = দিক × গতি
        this.velocityX = dirX * SPEED;
        this.velocityY = dirY * SPEED;
    }

    // প্রতি ফ্রেমে গুলিকে এগিয়ে নেওয়া
    public void update(double deltaSeconds) {
        x = x + velocityX * deltaSeconds;
        y = y + velocityY * deltaSeconds;
    }

    // গুলি কি স্ক্রিনের বাইরে চলে গেছে?
    public boolean isOffScreen(double width, double height) {
        return x < -RADIUS || x > width + RADIUS
                || y < -RADIUS || y > height + RADIUS;
    }

    public void draw(GraphicsContext gc) {
        // বাইরের হালকা আভা
        gc.setFill(Color.rgb(255, 200, 0, 0.35));
        gc.fillOval(x - RADIUS * 2, y - RADIUS * 2, RADIUS * 4, RADIUS * 4);

        // গুলির মূল অংশ (হলুদ)
        gc.setFill(Color.YELLOW);
        gc.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
    }
}
