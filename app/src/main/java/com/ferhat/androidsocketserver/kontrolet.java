package com.ferhat.androidsocketserver;

public class kontrolet {

    public static boolean ipAdresiMi(String adres) {
        // buna simdi bi string vercen aga

        // her karaktere tek tek bakarken sayi veya harf mi diye bak
        // degilse return false

        // sonra icinde 3 tane nokta varmi diye bakacan
        // 3'den fazlaysa false

        int noktaAdeti = 0;

        for (int i = 0; i < adres.length();i++){
            // bak simdi...
            char harf = adres.charAt(i); // bunu bi kere cebe koycan

            // ya aslinda ayri ayri if'lerle daha etkili olur

            if (harf == '.')
                noktaAdeti++;
            else if (Character.isDigit(harf))
                continue; //bu sayiysa zaten atla bu karakteri
            else
                return false; //nokta veya sayi disinda bir seyse zaten bastan kaybetti
        } // EndFor

        if (noktaAdeti != 3) // IP adreslerinde her zaman 3 nokta kesin olmak zorunda
            return false;



        // buraya kadar problemsiz gelebildiyse bu kesin IP adresidir
        return true;
    }
}
