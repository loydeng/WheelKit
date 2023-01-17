package com.loy.kit.log.core;

import com.loy.kit.utils.FileIOUtil;

/**
 * @author Loy
 * @time 2022/8/30 17:09
 * @des
 */
public class PrettyFormat implements Format {

    public static final String TOP_CORNER = "┌";
    public static final String MIDDLE_CORNER = "├";
    public static final String LEFT_BORDER = "│ ";
    public static final String BOTTOM_CORNER = "└";
    private static final String SIDE_DIVIDER =
            "────────────────────────────────────────────────────────";
    private static final String MIDDLE_DIVIDER =
            "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";

    private static final String TOP_BORDER = TOP_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + MIDDLE_DIVIDER + MIDDLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;

    private static final int MAX_LEN = 1100;

    @Override
    public String log(Config config, int level, String tag, String message) {
        int maxLen = config.lineMaxLen;

        if (maxLen == 0) {
            maxLen = MAX_LEN;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(TOP_BORDER).append(FileIOUtil.LINE_SEP);

        int length = message.length();
        int line = length / maxLen;
        if (line > 0) {
            int index = 0;
            for (int i = 0; i < line; i++) {
                appendSubMessage(sb, message.substring(index, index + maxLen));
                index += maxLen;
            }
            if (index != length) {
                appendSubMessage(sb, message.substring(index, length));
            }
        } else {
            appendSubMessage(sb, message);
        }

        sb.append(BOTTOM_BORDER).append(FileIOUtil.LINE_SEP);
        return sb.toString();
    }

    private void appendSubMessage(StringBuilder sb, String msg) {
        String[] split = msg.split(FileIOUtil.LINE_SEP);
        for (String s : split) {
            sb.append(LEFT_BORDER).append(s).append(FileIOUtil.LINE_SEP);
        }
    }

}
