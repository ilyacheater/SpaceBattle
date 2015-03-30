package ru.iliasmirnov.spacebattle.app;

public abstract class Ship{
    protected static float[][] circle = {{0, 0, 4}}; // [circle1, circle2][x, y, r]
    protected static float goalAccel = 100;
    protected Player player;
    protected float x, y;
    protected float sin = 1, cos = 0;
    private float startX, startY;
    private float health, velocity, damage;
    private float kElastic = 10;
    private float velX, velY, accelX, accelY;

    public float getGoalAccel() {
        return goalAccel;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getSin() {
        return sin;
    }

    public void setSin(float sin) {
        this.sin = sin;
    }

    public float getCos() {
        return cos;
    }

    public void setCos(float cos) {
        this.cos = cos;
    }

    public float getVelocity() {
        return (float) Math.hypot(velX, velY);
    }

    public float getAcceleration() {
        return (float) Math.hypot(accelX, accelY);
    }


    public float getkElastic() {
        return kElastic;
    }

    public float getCircleX(int i) {
        return circle[i][0] + x;
    }

    public float getCircleY(int i) {
        return circle[i][1] + y;
    }

    public float getCircleR(int i) {
        return circle[i][2];
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float[][] getCircle() {
        return circle;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getAccelX() {
        return accelX;
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }


}
