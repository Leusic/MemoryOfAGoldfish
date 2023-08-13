package trotter.max.memoryofagoldfish;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import java.util.Random;

public class CoinTossActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    Random random = new Random();
    private int mNumberOfTosses;
    private boolean mShowingHeads;
    private ImageView mHeads;
    private ImageView mTails;
    private Animator mFlipInAnimator;
    private Animator mFlipOutAnimator;

    private int mNumFlips;
    private int mAnimationCompleteCount;

    private GestureDetectorCompat mDetector;

    @Override
    public void finish() {
        Intent data = new Intent();
        TextView coinTossView = (TextView) findViewById(R.id.coinTossView);
        String responseString = coinTossView.getText().toString();
        data.putExtra("ResponseString", responseString);
        setResult(RESULT_OK, data);
        super.finish();
    }

    private void storePreviousTosses(int pNumberOfTosses) {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
                "trotter.max.memoryofagoldfish",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(
                "numberOfTosses",
                pNumberOfTosses);
        editor.commit();
    }

    private int retrievePreviousTosses() {
        int previousTosses = 0;
        SharedPreferences sharedPreferences =
                this.getApplication().getSharedPreferences(
                        "trotter.max.memoryofagoldfish",
                        Context.MODE_PRIVATE);
        previousTosses = sharedPreferences.getInt(
                "numberOfTosses",
                -1);
        return previousTosses;
    }

    private String getCoinToss() {
        if(random.nextBoolean()){
            return getString(R.string.coinTossResult1);
        }
        return getString(R.string.coinTossResult2);
    }

    private void flipCoin() {
        String result = "Heads!";
        if (mShowingHeads) {
            mShowingHeads = false;
            mFlipInAnimator.setTarget(mTails);
            mFlipOutAnimator.setTarget(mHeads);

            result = "Tails!";
        }
        else {
            mShowingHeads = true;
            mFlipInAnimator.setTarget(mHeads);
            mFlipOutAnimator.setTarget(mTails);

            result = "Heads!";
        }

        mFlipInAnimator.start();
        mFlipOutAnimator.start();

        TextView coinTossView = (TextView) findViewById(R.id.coinTossView);
        coinTossView.setText(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_toss);
        Log.i("Activity Lifecycle", "onCreate");
        mHeads = findViewById(R.id.heads);
        mTails = findViewById(R.id.tails);
        mShowingHeads = false;
        mFlipInAnimator = AnimatorInflater.loadAnimator(this, R.animator.flip_vertically_top_in);
        mFlipOutAnimator = AnimatorInflater.loadAnimator(this, R.animator.flip_vertically_top_out);
        addAnimationListeners();

        mNumFlips = random.nextInt(10);
        mAnimationCompleteCount = 0;
        mDetector = new GestureDetectorCompat(this, this);

        View containerView = findViewById(R.id.container);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // pass the events to the gesture detector
                // a return value of true means the detector is handling it
                // a return value of false means the detector didn't recognise the event
                return mDetector.onTouchEvent(event);
            }
        };
        containerView.setOnTouchListener(touchListener);

        int numberOfTosses = retrievePreviousTosses();
        increaseTossCount();

        Toast.makeText(getApplicationContext(),
                "The coin has been tossed: " + numberOfTosses + " times.",
                Toast.LENGTH_LONG).show();
        storePreviousTosses(numberOfTosses);
    }

    private void increaseTossCount(){
        int numberOfTosses = retrievePreviousTosses();
        if(numberOfTosses == -1) {
            numberOfTosses = 1;
        }
        else {
            numberOfTosses++;
        }
    }

    private void addAnimationListeners() {
        mFlipInAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationCompleteCount++;
                animationComplete();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mFlipOutAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationCompleteCount++;
                animationComplete();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation){

            }
        });
    };


    private void animationComplete() {
        if(mAnimationCompleteCount == 2) {
            mAnimationCompleteCount = 0;
            if(mNumFlips > 0){
                mNumFlips--;
                flipCoin();
            }
            else {
                mNumFlips = random.nextInt(10);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Activity Lifecycle", "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Activity Lifecycle", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Activity Lifecycle", "onResume");
        TextView coinTossView = (TextView) findViewById(R.id.coinTossView);
        String result = getCoinToss();
        coinTossView.setText(result);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Activity Lifecycle", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Activity Lifecycle", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Activity Lifecycle", "onRestart");
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        if(velocityY < -5) {
            increaseTossCount();
            flipCoin();
        }
        return false;
    }
}