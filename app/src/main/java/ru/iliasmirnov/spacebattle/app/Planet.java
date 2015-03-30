package ru.iliasmirnov.spacebattle.app;

import android.graphics.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Planet {
    private static Random rand = new Random();
    private static float startAngle;
    private float healthOfShip, velOfShip, damageOfShip;
    private float radius;
    private float health;
    private float x, y;
    private Player player;
    private List<Factory> factories = new ArrayList<>();
    private List<AirDefense> airDefenses = new ArrayList<>();
    private List<Ship> ships = new ArrayList<>();

    public Planet(float x, float y, float radius, Player player) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.player = player;
        startAngle = -90;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public Player getPlayer() {
        return player;
    }

    public static float getStartAngle() {

        return startAngle;
    }

    public Planet(float x, float y, float radius, Player player, float healthOfShip, float velOfShip, float damageOfShip) {
        this(x, y, radius, player);
        this.healthOfShip = healthOfShip;
        this.velOfShip = velOfShip;
        this.damageOfShip = damageOfShip;
    }

    public float getHealthOfShip() {
        return healthOfShip;
    }

    public float getVelOfShip() {
        return velOfShip;
    }

    public float getDamageOfShip() {
        return damageOfShip;
    }

    public float getRadius() {
        return radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public List<Factory> getFactories() {
        return factories;
    }

    public List<AirDefense> getAirDefenses() {
        return airDefenses;
    }

    private void drawPlanet(Canvas canvas) {
        RectF rectf = new RectF(x - radius, y - radius, x + radius, y + radius);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawArc(rectf, startAngle, 3.6f * velOfShip, true, paint);
        paint.setColor(Color.GREEN);
        canvas.drawArc(rectf, startAngle + 3.6f * velOfShip, 3.6f * healthOfShip, true, paint);
        paint.setColor(Color.RED);
        canvas.drawArc(rectf, startAngle + 3.6f * (velOfShip + healthOfShip), 3.6f * damageOfShip, true, paint);

    }

    class AirDefense{
        private float health;

        public float getHealth() {
            return health;
        }

        public AirDefense(float health) {
            this.health = health;
        }


    }

class Factory {
    private int duration = 3000;
    public long lasTime;

    public int getDuration() {
        return duration;
    }

    public void addShip() {
        float x = -1 + rand.nextFloat()*2;
        float y = -1 + rand.nextFloat()*2;
        float len = Vector.len(x, y);
        x = x / len * (radius + 5) + Planet.this.x;
        y = y / len * (radius + 5) + Planet.this.y;
        player.getShips().add(new StandardShip(x, y, player));
    }

}
}
