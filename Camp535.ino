#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#define NOTE_B0  31
#define NOTE_C1  33
#define NOTE_CS1 35
#define NOTE_D1  37
#define NOTE_DS1 39
#define NOTE_E1  41
#define NOTE_F1  44
#define NOTE_FS1 46
#define NOTE_G1  49
#define NOTE_GS1 52
#define NOTE_A1  55
#define NOTE_AS1 58
#define NOTE_B1  62
#define NOTE_C2  65
#define NOTE_CS2 69
#define NOTE_D2  73
#define NOTE_DS2 78
#define NOTE_E2  82
#define NOTE_F2  87
#define NOTE_FS2 93
#define NOTE_G2  98
#define NOTE_GS2 104
#define NOTE_A2  110
#define NOTE_AS2 117
#define NOTE_B2  123
#define NOTE_C3  131
#define NOTE_CS3 139
#define NOTE_D3  147
#define NOTE_DS3 156
#define NOTE_E3  165
#define NOTE_F3  175
#define NOTE_FS3 185
#define NOTE_G3  196
#define NOTE_GS3 208
#define NOTE_A3  220
#define NOTE_AS3 233
#define NOTE_B3  247
#define NOTE_C4  262
#define NOTE_CS4 277
#define NOTE_D4  294
#define NOTE_DS4 311
#define NOTE_E4  330
#define NOTE_F4  349
#define NOTE_FS4 370
#define NOTE_G4  392
#define NOTE_GS4 415
#define NOTE_A4  440
#define NOTE_AS4 466
#define NOTE_B4  494
#define NOTE_C5  523
#define NOTE_CS5 554
#define NOTE_D5  587
#define NOTE_DS5 622
#define NOTE_E5  659
#define NOTE_F5  698
#define NOTE_FS5 740
#define NOTE_G5  784
#define NOTE_GS5 831
#define NOTE_A5  880
#define NOTE_AS5 932
#define NOTE_B5  988
#define NOTE_C6  1047
#define NOTE_CS6 1109
#define NOTE_D6  1175
#define NOTE_DS6 1245
#define NOTE_E6  1319
#define NOTE_F6  1397
#define NOTE_FS6 1480
#define NOTE_G6  1568
#define NOTE_GS6 1661
#define NOTE_A6  1760
#define NOTE_AS6 1865
#define NOTE_B6  1976
#define NOTE_C7  2093
#define NOTE_CS7 2217
#define NOTE_D7  2349
#define NOTE_DS7 2489
#define NOTE_E7  2637
#define NOTE_F7  2794
#define NOTE_FS7 2960
#define NOTE_G7  3136
#define NOTE_GS7 3322
#define NOTE_A7  3520
#define NOTE_AS7 3729
#define NOTE_B7  3951
#define NOTE_C8  4186
#define NOTE_CS8 4435
#define NOTE_D8  4699
#define NOTE_DS8 4978
#define REST      0


// change this to make the song slower or faster
int tempo = 200;

// change this to whichever pin you want to use
int buzzer = 15;


