package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.GameWordModel;
import java.io.IOException;
import java.util.ArrayList;

public class GameWordAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    public  ArrayList<Object> data;
    private final LayoutInflater mInflater;
    MediaPlayer mPlayer;
    public GameWordAdapter(Activity c, ArrayList<Object> data){
        this.data=data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View ItemView=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_word,parent,false);
            return new GameHolder(ItemView);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        final GameWordModel model=(GameWordModel)data.get(position);
        GameWordAdapter.GameHolder gameHolder=(GameHolder)holder;

        if(model.getCategory().equals("1")){
            gameHolder.tvDisplayWord.setText(model.getDisplayWord());
            gameHolder.cvText.setVisibility(View.VISIBLE);
            gameHolder.cvAudio.setVisibility(View.GONE);
            gameHolder.cvImage.setVisibility(View.GONE);

        }else if(model.getCategory().equals("2")){
            AppHandler.setPhotoFromRealUrl(gameHolder.ivMain,model.getDisplayImage());
            gameHolder.cvImage.setVisibility(View.VISIBLE);
            gameHolder.cvAudio.setVisibility(View.GONE);
            gameHolder.cvText.setVisibility(View.GONE);

        }else{
            gameHolder.cvAudio.setVisibility(View.VISIBLE);
            gameHolder.cvImage.setVisibility(View.GONE);
            gameHolder.cvText.setVisibility(View.GONE);

            playAudio(model.getDisplayAudio());

            gameHolder.cvAudio.setOnClickListener(v -> mPlayer.start());
        }

        gameHolder.tvA.setText("A - "+ AppHandler.setMyanmar(model.getAnsA()));
        gameHolder.tvB.setText("B - "+ AppHandler.setMyanmar(model.getAnsB()));
        gameHolder.tvC.setText("C - "+ AppHandler.setMyanmar(model.getAnsC()));

    }



    @Override
    public int getItemCount() {
        return data.size();
    }


    public class GameHolder extends RecyclerView.ViewHolder {
        CardView cvImage,cvAudio,cvText;
        ImageView ivMain;
        TextView tvA,tvB,tvC,tvDisplayWord;

        public GameHolder(@NonNull View itemView) {
            super(itemView);
            cvAudio=itemView.findViewById(R.id.card_audio);
            cvImage=itemView.findViewById(R.id.card_image);
            cvText=itemView.findViewById(R.id.card_text);
            tvA=itemView.findViewById(R.id.tv_ansA);
            tvB=itemView.findViewById(R.id.tv_ansB);
            tvC=itemView.findViewById(R.id.tv_ansC);
            ivMain=itemView.findViewById(R.id.ivDisplayImage);
            tvDisplayWord=itemView.findViewById(R.id.tv_displayWord);

        }
    }

    private void playAudio(String url){


        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{

            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();


        }catch (IOException e){

            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();

        }catch (SecurityException e){
            e.printStackTrace();

        }catch (IllegalStateException e){
            e.printStackTrace();

        }

        mPlayer.setOnCompletionListener(mediaPlayer -> {

        });
    }
}
