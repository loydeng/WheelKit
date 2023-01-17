package com.loy.kit.utils;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.RequiresPermission;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.loy.kit.Utils;
import com.loy.kit.constants.Constants;
import com.loy.kit.log.SdkLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Loy
 * @time 2021/5/7 15:09
 * @des
 */
public class LocationUtil {

    /*
    以下 android 原生定位:
    gps  精确定位信息, 室内不可用, 只能室外定位, 相对耗时长耗电大.
    网络  粗略定位信息, 位置不限有网即可, 有百米差距.精度定位到街道问题不大.
    以上基于 LocationManager 只能获取当前的位置的经纬度

    将经纬度转换为具体地址信息, 需要逆地理编码

    Google 使用 Geocoder 类, 可根据经纬度解析出具体位置, 对应Address对象
    部分手机无法使用 Geocoder , 原生定位服务无法使用, 内部没有替换第三方定位服务
    Geocoder 华为手机可用, 小米不可用

    因此在获取不到逆地址编码时, 返回null , 基于第三方web 接口查询. 目前基于高德的web API 接口查询. 使用百度需新增解析方法
    **/

    // 以时间更新, 每5分钟更新一次位置信息
    public static final int DEFAULT_MIN_SECOND = 1000 * 60 *5;

    // 以距离更新, 每变动50米更新一次位置信息
    public static final int DEFAULT_MIN_METERS = 50;

    private static final LocationManager mLocationManager = ServiceManagerUtil.getLocationManager();

    private static final MyLocation sMyLocation = new MyLocation();

