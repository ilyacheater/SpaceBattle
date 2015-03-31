package ru.iliasmirnov.spacebattle.app;

public class StandardShip extends Ship {

    public StandardShip(float x, float y, Player p, Planet pl) {
        this.x = x;
        this.y = y;
        this.player = p;
        this.planet = pl;
    }
}
