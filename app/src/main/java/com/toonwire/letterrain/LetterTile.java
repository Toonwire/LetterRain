package com.toonwire.letterrain;

import android.graphics.Bitmap;

/**
 * Created by Toonwire on 18-07-2016.
 */
public class LetterTile {

    Bitmap bitmap;
    char letter;
    float posX;
    float posY;
    float rate;

    public LetterTile(Bitmap bitmap, char letter, float x, float y) {
        this.letter = letter;
        this.bitmap = bitmap;
        posX = x;
        posY = y;

        rate = 5;
    }

    public void tick() {
        posY += rate;
    }

    public float getX() {
        return posX;
    }

    public float getY() {
        return posY;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public char getLetter() {
        return letter;
    }
}
