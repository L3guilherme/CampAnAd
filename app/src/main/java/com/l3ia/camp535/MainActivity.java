package com.l3ia.camp535;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.app.ActivityManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    Context ctx;

    //Intent intentSevice;

//    public Context getCtx() {
//        return ctx;
//    }

//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                Log.i ("isMyServiceRunning?", true+"");
//                return true;
//            }
//        }
//        Log.i ("isMyServiceRunning?", false+"");
//        return false;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

//        intentSevice = new Intent(ctx, serviceCamp535.class);
//        if (!isMyServiceRunning(serviceCamp535.class)) {
//            startService(intentSevice);
//        }



        Button button_run = (Button) findViewById(R.id.button_run);
        TextView txt_st = findViewById(R.id.txt_status);

        button_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Rodar!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent("STOP");
                //intent.putExtra("acc", "play");
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
            }
        });

        Button button_reg = (Button) findViewById(R.id.btn_reg);

        button_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Reg!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                EditText txt_ip = (EditText)findViewById(R.id.txt_IP);
                EditText txt_porta = (EditText)findViewById(R.id.txt_porta);

                PeriodicWorkRequest campRequest =
                        new PeriodicWorkRequest.Builder(workerCam535.class, 1, TimeUnit.SECONDS)
                                .setInputData(
                                        new Data.Builder()
                                                .putString("IP", String.valueOf(txt_ip.getText()))
                                                .putInt("PT", Integer.parseInt(String.valueOf(txt_porta.getText())))
                                                .build())
                                .build();

                WorkManager.getInstance(getApplicationContext()).enqueue(campRequest);
                //WorkManager.getInstance(this).enqueueUniquePeriodicWork("camp535", ExistingPeriodicWorkPolicy.REPLACE,campRequest);
                Intent intent = new Intent("REG");
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
                Log.i("UDP", "REG!");
            }
        });


        Log.i("UDP", "onCreate");


    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("STOP");
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        Log.i("UDP", "onDestroy");
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
//        Intent intent = new Intent("REG");
//        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
//        Log.i("UDP", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
//        Intent intent = new Intent("REG");
//        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
//        Log.i("UDP", "onPause");

//        Intent intent = new Intent("STOP");
//        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);

    }

}