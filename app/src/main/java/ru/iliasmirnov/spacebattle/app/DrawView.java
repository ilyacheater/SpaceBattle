package ru.iliasmirnov.spacebattle.app;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {
    private Map<Goal, List> goals = new HashMap<>();
    private Planet planetInFocus;
    private DrawThread drawThread;
    private List<Player> players = new ArrayList<>();
    private List<Planet> planets = new ArrayList<>();
    private ScaleGestureDetector detector;
    private float translateX = 0.f, translateY = 0.f, scale = 1.f, scaleToX = 0.f, scaleToY = 0.f;
    private float prevX, prevY, moveX = 0.f, moveY = 0.f, startX, startY;
    private boolean move = true;
    private float x0 = 0.f, y0 = 0.f, x, y;

    public DrawView(Context context) {
        super(context);
        getHolder().addCallback(this);
        detector = new ScaleGestureDetector(context, new MyScaleListener());

    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int n = 0;
        for (Player p : players)
            n += p.getShips().size();
        GameActivity.log(String.valueOf(n));

        detector.onTouchEvent(event);
        float x = (event.getX() - x0) / scale;
        float y = (event.getY() - y0) / scale;
        this.x = x;
        this.y = y;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = x;
            startY = y;
        }
        if (detector.isInProgress())
            move = false;
        if (move && event.getAction() == MotionEvent.ACTION_MOVE) {
            moveX += x - prevX;
            moveY += y - prevY;
        }
        float maxLenToClick = 50;
        if (event.getAction() == MotionEvent.ACTION_UP && Vector.len(x, y, startX, startY) / scale < maxLenToClick) {
            if (isPlanet(x, y) != null)
                GameActivity.log("Click on the planet");
            else
                GameActivity.log("Click");


            if (planetInFocus != null) {
                Planet toThePlanet = isPlanet(x, y);
                if (toThePlanet != null) {
                    Goal goal = new Goal(toThePlanet, System.currentTimeMillis());
                    ArrayList<Ship> listOfShips = new ArrayList<>();
                    listOfShips.addAll(planetInFocus.getShips());
                    goals.put(goal, listOfShips);
                    toThePlanet.getShips().addAll(planetInFocus.getShips());
                    planetInFocus.getShips().clear();
                }

            } else {
                Planet planet = isPlanet(x, y);
                if (planet == null) {
                    planetInFocus = null;
                } else {
                    planetInFocus = planet;
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP)
            move = true;


        prevX = x;
        prevY = y;
        return true;
    }

    private Planet isPlanet(float x, float y) {
        for (Planet planet : planets)
            if (Vector.len(planet.getX(), planet.getY(), x, y) < planet.getRadius())
                return planet;
        return null;
    }

    class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float realX, realY;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            scale *= detector.getScaleFactor();
            x0 = focusX + detector.getScaleFactor() * (x0 - focusX);
            y0 = focusY + detector.getScaleFactor() * (y0 - focusY);
            realX = (focusX - x0) / scale;
            realY = (focusY - y0) / scale;
            scaleToX = realX;
            scaleToY = realY;
            translateX = focusX - realX;
            translateY = focusY - realY;
            return true;
        }
    }

    class DrawThread extends Thread {

        private boolean running = false;
        private SurfaceHolder surfaceHolder;
        private float t = 0.01f;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
        }

        private void collision(Ship ship, int c, Ship enemyShip, int cEnemy) {
            //GameActivity.log("Collision");
            float lenCol = ship.getCircleR(c) + enemyShip.getCircleR(cEnemy) -
                    Vector.len(ship.getCircleX(c), ship.getCircleY(c),
                            enemyShip.getCircleX(cEnemy), enemyShip.getCircleY(cEnemy));
            float length = Vector.len(ship, enemyShip);
            float k = ship.getkElastic() + enemyShip.getkElastic();
            float accel = k * lenCol;
            ship.setAccelX(ship.getAccelX() + (ship.getX() - enemyShip.getX()) / length * accel);
            ship.setAccelY(ship.getAccelY() + (ship.getY() - enemyShip.getY()) / length * accel);
            enemyShip.setAccelX(enemyShip.getAccelX() + (enemyShip.getX() - ship.getX()) / length * accel);
            enemyShip.setAccelY(enemyShip.getAccelY() + (enemyShip.getY() - ship.getY()) / length * accel);
            // need to test
        }

        private void setNewAcceleration() {
            // reset acceleration
            for (Player player : players) {
                for (Ship ship : player.getShips()) {
                    ship.setAccelX(0);
                    ship.setAccelY(0);

                }
            }
//             Keep calm. It's just O(n^2) collision detection
            for (Player player : players) {
                for (Ship ship : player.getShips()) {
                    for (int i = 0; i < ship.getCircle().length; i++) {
                        for (Player enemyPlayer : players) {
                            for (Ship enemyShip : enemyPlayer.getShips()) {
                                if (ship == enemyShip) continue;
                                for (int j = 0; j < enemyShip.getCircle().length; j++) {
                                    if (Vector.len(ship.getCircleX(i), ship.getCircleY(i), enemyShip.getCircleX(j), enemyShip.getCircleY(j)) <
                                            ship.getCircleR(i) + enemyShip.getCircleR(j))
                                        collision(ship, i, enemyShip, j);
                                }
                            }

                        }
                    }
                }
            }

        }


        private void nextPosition() {
            setNewAcceleration();
            for (Player player : players) {
                for (Ship ship : player.getShips()) {
                    ship.setX(ship.getX() + ship.getVelX() * t + ship.getAccelX() * t * t / 2);
                    ship.setY(ship.getY() + ship.getVelY() * t + ship.getAccelY() * t * t / 2);
                    ship.setVelX(ship.getVelX() + ship.getAccelX() * t);
                    ship.setVelY(ship.getVelY() + ship.getAccelY() * t);
                    float lenVel = Vector.len(ship.getVelX(), ship.getVelY());
                    ship.setCos(-(ship.getVelY()) / lenVel);
                    ship.setSin((ship.getVelX()) / lenVel);

                }

            }

        }

        private void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(translateX, translateY); // translation of scaling
            canvas.scale(scale, scale, scaleToX, scaleToY);
            canvas.translate(moveX, moveY); // translation of finger moving
            drawBackground(canvas);
            drawAllObjects(canvas);
            Paint p = new Paint();
            p.setColor(Color.RED);
            canvas.drawCircle(x, y, 20, p);
            canvas.restore();
            p.setColor(Color.BLUE);
            canvas.drawCircle(x0, y0, 20, p);

        }

        private void drawAllObjects(Canvas canvas) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (Planet p : planets) {
                RectF rectf = new RectF(p.getX() - p.getRadius(), p.getY() - p.getRadius(), p.getX() + p.getRadius(), p.getY() + p.getRadius());

                paint.setColor(Color.DKGRAY);
                canvas.drawCircle(p.getX(), p.getY(), p.getRadius(), paint);
                paint.setColor(Color.YELLOW);
                canvas.drawArc(rectf, Planet.getStartAngle(), 3.6f * p.getVelOfShip(), true, paint);
                paint.setColor(Color.GREEN);
                canvas.drawArc(rectf, Planet.getStartAngle() + 3.6f * p.getVelOfShip(), 3.6f * p.getHealthOfShip(), true, paint);
                paint.setColor(Color.RED);
                canvas.drawArc(rectf, Planet.getStartAngle() + 3.6f * (p.getVelOfShip() + p.getHealthOfShip()), 3.6f * p.getDamageOfShip(), true, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            for (Planet p : planets)
                for (Planet.AirDefense a : p.getAirDefenses()) {
                    paint.setStrokeWidth(15 * a.getHealth() / 100);
                    paint.setColor(Color.LTGRAY);
                    canvas.drawCircle(p.getX(), p.getY(), p.getRadius() * 1.8f, paint);
                }
            for (Player p : players)
                for (Ship s : p.getShips()) {
                    paint.setColor(p.getColor());
                    Path path = new Path();
                    path.reset();
                    path.moveTo(s.getX(), s.getY() - 5);
                    path.lineTo(s.getX() - 5, s.getY() + 5);
                    path.lineTo(s.getX(), s.getY() + 2);
                    path.lineTo(s.getX() + 5, s.getY() + 5);
                    path.lineTo(s.getX(), s.getY() - 5);
                    Matrix m = new Matrix();
                    m.setSinCos(s.getSin(), s.getCos(), s.getX(), s.getY());
                    path.transform(m);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(path, paint);
                }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null)
                        continue;
                    nextPosition();
                    //checkFactories();
                    draw(canvas);
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void checkFactories() {
            for (Planet planet : planets)
                for (Planet.Factory fact : planet.getFactories())
                    if (System.currentTimeMillis() - fact.lasTime >= fact.getDuration()) {
                        fact.lasTime = System.currentTimeMillis();
                        fact.addShip();
                    }
            int n = 0;
            for (Player player : players) {
                n += player.getShips().size();
            }
        }
    }
}



