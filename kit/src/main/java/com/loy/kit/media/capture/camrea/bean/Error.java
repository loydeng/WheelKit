package com.loy.kit.media.capture.camrea.bean;

import android.hardware.camera2.CameraDevice;

/**
 * @author loy
 * @tiem 2023/7/26 15:45
 * @des
 */
public enum Error {
    UNKNOWN_ERROR(-1,"未知错误"),
    OK(0, "ok"),
    OPEN_FAILURE(-2, "打开相机失败,不具备相机设备或相机被占用或无权限使用或超出资源数量限制等"),
    DEVICE_ERROR(CameraDevice.StateCallback.ERROR_CAMERA_DEVICE, "相机设备故障"),
    STRATEGY_REFUSE(CameraDevice.StateCallback.ERROR_CAMERA_DISABLED, "无相机使用权限"),
    ALREADY_USE(CameraDevice.StateCallback.ERROR_CAMERA_IN_USE,"相机设备正在使用中,无法重复开启"),
    SERVICE_ERROR(CameraDevice.StateCallback.ERROR_CAMERA_SERVICE,"相机服务异常"),
    LIMITED_USE(CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE,"相机设备使用超过数量限制"),
    SESSION_CONFIG_FAILURE(6,"会话配置失败"),
    REQUEST_FAILURE(7,"创建请求失败"),
    SWITCH_FAILURE(11,"没有反向的相机设备"),
    ;
    private final int code;
    private final String message;

    Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static Error getError(int code) {
        for (Error value : Error.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNKNOWN_ERROR;
    }

    public static String codeToMessage(int code) {
        for (Error value : Error.values()) {
            if (value.code == code) {
                return value.message;
            }
        }
        return UNKNOWN_ERROR.message;
    }


    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
