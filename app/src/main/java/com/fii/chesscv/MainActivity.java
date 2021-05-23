package com.fii.chesscv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    private static final int GALLERY_ACTION_CODE = 2;
    private static final int CAMERA_ACTION_CODE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private Retrofit retrofit;
    private ChessCVAPI chessCVAPI;

    private void verifyStoragePermissions() {
        int writePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writePermission != PackageManager.PERMISSION_GRANTED &&
                readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
        else {
            chooseImage();
        }
    }

    private ImageView imageView;
    private Button takePhotoButton;
    private Button choosePhotoButton;
    private Button getBoardButton;
    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosePhotoButton = findViewById(R.id.choose_photo_button);
        takePhotoButton = findViewById(R.id.take_photo_button);
        getBoardButton = findViewById(R.id.get_board_button);
        imageView = findViewById(R.id.imageView);

        buildRetrofit();

        getBoardButton.setEnabled(false);

        takePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_ACTION_CODE);
            } else {
                Toast.makeText(this, "There is no app that can make a photo",
                        Toast.LENGTH_SHORT).show();
            }
        });

        choosePhotoButton.setOnClickListener(view -> {
            verifyStoragePermissions();
        });

        getBoardButton.setOnClickListener(view -> {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/*"), bitmapdata);

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "img",
                    "img",
                    requestFile);

            Call<BoardResponse> call = chessCVAPI.findBoard(body);

            call.enqueue(new Callback<BoardResponse>() {
                @Override
                public void onResponse(Call<BoardResponse> call, Response<BoardResponse> response) {
                    if(!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        Toast.makeText(getApplicationContext(),
                                "No board found, plase try again!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BoardResponse body = response.body();
                    Log.i(TAG, "onResponse: " + body.url);

                    Intent intent = new Intent(MainActivity.this, CheckBoardActivity.class);
                    Bundle b = new Bundle();
                    b.putString("url", body.url);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<BoardResponse> call, Throwable t) {
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

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    GALLERY_ACTION_CODE);
        } else {
            Toast.makeText(this, "There is no app that can let you choose a photo",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTION_CODE && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            Bitmap takenPhoto = (Bitmap) bundle.get("data");
            selectPhoto(takenPhoto);
        }
        if (requestCode == GALLERY_ACTION_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                try {
                    Bitmap selectedPhoto = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), selectedImageUri);
                    selectPhoto(selectedPhoto);
                } catch (IOException e) {
                    Log.e(TAG, "onActivityResult: ", e);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();
                }
                return;
        }
    }

    private void selectPhoto(Bitmap photo) {
        imageView.setImageBitmap(photo);
        getBoardButton.setEnabled(true);
        this.photo = photo;
    }
}