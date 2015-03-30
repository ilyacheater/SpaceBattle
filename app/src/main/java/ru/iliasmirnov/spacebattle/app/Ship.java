package ru.iliasmirnov.spacebattle.app;

public abstract class Ship{
    private float startX, startY, endX, endY;
    private float health, velocity, damage;
    private float kElastic = 10;
    protected Player player;
    protected float x, y, moveToX, moveToY;
    protected boolean hasGoal;
    protected float sin, cos;
    private float velX, velY, accelX, accelY;
    protected static float[][] circle = {{0, 0, 4}}; // [circle1, circle2][x, y, r]
    private float maxVel;
    private Planet planet;

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

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public float getEndY() {
        return endY;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }

    public void setSin(float sin) {
        this.sin = sin;
    }

    public void setCos(float cos) {
        this.cos = cos;
    }

    public float getSin() {
        return sin;
    }

    public float getCos() {
        return cos;
    }


    public float getVelocity() {
        return (float) Math.hypot(velX, velY);
    }

    public float getAcceleration() {
        return (float) Math.hypot(accelX, accelY);
    }

    public float getMoveToX() {
        return moveToX;
    }

    public float getMoveToY() {
        return moveToY;
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

    public void moveTo(float x, float y) {
        moveToX = x;
        moveToY = y;
        hasGoal = true;
    }

    public boolean isHasGoal() {
        return hasGoal;
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

    public float getMaxVel() {
        return maxVel;
    }

    public void setMaxVel(float maxVel) {
        this.maxVel = maxVel;
    }
}
