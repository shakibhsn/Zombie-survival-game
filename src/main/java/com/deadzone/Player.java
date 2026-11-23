package com.deadzone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Player = আমাদের চরিত্র।
 * এই ক্লাস জানে: প্লেয়ার কোথায় আছে, কত দ্রুত চলে, কীভাবে নড়ে, কীভাবে আঁকতে হয়।
 */
public class Player {

    // প্লেয়ারের কেন্দ্রের অবস্থান (x = বাম-ডান, y = উপর-নিচ)
    private double x;
    private double y;

    // প্লেয়ার একটি বৃত্ত, এটি তার ব্যাসার্ধ (পিক্সেলে)
    private final double radius = 16;

    // গতি: প্রতি সেকেন্ডে কত পিক্সেল চলবে
    private final double speed = 250;

    // প্লেয়ার কোন দিকে তাকিয়ে আছে (মাউসের দিক)। শুরুতে ডানে।
    // এটি একটি "দিক-ভেক্টর", যার দৈর্ঘ্য সবসময় ১।
    private double faceX = 1;
    private double faceY = 0;

    // Constructor: প্লেয়ার তৈরি হওয়ার সময় শুরুর জায়গা দেওয়া হয়
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * প্লেয়ারকে সরানো।
     * dirX, dirY: -1, 0 বা 1 (কোন দিকে যেতে চাই)
     * deltaSeconds: আগের ফ্রেম থেকে কত সেকেন্ড পার হয়েছে
     * maxWidth, maxHeight: উইন্ডোর মাপ (সীমানা)
     */
    public void update(double dirX, double dirY, double deltaSeconds,
                       double maxWidth, double maxHeight) {

        // কোনো বোতাম চাপা থাকলে তবেই নড়বে
        if (dirX != 0 || dirY != 0) {

            // কোণাকুণি চললে (যেমন W+D) দিক-ভেক্টরের দৈর্ঘ্য 1.41 হয়ে যায়,
            // তাই দৈর্ঘ্য দিয়ে ভাগ করে গতি সমান রাখি
            double length = Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // নতুন অবস্থান = পুরোনো অবস্থান + দিক × গতি × সময়
            x = x + dirX * speed * deltaSeconds;
            y = y + dirY * speed * deltaSeconds;
        }

        // সীমানা আটকানো: প্লেয়ারের কিনারা যেন উইন্ডোর বাইরে না যায়
        if (x < radius) {
            x = radius;
        }
        if (x > maxWidth - radius) {
            x = maxWidth - radius;
        }
        if (y < radius) {
            y = radius;
        }
        if (y > maxHeight - radius) {
            y = maxHeight - radius;
        }
    }

    /** প্লেয়ারকে মাউসের দিকে ঘোরানো */
    public void aimAt(double mouseX, double mouseY) {
        // প্লেয়ার থেকে মাউস পর্যন্ত দূরত্ব (x ও y দিকে আলাদা)
        double dx = mouseX - x;
        double dy = mouseY - y;

        // সোজা দূরত্ব (পীথাগোরাস)
        double length = Math.sqrt(dx * dx + dy * dy);

        // মাউস প্লেয়ারের ঠিক উপরে থাকলে length ≈ 0, ০ দিয়ে ভাগ হয়ে যাবে।
        // তাই খুব কাছে থাকলে দিক বদলাই না।
        if (length > 0.001) {
            faceX = dx / length;
            faceY = dy / length;
        }
    }

    // বন্দুকের মুখের অবস্থান (গুলি এখান থেকে বের হয়)
    public double getMuzzleX() {
        return x + faceX * (radius + 10);
    }

    public double getMuzzleY() {
        return y + faceY * (radius + 10);
    }

    // তাকানোর দিক (গুলির দিক ঠিক করতে লাগে)
    public double getFaceX() {
        return faceX;
    }

    public double getFaceY() {
        return faceY;
    }

    /** প্লেয়ারকে ক্যানভাসে আঁকা */
    public void draw(GraphicsContext gc) {
        // ছায়া (একটু নিচে-ডানে গাঢ় বৃত্ত)
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillOval(x - radius + 3, y - radius + 4, radius * 2, radius * 2);

        // শরীর (সবুজ বৃত্ত)
        gc.setFill(Color.LIMEGREEN);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // শরীরের বর্ডার
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // বন্দুক: কেন্দ্র থেকে তাকানোর দিকে একটি মোটা লাইন
        gc.setStroke(Color.DIMGRAY);
        gc.setLineWidth(5);
        gc.strokeLine(x, y, x + faceX * (radius + 10), y + faceY * (radius + 10));
    }
}
