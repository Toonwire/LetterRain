package com.toonwire.letterrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.plattysoft.leonids.ParticleSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Toonwire on 18-07-2016.
 */
public class ActionSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawingThread drawingThread;
    private int layoutWidth;
    private Random r;
    private GameScreenActivity mActivity;

    private static LinkedList<Character> letterBag;


    public ActionSurfaceView(Context context) {
        super(context);

        // load the letters A-Z
        char[] letters = new char[26];
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            letters[i] = c;
        }

        // total of 100 tiles: 9 A's, 2 B's, 2 C's ...
        int[] numberOfTiles = {9,2,2,4,12,2,3,2,9,1,1,4,2,6,8,2,1,6,4,6,4,2,2,1,2,1};

        // load the proper amount of each letter into a big "bag"
        letterBag = new LinkedList<>();
        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < numberOfTiles[i]; j++) {
                letterBag.add(letters[i]);
            }
        }
        // let's not have the letters in order..
        Collections.shuffle(letterBag);

        r = new Random();
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        Handler mHandler = new Handler();
        holder.addCallback(this);
        mActivity = (GameScreenActivity) context;

        // create thread only; it's started in surfaceCreated()
        drawingThread = new DrawingThread(holder, context, mHandler);
        setFocusable(true);
    }

    public DrawingThread getThread() {
        return drawingThread;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (drawingThread.getState() == Thread.State.TERMINATED)
            drawingThread = new DrawingThread(getHolder(), getContext(), getHandler());

        drawingThread.setRunning(true);
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        drawingThread.setRunning(false);

        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addLetterTile() {
        // draw inside the view params after measuring the layout params
        float x = r.nextFloat() * (layoutWidth-drawingThread.getBitmap().getWidth());
        drawingThread.addLetterTile(x,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return drawingThread.onTouchEvent(event);
    }

    class DrawingThread extends Thread {
        private ArrayList<LetterTile> letterTiles;
        private Bitmap letterTile;
        private Paint background;

        private Handler mHandler;
        private boolean running;

        private final SurfaceHolder mSurfaceHolder;
        private Context mContext;

        private long frameRate;

        public DrawingThread(SurfaceHolder holder, Context context, Handler handler) {
            mSurfaceHolder = holder;
            mContext = context;
            mHandler = handler;

            letterTiles = new ArrayList<>();

            // only used to get the dimension of the first letter tile
            letterTile = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.letter_tile_a), 140, 140, false);
            background = new Paint();
            background.setStyle(Paint.Style.FILL);
            background.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

            running = true;

            // This equates to 26 frames per second.
            frameRate = (long) (1000 / 26);
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas();

                    synchronized (mSurfaceHolder) {
                        long start = System.currentTimeMillis();
                        doDraw(c);
                        long diff = System.currentTimeMillis() - start;

                        if (diff < frameRate)
                            Thread.sleep(frameRate - diff);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if (c != null)
                        mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

        protected void doDraw(Canvas canvas) {
            // Used to draw over all the previous frames of the falling tile
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), background);

            // Draw
            for (int i = 0; i < letterTiles.size(); i++) {
                LetterTile tile = letterTiles.get(i);
                canvas.drawBitmap(tile.getBitmap(), tile.getX(), tile.getY(), null);
                letterTiles.get(i).tick();
            }

            // Remove if out of view
            for (int i = 0; i < letterTiles.size(); i++) {
                if (letterTiles.get(i).getY() > canvas.getHeight()) {
                    letterTiles.remove(i);

                    // post update of the new time left
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            long millisRemaining = mActivity.getCountdownTimer().getRemainingMillis();
                            long stopTimeInFuture = mActivity.getCountdownTimer().getStopTimeInFuture();

                            if (millisRemaining <= 5000 && millisRemaining != 0) {
                                // reduce game time left to 0
                                long newMillisRemaining = 0;
                                long newStopTimeInFuture = stopTimeInFuture - millisRemaining;
                                mActivity.getCountdownTimer().setRemainingMillis(newStopTimeInFuture);
                                mActivity.getTimeView().setText(mActivity.formatTime(newMillisRemaining));
                            }

                            else {
                                // reduce game time left by 5 seconds
                                long newMillisRemaining = millisRemaining - 5000;
                                long newStopTimeInFuture = stopTimeInFuture - 5000;
                                mActivity.getCountdownTimer().setRemainingMillis(newStopTimeInFuture);
                                mActivity.getTimeView().setText(mActivity.formatTime(newMillisRemaining));
                            }
                        }
                    });
                }
            }
        }

        public void setRunning(boolean bRun) {
            running = bRun;
        }

        public long getFrameRate() {
            return frameRate;
        }

        public Bitmap getBitmap() {
            return letterTile;
        }

        public void addLetterTile(float x, int y) {
            char letter = letterBag.pop();
            int resourceId = mContext.getResources().getIdentifier("letter_tile_" + letter, "drawable", mContext.getPackageName());
            letterTile = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resourceId), 140, 140, false);
            LetterTile tile = new LetterTile(letterTile, letter, x, y);
            letterTiles.add(tile);
        }

        public ArrayList<LetterTile> getLetterTiles() {
            return letterTiles;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN)
                return false;

            float x = event.getX();
            float y = event.getY();

            LetterTile tileToBeRemoved = null;
            for (LetterTile tile : letterTiles) {
                // Remove the current element from the iterator and the list.
                if (x > tile.getX() && x < tile.getBitmap().getWidth() + tile.getX()
                        && y > tile.getY() && y < tile.getBitmap().getHeight() + tile.getY()) {

                    // add the letter of the tile to the editText
                    mActivity.addLetterToEditText(Character.toUpperCase(tile.getLetter()));
                    tileToBeRemoved = tile;
                }
            }

            if (tileToBeRemoved != null) {
                // remove it with animation
                post(new TileDisappearingAnimation(tileToBeRemoved));
                letterTiles.remove(tileToBeRemoved);
            }

            return true;
        }

        private class TileDisappearingAnimation implements Runnable {
            private LetterTile letterTile;

            public TileDisappearingAnimation(LetterTile tile) {
                this.letterTile = tile;
            }

            @Override
            public void run() {
                new ParticleSystem(mActivity, 8, BitmapFactory.decodeResource(getResources(), R.drawable.star_white), 500)
                        .setFadeOut(300, new LinearOutSlowInInterpolator())
                        .setSpeedRange(0.2f, 0.2f)
                        .emit((int) letterTile.getX()+letterTile.getBitmap().getWidth()/2 + letterTile.getBitmap().getWidth()/4, (int) letterTile.getY()+letterTile.getBitmap().getHeight(), 80, 100);
            }
        }
    }

    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
