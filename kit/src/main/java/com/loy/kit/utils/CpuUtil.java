package com.loy.kit.utils;

import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * CPU 工具
 * 读取 cpu 的使用率,暂时不可用, 原因如下
 * Android 26 及以上, 只有系统应用可以获得访问 /proc/stat 文件权限
 * 其他 命令内部基于此文件实现, 因此普通用户无法使用, 文件流 或 sh命令,读取cpu使用时间
 *
 * @author Loy
 * @time 2021/3/30 10:23
 * @des
 */
public class CpuUtil {

    /*// cpu 使用时间状态文件, 是开机到当前时间段的使用情况统计
    public static final String CAT_CPU_STAT = "cat /proc/stat";

    // sh 下权限拒绝, shell 不可执行
    public static final String DUMPSYS_CPU_INFO = "dumpsys cpuinfo";

    // 返回参数无效, 内部基于 /proc/stat
    public static final String TOP_CPU = "top -n 1 | grep -i cpu";

    public static final String TOP_CPU_PROCESS = "top -n 1 | grep -i %s";

    // 默认统计时间段
    public static final int INTERVAL_TIME = 360;*/

    private static boolean isDouble(String str) {
        boolean isHasDot = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c)) {
                if (!isHasDot && '.' == c) {
                    isHasDot = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    // 获取 CPU 使用率, 返回数组长度为2, 第一个是系统 cpu 的使用率, 第二个是应用cpu的使用率
    // 系统cpu使用率 android 8 及以上不可获取时, 其值为0
    public static String[] getCpuUsage() {
        String[] ret = new String[]{"0","0"};
        String cpuCoreFlag = "%cpu";
        String pid = String.valueOf(android.os.Process.myPid());
        String cmd = "top -n 1";
        ShellUtil.CommandResult commandResult = ShellUtil.execCmd(new String[]{cmd}, false, true);
        if (commandResult.result == 0) {
            Iterator<String> iterator = commandResult.successMsgLines.iterator();
            String line;
            String totalLine = null;
            String targetLine = null;
            while (iterator.hasNext()) {
                line = iterator.next();
                if (line.contains(cpuCoreFlag)) {
                    totalLine = line.trim();
                } else if (line.contains(pid)) {
                    targetLine = line.trim();
                    break;
                }
            }

            if (targetLine != null && totalLine != null) {
                //800%cpu   0%user   0%nice   0%sys 800%idle   0%iow   0%irq   0%sirq   0%host
                String[] arr1 = totalLine.split(" +");
                String[] arr2 = arr1[3].split("%");
                ret[0] = arr2[0];
                String[] arr3 = arr1[0].split("%");
                double total = Double.parseDouble(arr3[0]);
                String[] arr = targetLine.split(" +");

                // PID  USER         PR  NI VIRT  RES  SHR S[%CPU] %MEM     TIME+ ARGS
                //22347 u0_a746      10 -10 4.3G 127M  91M S 56.0   2.2   0:01.47 com.cmcc.hy.wif+
                double appCpuUsage = 0;
                if (isDouble(arr[8])) {
                   appCpuUsage =  Double.parseDouble(arr[8]);
                }else {
                    //SdkLog.e("str parse double err, str:" + arr[8]);
                }
                double percentUsage = appCpuUsage / total * 100;
                DecimalFormat df = new DecimalFormat("#.00");
                ret[1] = df.format(percentUsage);
            }
        }
        return ret;
    }

    /*public static String getCpuUsageByTop() {
        int pid = android.os.Process.myPid();
        //String cmd = String.format(TOP_CPU_PROCESS, pid);
        String cmd = "top -n 1";
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(new String[]{cmd}, false, true);
        if (commandResult.result == 0) {
            List<String> results = commandResult.successMsgLines;
            //800%cpu   9%user   3%nice  15%sys 773%idle   0%iow   0%irq   0%sirq   0%host
            String firstLine = results.get(0);

            String[] split = firstLine.split(" +");
            String cpu = null;
            String idle = null;
            for (String s : split) {
                String[] kv = s.split("%");
                if ("cpu".equalsIgnoreCase(kv[1])) {
                    cpu = kv[0];
                }
                if ("idle".equalsIgnoreCase(kv[1])) {
                    idle = kv[0];
                    break;
                }
            }
            if (cpu != null & idle != null) {
                double total = Double.parseDouble(cpu);
                double free = Double.parseDouble(idle);
                double cpuUsagePercent = (total - free) / total * 100;
                return String.valueOf(cpuUsagePercent);
            }
        }
        return "0";
    }


    public static String getCpuUsageByDump() {
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(new String[]{DUMPSYS_CPU_INFO}, false, true);
        if (commandResult.result == 0) {
            List<String> results = commandResult.successMsgLines;
            String lastLine = results.get(results.size() - 1);
            return lastLine.split(" +")[0];
        }
        return "0";
    }

    public static double readUsage() throws Exception {
        return readUsage(INTERVAL_TIME);
    }

    private static String getCPUStateFirstLine() throws IOException {
        String firstLine = null;
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(new String[]{CAT_CPU_STAT}, false, true);
        if (commandResult.result == 0) {
            firstLine = commandResult.successMsgLines.get(0);
        }
        return firstLine;
    }

    // 阻塞方法, 需要指定一小段时间, 才能给出cpu在这段时间段的使用率
    public static double readUsage(int intervalTime) throws Exception {

        //RandomAccessFile reader = new RandomAccessFile(CPU_STAT_PATH, "r");
        //String load = reader.readLine();

        String load = getCPUStateFirstLine();

        // cpu  630590 102885 826532 9841256 15412 133798 86596 0 0 0
        String[] toks = load.split(" +");  // Split on one or more spaces

        long cpu1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                + Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);
        long use1 = cpu1 - Long.parseLong(toks[4]);

        try {
            Thread.sleep(intervalTime);
        } catch (Exception e) {
        }

        load = getCPUStateFirstLine();

        toks = load.split(" +");

        long cpu2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                + Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);
        long use2 = cpu2 - Long.parseLong(toks[4]);

        return (double) (use2 - use1) / (cpu2 - cpu1);
    }
*/
}
