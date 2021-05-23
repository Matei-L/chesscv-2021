package com.fii.chesscv;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ChessCVAPI {

    @Multipart
    @POST("board")
    Call<BoardResponse> findBoard(@Part MultipartBody.Part image);

    @POST("fen")
    Call<FenResponse> fen(@Body FenRequest request);
}
