package ru.iliasmirnov.spacebattle.app;

public class Goal {
    private Planet planet;
    private long startT;

    public Goal(Planet planet, long startT) {
        this.planet = planet;
        this.startT = startT;
    }

    public Planet getPlanet() {
        return planet;
    }

    public long getStartT() {
        return startT;
    }


}
