package com.fii.chesscv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckBoardActivity extends AppCompatActivity {
    private static final String TAG = "CheckBoardActivity";
    private Retrofit retrofit;
    private ChessCVAPI chessCVAPI;

    private ImageView imageView;
    private Button getDigitalBoardButton;
    private Button facingWhiteButton;
    private Button facingBlackButton;

    private String facing;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_board);

        getDigitalBoardButton = findViewById(R.id.get_digital_board_button);
        facingWhiteButton = findViewById(R.id.facing_white_button);
        facingBlackButton = findViewById(R.id.facing_black_button);
        imageView = findViewById(R.id.imageView);

        getDigitalBoardButton.setEnabled(false);

        buildRetrofit();

        Bundle b = getIntent().getExtras();
        url = "";
        if(b != null)
            url = b.getString("url");

        Picasso.get().load(url).into(imageView);

        facingWhiteButton.setOnClickListener(view -> {
            getDigitalBoardButton.setEnabled(true);
            facing = "white";
        });
        facingBlackButton.setOnClickListener(view -> {
            getDigitalBoardButton.setEnabled(true);
            facing = "black";
        });

        getDigitalBoardButton.setOnClickListener(view ->{
            Call<FenResponse> call = chessCVAPI.fen(new FenRequest(url, facing));

            call.enqueue(new Callback<FenResponse>() {
                @Override
                public void onResponse(Call<FenResponse> call, Response<FenResponse> response) {
                    if(!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        Toast.makeText(getApplicationContext(),
                                "Url not found, plase try again!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FenResponse body = response.body();
                    Log.i(TAG, "onResponse: " + body.fen);

                    Intent intent = new Intent(CheckBoardActivity.this, FenBoardActivity.class);
                    Bundle b = new Bundle();
                    b.putString("fen", body.fen);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<FenResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: call ", t);
                    Toast.makeText(getApplicationContext(),
                            "No board found, plase try again!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

    }


    private void buildRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        chessCVAPI = retrofit.create(ChessCVAPI.class);
    }
}