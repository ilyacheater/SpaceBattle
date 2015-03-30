package ru.iliasmirnov.spacebattle.app;

public class Goal {
    private Planet planet;

    public Goal(Planet planet) {
        this.planet = planet;
    }

    public Planet getPlanet() {
        return planet;
    }
}
