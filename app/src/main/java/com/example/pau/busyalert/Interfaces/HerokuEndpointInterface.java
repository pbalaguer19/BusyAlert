package com.example.pau.busyalert.Interfaces;

import com.example.pau.busyalert.JavaClasses.HerokuLog;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pau on 24/5/17.
 */

public interface HerokuEndpointInterface {
    @POST("/api")
    @FormUrlEncoded
    Call<HerokuLog> createLog(@Field("userId") String userId,
                              @Field("action") String action,
                              @Field("extraData") String extraData);

    @DELETE("users/{userId}")
    Call<ResponseBody> deleteLogs(@Path("userId") String userId);

    @GET("history/{userId}")
    Call<List<HerokuLog>> getUserLogs(@Path("userId") String userId);

}
