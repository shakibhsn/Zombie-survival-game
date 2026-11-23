package com.deadzone;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main = গেমের প্রবেশদ্বার। উইন্ডো, কিবোর্ড ইনপুট ও গেম লুপ এখানে।
 */
public class Main extends Application {

    // উইন্ডোর মাপ
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;

    // এই মুহূর্তে যে কীগুলো চেপে ধরা আছে
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private Arena arena;
    private Player player;

    // মাউসের বর্তমান অবস্থান (শুরুতে ডানদিকে, যাতে প্লেয়ার ডানে তাকায়)
    private double mouseX = WIDTH;
    private double mouseY = HEIGHT / 2.0;

    // বাম বোতাম চেপে ধরা আছে কিনা
    private boolean mouseHeld = false;

    // সব সক্রিয় গুলির তালিকা
    private final List<Bullet> bullets = new ArrayList<>();

    // গুলির মাঝের বিরতি (সেকেন্ড): 0.2 মানে প্রতি সেকেন্ডে ৫টি গুলি
    private static final double SHOOT_COOLDOWN = 0.2;

    // পরের গুলি ছুড়তে আর কত সেকেন্ড বাকি (০ বা কম হলে গুলি ছোড়া যাবে)
    private double cooldownTimer = 0;

    // JavaFX অ্যাপ শুরু হলে start() নিজে থেকে চলে
    @Override
    public void start(Stage stage) {

        // ক্যানভাস = আমাদের আঁকার সাদা কাগজ
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // গেমের জিনিসপত্র তৈরি; প্লেয়ার শুরুতে ঠিক মাঝখানে
        arena = new Arena(WIDTH, HEIGHT);
        player = new Player(WIDTH / 2.0, HEIGHT / 2.0);

        // Scene = উইন্ডোর ভেতরের দৃশ্য
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // কী চাপলে সেটটিতে যোগ, ছাড়লে বাদ
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

        // মাউস নড়লে (বোতাম না চেপে) অবস্থান মনে রাখি
        scene.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });

        // বোতাম চেপে মাউস টানলেও অবস্থান আপডেট হবে
        scene.setOnMouseDragged(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });

        // বাম বোতাম চাপলে গুলি চালু
        scene.setOnMousePressed(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
            if (event.getButton() == MouseButton.PRIMARY) {
                mouseHeld = true;
            }
        });

        // বাম বোতাম ছাড়লে গুলি বন্ধ
        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                mouseHeld = false;
            }
        });

        // উইন্ডো সেটআপ
        stage.setTitle("Dead Zone - Zombie Survival");
        stage.setScene(scene);
        stage.setResizable(false); // মাপ বদলানো বন্ধ
        stage.show();

        // গেম লুপ শুরু
        startGameLoop(gc);
    }

    private void startGameLoop(GraphicsContext gc) {
        AnimationTimer timer = new AnimationTimer() {

            // আগের ফ্রেমের সময় (ন্যানোসেকেন্ড)। 0 মানে এখনো প্রথম ফ্রেম আসেনি।
            private long lastTime = 0;

            // handle() প্রতি সেকেন্ডে প্রায় ৬০ বার নিজে থেকে চলে
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // ন্যানোসেকেন্ডকে সেকেন্ডে রূপান্তর (১ সেকেন্ড = ১,০০০,০০০,০০০ ন্যানো)
                double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                update(deltaSeconds);
                render(gc);
            }
        };
        timer.start();
    }

    // গেমের হিসাব-নিকাশ
    private void update(double deltaSeconds) {
        double dirX = 0;
        double dirY = 0;

        if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) {
            dirX = dirX - 1;
        }
        if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) {
            dirX = dirX + 1;
        }
        if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) {
            dirY = dirY - 1; // স্ক্রিনে উপরে গেলে y কমে
        }
        if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) {
            dirY = dirY + 1;
        }

        player.update(dirX, dirY, deltaSeconds, WIDTH, HEIGHT);

        // প্লেয়ার চলার পরের অবস্থান থেকে মাউসের দিকে ঘোরে
        player.aimAt(mouseX, mouseY);

        // cooldown টাইমার কমাই (সময় পার হচ্ছে)
        cooldownTimer = cooldownTimer - deltaSeconds;

        // বোতাম চেপে ধরা আছে এবং cooldown শেষ হলে গুলি ছুড়ি
        if (mouseHeld && cooldownTimer <= 0) {
            shoot();
            cooldownTimer = SHOOT_COOLDOWN;
        }

        // সব গুলিকে এগিয়ে নিই
        for (Bullet bullet : bullets) {
            bullet.update(deltaSeconds);
        }

        // স্ক্রিনের বাইরে যাওয়া গুলি মুছে ফেলি (না মুছলে তালিকা বাড়তেই থাকবে)
        bullets.removeIf(bullet -> bullet.isOffScreen(WIDTH, HEIGHT));
    }

    // একটি নতুন গুলি তৈরি করে তালিকায় রাখা
    private void shoot() {
        Bullet bullet = new Bullet(
                player.getMuzzleX(), player.getMuzzleY(),
                player.getFaceX(), player.getFaceY());
        bullets.add(bullet);
    }

    // পর্দায় আঁকা: ব্যাকগ্রাউন্ড, গুলি, প্লেয়ার, ক্রসহেয়ার (পরেরটা উপরে পড়ে)
    private void render(GraphicsContext gc) {
        arena.draw(gc);

        for (Bullet bullet : bullets) {
            bullet.draw(gc);
        }

        player.draw(gc);
        drawCrosshair(gc);
    }

    // মাউসের জায়গায় ছোট লক্ষ্যচিহ্ন (+)
    private void drawCrosshair(GraphicsContext gc) {
        gc.setStroke(Color.rgb(255, 255, 255, 0.8));
        gc.setLineWidth(1.5);
        gc.strokeOval(mouseX - 8, mouseY - 8, 16, 16);
        gc.strokeLine(mouseX - 12, mouseY, mouseX + 12, mouseY);
        gc.strokeLine(mouseX, mouseY - 12, mouseX, mouseY + 12);
    }

    // জাভা প্রোগ্রাম এখান থেকে শুরু হয়
    public static void main(String[] args) {
        launch(args);
    }
}
