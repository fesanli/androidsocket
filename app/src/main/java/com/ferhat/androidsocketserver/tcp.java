package com.ferhat.androidsocketserver;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.*;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
//import java.util.Date;


public class tcp {
    final int port = 43135;
    String TAG = "c_tcp";
    String hedefIP = "127.0.0.1"; // varsayilan deger olarak localhost verilir ki hata almayayim

    ServerSocket ss;
    Socket s;
    public tcp(){
//        Log.d(TAG, "tcp: Initialize commands running");
//        try {
//            ServerSocket server_socket = new ServerSocket(port);
//            Log.d(TAG, "tcp: ServerSocket olusturuldu");
//
//            while(true) {
//                Socket socket = server_socket.accept();
//                Log.d(TAG, "tcp: baglanti kabul edildi");
//
//
//                OutputStream output = socket.getOutputStream();
//                PrintWriter writer = new PrintWriter(output, true);
//
//                String tarih = new Date().toString();
//                writer.println(tarih);
//                Log.d(TAG, "tcp: 1 satir veri yazildi");
//                Log.d(TAG, "tcp: " + tarih);
//            }
//        }
//        catch (Exception ex) {
//            Log.e(TAG, "tcp (HATA): " + ex.getMessage() );
//            ex.printStackTrace();
//        }
    }

