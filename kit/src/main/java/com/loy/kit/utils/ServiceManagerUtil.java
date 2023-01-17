package com.loy.kit.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.job.JobScheduler;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.loy.kit.Utils;

/**
 * @author Loy
 * @time 2022/8/19 8:59
 * @des
 */
public class ServiceManagerUtil {

    public enum Service{
        Activity(Context.ACTIVITY_SERVICE),
        Package("package"),
        Window(Context.WINDOW_SERVICE),
        Camera(Context.CAMERA_SERVICE),
        Audio(Context.AUDIO_SERVICE),
        Sensor(Context.SENSOR_SERVICE),
        Vibrator(Context.VIBRATOR_SERVICE),
        Alarm(Context.ALARM_SERVICE),
        Power(Context.POWER_SERVICE),
        Wifi(Context.WIFI_SERVICE),
        Bluetooth(Context.BLUETOOTH_SERVICE),
        NFC(Context.NFC_SERVICE),
        Connectivity(Context.CONNECTIVITY_SERVICE),
        Clipboard(Context.CLIPBOARD_SERVICE),
        Location(Context.LOCATION_SERVICE),
        TelePhony(Context.TELEPHONY_SERVICE),
        NetworkStats(Context.NETWORK_STATS_SERVICE),
        JobScheduler(Context.JOB_SCHEDULER_SERVICE),
        ;
        private final String name;

        Service(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static Object getServiceManager(Service service) {
        if (service == Service.Package) {
            return Utils.getAppContext().getPackageManager();
        }else {
            return Utils.getAppContext().getSystemService(service.name);
        }
    }

    public static ActivityManager getActivityManager() {
        return (ActivityManager) getServiceManager(Service.Activity);
    }

    public static PackageManager getPackageManager() {
        return Utils.getAppContext().getPackageManager();
    }

    public static WindowManager getWindowManager() {
        return (WindowManager) getServiceManager(Service.Window);
    }

    public static WifiManager getWifiManager() {
        return (WifiManager) getServiceManager(Service.Wifi);
    }

    public static ClipboardManager getClipboardManager() {
        return (ClipboardManager) getServiceManager(Service.Clipboard);
    }

    public static LocationManager getLocationManager() {
        return (LocationManager) getServiceManager(Service.Location);
    }

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getServiceManager(Service.Connectivity);
    }

    public static TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getServiceManager(Service.TelePhony);
    }

    public static Vibrator getVibrator() {
        return (Vibrator) getServiceManager(Service.Vibrator);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static NetworkStatsManager getNetworkStatsManager() {
        return (NetworkStatsManager) getServiceManager(Service.NetworkStats);
    }

    public static AudioManager getAudioManager() {
        return (AudioManager) getServiceManager(Service.Audio);
    }

    public static CameraManager getCameraManager() {
        return (CameraManager) getServiceManager(Service.Camera);
    }

    public static SensorManager getSensorManager() {
        return (SensorManager) getServiceManager(Service.Sensor);
    }

    public static AlarmManager getAlarmManager() {
        return (AlarmManager) getServiceManager(Service.Alarm);
    }

    public static PowerManager getPowerManager() {
        return (PowerManager) getServiceManager(Service.Power);
    }

    public static BluetoothManager getBluetoothManager() {
        return (BluetoothManager) getServiceManager(Service.Bluetooth);
    }

    public static NfcManager getNfcManager() {
        return (NfcManager) getServiceManager(Service.NFC);
    }

    public static JobScheduler getJobScheduler() {
        return (JobScheduler) getServiceManager(Service.JobScheduler);
    }
}