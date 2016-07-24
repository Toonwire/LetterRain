package com.toonwire.letterrain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartMenuActivity extends Activity {

    static final int SCORE_REQUEST_CODE = 0;
    private int wordScore, wordCount, multiplier;

    private TextView tvWordScore, tvWordCount, tvMultiplier, tvScoreTotal;
    private LinearLayout scoreLayout;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        tvWordScore = (TextView) findViewById(R.id.tvWordScore);
        tvWordCount = (TextView) findViewById(R.id.tvWordCount);
        tvMultiplier = (TextView) findViewById(R.id.tvMultiplier);
        tvScoreTotal = (TextView) findViewById(R.id.tvScoreTotal);

        scoreLayout = (LinearLayout) findViewById(R.id.score_layout);
        scoreLayout.setVisibility(View.INVISIBLE);

        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartMenuActivity.this, GameScreenActivity.class);
                startActivityForResult(intent, SCORE_REQUEST_CODE);
                overridePendingTransition(R.anim.pull_activity_in_right, R.anim.push_activity_out_left);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // place the play button back at its origin position
        playButton.setTranslationY(0);
        // hide the view again to make the animated intro
        scoreLayout.setVisibility(View.INVISIBLE);
        if (requestCode == SCORE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                wordScore = data.getIntExtra("score" , 0);
                wordCount = data.getIntExtra("count", 0);
                multiplier = data.getIntExtra("multiplier", 0);

                makeRoomAndShowScoreLayout();
                // count and display final score
                tvWordScore.setText(String.valueOf(wordScore));
                tvMultiplier.setText(String.format("x%s", String.valueOf(multiplier)));
                tvWordCount.setText(String.valueOf(wordCount));
                int totalScore = (wordScore + wordCount)*multiplier;

                // animate counting the final score
                ValueAnimator animator = new ValueAnimator();
                animator.setObjectValues(0, totalScore);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        tvScoreTotal.setText(String.valueOf(animation.getAnimatedValue()));
                    }
                });
                animator.setEvaluator(new TypeEvaluator<Integer>() {
                    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                        return Math.round(startValue + (endValue - startValue) * fraction);
                    }
                });
                animator.setDuration(2000);
                animator.start();
            }
        }
    }

    private void makeRoomAndShowScoreLayout() {
        playButton.animate()
            .translationY(scoreLayout.getHeight())
            .setDuration(1000)
            .setStartDelay(500)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreLayout.setVisibility(View.VISIBLE);
                }
            }).start();
    }
}