    private static final OnLocationChangeListener sListener = new OnLocationChangeListener() {
        @Override
        public void getLastKnownLocation(Location location) {
            updateLocation(location);
        }

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private static final Map<OnLocationChangeListener, MyLocationListener> sOutInnerListenerMap = new HashMap<>();

    public static class MyLocation {
        private double latitude;
        private double longitude;
        private String country;
        private String province;
        private String city;
        private String region;
        private String street;

        public double getLatitude() {
            return latitude;
        }

        public MyLocation setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public double getLongitude() {
            return longitude;
        }

        public MyLocation setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public String getCountry() {
            return country;
        }

        public MyLocation setCountry(String country) {
            this.country = country;
            return this;
        }

        public String getProvince() {
            return province;
        }

        public MyLocation setProvince(String province) {
            this.province = province;
            return this;
        }

        public String getCity() {
            return city;
        }

        public MyLocation setCity(String city) {
            this.city = city;
            return this;
        }

        public String getRegion() {
            return region;
        }

        public MyLocation setRegion(String region) {
            this.region = region;
            return this;
        }

        public String getStreet() {
            return street;
        }

        public MyLocation setStreet(String street) {
            this.street = street;
            return this;
        }
    }

    private static String getGaoDeQueryUri(Location location) {
        String coordinate = location.getLongitude() + "," + location.getLatitude();
        return String.format(Constants.Location.GAO_DE_MAP_QUERY_KEY, coordinate);
    }

    private static String getBaiDuQueryUri(Location location) {
        String coordinate = location.getLatitude() + "," + location.getLongitude();
        return String.format(Constants.Location.BAI_DU_MAP_QUERY_KEY, coordinate);
    }

    private static void updateLocation(Location location) {
        Address address = getAddress(location.getLatitude(), location.getLongitude());
        if (address != null) {
            syncFromAddress(address);
            //AppLog.d("sync location address:" + JsonUtils.toJson(sMyLocation));
        } else {
            if (EmptyUtil.isStringEmpty(Constants.Location.GAO_DE_MAP_QUERY_KEY)) {
                SdkLog.e("use thirdParty parse location failed, " +
                                 "not set query url in Constants Location");
                return;
            }
            ThreadUtil.runOnBackground(() -> {
                try {
                    String queryUri = getGaoDeQueryUri(location);

                    SdkLog.d("queryUri:" + queryUri);

                    URL url = new URL(queryUri);

                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    int timeout = 5000;
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestMethod("GET");

                    NetworkUtil.NetworkCertification allPermitCertification = NetworkUtil.NetworkCertification.getAllPermitCertification();
                    connection.setHostnameVerifier(allPermitCertification.getHostnameVerifier());
                    connection.setSSLSocketFactory(allPermitCertification.getSSLSocketFactory());

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        JsonReader jsonReader = JsonUtil.getGson().newJsonReader(new InputStreamReader(inputStream));

                        syncFromGaoDe(jsonReader);
                        //AppLog.d("sync location AMap:" + JsonUtils.toJson(sMyLocation));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    }

    //TODO
    private static void syncFromBaidu(JsonReader jsonReader) throws IOException {

    }

    /* // https://lbs.amap.com/api/android-location-sdk/locationsummary/
       {
         "status": "1",
         "regeocode": {
           "addressComponent": {
             "city": "杭州市",
             "province": "浙江省",
             "adcode": "330110",
             "district": "余杭区",
             "towncode": "330110005000",
             "streetNumber": {
               "number": "1588号",
               "location": "120.026466,30.292472",
               "direction": "东南",
               "distance": "203.67",
               "street": "余杭塘路"
             },
             "country": "中国",
             "township": "五常街道",
             "businessAreas": [
               []
             ],
             "building": {
               "name": [],
               "type": []
             },
             "neighborhood": {
               "name": [],
               "type": []
             },
             "citycode": "0571"
           },
           "formatted_address": "浙江省杭州市余杭区五常街道聚橙路杭州师范大学仓前校区"
         },
         "info": "OK",
         "infocode": "10000"
       }
    * */
    private static void syncFromGaoDe(JsonReader jsonReader) throws IOException {
        //jsonReader.setLenient(true);
        while (jsonReader.hasNext()) {
            JsonToken jsonToken = jsonReader.peek();
            switch (jsonToken) {
                case BEGIN_OBJECT: // 解析到对象头 {
                    jsonReader.beginObject();
                    break;
                case END_OBJECT: // 解析到对象尾 }
                    jsonReader.endObject();
                    break;
                case BEGIN_ARRAY: // 解析到数组头 [
                    jsonReader.beginArray();
                    break;
                case END_ARRAY: // 解析到数组尾 ]
                    jsonReader.endArray();
                    break;
                case NAME: // 解析到键值对, 键的名称, 后面一定有值, 可能是 json对象, 可能是json数组, 也可能是基础数值, 要消费掉
                    String name = jsonReader.nextName();
                    switch (name) {
                        case "infocode":
                            String info = jsonReader.nextString();
                            if (!"10000".equals(info)) {
                                SdkLog.e("error infocode:" + info);
                            }else {
                                SdkLog.d("info ok");
                            }
                            break;
                        case "regeocode":
                        case "addressComponent":
                        case "streetNumber":
                            jsonReader.beginObject(); // 开始 进入对象, 一定要有对应的 结束对象, 一一对应才能正常解析
                            break;
                        case "country":
                            String country = jsonReader.nextString();
                            sMyLocation.setCountry(country);
                            break;
                        case "province":
                            String province = jsonReader.nextString();
                            sMyLocation.setProvince(province);
                            break;
                        case "city":
                            String city = jsonReader.nextString();
                            sMyLocation.setCity(city);
                            break;
                        case "district":
                            String district = jsonReader.nextString();
                            sMyLocation.setRegion(district);
                            break;
                        case "street":
                            String street = jsonReader.nextString();
                            sMyLocation.setStreet(street);
                            jsonReader.endObject();//当前是最后一个键值对, 退出对象
                            break;
                        case "location":
                            String coordinate = jsonReader.nextString();
                            String[] split = coordinate.split(",");
                            sMyLocation.setLatitude(Double.parseDouble(split[1]));
                            sMyLocation.setLongitude(Double.parseDouble(split[0]));
                            break;
                        case "citycode":
                        case "formatted_address":
                            jsonReader.skipValue(); //会忽略值
                            jsonReader.endObject(); //当前是最后一个键值对, 退出对象
                            break;
                        default:
                            jsonReader.skipValue(); //会忽略值
                            break;
                    }
                    break;
                case END_DOCUMENT: // 解析到末尾
                    jsonReader.close();
                    break;
            }
        }
    }

    private static void syncFromAddress(Address address) {
        //address.getSubAdminArea();//五常街道
        sMyLocation.setCountry(address.getCountryName());
        sMyLocation.setProvince(address.getAdminArea());
        sMyLocation.setCity(address.getLocality());
        sMyLocation.setRegion(address.getSubLocality());
        //address.getFeatureName();//聚橙路
        sMyLocation.setStreet(address.getFeatureName());
        sMyLocation.setLatitude(address.getLatitude());
        sMyLocation.setLongitude(address.getLongitude());
        int maxAddressLineIndex = address.getMaxAddressLineIndex();
        if (maxAddressLineIndex > 0) {
            sMyLocation.setStreet(address.getAddressLine(0));
        }
    }

    private static Criteria getDefaultCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    /**
     * 判断定位是否可用, 判断是否可用不需要定位权限
     * 这里为保证获取到定位信息, 需要网络和gps两个定位提供者都可用
     */
    public static boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) Utils.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 打开Gps设置界面
     */
    public static void openGpsSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        Utils.getAppContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    public static void startLocate() {
        register(DEFAULT_MIN_SECOND, DEFAULT_MIN_METERS, sListener);
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    public static void stopLocate() {
        unregister(sListener);
    }

    /**
     * 注册定位监听器
     * 需要权限
     * 网络 INTERNET
     * 粗略定位 ACCESS_COARSE_LOCATION
     * 精确定位(含粗略定位权限) ACCESS_FINE_LOCATION
     *
     * @param minTime     位置信息更新周期（单位：毫秒）
     * @param minDistance 位置变化最小距离：当位置距离变化超过此值时，将更新位置信息（单位：米）
     *                    以上两个值, 要两个都满足时才回调监听以更新位置.
     *                    若其一为0, 则以另一值为准.
     *                    若都为0则随时更新,但不可取.
     * @param listener    位置刷新的回调接口
     * @return {@code true}: 初始化成功与否
     */
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public static boolean register(long minTime, long minDistance, OnLocationChangeListener listener) {
        if (listener == null) {
            return false;
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            ToastUtil.show("无法定位，请打开网络");
            return false;
        }
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ToastUtil.show("无法定位，请打开GPS定位");
            return false;
        }
        // 当GPS和Network 都可用时, 会时GPS , 但是GPS 室内不可的. 官方提供了比较定位Location优劣的算法, 这里简略处理
        //String provider = mLocationManager.getBestProvider(getCriteria(), true);
        String provider = null;
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.NETWORK_PROVIDER;
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        SdkLog.d("provider: " + provider);

        if (location != null) {
            listener.getLastKnownLocation(location);
        }
        MyLocationListener locationListener = new MyLocationListener(listener);
        sOutInnerListenerMap.put(listener, locationListener);

        mLocationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
        return true;
    }

    /**
     * 注销
     */
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public static void unregister(OnLocationChangeListener listener) {
        MyLocationListener locationListener = sOutInnerListenerMap.remove(listener);
        if (locationListener != null) {
            mLocationManager.removeUpdates(locationListener);
        }
    }


    public static MyLocation getLocation() {
        return sMyLocation;
    }

    /**
     * 根据经纬度获取地理位置
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return {@link Address}
     */
    public static Address getAddress(double latitude, double longitude) {
        if (Geocoder.isPresent()) { //判断当前设备是否内置了地理位置服务
            Geocoder geocoder = new Geocoder(Utils.getAppContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0){
                    return addresses.get(0);
                }
            } catch (IOException e) {
                SdkLog.w("geocoder getFromLocation exception, will use third part api");
            }
        } else {
            SdkLog.d("no Geocoder Server");
        }
        return null;
    }

    private static class MyLocationListener implements LocationListener {
        private final OnLocationChangeListener mListener;

        public MyLocationListener(OnLocationChangeListener listener) {
            this.mListener = listener;
        }

        /**
         * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         */
        @Override
        public void onLocationChanged(Location location) {
            if (mListener != null) {
                mListener.onLocationChanged(location);
            }
        }

        /**
         * provider的在可用、暂时不可用和无服务三个状态直接切换时触发此函数
         *
         * @param provider 提供者
         * @param status   状态
         * @param extras   provider可选包
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (mListener != null) {
                mListener.onStatusChanged(provider, status, extras);
            }
            switch (status) {
                case LocationProvider.AVAILABLE:
                    SdkLog.d("LocationUtils", provider + " 的状态为可见状态");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    SdkLog.d("LocationUtils", provider + "  的状态为服务区外状态");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    SdkLog.d("LocationUtils", provider + " 的状态为暂停服务状态");
                    break;
            }
        }

        /**
         * provider被enable时触发此函数，比如GPS被打开
         */
        @Override
        public void onProviderEnabled(String provider) {
            SdkLog.d("LocationUtils", "onProviderEnabled:" + provider);
        }

        /**
         * provider被disable时触发此函数，比如GPS被关闭
         */
        @Override
        public void onProviderDisabled(String provider) {
            SdkLog.d("LocationUtils", "onProviderDisabled:" + provider);
        }
    }

    public interface OnLocationChangeListener {

        /**
         * 获取最后一次保留的坐标
         *
         * @param location 坐标
         */
        void getLastKnownLocation(Location location);

        /**
         * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         *
         * @param location 坐标
         */
        void onLocationChanged(Location location);

        /**
         * provider的在可用、暂时不可用和无服务三个状态直接切换时触发此函数
         *
         * @param provider 提供者
         * @param status   状态
         * @param extras   provider可选包
         */
        void onStatusChanged(String provider, int status, Bundle extras);//位置状态发生改变
    }


}
