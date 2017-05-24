package com.example.pau.busyalert.JavaClasses;

import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;

/**
 * Created by pau on 24/5/17.
 */

public class ApiUtils {
    private ApiUtils() {}

    public static final String BASE_URL = "https://ancient-cliffs-50679.herokuapp.com/";

    public static HerokuEndpointInterface getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(HerokuEndpointInterface.class);
    }
}
