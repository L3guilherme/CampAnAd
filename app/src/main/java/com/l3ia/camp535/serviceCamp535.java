package com.l3ia.camp535;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;


public class serviceCamp535 extends Service {

    boolean run = true;

    String ip;
    int porta;
    String status;
    String ul_msg = "Desconectado";
    //DatagramSocket udpSocket ;
    InetAddress serverAddr ;

    private static final String CHANNEL_ID = "channel_535";
    static final String FULL_SCREEN_ACTION = "full_screen_action";
    static final int NOTIFICATION_ID = 5353;

    private Timer timer;
    private TimerTask timerTask;

    MediaPlayer mp;

    private BroadcastReceiver aLBReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // perform action here.
            Enviar("#!:REG:!#");
            //Enviar("#!:PLAY:!#");
            String command = intent.getStringExtra("acc");
            Log.i("UDP", command);
        }
    };

    private BroadcastReceiver recReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Receber();
            String command = intent.getStringExtra("acc");
            Log.i("UDP", command);
        }
    };




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public serviceCamp535(){}

    public serviceCamp535(Context applicationContext) {
        super();
        Log.i("UDP", "here I am!");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startForeground();
        Log.i("UDP", "Restart...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        LocalBroadcastManager.getInstance(this).registerReceiver(aLBReceiver,
                new IntentFilter("PLAY"));

        LocalBroadcastManager.getInstance(this).registerReceiver(recReceiver,
                new IntentFilter("REC"));
        
        mp= MediaPlayer.create(this,R.raw.mario_ring);
        this.ip = "192.168.15.253";
        this.porta = 9853;


        try{
            //udpSocket = new DatagramSocket(this.porta);
            serverAddr = InetAddress.getByName(this.ip);

            Log.d("Udp", "Socket Check INIT : "+ip+" @ "+Integer.toString(porta));
            //Log.d("Udp", "Socket Check INIT");
            Enviar("#!:REG:!#");
            startForeground();

            Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            this.run = false;
            Log.e("Udp", "IO Error:", e);
        }


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        run = false;
        stoptimertask();
        Toast.makeText(this, "FIM!", Toast.LENGTH_LONG).show();
        Log.i("UDP", "ondestroy!");
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every X msecond
        timer.schedule(timerTask, 500, 500); //

    }


    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                Receber();
            }
        };
    }

    public void Notificar(){

        Intent fullScreenIntent = new Intent(this, serviceCamp535.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);


        NotificationChannel channel= new NotificationChannel("Cam535","Cam535",NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager)this.getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder;

        String mess="Campainha!";
        builder = new NotificationCompat.Builder(this,"Cam535");
        builder.setContentTitle("Cam535");
        builder.setContentText(mess);
        builder.setSmallIcon(R.drawable.ic_camp_at_foreground);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);


        NotificationManagerCompat managerCompat=NotificationManagerCompat.from(this);

        managerCompat.notify(5352,builder.build());

    }


    public void startForeground() {

        NotificationCompat.Builder builder;

        String mess="Rodando Camp535!";
        builder = new NotificationCompat.Builder(this,"Cam535");
        builder.setContentTitle("Cam535");
        builder.setContentText(mess);
        builder.setSmallIcon(R.drawable.ic_runcamp_foreground);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        int mNotificationId = 5351;
        startForeground(mNotificationId, builder.build());

        startTimer();

        //this.run();
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
                    //resp = "Nada";
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
}
