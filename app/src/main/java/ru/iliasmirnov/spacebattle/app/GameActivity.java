package ru.iliasmirnov.spacebattle.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;


public class GameActivity extends Activity {
    final static String TAG = "SpaceBattle_App";
    static Random rand = new Random();
    DrawView drawView;
    int width = 1200;
    int height = 1920;

    public static void log(String s) {
        Log.i(TAG, s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawView = new DrawView(this);
        setContentView(drawView);
        generateLevel(20);
    }

    private void generateLevel(int qtyOfPlanets) {
        generatePlanets(qtyOfPlanets);
        generateArmy();
    }

    private void generatePlanets(int qty) {
        boolean retry;
        Planet planet;
        Player player = new Player(Color.WHITE);
        for (int i = 0; i < qty; i++) {
            do {
                retry = false;
                float x = rand.nextInt(width);
                float y = rand.nextInt(height);
                float r = rand.nextInt(width/50) + width/50;
                int health = rand.nextInt(50) + 10;
                int vel = rand.nextInt(80 - health) + 10;
                int damage = 100 - health - vel;
                planet = new Planet(x, y, r, player, health, vel, damage);
                planet.getAirDefenses().add(planet.new AirDefense(rand.nextInt(70) + 30));
                for (Planet p : drawView.getPlanets())
                    if (length(planet, p) < planet.getRadius() + p.getRadius() + width / 10 ||
                            x + r > width || x - r < 0 || y + r > height || y - r < 0) {
                        retry = true;
                        break;
                    }
            } while(retry);
            planet.getFactories().add(planet.new Factory());
            drawView.getPlanets().add(planet);
        }
        drawView.getPlayers().add(player);
    }

    private void generateArmy() {

    }

    private float length(Planet p1, Planet p2) {
        return (float)Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
