package com.zircon.mx.wallintepretationmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class AreaDibujo extends View {
    Canvas cavas;
    float posX = 10;
    float posY = 700;
    Path path;
    Paint paint;
    List<Path> paths;
    List<Paint> paints;
    List<Punto> posiciones;

    public AreaDibujo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paths = new ArrayList<>();
        paints = new ArrayList<>();
        posiciones = new ArrayList<>();

        //posiciones.add(new Punto(0.0F, 0.0F));
        paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (posiciones.size() > 1) {
            for (int i = 0; i < posiciones.size(); i++) {
                try {
                    if (i == posiciones.size() - 1) {
                        canvas.drawLine(posiciones.get(i - 1).getX(), posiciones.get(i - 1).getY(), posiciones.get(i).getX(), posiciones.get(i).getY(), paint);
                    } else
                        canvas.drawLine(posiciones.get(i).getX(), posiciones.get(i).getY(), posiciones.get(i + 1).getX(), posiciones.get(i + 1).getY(), paint);
                } catch (Exception ex) {
                    Log.d("Error:", ex.getMessage());
                }

            }
        }

    }

    //coloca el punto en la posicion definida en X e Y

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void ColocarPunto(float x, float y) {
        try {
            posX = x;
            posY = y;

            Predicate<Punto> findPosition = s -> s.X == posX && s.Y == posY;

            if (posiciones.stream().anyMatch(findPosition) == false) {
                posiciones.add(new Punto(posX, posY));

                paint = new Paint();
                paint.setStrokeWidth(10);
                paint.setARGB(255, 255, 0, 0);
                paint.setStyle(Paint.Style.STROKE);
                paints.add(paint);

                path = new Path();
                path.moveTo(x, y);//punto de inicio del trazado
                paths.add(path);
                invalidate();
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void PutSignal(int x, int y, int x2, int y2) {
        try {

            Predicate<Punto> findPosition1 = s -> s.X == x && s.Y == posY;
            Predicate<Punto> findPosition2 = s -> s.X == x2 && s.Y == y2;
            if (posiciones.stream().anyMatch(findPosition1) == false && posiciones.stream().anyMatch(findPosition2) == false) {
                posiciones.add(new Punto(x, y));
                posiciones.add(new Punto(x2, y2));
                paint = new Paint();
                paint.setStrokeWidth(10);
                paint.setARGB(255, 255, 0, 0);
                paint.setStyle(Paint.Style.STROKE);
                paints.add(paint);

                path = new Path();
                path.moveTo(x, y);//punto de inicio del trazado
                paths.add(path);
                invalidate();
            }
        } catch (Exception ex) {
            throw ex;
        }
    }


    private class Punto {
        public Punto(float x, float y) {
            X = x;
            Y = y;
        }

        public float getX() {
            return X;
        }

        public void setX(float x) {
            X = x;
        }

        public float getY() {
            return Y;
        }

        public void setY(float y) {
            Y = y;
        }


        public float X;
        public float Y;


    }
}
