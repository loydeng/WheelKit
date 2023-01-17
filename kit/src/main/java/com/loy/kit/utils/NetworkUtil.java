package com.loy.kit.utils;


import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;

import androidx.annotation.RequiresPermission;

import com.loy.kit.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkUtil {

    public enum NetworkType {
        NETWORK_WIFI("wifi"),
        NETWORK_5G("5g"),
        NETWORK_4G("4g"),
        NETWORK_3G("3g"),
        NETWORK_2G("2g"),
        NETWORK_UNKNOWN("未知网络"),
        NETWORK_NO("无网络连接");

        private final String displayName;

        NetworkType(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private NetworkUtil() {
    }

    public static void openWirelessSettings() {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.getAppContext().startActivity(intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.getAppContext().startActivity(intent);
        }
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = ServiceManagerUtil.getConnectivityManager();
        return cm.getActiveNetworkInfo();
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isAvailable() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isWifiConnected() {
        ConnectivityManager cm = ServiceManagerUtil.getConnectivityManager();
        return cm != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static String getNetworkOperatorName() {
        TelephonyManager tm = ServiceManagerUtil.getTelephonyManager();
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static NetworkType getNetWorkType() {
        NetworkInfo info = getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NetworkType.NETWORK_2G;

                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NetworkType.NETWORK_3G;

                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NetworkType.NETWORK_4G;

                    case TelephonyManager.NETWORK_TYPE_NR:
                        return NetworkType.NETWORK_5G;
                    default:
                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            return NetworkType.NETWORK_3G;
                        } else {
                            return NetworkType.NETWORK_UNKNOWN;
                        }
                }
            } else {
                return NetworkType.NETWORK_UNKNOWN;
            }
        }
        return NetworkType.NETWORK_NO;
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    public static String getIpAddressByWifi() {
        @SuppressLint("WifiManagerLeak")
        WifiManager wm = ServiceManagerUtil.getWifiManager();
        if (wm == null)
            return "";
        return Formatter.formatIpAddress(wm.getDhcpInfo().ipAddress);
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    public static String getGatewayByWifi() {
        @SuppressLint("WifiManagerLeak")
        WifiManager wm = ServiceManagerUtil.getWifiManager();
        if (wm == null)
            return "";
        return Formatter.formatIpAddress(wm.getDhcpInfo().gateway);
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    public static String getServerAddressByWifi() {
        @SuppressLint("WifiManagerLeak")
        WifiManager wm = ServiceManagerUtil.getWifiManager();
        if (wm == null)
            return "";
        return Formatter.formatIpAddress(wm.getDhcpInfo().serverAddress);
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    public static String getSSID() {
        WifiManager wm = ServiceManagerUtil.getWifiManager();
        if (wm == null)
            return "";
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null)
            return "";
        String ssid = wi.getSSID();
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        if (ssid.length() > 2 && ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"') {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static boolean isRegistered(NetworkStatusChangedListener listener) {
        return NetworkChangedReceiver.getInstance().isRegistered(listener);
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean registerNetworkStateChangeListener(NetworkStatusChangedListener listener) {
        return NetworkChangedReceiver.getInstance().register(listener);
    }

    public static boolean unregisterNetworkStateChangeListener(NetworkStatusChangedListener listener) {
        return NetworkChangedReceiver.getInstance().unregister(listener);
    }

    public interface NetworkStatusChangedListener {
        void onDisconnected();

        void onConnected(NetworkType networkType);
    }

    public static class NetworkChangedReceiver extends BroadcastReceiver {

        private static class Holder {
            private static final NetworkChangedReceiver INSTANCE = new NetworkChangedReceiver();
        }

        public static NetworkChangedReceiver getInstance() {
            return Holder.INSTANCE;
        }

        private NetworkType mNetworkType;
        private final Set<NetworkStatusChangedListener> mListeners = new HashSet<>();

        boolean isRegistered(NetworkStatusChangedListener listener) {
            return listener != null && mListeners.contains(listener);
        }

        @RequiresPermission(ACCESS_NETWORK_STATE)
        boolean register(NetworkStatusChangedListener listener) {
            boolean ret = (listener != null && !mListeners.contains(listener));
            if (ret) {
                ThreadUtil.runOnUIThread(() -> {
                    mListeners.add(listener);
                    if (mListeners.size() == 1) {
                        mNetworkType = getNetWorkType();
                        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                        Utils.getAppContext().registerReceiver(Holder.INSTANCE, filter);
                    }
                });
            }
            return ret;
        }

        boolean unregister(NetworkStatusChangedListener listener) {
            boolean ret = (listener != null && mListeners.contains(listener));
            if (ret) {
                ThreadUtil.runOnUIThread(() -> {
                    mListeners.remove(listener);
                    if (mListeners.size() == 0) {
                        Utils.getAppContext().unregisterReceiver(Holder.INSTANCE);
                    }
                });
            }
            return ret;
        }

        @RequiresPermission(ACCESS_NETWORK_STATE)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ThreadUtil.postDelayOnUI(() -> {
                    NetworkType type = getNetWorkType();
                    if (mNetworkType != type) {
                        mNetworkType = type;
                        if (type == NetworkType.NETWORK_NO) {
                            for (NetworkStatusChangedListener listener : mListeners) {
                                listener.onDisconnected();
                            }
                        } else {
                            for (NetworkStatusChangedListener listener : mListeners) {
                                listener.onConnected(type);
                            }
                        }
                    }
                }, 1000);
            }
        }
    }

    public static class NetworkCertification {
        private final X509TrustManager mTrustManager;
        private final SSLSocketFactory mSSLSocketFactory;
        private final HostnameVerifier mHostnameVerifier;

        private static NetworkCertification mAllPermitCertification;

        private NetworkCertification(X509TrustManager trustManager, SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
            this.mTrustManager = trustManager;
            this.mSSLSocketFactory = sslSocketFactory;
            this.mHostnameVerifier = hostnameVerifier;
        }

        public X509TrustManager getTrustManager() {
            return mTrustManager;
        }

        public SSLSocketFactory getSSLSocketFactory() {
            return mSSLSocketFactory;
        }

        public HostnameVerifier getHostnameVerifier() {
            return mHostnameVerifier;
        }

        // 信任所有证书
        public static NetworkCertification getAllPermitCertification() {
            if (mAllPermitCertification == null) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    X509TrustManager x509TrustManager = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        x509TrustManager = new X509ExtendedTrustManager() {
                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {/**/}

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {/**/}

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {/**/}

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {/**/}

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {/**/}

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {/**/}
                        };
                    }else {
                        x509TrustManager = new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        };
                    }
                    sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
                    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    };
                    mAllPermitCertification = new NetworkCertification(x509TrustManager, socketFactory, hostnameVerifier);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
            }

            return mAllPermitCertification;
        }

        // 信任自定义的证书
        public static NetworkCertification getCustomCertification(InputStream caInputStream, String... ips) {
            NetworkCertification ret = null;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca = cf.generateCertificate(caInputStream);
                caInputStream.close();

                // Create a KeyStore containing the trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                TrustManager[] trustManagers = tmf.getTrustManagers();
                SSLContext tls = SSLContext.getInstance("TLS");
                tls.init(null, trustManagers, null);
                HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        List<String> list = Arrays.asList(ips);
                        return list.contains(hostname);
                    }
                };
                ret = new NetworkCertification((X509TrustManager) trustManagers[0], tls.getSocketFactory(), hostnameVerifier);
            } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }
}