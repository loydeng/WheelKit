package com.loy.kit.constants;

import com.loy.kit.log.core.Level;
import com.loy.kit.log.core.Logger;

/**
 * @author Loy
 * @time 2022/8/18 17:43
 * @des
 */
public interface Constants {
    interface Log {
        String GLOBlE_TAG = "kit-logger";
        @Level
        int LEVEL = Logger.D;
        String LOG_DIR = "log";
        String CRASH_DIR = "crash";
        String LOG_PREFIX = "record";
        String CRASH_PREFIX = "crash";
        String LOG_FILE_EXTENSION = "log";

        int STACK_TRACE_DEPTH = 5;

        // 系统日志打印最大长度限制为4K, 超出部分不会打印, 这个作为分段输出的日志单元大小
        int MAX_LEN = 1024 * 3;

        String UPLOAD_URL = ""; // TODO 默认上传地址
    }

    interface Location{
        // 高德地图定位 参考地址 https://lbs.amap.com/api/android-location-sdk/locationsummary/
        // 逆地址编码查询接口, 每日 300000 次, location值为经度纬度,逗号分隔,如 location=120.025157,30.293913
        String GAO_DE_MAP_QUERY_KEY =
                "https://restapi.amap.com/v3/geocode/regeo?key=bcebd0d84bc0ee3b55820a3caf253b6b&location=%s";

        // 百度地图定位 参考地址 https://lbsyun.baidu.com/index.php?title=android-locsdk
        // 逆地址编码查询接口, 每日 6000 次, location值为纬度经度,逗号分隔,如 location=30.292472,120.026466
        String BAI_DU_MAP_QUERY_KEY =
                "http://api.map.baidu.com/reverse_geocoding/v3/?ak=6VdOOGEF8RWNXHB1hZZzz7NeeRKfE4CY&output=json&coordtype=wgs84ll&location=%s";

    }
}
