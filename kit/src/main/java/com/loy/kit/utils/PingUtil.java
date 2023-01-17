package com.loy.kit.utils;

import static com.loy.kit.utils.ShellUtil.RETURN_ERR;
import static com.loy.kit.utils.ShellUtil.RETURN_OK;

import java.util.List;

/**
 * @author Loy
 * @time 2022/8/19 15:57
 * @des
 */
public class PingUtil {
    // 默认 发送4次数据包, 超时时间为 1s
    public static final String PING_TEMPLATE = "ping -c 4 -W 1 %s";

    // ping 常见错误, 未知域名或IP
    public static final String UNKNOWN_HOST = "unknown host";

    // ping 参数错误, 域名 或 IP 格式非法
    public static final String INVALID_ARG = "Invalid argument";

    // ping 不通的默认值
    public static final String BLOCK_VALUE = "0";

    // percent
    public static final String PERCENT = "%";

    public static String getPingCmd(String url) {
        return String.format(PING_TEMPLATE, url);
    }

    public static PingResult ping(String url) {
        PingResult result = new PingResult().setMaxRTT(BLOCK_VALUE).setAvgRTT(BLOCK_VALUE).setMinRTT(BLOCK_VALUE);
        ShellUtil.CommandResult commandResult = ShellUtil.execCmd(new String[]{getPingCmd(url)}, false, true);

        if (commandResult.result == RETURN_OK) {
            result.setCode(RETURN_OK);
            List<String> resultLines = commandResult.successMsgLines;

            // 4 packets transmitted, 4 received, 0% packet loss, time 3020ms
            String firstStatistics = resultLines.get(resultLines.size() - 2);

            String[] count = firstStatistics.split(",");
            String lossRate = count[2].trim().split(" +")[0].trim();
            int end = lossRate.indexOf(PERCENT);
            String lossRateValue = lossRate.substring(0, end);
            if ("100%".equalsIgnoreCase(lossRate)) {
                return result.setLossRate(lossRateValue)
                             .setCode(RETURN_ERR)
                             .setErrorMessage("数据丢失率:" + lossRate);
            } else {
                result.setLossRate(lossRateValue);
            }

            //  rtt min/avg/max/mdev = 36.190/52.332/64.351/10.178 ms
            String secondStatistics = resultLines.get(resultLines.size() - 1);
            String[] kv = secondStatistics.split("=");
            String[] data = kv[1].split("/");
            result.setMinRTT(data[0].trim());
            result.setAvgRTT(data[1].trim());
            result.setMaxRTT(data[2].trim());
        } else {
            result.setCode(RETURN_ERR);
            List<String> errorMsg = commandResult.errorMsgLines;
            if (errorMsg.contains(UNKNOWN_HOST)) {
                result.setErrorMessage("未知域名: " + url);
            } else if (errorMsg.contains(INVALID_ARG)) {
                result.setErrorMessage("参数非法: " + url);
            } else {
                result.setErrorMessage(errorMsg.toString());
            }
        }
        return result;
    }

    //rtt min/avg/max/mdev = 36.190/52.332/64.351/10.178 ms
    //mdev 就是 Mean Deviation 的缩写，它表示这些 ICMP 包的 RTT 偏离平均值的程度，这个值越大说明你的网速越不稳定
    public static class PingResult {
        private int code;
        private String lossRate;
        private String minRTT;
        private String avgRTT;
        private String maxRTT;
        private String errorMessage;

        public PingResult() {
        }

        public int getCode() {
            return code;
        }

        public PingResult setCode(int code) {
            this.code = code;
            return this;
        }

        public String getLossRate() {
            return lossRate;
        }

        public PingResult setLossRate(String lossRate) {
            this.lossRate = lossRate;
            return this;
        }

        public String getMinRTT() {
            return minRTT;
        }

        public PingResult setMinRTT(String minRTT) {
            this.minRTT = minRTT;
            return this;
        }

        public String getAvgRTT() {
            return avgRTT;
        }

        public PingResult setAvgRTT(String avgRTT) {
            this.avgRTT = avgRTT;
            return this;
        }

        public String getMaxRTT() {
            return maxRTT;
        }

        public PingResult setMaxRTT(String maxRTT) {
            this.maxRTT = maxRTT;
            return this;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public PingResult setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        @Override
        public String toString() {
            return "PingResult{" +
                    "code=" + code +
                    ", minRTT='" + minRTT + '\'' +
                    ", avgRTT='" + avgRTT + '\'' +
                    ", maxRTT='" + maxRTT + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }

}
