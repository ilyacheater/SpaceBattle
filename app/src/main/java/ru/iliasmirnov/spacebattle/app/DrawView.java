package ru.iliasmirnov.spacebattle.app;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.*;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap shipBitmap;
    private Canvas shipCanvas;
    private Path shipPath;
    private Map<Goal, List> goals = new HashMap<>();
    private Planet planetInFocus;
    private DrawThread drawThread;
    private List<Player> players = new ArrayList<>();
    private List<Planet> planets = new ArrayList<>();
    private ScaleGestureDetector detector;
    private float scale = 1.f;
    private float prevX, prevY, startX, startY;
    private boolean move = true;
    private float x0 = 0.f, y0 = 0.f;

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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }


    private Planet isPlanet(float x, float y) {
        for (Planet planet : planets)
            if (Vector.len(planet.getX(), planet.getY(), x, y) < planet.getRadius())
                return planet;
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int n = 0;
        for (Player p : players)
            n += p.getShips().size();
        GameActivity.log(String.valueOf(n));

        detector.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        float realX = (x - x0) / scale;
        float realY = (y - y0) / scale;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = x;
            startY = y;
        }
        if (detector.isInProgress())
            move = false;
        if (move && event.getAction() == MotionEvent.ACTION_MOVE) {
            x0 += (x - prevX);
            y0 += (y - prevY);
        }
        float maxLenToClick = 50;
        if (event.getAction() == MotionEvent.ACTION_UP && Vector.len(x, y, startX, startY) < maxLenToClick) {
            if (planetInFocus != null) {
                Planet toThePlanet = isPlanet(realX, realY);
                if (toThePlanet != null) {
                    Goal goal = new Goal(toThePlanet);
                    List<Ship> shipList = new ArrayList<>();
                    shipList.addAll(planetInFocus.getShips());
                    for (Ship ship : shipList)
                        ship.setPlanet(toThePlanet);
                    goals.put(goal, shipList);
                    toThePlanet.getShips().addAll(shipList);
                    planetInFocus.getShips().clear();
                    planetInFocus = null;
                }

            } else {
                Planet planet = isPlanet(realX, realY);
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

    class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            scale *= detector.getScaleFactor();
            x0 = focusX + detector.getScaleFactor() * (x0 - focusX);
            y0 = focusY + detector.getScaleFactor() * (y0 - focusY);
            return true;
        }
    }

    class DrawThread extends Thread {

        private boolean running = false;
        private SurfaceHolder surfaceHolder;
        private float t = 0.02f;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            preRenderOfShip();
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

            Iterator iterator = goals.entrySet().iterator();
            ArrayList<Goal> goalDelList = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<Goal, ArrayList<Ship>> pair = (Map.Entry) iterator.next();
                Goal goal = pair.getKey();
                Planet pl = goal.getPlanet();
                ArrayList<Ship> shipDelList = new ArrayList<>();
                for (Ship ship : pair.getValue()) {
                    float len = Vector.len(ship, pl);
                    if (len < pl.getRadius() * 1.8f) {
                        shipDelList.add(ship);
                        continue;
                    }
                    ship.setAccelX(ship.getAccelX() + (pl.getX() - ship.getX()) / len * ship.getGoalAccel());
                    ship.setAccelY(ship.getAccelY() + (pl.getY() - ship.getY()) / len * ship.getGoalAccel());
                }
                goals.get(goal).removeAll(shipDelList);
                if (goals.get(goal).isEmpty())
                    goalDelList.add(goal);
            }
            for (int i = 0; i < goalDelList.size(); i++)
                goals.remove(goalDelList.get(i));
            for (Player player : players)
                for (Ship ship : player.getShips()) {
                    float len = Vector.len(ship, ship.getPlanet());
                    if (len < 15 + ship.getPlanet().getRadius()) {
                        ship.setAccelX(ship.getAccelX() + (ship.getPlanet().getX() - ship.getX()) / len * -200);
                        ship.setAccelY(ship.getAccelY() + (ship.getPlanet().getY() - ship.getY()) / len * -200);
                    }
                    if (len > 40 + ship.getPlanet().getRadius()) {
                        ship.setAccelX(ship.getAccelX() + (ship.getPlanet().getX() - ship.getX()) / len * 100);
                        ship.setAccelY(ship.getAccelY() + (ship.getPlanet().getY() - ship.getY()) / len * 100);
                    }
                }

