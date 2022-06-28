package com.zircon.mx.wallintepretationmovil;


public class KeyValuePair<F extends Number, F1 extends Number> {
    private float X;
    private float Y;

    public KeyValuePair(float X,float Y) {

        setX(X);
        setY(Y);
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
}
