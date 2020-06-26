package com.ferhat.androidsocketserver;

import androidx.appcompat.app.AppCompatActivity; //arayuz

import android.os.Bundle; //bilmiyorum :(
import android.util.Log;
import android.view.View; //button onclick fonksiyon parametresi
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast; //button onclick test tiklama fonksiyonu

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity_ferhat";
    tcp bag;
    Switch itsServerConnection; //it's server connection


    boolean baglantiSaglandi = false;

    String hedefIP; //Server kurarken kullanilmayacak diye varsayiyorum


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itsServerConnection = (Switch)findViewById(R.id.switch_server);
        hedefIP = ((EditText)findViewById(R.id.et_hedefIP)).getText().toString();

    }

    public void connect(View v){
         boolean yapServer = itsServerConnection.isChecked();

         bag = new tcp();
        Toast.makeText(this, "Android IP:"+bag.verIP(), Toast.LENGTH_SHORT).show();

        hedefIP = ((EditText)findViewById(R.id.et_hedefIP)).getText().toString();

        try {
            if (yapServer) {
                // burada server olarak baglanti acacaksin
                bag.dinle();
                Log.i(TAG, "connect: Dinlemeye baslanildi");
            } else {
                // burada da istemci (client) olarak baglanti (socket) acacaksin
                // Tek satirlik bi komut ama siniftan cagirmak daha kolay

                bag.baglan(hedefIP);
                Log.i(TAG, "connect: Baglanildi");
            }
            baglantiSaglandi = true;
        } catch (Exception ex) {
            Log.e(TAG, "connect: " + ex.getMessage() );
        }
        Log.i(TAG, "connect: Baglanti islemleri tamamlandi.");
    }

    public void gonder(View v) {
        Log.i(TAG, "gonder: Mesaj gonderimi basladi.");
        if (baglantiSaglandi){
            Log.i(TAG, "gonder: Baglanti onaylandi");
            try {
                String mesaj = gonderilecekMesaj();
                Log.i(TAG, "gonder: Mesaj:" + mesaj);
                bag.mesajGonderByte(gonderilecekHedef(), mesaj);
                Log.i(TAG, "gonder: Mesaj gonderme tamamlandi.");
            } catch  (Exception ex) {
                Log.e(TAG, "gonder: " + ex.getMessage() );
                mesajVer("HATA!");
            }
        }
        else
            mesajVer("Lütfen ilk önce hedef aygıt ile bağlantı sağlayın.");
    }

    private String gonderilecekHedef(){
        return ((EditText) findViewById(R.id.et_hedefIP)).getText().toString();
    }

    private String gonderilecekMesaj(){
        return ((EditText) findViewById(R.id.et_GidecekMesaj)).getText().toString();
    }

    private void mesajVer(String mesaj) {
        Toast.makeText(this, mesaj, Toast.LENGTH_LONG).show();

    }
}