// notes of the moledy followed by the duration.
// a 4 means a quarter note, 8 an eighteenth , 16 sixteenth, so on
// !!negative numbers are used to represent dotted notes,
// so -4 means a dotted quarter note, that is, a quarter plus an eighteenth!!
int melody[] = {

  // Super Mario Bros theme
  // Score available at https://musescore.com/user/2123/scores/2145
  // Theme by Koji Kondo
  
  
  NOTE_E5,8, NOTE_E5,8, REST,8, NOTE_E5,8, REST,8, NOTE_C5,8, NOTE_E5,8, //1
  NOTE_G5,4, REST,4, NOTE_G4,8, REST,4, 
  NOTE_C5,-4, NOTE_G4,8, REST,4, NOTE_E4,-4, // 3
  NOTE_A4,4, NOTE_B4,4, NOTE_AS4,8, NOTE_A4,4,
  NOTE_G4,-8, NOTE_E5,-8, NOTE_G5,-8, NOTE_A5,4, NOTE_F5,8, NOTE_G5,8,
  REST,8, NOTE_E5,4,NOTE_C5,8, NOTE_D5,8, NOTE_B4,-4,
  NOTE_C5,-4, NOTE_G4,8, REST,4, NOTE_E4,-4, // repeats from 3
  NOTE_A4,4, NOTE_B4,4, NOTE_AS4,8, NOTE_A4,4,
  NOTE_G4,-8, NOTE_E5,-8, NOTE_G5,-8, NOTE_A5,4, NOTE_F5,8, NOTE_G5,8,
  REST,8, NOTE_E5,4,NOTE_C5,8, NOTE_D5,8, NOTE_B4,-4,

  
  REST,4, NOTE_G5,8, NOTE_FS5,8, NOTE_F5,8, NOTE_DS5,4, NOTE_E5,8,//7
  REST,8, NOTE_GS4,8, NOTE_A4,8, NOTE_C4,8, REST,8, NOTE_A4,8, NOTE_C5,8, NOTE_D5,8,
  REST,4, NOTE_DS5,4, REST,8, NOTE_D5,-4,
  NOTE_C5,2, REST,2,

  REST,4, NOTE_G5,8, NOTE_FS5,8, NOTE_F5,8, NOTE_DS5,4, NOTE_E5,8,//repeats from 7
  REST,8, NOTE_GS4,8, NOTE_A4,8, NOTE_C4,8, REST,8, NOTE_A4,8, NOTE_C5,8, NOTE_D5,8,
  REST,4, NOTE_DS5,4, REST,8, NOTE_D5,-4,
  NOTE_C5,2, REST,2,


};

// sizeof gives the number of bytes, each int value is composed of two bytes (16 bits)
// there are two values per note (pitch and duration), so for each note there are four bytes
int notes = sizeof(melody) / sizeof(melody[0]) / 2;

// this calculates the duration of a whole note in ms
int wholenote = (60000 * 4) / tempo;

int divider = 0, noteDuration = 0;

void play_song(){
  // iterate over the notes of the melody.
  // Remember, the array is twice the number of notes (notes + durations)
  for (int thisNote = 0; thisNote < notes * 2; thisNote = thisNote + 2) {

    // calculates the duration of each note
    divider = melody[thisNote + 1];
    if (divider > 0) {
      // regular note, just proceed
      noteDuration = (wholenote) / divider;
    } else if (divider < 0) {
      // dotted notes are represented with negative durations!!
      noteDuration = (wholenote) / abs(divider);
      noteDuration *= 1.5; // increases the duration in half for dotted notes
    }

    // we only play the note for 90% of the duration, leaving 10% as a pause
    tone(buzzer, melody[thisNote], noteDuration * 0.9);

    // Wait for the specief duration before playing the next note.
    delay(noteDuration);

    // stop the waveform generation before the next note.
    noTone(buzzer);
  }
}


int sinal_camp = 13;
#ifndef STASSID
#define STASSID "L3Com"
#define STAPSK  "Lg@viv#513"
#endif

unsigned int localPort = 4453;      // local port to listen on

// buffers for receiving and sending data
char packetBuffer[UDP_TX_PACKET_MAX_SIZE + 1]; //buffer to hold incoming packet,
char ReplyBuffer[] = "acknowledged\r\n";       // a string to send back

unsigned long time_mark;

String decode_msg(char* msg){

  Serial.println(msg);
  String s_msg = String(msg);
  //#!:REG:!#
  if(s_msg.substring(0,2) == "#!"){
    int posFim = s_msg.indexOf("!#",3);
    Serial.println("Pos:");
    Serial.println(posFim);
    Serial.println(s_msg.length());
    if((s_msg.length()-2) == posFim || (s_msg.length()-3) == posFim){
      Serial.println("OK!");
      int iniCom = s_msg.indexOf(":",2)+1;
      int fimCom = s_msg.lastIndexOf(":");
      String comando = s_msg.substring(iniCom,fimCom);
      Serial.println(comando);
      return comando;
      }
    }

    return "NECOM";
}

