package com.android.victory.schedule.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.android.victory.schedule.activity.LoginActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by PCJ on 1/9/2018.
 */

public class Model {
    private static final Model ourInstance = new Model();

    public static Model getInstance() {
        return ourInstance;
    }

    private Model() {

    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public String getFilePath(){
        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Backup/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return directory_path;
    }

    public SSLContext getSSLContext(Context mContext){
        try{
            // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream inputStream = mContext.getResources().getAssets().open("site.cer");
            InputStream caInput = new BufferedInputStream(inputStream);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String username = "",comp = "", password = "", nickname = "",nickcode = "";
    public boolean isVailable = true;
    public boolean isEnglish = true;
    public ArrayList<Schedule> scheduleArrayList = new ArrayList<>();
    public boolean isFlag = false;
    public Schedule hasEditedSchedule;
    public int localPeriodTime = 7200000;//Default is 7200s
    public String displayMode = "3";


}
