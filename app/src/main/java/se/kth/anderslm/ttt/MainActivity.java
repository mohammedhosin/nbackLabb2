package se.kth.anderslm.ttt;

import static se.kth.anderslm.ttt.model.TicLogic.SIZE;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import se.kth.anderslm.ttt.model.TicLogic;
import se.kth.anderslm.ttt.utils.AnimationUtils;
import se.kth.anderslm.ttt.utils.TextToSpeechUtil;
import se.kth.anderslm.ttt.utils.UiUtils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TicLogic ticLogic;

    private Timer msgTimer;
    private Handler handler;

    private ImageView[] imageViews;
    private Drawable crossDrawable;

    private TextView totalRounds,correctAnswers;

    private Button mButton;

    private TextToSpeechUtil textToSpeechUtil;
    private Resources resources;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ui stuff
        setContentView(R.layout.activity_main);
        imageViews = loadReferencesToImageViews();
        totalRounds = findViewById(R.id.nrOfRounds);
        correctAnswers = findViewById(R.id.correctAnswers);
        mButton = findViewById(R.id.matchBtn);
        findViewById(R.id.restartBtn).setOnClickListener(v -> onGameEnd());
        findViewById(R.id.startTimerBtn).setOnClickListener(this::onStartButtonClicked);
        mButton.setOnClickListener(this::onMatchButtonClicked);
        textToSpeechUtil = new TextToSpeechUtil();  // also part of the user interface(!)
        // load drawables (images)
        resources = getResources();
        crossDrawable = ResourcesCompat.getDrawable(resources, R.drawable.cross, null);
        //noughtDrawable = ResourcesCompat.getDrawable(resources, R.drawable.nought, null);

        ticLogic = TicLogic.getInstance(3); // singleton

        msgTimer = null;
        handler = new Handler();

        //updateImageViews(null); // game might already be started, so update image views
    }


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:

                return true;
            case R.id.item2:

                // Your desired class
               // startActivity(new Intent(GameActivity.this, MenuActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    private void onMatchButtonClicked(View view) {
        if (ticLogic.isNstepsBackMatched()){
            ticLogic.incrementCorrectAnswers();
            correctAnswers.setText("Correct answers: "+ticLogic.getCorrectAnswers());
            Toast.makeText(this,"Matched!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "FAIL", Toast.LENGTH_SHORT).show();
            mButton.setTextColor(Color.parseColor("#FFED0B0B"));
        }
    }

    // the task to execute periodically
    private class StimuliTimerTask extends TimerTask {
        public void run() {
            mButton.setTextColor(Color.parseColor("#FFFFFFFF"));
            int index = (int) ( Math.round(Math.random() * 8));
            // String msg = "Messge number " + noOfMsgs;
            Log.i("MsgTask", "Randomized index:" + index);
            // post message to main thread
            handler.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    if ((ticLogic.getCurrentRound() + 1)<=ticLogic.getNrOfRounds()) {
                        ticLogic.incrementCurrentRound();
                        //applyVisualStimuli(imageViews[index]);
                        applyAudioStimuli();
                    }
                    else
                        onGameEnd();
                }
            });
        }
    }

    // start a new timer and task on button clicked
    public void onStartButtonClicked(View view) {
        boolean started = startTimer();
        if (!started) {
            Toast.makeText(this, "Task already running", Toast.LENGTH_SHORT).show();
        }else
            correctAnswers.setText("Correct answers: "+0);

    }

    // NB! Cancel the current and queued utterances, then shut down the service to
    // de-allocate resources
    @Override
    protected void onPause() {
        textToSpeechUtil.shutdown();
        cancelTimer();
        super.onPause();
    }

    // Initialize the text-to-speech service - we do this initialization
    // in onResume because we shutdown the service in onPause
    @Override
    protected void onResume() {
        super.onResume();
        textToSpeechUtil.initialize(getApplicationContext());
    }

    private boolean startTimer() {
        if (msgTimer == null) { // first, check if task is already running
            msgTimer = new Timer();
            // schedule a new task: task , delay, period (milliseconds)
            totalRounds.setText("Rounds: "+ticLogic.getCurrentRound()+"/"+ticLogic.getNrOfRounds());
            msgTimer.schedule(new StimuliTimerTask(), 1000, 2000);
            return true; // new task started
        }
        return false;
    }

    private boolean cancelTimer() {
        if (msgTimer != null) {
            msgTimer.cancel();
            msgTimer = null;
            Toast.makeText(this, "Game ended", Toast.LENGTH_SHORT).show();
            Log.i("MsgTask", "timer canceled");
            return true; // task cancelede
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onGameEnd() {
        ticLogic.reset();
        cancelTimer();
        textToSpeechUtil.speakNow("Ending");
    }

    // ui helpers
    private void applyVisualStimuli(View tappedView) {
        Drawable img = crossDrawable;
        imageViews[Integer.valueOf((String) tappedView.getTag())].setImageDrawable(img);
        totalRounds.setText("Rounds: "+ticLogic.getCurrentRound()+"/"+ticLogic.getNrOfRounds());
        ticLogic.addToLatestStimuliArray(Integer.valueOf((String) tappedView.getTag()));
        AnimationUtils.fadeInAndOutImageView(tappedView,imageViews[Integer.valueOf((String) tappedView.getTag())]);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void applyAudioStimuli(){
        int letter = (int) Math.round((Math.random() * 8) + 65);
        totalRounds.setText("Rounds: "+ticLogic.getCurrentRound()+"/"+ticLogic.getNrOfRounds());
        ticLogic.addToLatestStimuliArray(letter);
        textToSpeechUtil.speakNow(String.valueOf((char) letter));
        Log.d("Letter",letter+":"+String.valueOf((char) letter));
    }
    // load references to, and add listener on, all image views
    private ImageView[] loadReferencesToImageViews() {
        // well, it would probably be easier (for a larger matrix) to create
        // the views in Java code and then add them to the appropriate layout
        ImageView[] imgViews = new ImageView[SIZE * SIZE];
        imgViews[0] = findViewById(R.id.imageView0);
        imgViews[1] = findViewById(R.id.imageView1);
        imgViews[2] = findViewById(R.id.imageView2);
        imgViews[3] = findViewById(R.id.imageView3);
        imgViews[4] = findViewById(R.id.imageView4);
        imgViews[5] = findViewById(R.id.imageView5);
        imgViews[6] = findViewById(R.id.imageView6);
        imgViews[7] = findViewById(R.id.imageView7);
        imgViews[8] = findViewById(R.id.imageView8);
        return imgViews;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // to prevent the staus bar from reappearing in landscape mode when,
        // for example, a dialog is shown
        if(hasFocus) UiUtils.setStatusBarHiddenInLandscapeMode(this);
    }
}