//             Keep calm. It's just O(n^2) collision detection
//            for (Player player : players) {
//                for (Ship ship : player.getShips()) {
//                    for (int i = 0; i < ship.getCircle().length; i++) {
//                        for (Player enemyPlayer : players) {
//                            for (Ship enemyShip : enemyPlayer.getShips()) {
//                                if (ship == enemyShip) continue;
//                                for (int j = 0; j < enemyShip.getCircle().length; j++) {
//                                    if (Vector.len(ship.getCircleX(i), ship.getCircleY(i), enemyShip.getCircleX(j), enemyShip.getCircleY(j)) <
//                                            ship.getCircleR(i) + enemyShip.getCircleR(j))
//                                        collision(ship, i, enemyShip, j);
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }

        }


        private void nextPosition() {
            float kFriction = 0.9f;
            //float maxVel = -1;
            setNewAcceleration();
            for (Player player : players) {
                for (Ship ship : player.getShips()) {
                    ship.setX(ship.getX() + ship.getVelX() * t + ship.getAccelX() * t * t / 2);
                    ship.setY(ship.getY() + ship.getVelY() * t + ship.getAccelY() * t * t / 2);
                    ship.setVelX(ship.getVelX() + ship.getAccelX() * t);
                    ship.setVelY(ship.getVelY() + ship.getAccelY() * t);
                    ship.setVelX(ship.getVelX() * kFriction);
                    ship.setVelY(ship.getVelY() * kFriction);
//                    if (ship.getVelocity() < 20) {
//                        ship.setVelX(0);
//                        ship.setVelY(0);
//                    }

                    if (ship.getVelocity() != 0) {
                        float lenVel = Vector.len(ship.getVelX(), ship.getVelY());
                        ship.setCos(-(ship.getVelY()) / lenVel);
                        ship.setSin((ship.getVelX()) / lenVel);
                    } else {
                        ship.setCos(0);
                        ship.setSin(1);
                    }
                    //maxVel = Math.max(maxVel, ship.getVelocity());

                }


            }
            //GameActivity.log(String.valueOf(maxVel));

        }

        private void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(x0, y0);
            canvas.scale(scale, scale);
            drawBackground(canvas);
            drawAllObjects(canvas);
            canvas.restore();


        }

        private void preRenderOfShip() {
//            shipBitmap = Bitmap.createBitmap(10, 11, Bitmap.Config.ALPHA_8);
//            shipCanvas = new Canvas(shipBitmap);
            shipPath = new Path();
            shipPath.moveTo(0, -5);
            shipPath.lineTo(-5, 5);
            shipPath.lineTo(0, 2);
            shipPath.lineTo(5, 5);
            shipPath.lineTo(0, -5);
            shipPath.close();

        }

        private void drawAllObjects(Canvas canvas) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (Planet p : planets) {
                RectF rectf = new RectF(p.getX() - p.getRadius(), p.getY() - p.getRadius(), p.getX() + p.getRadius(), p.getY() + p.getRadius());
                paint.setColor(Color.YELLOW);
                canvas.drawArc(rectf, Planet.getStartAngle(), 3.6f * p.getVelOfShip(), true, paint);
                paint.setColor(Color.GREEN);
                canvas.drawArc(rectf, Planet.getStartAngle() + 3.6f * p.getVelOfShip(), 3.6f * p.getHealthOfShip(), true, paint);
                paint.setColor(Color.RED);
                canvas.drawArc(rectf, Planet.getStartAngle() + 3.6f * (p.getVelOfShip() + p.getHealthOfShip()), 3.6f * p.getDamageOfShip(), true, paint);
                paint.setColor(p.getPlayer().getColor());
                canvas.drawCircle(p.getX(), p.getY(), p.getRadius() / 2, paint);
            }
            if (planetInFocus != null) {
                float strokeWidth = 10;
                paint.setStrokeWidth(strokeWidth);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(planetInFocus.getX(), planetInFocus.getY(),
                        planetInFocus.getRadius() + strokeWidth / 2, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            for (Planet p : planets)
                for (Planet.AirDefense a : p.getAirDefenses()) {
                    paint.setStrokeWidth(15 * a.getHealth() / 100);
                    paint.setColor(Color.DKGRAY);
                    canvas.drawCircle(p.getX(), p.getY(), p.getRadius() * 1.8f, paint);
                }
            for (Player p : players)
                for (Ship s : p.getShips()) {
                    paint.setColor(p.getColor());
                    Path path = new Path();
                    path.addPath(shipPath);
//                    path.moveTo(s.getX(), s.getY() - 5);
//                    path.lineTo(s.getX() - 5, s.getY() + 5);
//                    path.lineTo(s.getX(), s.getY() + 2);
//                    path.lineTo(s.getX() + 5, s.getY() + 5);
//                    path.lineTo(s.getX(), s.getY() - 5);
                    Matrix m = new Matrix();
                    m.setSinCos(s.getSin(), s.getCos(), 0, 0);
                    m.postTranslate(s.getX(), s.getY());
                    path.transform(m);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(path, paint);
                    //GameActivity.log(s.getX() + " " + s.getY());
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
                    checkFactories();
                    draw(canvas);
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void checkFactories() {
            int n = 0;
            for (Player p : players)
                n += p.getShips().size();
            if (n < 300) {
                for (Planet planet : planets)
                    for (Planet.Factory fact : planet.getFactories())
                        if (System.currentTimeMillis() - fact.lasTime >= fact.getDuration()) {
                            fact.lasTime = System.currentTimeMillis();
                            fact.addShip();
                        }
            }

        }
    }
}



