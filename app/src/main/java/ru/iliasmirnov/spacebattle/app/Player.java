package ru.iliasmirnov.spacebattle.app;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private int color;
    private List<Ship> ships = new ArrayList<Ship>();

    public List<Ship> getShips() {
        return ships;
    }

    public int getColor() {
        return color;
    }

    public Player(int color) {
        this.color = color;
    }

}
