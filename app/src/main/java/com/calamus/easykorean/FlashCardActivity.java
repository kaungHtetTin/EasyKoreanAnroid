package com.calamus.easykorean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class FlashCardActivity extends AppCompatActivity {

    // Views
    private TextView tvAppBar, tvProgress, tvLearningDay, tvWordCounts;
    private LinearLayout progressHeader, flashcardFront, flashcardBack;
    private RelativeLayout  loadingOverlay, skipDialogOverlay;
    CardView cardFlashcardContainer;
    LinearLayout emptyState, completedState;
    private Button btnShowAnswer, btnSkip, btnSkipCancel, btnSkipConfirm, btnStartNewSession;
    private Button btnRating1, btnRating2, btnRating3, btnRating4, btnRating5;
    private TextView tvCardWord, tvCardIpa, tvBackCardWord, tvBackCardIpa, tvCardTranslation, tvCardExamples;

    // Data
    private List<CardData> cards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isFlipped = false;
    private boolean isLoading = false;
    private String deckId;
    private String userId;
    private String languageId = Routing.FLASHCARD_LANGUAGE_ID;
    private LearningStats learningStats;

    // Executor
    private Executor postExecutor;
    private SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd=null;
    boolean isVip;

    // Card Data class
    private static class CardData {
        String cardId;
        String word;
        String ipa;
        String translation;
        List<String> examples = new ArrayList<>();
        JSONObject rawData; // Store raw JSON for API calls

        CardData(JSONObject cardJson) throws JSONException {
            rawData = cardJson;
            JSONObject card = cardJson.has("card") ? cardJson.getJSONObject("card") : cardJson;
            JSONObject richData = cardJson.has("rich_data") ? cardJson.getJSONObject("rich_data") : new JSONObject();

            cardId = card.getString("id");
            word = card.optString("word", "");
            ipa = richData.optString("ipa", "");
            translation = richData.optString("burmese_translation", card.optString("burmese_translation", ""));

            // Parse examples
            if (richData.has("example_sentences") && !richData.isNull("example_sentences")) {
                try {
                    JSONArray examplesArray = richData.getJSONArray("example_sentences");
                    for (int i = 0; i < examplesArray.length(); i++) {
                        String example = examplesArray.optString(i, "");
                        if (!TextUtils.isEmpty(example)) {
                            examples.add(example);
                        }
                    }
                } catch (Exception e) {
                    // Try as string
                    String exampleStr = richData.optString("example_sentences", "");
                    if (!TextUtils.isEmpty(exampleStr)) {
                        try {
                            JSONArray examplesArray = new JSONArray(exampleStr);
                            for (int i = 0; i < examplesArray.length(); i++) {
                                String example = examplesArray.optString(i, "");
                                if (!TextUtils.isEmpty(example)) {
                                    examples.add(example);
                                }
                            }
                        } catch (Exception e2) {
                            // Use as single string
                            examples.add(exampleStr);
                        }
                    }
                }
            } else if (card.has("example_sentences") && !card.isNull("example_sentences")) {
                try {
                    String exampleStr = card.getString("example_sentences");
                    JSONArray examplesArray = new JSONArray(exampleStr);
                    for (int i = 0; i < examplesArray.length(); i++) {
                        String example = examplesArray.optString(i, "");
                        if (!TextUtils.isEmpty(example)) {
                            examples.add(example);
                        }
                    }
                } catch (Exception e) {
                    String exampleStr = card.optString("example_sentences", "");
                    if (!TextUtils.isEmpty(exampleStr)) {
                        examples.add(exampleStr);
                    }
                }
            }
        }
    }

    // Learning Stats class
    private static class LearningStats {
        int learningDayNumber;
        int newWords;
        int recallWords;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        // Handle edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize
        sharedPreferences = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postExecutor = ContextCompat.getMainExecutor(this);
        userId = sharedPreferences.getString("_id", null);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        deckId = getIntent().getStringExtra("deck_id");

        if (TextUtils.isEmpty(deckId) || TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Invalid deck or user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(!isVip){
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    loadAd();
                }
            });
        }


        initViews();
        setupAppBar();
        setupClickListeners();
        loadCards();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(FlashCardActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });
    }

    private void initViews() {
        // App bar
        tvAppBar = findViewById(R.id.tv_appbar);
        ImageView ivBack = findViewById(R.id.iv_back);

        // Progress header
        progressHeader = findViewById(R.id.progress_header);
        tvProgress = findViewById(R.id.tv_progress);
        tvLearningDay = findViewById(R.id.tv_learning_day);
        tvWordCounts = findViewById(R.id.tv_word_counts);

        // Flashcard container
        cardFlashcardContainer = findViewById(R.id.card_flashcard_container);
        flashcardFront = findViewById(R.id.flashcard_front);
        flashcardBack = findViewById(R.id.flashcard_back);
        loadingOverlay = findViewById(R.id.loading_overlay);
        emptyState = findViewById(R.id.empty_state);
        completedState = findViewById(R.id.completed_state);

        // Buttons
        btnShowAnswer = findViewById(R.id.btn_show_answer);
        btnSkip = findViewById(R.id.btn_skip);
        btnSkipCancel = findViewById(R.id.btn_skip_cancel);
        btnSkipConfirm = findViewById(R.id.btn_skip_confirm);
        btnStartNewSession = findViewById(R.id.btn_start_new_session);
        btnRating1 = findViewById(R.id.btn_rating_1);
        btnRating2 = findViewById(R.id.btn_rating_2);
        btnRating3 = findViewById(R.id.btn_rating_3);
        btnRating4 = findViewById(R.id.btn_rating_4);
        btnRating5 = findViewById(R.id.btn_rating_5);

        // Text views
        tvCardWord = findViewById(R.id.tv_card_word);
        tvCardIpa = findViewById(R.id.tv_card_ipa);
        tvBackCardWord = findViewById(R.id.tv_back_card_word);
        tvBackCardIpa = findViewById(R.id.tv_back_card_ipa);
        tvCardTranslation = findViewById(R.id.tv_card_translation);
        tvCardExamples = findViewById(R.id.tv_card_examples);

        // Skip dialog
        skipDialogOverlay = findViewById(R.id.skip_dialog_overlay);

        // Back button
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(FlashCardActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");
                        finish();
                    }
                }
            });
        }
    }

    private void setupAppBar() {
        if (tvAppBar != null) {
            tvAppBar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvAppBar.setMarqueeRepeatLimit(-1);
            tvAppBar.setSingleLine(true);
            tvAppBar.setSelected(true);
            tvAppBar.setText("Flashcard Learning");
        }
    }

    private void setupClickListeners() {
        btnShowAnswer.setOnClickListener(v -> flipCard());
        btnSkip.setOnClickListener(v -> showSkipConfirmation());
        btnSkipCancel.setOnClickListener(v -> hideSkipConfirmation());
        btnSkipConfirm.setOnClickListener(v -> {
            hideSkipConfirmation();
            skipCard();
        });
        btnStartNewSession.setOnClickListener(v -> {
            currentIndex = 0;
            cards.clear();
            loadCards();
        });

        // Rating buttons
        btnRating1.setOnClickListener(v -> rateCard(1));
        btnRating2.setOnClickListener(v -> rateCard(2));
        btnRating3.setOnClickListener(v -> rateCard(3));
        btnRating4.setOnClickListener(v -> rateCard(4));
        btnRating5.setOnClickListener(v -> rateCard(5));

        // Close skip dialog on overlay click
        skipDialogOverlay.setOnClickListener(v -> hideSkipConfirmation());
    }

    private void loadCards() {
        showLoading();
        hideAllStates();

        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        handleCardsResponse(response);
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        Toast.makeText(FlashCardActivity.this, "Failed to load cards: " + msg, Toast.LENGTH_SHORT)
                                .show();
                        showEmptyState();
                    });
                }
            }).url(Routing.FC_GET_CARDS+"?user_id="+userId+"&language_id="+languageId+"&deck_id="+deckId);

            myHttp.runTask();
        }).start();
    }

    private void handleCardsResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.optBoolean("success", false);

            if (!success) {
                String message = jsonResponse.optString("message", "No cards available for this deck.");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                showEmptyState();
                return;
            }

            JSONObject data = jsonResponse.getJSONObject("data");
            JSONArray wordsArray = data.getJSONArray("words");

            cards.clear();
            for (int i = 0; i < wordsArray.length(); i++) {
                try {
                    JSONObject wordJson = wordsArray.getJSONObject(i);
                    cards.add(new CardData(wordJson));
                } catch (Exception e) {
                    Log.e("FlashCard", "Error parsing card: " + e.getMessage());
                }
            }

            // Parse learning stats
            learningStats = new LearningStats();
            learningStats.learningDayNumber = data.optInt("learning_day_number", 0);
            if (data.has("word_counts")) {
                JSONObject wordCounts = data.getJSONObject("word_counts");
                learningStats.newWords = wordCounts.optInt("new_words", 0);
                learningStats.recallWords = wordCounts.optInt("recall_words", 0);
            }

            if (cards.size() > 0) {
                currentIndex = 0;
                displayCard();
                updateProgress();
                updateStats();
                progressHeader.setVisibility(View.VISIBLE);
                cardFlashcardContainer.setVisibility(View.VISIBLE);
            } else {
                showEmptyState();
            }

        } catch (JSONException e) {
            Log.e("FlashCard", "Error parsing response: " + e.getMessage());
            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void displayCard() {
        if (currentIndex >= cards.size()) {
            showCompletedState();
            return;
        }

        CardData card = cards.get(currentIndex);

        // Reset to front
        isFlipped = false;
        flashcardFront.setVisibility(View.VISIBLE);
        flashcardBack.setVisibility(View.GONE);

        // Set front side content
        tvCardWord.setText(card.word);
        tvCardIpa.setText(card.ipa);

        // Set back side content
        tvBackCardWord.setText(card.word);
        tvBackCardIpa.setText(card.ipa);
        tvCardTranslation.setText(card.translation);

        // Display examples
        StringBuilder examplesText = new StringBuilder();
        for (String example : card.examples) {
            if (!TextUtils.isEmpty(example)) {
                if (examplesText.length() > 0) {
                    examplesText.append("\n\n");
                }
                examplesText.append("• ").append(example);
            }
        }
        tvCardExamples.setText(examplesText.toString());

        updateProgress();
    }

    private void flipCard() {
        if (isFlipped)
            return;

        isFlipped = true;

        // Animate flip
        ObjectAnimator flipAnimator = ObjectAnimator.ofFloat(cardFlashcardContainer, "rotationY", 0f, 180f);
        flipAnimator.setDuration(600);
        flipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flashcardFront.setVisibility(View.GONE);
                flashcardBack.setVisibility(View.VISIBLE);
                cardFlashcardContainer.setRotationY(0f);
            }
        });
        flipAnimator.start();
    }

    private void rateCard(int rating) {
        if (isLoading || currentIndex >= cards.size())
            return;

        CardData card = cards.get(currentIndex);
        showLoading();
        disableButtons();

        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        enableButtons();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.optBoolean("success", false);
                            if (success) {
                                // Move to next card
                                currentIndex++;
                                if (currentIndex < cards.size()) {
                                    displayCard();
                                } else {
                                    showCompletedState();
                                }
                            } else {
                                String message = jsonResponse.optString("message", "Failed to save rating.");
                                Toast.makeText(FlashCardActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("FlashCard", "Error parsing rating response: " + e.getMessage());
                            Toast.makeText(FlashCardActivity.this, "Failed to save rating", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        enableButtons();
                        Toast.makeText(FlashCardActivity.this, "Failed to save rating: " + msg, Toast.LENGTH_SHORT)
                                .show();
                    });
                }
            }).url(Routing.FC_RATE_WORD)
                    .field("user_id", userId)
                    .field("card_id", card.cardId)
                    .field("quality", String.valueOf(rating));

            myHttp.runTask();
        }).start();
    }

    private void skipCard() {
        if (isLoading || currentIndex >= cards.size())
            return;

        CardData card = cards.get(currentIndex);
        showLoading();
        disableButtons();

        // Get all card IDs in current session
        JSONArray sessionCardIds = new JSONArray();
        for (CardData c : cards) {
            sessionCardIds.put(c.cardId);
        }

        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        enableButtons();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.optBoolean("success", false);
                            if (success) {
                                // Check if there's a replacement word
                                if (jsonResponse.has("data")) {
                                    JSONObject data = jsonResponse.getJSONObject("data");
                                    if (data.has("replacement_word") && !data.isNull("replacement_word")) {
                                        try {
                                            JSONObject replacementWord = data.getJSONObject("replacement_word");
                                            cards.set(currentIndex, new CardData(replacementWord));
                                            displayCard();
                                            return;
                                        } catch (Exception e) {
                                            Log.e("FlashCard", "Error parsing replacement word: " + e.getMessage());
                                        }
                                    }
                                }
                                // No replacement, move to next
                                currentIndex++;
                                if (currentIndex < cards.size()) {
                                    displayCard();
                                } else {
                                    showCompletedState();
                                }
                            } else {
                                String message = jsonResponse.optString("message", "Failed to skip word.");
                                Toast.makeText(FlashCardActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("FlashCard", "Error parsing skip response: " + e.getMessage());
                            Toast.makeText(FlashCardActivity.this, "Failed to skip word", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        hideLoading();
                        enableButtons();
                        Toast.makeText(FlashCardActivity.this, "Failed to skip word: " + msg, Toast.LENGTH_SHORT)
                                .show();
                    });
                }
            }).url(Routing.FC_SKIP_WORD)
                    .field("user_id", userId)
                    .field("card_id", card.cardId)
                    .field("language_id", languageId)
                    .field("deck_id", deckId)
                    .field("session_card_ids", sessionCardIds.toString());

            myHttp.runTask();
        }).start();
    }

    private void showSkipConfirmation() {
        skipDialogOverlay.setVisibility(View.VISIBLE);
    }

    private void hideSkipConfirmation() {
        skipDialogOverlay.setVisibility(View.GONE);
    }

    private void updateProgress() {
        tvProgress.setText("Card " + (currentIndex + 1) + " of " + cards.size());
    }

    private void updateStats() {
        if (learningStats != null) {
            tvLearningDay.setText("Learning Day: " + learningStats.learningDayNumber);
            tvWordCounts.setText("New: " + learningStats.newWords + " | Recall: " + learningStats.recallWords);
        }
    }

    private void showLoading() {
        isLoading = true;
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        isLoading = false;
        loadingOverlay.setVisibility(View.GONE);
    }

    private void disableButtons() {
        btnRating1.setEnabled(false);
        btnRating2.setEnabled(false);
        btnRating3.setEnabled(false);
        btnRating4.setEnabled(false);
        btnRating5.setEnabled(false);
        btnSkip.setEnabled(false);
    }

    private void enableButtons() {
        btnRating1.setEnabled(true);
        btnRating2.setEnabled(true);
        btnRating3.setEnabled(true);
        btnRating4.setEnabled(true);
        btnRating5.setEnabled(true);
        btnSkip.setEnabled(true);
    }

    private void showEmptyState() {
        progressHeader.setVisibility(View.GONE);
        cardFlashcardContainer.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        completedState.setVisibility(View.GONE);
    }

    private void showCompletedState() {
        progressHeader.setVisibility(View.GONE);
        cardFlashcardContainer.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        completedState.setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        cardFlashcardContainer.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        completedState.setVisibility(View.GONE);
        progressHeader.setVisibility(View.GONE);
    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,Routing.ADMOB_INTERSTITIAL, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Toast.makeText(getApplicationContext(),"Ad loaded",Toast.LENGTH_SHORT).show();
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.

                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                mInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.

                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.

                            }
                        });

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error

                        mInterstitialAd = null;
                        Toast.makeText(getApplicationContext(),"Ad fail",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
