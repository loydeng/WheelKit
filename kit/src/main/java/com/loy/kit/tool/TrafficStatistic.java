package com.loy.kit.tool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import com.loy.kit.log.SdkLog;
import com.loy.kit.utils.AppUtil;
import com.loy.kit.utils.ServiceManagerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;

/**
 * 网络流量统计
 * @author loyde
 * @des
 * @time 2022/12/3 21:50
 */
public class TrafficStatistic {

    private static final String SEND_FILE_PATH_TEMPLATE = "/proc/uid_stat/%d/tcp_snd";
    private static final String RECV_FILE_PATH_TEMPLATE = "/proc/uid_stat/%d/tcp_rcv";

    public static class Stats {
        public final long sendBytes;
        public final long receiveBytes;

        public Stats(long sendBytes, long receiveBytes) {
            this.sendBytes = sendBytes;
            this.receiveBytes = receiveBytes;
        }
    }

    // 所有应用总计流量, TrafficStats可能获取不到, 与手机有关
    public static Stats getTotalStatsByTraffic() {
        return new Stats(TrafficStats.getTotalTxBytes(), TrafficStats.getTotalRxBytes());
    }

    // 指定 uid 应用总计流量, TrafficStats可能获取不到, 与手机有关
    public static Stats getCurrentStatsByTraffic() {
        int uid = AppUtil.getAppUid();
        return new Stats(TrafficStats.getUidTxBytes(uid), TrafficStats.getUidRxBytes(uid));
    }


    // 兼容性不好
    public static Stats getCurrentStatsByShell() {
        int uid = AppUtil.getAppUid();
        File sendFile = new File(String.format(SEND_FILE_PATH_TEMPLATE, uid));
        File recvFile = new File(String.format(RECV_FILE_PATH_TEMPLATE, uid));
        if (!sendFile.exists() || !recvFile.exists()) {
            // maybe use /proc/net/xt_qtaguid/stats
            SdkLog.color(TrafficStatistic.class.getSimpleName(), "getCurrentStatsByShell failed");
        } else {
            try (BufferedReader sendReader = new BufferedReader(new FileReader(sendFile));
                 BufferedReader recvReader = new BufferedReader(new FileReader(recvFile))) {
                String sendLine, recvLine;
                while (((sendLine = sendReader.readLine()) != null) &&
                        ((recvLine = recvReader.readLine()) != null)) {
                    long sendBytes = Long.parseLong(sendLine);
                    long recvBytes = Long.parseLong(recvLine);
                    return new Stats(sendBytes, recvBytes);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Stats(0, 0);
    }

    @IntDef({NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_BLUETOOTH,
            NetworkCapabilities.TRANSPORT_ETHERNET,
            NetworkCapabilities.TRANSPORT_VPN})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }


    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Stats getStatsByNetworkStats(@Type int type, long startTime, long endTime) {
        int uid = AppUtil.getAppUid();
        TelephonyManager telephonyManager = ServiceManagerUtil.getTelephonyManager();
        @SuppressLint("MissingPermission")
        String subscriberId = telephonyManager.getSubscriberId();
        NetworkStatsManager networkStatsManager = ServiceManagerUtil.getNetworkStatsManager();
        NetworkStats networkStats = networkStatsManager.queryDetailsForUid(type, subscriberId, startTime, endTime, uid);
        long sendBytes = 0, recvBytes = 0;
        if (networkStats != null) {
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                sendBytes += bucket.getTxBytes();
                recvBytes += bucket.getRxBytes();
            }
        }
        return new Stats(sendBytes, recvBytes);
    }

    // 获取今天的起始时间
    public static long getTodayStartTime() {
        Calendar cal = Calendar.getInstance();
        clearBelowHourTime(cal);
        return cal.getTimeInMillis();
    }

    // 获取当前周的周一起始时间
    public static long getWeekStartTime() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        clearBelowHourTime(cal);
        return cal.getTimeInMillis();
    }

    // 获取当前月的第一天起始时间
    public static long getMonthStartTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        clearBelowHourTime(cal);
        return cal.getTimeInMillis();
    }

    // 清空为今天起始点
    private static void clearBelowHourTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
