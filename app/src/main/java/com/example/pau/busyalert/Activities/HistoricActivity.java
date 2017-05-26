package com.example.pau.busyalert.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.pau.busyalert.Adapters.HistoricAdapter;
import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;
import com.example.pau.busyalert.JavaClasses.ApiUtils;
import com.example.pau.busyalert.JavaClasses.HerokuLog;
import com.example.pau.busyalert.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricActivity extends AppCompatActivity {

    /**
     * HEROKU
     */
    private HerokuEndpointInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.historic_recicle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiUtils.getAPIService();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        apiService.getUserLogs(uid).enqueue(new Callback<List<HerokuLog>>(){
            @Override
            public void onResponse(Call<List<HerokuLog>> call, Response<List<HerokuLog>> response) {
                int statusCode = response.code();
                List<HerokuLog> logs = response.body();
                recyclerView.setAdapter(new HistoricAdapter(logs, R.layout.historic_list, getApplicationContext()));
            }

            @Override
            public void onFailure(Call<List<HerokuLog>> call, Throwable t) {

            }
        });
    }
}
