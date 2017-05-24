package com.example.pau.busyalert.Interfaces;

import com.example.pau.busyalert.JavaClasses.HerokuLog;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by pau on 24/5/17.
 */

public interface HerokuEndpointInterface {
    @POST("/api")
    @FormUrlEncoded
    Call<HerokuLog> createLog(@Field("userId") String userId,
                              @Field("action") String action,
                              @Field("extraData") String extraData);
}
