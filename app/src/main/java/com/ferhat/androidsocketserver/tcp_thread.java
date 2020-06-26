package com.ferhat.androidsocketserver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
//import java.util.Date;


public class tcp_thread implements Runnable {
    int port = 43135;
    String TAG = "c_tcp";
    String hedefIP = "127.0.0.1"; // varsayilan deger olarak localhost verilir ki hata almayayim

    ServerSocket ss;
    Socket s;

//    ANAHTARLAR
    boolean neredeyim, gonder, dinle;
//    ANAHTARLAR
    public tcp_thread(String... parametreler){
        neredeyim = gonder = dinle = false;

        switch (parametreler[0]) {
            case "neredeyim":
//                yerel agdaki IP adresini ver
//                verIP()
                neredeyim = true;
                break;
            case "gonder":
//                mesaj gonder
                break;
            case "dinle":
//                port'u dinlemeye al
                break;
            default:
//                bilinmeyen parametre hatasi
                break;
        }
    }

    public void run(){

    }

    public void baglan() {
        // client pozisyonunda gecerli bu fonksiyon
        try {
            ExecutorService calistirici = Executors.newFixedThreadPool(3);

            Thread baglantiThread = new Thread(new runBaglan());
            calistirici.execute(baglantiThread);
            calistirici.shutdown();
            calistirici.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Log.e(TAG, "baglan: " + ex.toString() );
        }
    }
    public void baglan(String hedef) {
        hedefIP = hedef;
        baglan();
    }

    // TODO: mesajGonder(String hedef, String mesaj) ekle
    public List<String> dinle(){
        List<String> mesajlar = new ArrayList<String>();
        try {
            // kullanim sirasinda yeni sinif tanimlanacagi icin ServerSocket nesnesi global
            // global uzerinde degisiklik yapilarak tekrar tekrar nesne olusturmaya gerek kalmiyor
            ss = new ServerSocket(port, 0, InetAddress.getLocalHost());

            Log.i(TAG, "dinle: Localhost IP: " + InetAddress.getLocalHost().getHostAddress());

            // acilan port'tan baglanti istegi kabul ediliyor
            s = ss.accept();

            // beslemenin tamamini alacak nesne
            InputStreamReader in = new InputStreamReader(s.getInputStream());

            // beslemeyi parca parca alacak nesne
            BufferedReader br = new BufferedReader(in);

            String satir = "";
            do {
                satir = br.readLine();
                mesajlar.add(satir);
            } while(satir != "EOM"); // End Of Message (ben uydurdum)
            // mesaj gonderici mesaj gondermeyi bitirirken EOM satiri da EKLEMELI
            // bunu gonderici fonksiyona eklemek en mantiklisi


        } catch (Exception ex) {
            Log.e(TAG, "kurServer: " + ex.getMessage() );
            mesajlar.add("HATA");
        }

        return mesajlar;
    }

    // Global
    List<String> mesajlar;
    // Global
    public void mesajGonder(String mesaj){
        // mesaj alicisini hedefIP'den (global) al
        baglan(hedefIP);

        // gidecek mesajlar bunlar
        mesajlar = new ArrayList<String>();

        mesajlar.add(mesaj);
        mesajlar.add("EOM"); // mesaj gonderiminin sonuna gelindigin karsi tarafa bildir

        try {
            ExecutorService calistirici = Executors.newFixedThreadPool(3);
            Thread thGonder = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        PrintWriter pw = new PrintWriter(s.getOutputStream());
                        pw.println(mesajlar.get(0));
                        pw.println(mesajlar.get(1));

                        pw.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "run: " + e.toString() );
                    }
                }
            });
            calistirici.execute(thGonder);


        } catch (Exception ex) {
            Log.e(TAG, "mesajGonder: " + ex.toString() );
        }
    }
    public void mesajGonder(List<String> mesajlar){
        baglan(hedefIP);
        try {
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            for (String mesaj : mesajlar)
                pw.println(mesaj);
            pw.flush();
        } catch (Exception ex) {
            Log.e(TAG, "mesajGonder: " + ex.getMessage() );
        }
    }
    public void mesajGonder(String hedef, List<String> mesajlar) {
        hedefIP = hedef;
        mesajGonder(mesajlar);
    }

    public int test_dinle() {
        // baglanti alinirsa 1
        // hata alinirsa 0

        try {
            dinle();


            return 1;
        }
        catch (Exception ex) {
            Log.e(TAG, "test_dinle: " + ex.getMessage() );
            return 0;
        }
    }
    public int test_baglan(String hedef) {
        // TODO: Burada ip kontrolu ekle
        hedefIP = hedef;
        try {
            s = new Socket(hedefIP, port);
            return 1;
        }
        catch (Exception ex) {
            Log.e(TAG, "test_baglan: " + ex.getMessage() );
            return 0;
        }
    }

    public String verIP(){
        String cevap = "tcp";
        try{
            ExecutorService calistirici = Executors.newCachedThreadPool();
            Future<String> gelecekCevap = calistirici.submit(new localIPver());
            cevap = gelecekCevap.get();

        } catch (Exception ex) {
            Log.e(TAG, "verIP: " + ex.toString() );
        } finally {
            return cevap;
        }
    }

    public class runBaglan implements Runnable{
        public void run(){
            try {
                s = new Socket(hedefIP, port);
            } catch (Exception ex){
                Log.e(TAG, "run: " + ex.toString() );
            }
        }
    }

    public class localIPver implements Callable<String> {
        private String cevap = "";
        public String call() throws Exception{
            try {
                //          Burasi sadece 127.0.0.1 cevirebiliyor
                /*Log.i(TAG, "thread: try'a girdi");
                // burada hata veriyor cunku calisan thread'e baglanmaya calisirken bekleme yaptirabilir
                // Bu yuzden local IP'yi vermiyor bu alttaki satir
                // thread tanimlamak ilerisi icin en mantikli secim
                // TODO: Local IP'yi ver, problemi coz
                InetAddress inet = InetAddress.getLocalHost(); // HATA
                Log.i(TAG, "thread: inet olustu");
                Log.i(TAG, "thread: cevap:" + cevap);
                cevap = inet.getHostAddress();
                Log.i(TAG, "thread: " + cevap);*/
                ///////////////////////////////

                //          verIPV4 calisan tek yontem
                cevap = verIPV4();
            }
            catch (Exception ex) {
                Log.e(TAG, "thread: bilinmeyen hata");
                Log.e(TAG, "thread: " + ex.toString());
            }
            finally {
                return cevap;
            }

        }
        public String verIPV4(){
            String cevap = "";
            try {
                for (
                        final Enumeration<NetworkInterface> interfaces =
                        NetworkInterface.getNetworkInterfaces();
                        interfaces.hasMoreElements();
                ) {
                    final NetworkInterface cur = interfaces.nextElement();

                    if (cur.isLoopback()) {
                        continue;
                    }



                    for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
                        final InetAddress inet_addr = addr.getAddress();

                        if (!(inet_addr instanceof Inet4Address)) {
                            continue;
                        }
                        Log.i(TAG,"interface " + cur.getName());
                        Log.i(TAG,
                                "  address: " + inet_addr.getHostAddress() +
                                        "/" + addr.getNetworkPrefixLength()
                        );

                        Log.i(TAG,
                                "  broadcast address: " +
                                        addr.getBroadcast().getHostAddress()
                        );

                        cevap = inet_addr.getHostAddress();
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "logTumIP: " + ex.toString() );
            }
            return cevap;
        }

    }

}
