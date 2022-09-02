package com.l3ia.camp535;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.CoroutineWorker;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import kotlin.coroutines.Continuation;


public class workerCam535 extends CoroutineWorker {

    boolean run = true;

    String ip;
    int porta;
    String status;
    String ul_msg = "Desconectado";
    InetAddress serverAddr ;
    String resp;


    public workerCam535(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
    }

    MediaPlayer mp;

    private BroadcastReceiver aLBReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // perform action here.
            Enviar("#!:REG:!#");
            run = true;
            Log.i("UDP", "REG");
        }
    };

    private BroadcastReceiver recReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Receber();
            Log.i("UDP", "REC");
        }
    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            run= false;
            //close();
            Log.d("UDP", "STOP");
        }
    };

    @Nullable
    @Override
    public Object doWork(@NonNull Continuation<? super Result> continuation) {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(stopReceiver,
                new IntentFilter("STOP"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(aLBReceiver,
                new IntentFilter("REG"));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(recReceiver,
                new IntentFilter("REC"));

        mp= MediaPlayer.create(getApplicationContext(),R.raw.mario_ring);

        String ip_in = getInputData().getString("IP");
        int porta_in = getInputData().getInt("PT", 0);

        this.ip = ip_in;//"192.168.15.253";
        this.porta = porta_in;//4453;

        Log.d("Udp", "Socket Check INIT");


        StartF();

        try{

            serverAddr = InetAddress.getByName(this.ip);
            //udpSocket.setSoTimeout(5000);

            Log.d("Udp", "Socket Check INIT : "+ip+" @ "+Integer.toString(porta));

            Enviar("#!:REG:!#");

        } catch (IOException e) {
            this.run = false;
            Log.e("Udp", "IO Error:", e);
        }

        resp = "NADA";

        while(run){
            try {
                Thread.sleep(1500);
                Receber();
                Log.i("UDP", "Worker");
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

        }

        PeriodicWorkRequest campRequest =
                new PeriodicWorkRequest.Builder(workerCam535.class, 1, TimeUnit.SECONDS)
                        .setInputData(
                                new Data.Builder()
                                        .putString("IP", ip)
                                        .putInt("PT", porta)
                                        .build())
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(campRequest);
        Log.i("UDP", "FIM!");
        return Result.success();
    }

    public void Enviar(String msg){

        try {
            Log.d("Udp", "Socket ENV INI: "+this.ip+" @ "+Integer.toString(this.porta));
            DatagramSocket udpSocket = new DatagramSocket(porta);
            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, porta);
            Log.d("Udp", "Socket ENV TRY : "+this.ip+" @ "+Integer.toString(this.porta));
            udpSocket.send(packet);
            Log.d("Udp", "Socket ENV OK : "+this.ip+" @ "+Integer.toString(this.porta));
            ul_msg = msg;
            udpSocket.close();
        } catch (IOException e) {
            run = false;
            Log.e("Udp", "Socket ENV ERRO : "+this.ip+" @ "+Integer.toString(this.porta)+" E: "+e.getMessage());
            //e.printStackTrace();
        }
    }

    public void Receber(){
        if(run) {
            try {
                DatagramSocket udpSocket = new DatagramSocket(porta);
                byte[] message = new byte[8000];
                DatagramPacket packetR = new DatagramPacket(message, message.length,serverAddr, porta);

                Log.d("Udp:", "Socket Receber...");
                boolean b = !udpSocket.isClosed();
                if(b) {
                    udpSocket.receive(packetR);
                    udpSocket.close();
                    Log.d("Udp:", "Socket Receber FOI!");
                }

                String text = new String(message, 0, packetR.getLength());
                if (text.indexOf("#!:CAPON:!#") == 0) {
                    mp.start();
                    Notificar();
                    Enviar("#!:CAMPOK:!#");
                }

                if (text.indexOf("#!:PINGOK:!#") == 0) {
                    Enviar("#!:REG:!#");
                    Log.d("Udp:", "Socket MSG: #!:REG:!#");
                }


                if (text.indexOf("#!:OKOK:!#") == 0) {
                    resp = "Nada";
                    status = ul_msg + " : Exec";
                } else {
                    status = ul_msg + " : Erro";
                }

                Log.d("Udp:", "Socket MSG: " + text);
            } catch(SocketException e){
                run = false;
                Log.e("Udp:", "Socket Error:", e);
            } catch(IOException e){
                run = false;
               Log.e("Udp Send:", "IO Error:", e);
             }
        }
    }

    public void StartF() {

        NotificationCompat.Builder builder;

        String mess="Rodando Camp535!";
        builder = new NotificationCompat.Builder(getApplicationContext(),"Cam535");
        builder.setContentTitle("Cam535");
        builder.setContentText(mess);
        builder.setSmallIcon(R.drawable.ic_runcamp_foreground);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        int mNotificationId = 5351;

        NotificationManagerCompat managerCompat=NotificationManagerCompat.from(getApplicationContext());

        managerCompat.notify(mNotificationId,builder.build());

    }


    public void Notificar(){

        Intent fullScreenIntent = new Intent(getApplicationContext(), workerCam535.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);


        NotificationChannel channel= new NotificationChannel("Cam535","Cam535", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder;

        String mess="Campainha!";
        builder = new NotificationCompat.Builder(getApplicationContext(),"Cam535");
        builder.setContentTitle("Cam535");
        builder.setContentText(mess);
        builder.setSmallIcon(R.drawable.ic_camp_at_foreground);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);


        NotificationManagerCompat managerCompat=NotificationManagerCompat.from(getApplicationContext());

        managerCompat.notify(5352,builder.build());

    }




}
