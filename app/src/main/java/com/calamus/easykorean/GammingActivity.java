package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.calamus.easykorean.adapters.TopGamePlayerAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.TopGamePlayerModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;

public class GammingActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button bt_a, bt_b, bt_c;
    TextView tv_A,tv_B,tv_C,tv_displayText;
    ImageView iv_displayImage,iv_displayAudio,iv_back,iv_background;

    //score
    TextView tv_score,tv_name;
    ImageView iv_profile;

    ProgressBar pb;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Executor postExecutor;
    String ans, currentUserPhone,username,profileImage;
    TextView  tvCurrentScore;

    String displayWord,displayImageUrl,displayAudio,category,a,b,c;


    int currentScore = 0;
    int highestScore;
    int selectedButton=0;

    boolean isVip;

    ArrayList<TopGamePlayerModel> playerLists = new ArrayList<>();
    TopGamePlayerAdapter playerAdapter;


    MediaPlayer mPlayer;

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamming);


        sharedPreferences = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        currentUserPhone = sharedPreferences.getString("phone", null);
        profileImage=sharedPreferences.getString("imageUrl",null);
        username=sharedPreferences.getString("Username","");
        highestScore = sharedPreferences.getInt("GameScore", 0);
        isVip = sharedPreferences.getBoolean("isVIP", false);
        MDetect.INSTANCE.init(this);

        getSupportActionBar().hide();
        setUpView();



        postExecutor = ContextCompat.getMainExecutor(this);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        if (!isVip) {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(30000);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                loadAds();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            timer.start();

        }

    }

    private void setUpView() {
        recyclerView = findViewById(R.id.recyclerView);
        iv_displayImage=findViewById(R.id.iv_displayImage);
        iv_displayAudio=findViewById(R.id.iv_displayAudio);
        tv_displayText=findViewById(R.id.tv_displayText);
        bt_a = findViewById(R.id.buttonA);
        bt_b = findViewById(R.id.buttonB);
        bt_c = findViewById(R.id.buttonC);
        tv_A=findViewById(R.id.tv_ansA);
        tv_B=findViewById(R.id.tv_ansB);
        tv_C=findViewById(R.id.tv_ansC);
        pb = findViewById(R.id.pb_loadWord);
        tvCurrentScore = findViewById(R.id.tv_current_score);
        iv_back=findViewById(R.id.iv_back);

        tv_score=findViewById(R.id.tv_score);
        tv_name=findViewById(R.id.tv_name);
        iv_profile=findViewById(R.id.iv_profile);
        iv_background=findViewById(R.id.iv_background);

        AppHandler.setPhotoFromRealUrl(iv_background,"https://www.calamuseducation.com/uploads/icons/bg_gamming.png");

        makeTextSingleLine(tv_A);
        makeTextSingleLine(tv_B);
        makeTextSingleLine(tv_C);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        playerAdapter = new TopGamePlayerAdapter(this, playerLists);
        recyclerView.setAdapter(playerAdapter);

        bt_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton=1;
                checkAndLoad("a");
            }
        });

        bt_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton=2;
                checkAndLoad("b");
            }
        });

        bt_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton=3;
                checkAndLoad("c");
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmExit();
            }
        });

        fetchWord();

        tvCurrentScore.setText(currentScore + "");

        fetchTopGamePlayer();

        tv_name.setText(username);
        if(profileImage!=null) AppHandler.setPhotoFromRealUrl(iv_profile,profileImage);
        tv_score.setText(formatScore(highestScore));

    }


    private void fetchWord() {

        pb.setVisibility(View.VISIBLE);
        bt_a.setEnabled(false);
        bt_b.setEnabled(false);
        bt_c.setEnabled(false);
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                            doAsResult(response);
                            bt_a.setEnabled(true);
                            bt_b.setEnabled(true);
                            bt_c.setEnabled(true);
                            pb.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).url(Routing.GET_GAME_WORD);
            myHttp.runTask();
        }).start();

    }

    @Override
    public void onBackPressed() {


    }

    private void checkAndLoad(String ansWord) {
        if (ansWord.equals(ans)) {
            currentScore++;
            tvCurrentScore.setText("" + currentScore);
            fetchWord();
        } else {
            if (currentScore > highestScore) {
                setMarkGameScore();
                highestScore = currentScore;
                tv_score.setText(formatScore(highestScore));
                editor.putInt("GameScore", highestScore);
                editor.apply();
            }


            playerLists.clear();
            fetchWord();
            fetchTopGamePlayer();
            //   tvHighestScore.setText("" + highestScore);
            showGameOverDialog(currentScore);

        }
    }


    private void doAsResult(String response) {
        try {

            JSONArray ja=new JSONArray(response);
            JSONObject jo=ja.getJSONObject(0);

            displayWord = jo.getString("display_word");
            displayImageUrl = jo.getString("display_image");
            displayAudio = jo.getString("display_audio");
            category = jo.getString("category");
            a = jo.getString("a");
            b = jo.getString("b");
            c = jo.getString("c");
            ans = jo.getString("ans");

            tv_A.setText("A - "+a);
            tv_B.setText("B - "+b);
            tv_C.setText("C - "+c);


            if(category.equals("1")){
                tv_displayText.setText(displayWord);
                tv_displayText.setVisibility(View.VISIBLE);
                iv_displayAudio.setVisibility(View.GONE);
                iv_displayImage.setVisibility(View.GONE);


            }else if(category.equals("2")){
                AppHandler.setPhotoFromRealUrl(iv_displayImage,displayImageUrl);
                tv_displayText.setVisibility(View.GONE);
                iv_displayAudio.setVisibility(View.GONE);
                iv_displayImage.setVisibility(View.VISIBLE);

            }else{
                tv_displayText.setVisibility(View.GONE);
                iv_displayAudio.setVisibility(View.VISIBLE);
                iv_displayImage.setVisibility(View.GONE);

                playAudio(displayAudio);

                iv_displayAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPlayer.start();
                    }
                });
            }

        } catch (Exception e) {}

    }

    @SuppressLint("SetTextI18n")
    private void showGameOverDialog(int score) {
        TextView tv_highest, tv_current, tvResultAns,tvA,tvB,tvC,tv_displayText;
        Button bt_quit, bt_restart;
        ImageView iv_displayImage,iv_displayAudio;
        Button bt_a, bt_b, bt_c;

        View v = getLayoutInflater().inflate(R.layout.game_result_dialog, null);
        tv_highest = v.findViewById(R.id.tv_highest);
        tv_current = v.findViewById(R.id.tv_current);
        bt_quit = v.findViewById(R.id.bt_quit);
        bt_restart = v.findViewById(R.id.bt_restart);
        tvResultAns=v.findViewById(R.id.tv_answer);

        tvA = v.findViewById(R.id.tv_ansA);
        tvB = v.findViewById(R.id.tv_ansB);
        tvC = v.findViewById(R.id.tv_ansC);
        bt_a = v.findViewById(R.id.buttonA);
        bt_b = v.findViewById(R.id.buttonB);
        bt_c = v.findViewById(R.id.buttonC);

        iv_displayImage=v.findViewById(R.id.iv_displayImage);
        iv_displayAudio=v.findViewById(R.id.iv_displayAudio);
        tv_displayText=v.findViewById(R.id.tv_displayText);

        tvA.setText("A - " + a);
        tvB.setText("B - " + b);
        tvC.setText("C - " + c);
        makeTextSingleLine(tvA);
        makeTextSingleLine(tvB);
        makeTextSingleLine(tvC);

        tvResultAns.setText("The answer is ( " + ans + " )");

        if(selectedButton==1)bt_a.setBackground(getResources().getDrawable(R.drawable.bg_selected_game_button));
        if(selectedButton==2)bt_b.setBackground(getResources().getDrawable(R.drawable.bg_selected_game_button));
        if(selectedButton==3)bt_c.setBackground(getResources().getDrawable(R.drawable.bg_selected_game_button));

        if (category.equals("1")) {
            tv_displayText.setText(displayWord);
            tv_displayText.setVisibility(View.VISIBLE);
            iv_displayImage.setVisibility(View.GONE);
            iv_displayAudio.setVisibility(View.GONE);

        } else if (category.equals("2")) {
            AppHandler.setPhotoFromRealUrl(iv_displayImage, displayImageUrl);
            iv_displayImage.setVisibility(View.VISIBLE);
            iv_displayAudio.setVisibility(View.GONE);
            tv_displayText.setVisibility(View.GONE);

        } else {
            iv_displayAudio.setVisibility(View.VISIBLE);
            iv_displayImage.setVisibility(View.GONE);
            tv_displayText.setVisibility(View.GONE);

            playAudio(displayAudio);

            iv_displayAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPlayer.start();
                }
            });
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad = builder.create();
        ad.setCancelable(false);

        bt_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvCurrentScore.setText("0");
                currentScore = 0;
                ad.dismiss();


            }
        });

        bt_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interstitialAd != null) {
                    interstitialAd.show(GammingActivity.this);
                } else {
                    // Proceed to the next level.
                    finish();
                }
            }
        });

        tv_highest.setText("Highest Score : "+highestScore);
        tv_current.setText("Points : "+score);

        ad.show();
    }


    private void fetchTopGamePlayer() {
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray ja = new JSONArray(response);
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jo = ja.getJSONObject(i);
                                    String name = jo.getString("learner_name");
                                    String imageUrl = jo.getString("learner_image");
                                    int gameScore = (int)jo.getLong("game_score");
                                    String userId=jo.getString("user_id");
                                    playerLists.add(new TopGamePlayerModel(userId,name, imageUrl, gameScore));
                                }
                            } catch (Exception e) {
                            }

                            playerAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).url(Routing.GET_GAME_SCORE);
            myHttp.runTask();
        }).start();
    }

    private void setMarkGameScore() {
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).field("phone", currentUserPhone)
                    .field("score", currentScore + "")
                    .url(Routing.UPDATE_GAME_SCORE);
            myHttp.runTask();
        }).start();
    }


    public void confirmExit() {

        String title = "Exit Game!";
        String msg = "Your latest highest score have been saved";
        MyDialog myDialog = new MyDialog(this, title, msg, new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                if (currentScore > highestScore) {
                    setMarkGameScore();
                    highestScore = currentScore;
                    editor.putInt("GameScore", highestScore);
                    editor.apply();
                }
                if (interstitialAd != null) {
                    interstitialAd.show(GammingActivity.this);
                } else {
                    // Proceed to the next level.
                    finish();

                }
            }
        });
        myDialog.showMyDialog();
    }


    private void playAudio(String url) {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {

            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();


        } catch (IOException e) {

            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

        } catch (SecurityException e) {
            e.printStackTrace();

        } catch (IllegalStateException e) {
            e.printStackTrace();

        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

            }
        });

    }

    private void loadAds() {

        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                // Proceed to the next level.
                finish();
            }
        };

        InterstitialAd.load(
                this,
                "ca-app-pub-2472405866346270/9132394579",
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Code to be executed when an ad request fails.
                    }
                });
    }

    private String formatScore(int score){
        if(score>1) return score+" points";
        else return score+" point";
    }

    private void makeTextSingleLine(TextView tv){
        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv.setMarqueeRepeatLimit(-1);
        tv.setSingleLine(true);
        tv.setSelected(true);
    }

}