IPAddress ips[10];
int max_cons = 10;
int ips_cons = 0;
bool tocar = false;

WiFiUDP Udp;

void Enviar(IPAddress ip,String msg){
  Udp.beginPacket(ip, Udp.remotePort());
  Udp.write(msg.c_str());
  Udp.endPacket();
}

  

void setup() {

    // initialize the LED pin as an output:
  pinMode(buzzer, OUTPUT);
  // initialize the pushbutton pin as an input:
  pinMode(sinal_camp, INPUT);

  Serial.begin(115200);
  IPAddress local_IP(192, 168, 15, 253);
  IPAddress subnet(255, 255, 255, 0); 
  IPAddress gateway(192, 168, 15, 1);
  WiFi.config(local_IP,subnet,gateway);
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print('.');
    delay(500);
  }
  Serial.print("Connected! IP address: ");
  Serial.println(WiFi.localIP());
  Serial.printf("UDP server on port %d\n", localPort);
  Udp.begin(localPort);

  time_mark = millis();
  
}

void ChecarIpsCli(){
    
    if(millis() - time_mark >= 10000){
      int ips_con_last = ips_cons;
      IPAddress ips_last[max_cons];
      ips_cons = 0;
      for(int j = 0; j<max_cons;j++){
        IPAddress tmp_IP(0, 0, 0, 0);
        ips_last[j] = ips[j];
        ips[j] = tmp_IP;
      }
      time_mark = millis();
      for (int i = 0; i< ips_con_last;i++){
        Serial.println("Enviando PING: ");
        Serial.println(ips_last[i].toString());
        Enviar(ips_last[i],"#!:PINGOK:!#");
      }
      Serial.println("PING");
    }
  
}

void Checar_Camp(){

  bool campState = digitalRead(sinal_camp);

  if(campState || tocar){
    Serial.println("Acc");
    for (int i = 0; i< ips_cons;i++){
      Serial.println("Enviando: ");
      Serial.println(ips[i].toString());
      Enviar(ips[i],"#!:CAPON:!#");
      tocar = true;
    }
    //play_song();
  }else{
     Ping();
  }
}

void Ping(){

    for (int i = 0; i< ips_cons;i++){
      Serial.println("Enviando: ");
      Serial.println(ips[i].toString());
      Enviar(ips[i],"#!:OKOK:!#");
    }

}


void loop() {

    // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    Serial.printf("Received packet of size %d from %s:%d\n    (to %s:%d, free heap = %d B)\n",
                  packetSize,
                  Udp.remoteIP().toString().c_str(), Udp.remotePort(),
                  Udp.destinationIP().toString().c_str(), Udp.localPort(),
                  ESP.getFreeHeap());

    // read the packet into packetBufffer
    int n = Udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);
    packetBuffer[n] = 0;
    Serial.println("Contents:");
    String comando = decode_msg(packetBuffer);

    if(comando == "REG"){
      bool add_ip = true;
      for (int i = 0; i<= ips_cons;i++){
        if(ips[i] == Udp.remoteIP()){
          add_ip = false;
          break;
        }
      }
      if (add_ip && ips_cons < max_cons){
        Serial.println("add: ");
        ips[ips_cons] = Udp.remoteIP();
        Serial.println(ips[ips_cons].toString());
        Enviar(ips[ips_cons],"#!:OKOK!#");
        ips_cons++;
      }
    }

    if(comando == "PLAY"){
      Serial.println("Tocando...");
      play_song();
    }

    if(comando == "CAMPOK"){
      Serial.println("Tocar OK");
      tocar = false;
    }
    
    if(comando == "NECOM"){
      Serial.println("ERROU!");
    }
    
  }


  //ChecarIpsCli();
  Checar_Camp();

  delay(200);
}
