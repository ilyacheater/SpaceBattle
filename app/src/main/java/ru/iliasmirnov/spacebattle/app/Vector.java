package ru.iliasmirnov.spacebattle.app;

public class Vector {

    public static float len(float x1, float y1, float x2, float y2) {
        return (float)Math.hypot(x1 - x2, y1 - y2);
    }

    public static float len(float x, float y) {
        return (float)Math.hypot(x, y);
    }

    public static float len(Ship s1, Ship s2) {
        return len(s1.getX(), s1.getY(), s2.getX(), s2.getY());
    }

    public static float len(Ship s, Planet p) {
        return len(s.getX(), s.getY(), p.getX(), p.getY());
    }
}
