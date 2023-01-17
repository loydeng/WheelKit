package com.loy.kit.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * shell 脚本命令工具
 * shell 命令执行为耗时操作, 请在子线程调用
 *
 * @author Loy
 * @time 2021/3/30 18:47
 * @des
 */
public class ShellUtil {

    public static final String LINE_SEP = System.getProperty("line.separator");

    public static final int RETURN_ERR = -1;
    public static final int RETURN_OK = 0;

    public static CommandResult execCmd(final String[] commands,
                                        final boolean isRooted,
                                        final boolean isNeedResultMsg) {
        int result = RETURN_ERR;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        ArrayList<String> successMsg = null;
        ArrayList<String> errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRooted ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null)
                    continue;
                os.write(command.getBytes());
                os.writeBytes(LINE_SEP);
                os.flush();
            }
            os.writeBytes("exit" + LINE_SEP);
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new ArrayList<>();
                errorMsg = new ArrayList<>();
                successResult = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );
                errorResult = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                );
                String line;
                while ((line = successResult.readLine()) != null) {
                    successMsg.add(line);
                    successMsg.add(LINE_SEP);
                }

                while ((line = errorResult.readLine()) != null) {
                    errorMsg.add(line);
                    errorMsg.add(LINE_SEP);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (successResult != null) {
                    successResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(
                result,
                successMsg,
                errorMsg
        );
    }

    /**
     * The result of command.
     */
    public static class CommandResult {
        public int result;
        public List<String> successMsgLines;
        public List<String> errorMsgLines;

        public CommandResult(final int result, List<String> successMsgLines, List<String> errorMsgLines) {
            this.result = result;
            this.successMsgLines = successMsgLines;
            this.errorMsgLines = errorMsgLines;
        }

        @Override
        public String toString() {
            return "result: " + result + "\n" +
                    "successMsg: " + successMsgLines + "\n" +
                    "errorMsg: " + errorMsgLines;
        }
    }
}
