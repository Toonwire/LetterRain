package com.toonwire.letterrain;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashScreenActivity extends Activity {

    public static AnimatorSet splashAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new DictionaryLoaderAsync(this).execute("");
        final ImageView imageView = (ImageView) findViewById(R.id.ivSplash);

        splashAnimator = new AnimatorSet();
        ObjectAnimator flipY = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 360f);
        flipY.setDuration(3000);
        ValueAnimator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        fadeIn.setDuration(3000);

        splashAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                startActivity(new Intent(getApplicationContext(), StartMenuActivity.class));
//                startActivity(new Intent(getApplicationContext(), GameScreenActivity.class));
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                startActivity(new Intent(getApplicationContext(), StartMenuActivity.class));
//                startActivity(new Intent(getApplicationContext(), GameScreenActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        splashAnimator.playTogether(flipY, fadeIn);
        splashAnimator.start();

    }
}