    public void baglan() {
        // client pozisyonunda gecerli bu fonksiyon
        try {
            ExecutorService calistirici = Executors.newFixedThreadPool(3);

            Thread baglantiThread = new Thread(new p_baglan());
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
        Log.i(TAG, "mesajGonder: " + mesajlar.toString());
        try {
            final boolean mesajGonderildi = false;
            ExecutorService calistirici = Executors.newFixedThreadPool(3);
            Thread thGonder = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        PrintWriter pw = new PrintWriter(s.getOutputStream());
                        Log.i(TAG, "run: Yazici nesne olusturuldu");
                        pw.write(mesajlar.get(0));
                        Log.i(TAG, "run: yazilan satir 1:" + mesajlar.get(0));
                        pw.write(mesajlar.get(1));
                        Log.i(TAG, "run: yazilan satir 2:" + mesajlar.get(1));

                        pw.flush();
                        Log.i(TAG, "run: sifon cekildi");
                    } catch (IOException e) {
                        Log.e(TAG, "run: " + e.toString() );
                    }
                }
            });
            calistirici.execute(thGonder);
            Log.i(TAG, "mesajGonder: Thread calistirildi");


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
            pw.println("EOM");
            pw.flush();
        } catch (Exception ex) {
            Log.e(TAG, "mesajGonder: " + ex.getMessage() );
        }
    }
    public void mesajGonder(String hedef, List<String> mesajlar) {
        hedefIP = hedef;
        mesajGonder(mesajlar);
    }

    DatagramSocket dgsocket;
    DatagramPacket paket;
    public void mesajGonderByte(String hedef, String mesaj) {
        /*      Aciklama
        * PrintWriter ile Windows'a gonderdigim mesajlar Wireshark ile test ettigimde
        * Windows'a ulastigini gordugum halde Python bu giden mesajlari hic bir zaman
        * yazdirmadi.
        * Biraz arastirinca Datagram paketleri halinde gondermenin yolunu kesfettim.
        * Bu sekilde alici cihaz da byte cinsinden giden veriyi byte olarak okuyup
        * decode edebilecek.
        *
        * Char tipinden Byte tipine degistirmenin muhtemelen daha kolay yolu vardir.
        * (byte) ch_kume
        * gibi degisim aklima geldi ama denemek istemedim
        * TODO: (Dusuk oncelik) Burasi optimize edilebilir
        * */

        hedefIP = hedef;

        /*
        * mesajin karakter uzunlugu = byte cinsinden buyuklugu
        * ASCII formatinda karakterler 1 byte boyuta sahip*/
        int boyut = mesaj.length();
        try {
            dgsocket = new DatagramSocket();

//            kb olarak isimlendirdigim degisken KB (kilobayt) birimini temsil ediyor
            for (int kb = 0; kb <= mesaj.length() / 1024; kb++) {
                Log.i(TAG, "mesajGonderByte: kb=" + kb);
//                buffer icin kume boyutu
                int kumeBoyutu = (kb*1024)+(boyut%1024);
                byte[] buffer;
                char[] ch_kume = new char[kumeBoyutu];
                mesaj.getChars(kb*1024,kb*1024+kumeBoyutu, ch_kume,0);
                Log.i(TAG, "mesajGonderByte: ch_kume=" + ch_kume.toString());
                buffer = String.valueOf(ch_kume).getBytes();
                Log.i(TAG, "mesajGonderByte: buffer=" + buffer.toString());
                boyut -= ch_kume.length;
                Log.i(TAG, "mesajGonderByte: boyut azaltildi");

                Log.i(TAG, "mesajGonderByte: Hedef INet=" + verInet(hedefIP));
                paket = new DatagramPacket(buffer, buffer.length, verInet(hedefIP), port);

                Log.i(TAG, "mesajGonderByte: buffer.length=" + buffer.length);
                Log.i(TAG, "mesajGonderByte: paket olusturuldu");
//                p_send
                ExecutorService calistirici = Executors.newFixedThreadPool(3);
                Thread baglantiThread = new Thread(new p_send());
                calistirici.execute(baglantiThread);

                Log.i(TAG, "mesajGonderByte: paket soket araciligiyla gonderildi");

                dgsocket.close();
                Log.i(TAG, "mesajGonderByte: soket kapatildi");
                calistirici.shutdown();
                calistirici.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            Log.e(TAG, "mesajGonderByte: " + e.toString() );
        } catch (Exception ex) {
            Log.e(TAG, "mesajGonderByte: " + ex.toString() );
        }

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
        // TODO: (test fonksiyonu) Burada ip kontrolu ekle
        // IP olup olmadığına bak
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
        String cevap = verLocalInet().getHostAddress();
//        try{
//            ExecutorService calistirici = Executors.newCachedThreadPool();
//            Future<InetAddress> gelecekCevap = calistirici.submit(new localHostAl());
//            cevap = gelecekCevap.get().getHostAddress();
//
//        } catch (Exception ex) {
//            Log.e(TAG, "verIP: " + ex.toString() );
//        } finally {
//            return cevap;
//        }
        return cevap;
    }

    public InetAddress verLocalInet(){
        InetAddress cevap = null;
        try{
            ExecutorService calistirici = Executors.newCachedThreadPool();
            Future<InetAddress> gelecekCevap = calistirici.submit(new p_verLocalHost());
            cevap = gelecekCevap.get();

        } catch (Exception ex) {
            Log.e(TAG, "verLocal: " + ex.toString() );
        } finally {
            return cevap;
        }
    }

    public InetAddress verInet(String ip) throws UnknownHostException {

        InetAddress cevap;
        Log.i(TAG, "verInet: arg(ip)=" + ip );
        try {
            ExecutorService calistirici = Executors.newCachedThreadPool();
            Future<InetAddress> gelecekCevap = calistirici.submit(new p_verInet());
            cevap = gelecekCevap.get();


            return cevap;
        } catch (Exception e) {
            Log.e(TAG, "verInet: " + e.toString() );
        }
        return null;
    }

    public class p_baglan implements Runnable{
        public void run(){
            try {
                s = new Socket(hedefIP, port);
            } catch (Exception ex){
                Log.e(TAG, "run: " + ex.toString() );
            }
        }
    }

    public class p_send implements Runnable {
        public void run() {
            try {
                dgsocket.send(paket);
            } catch (IOException e) {
                Log.e(TAG, "p_send: run: " + e.toString());
            }
        }
    }

    public class p_verLocalHost implements Callable<InetAddress> {
        private InetAddress cevap;
        public InetAddress call() {
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

                        cevap = inet_addr;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "logTumIP: " + ex.toString() );
            }
            return cevap;
        }
    }
    public class p_verInet implements Callable<InetAddress> {
        private InetAddress cevap;
        public  InetAddress call() {
            try {
                cevap = InetAddress.getByName(hedefIP);
            } catch (Exception e) {
                Log.e(TAG, "call: " + e.toString() );
            }
            return cevap;
        }
    }
}
