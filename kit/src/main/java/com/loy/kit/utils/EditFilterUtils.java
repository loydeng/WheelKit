package com.loy.kit.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 给EditView提供InputFilter,用于限定EditView可输入的字符类型, 这个限定比设置 inputType 更精细
 * 方便拓展拓展新的Filter,只要将标题名称与Filter名称对应存储即可,拿到对应InputFilter实例
 */
public final class EditFilterUtils {

    /*
    正则规则
    \ 转义符,将普通字符转义为特殊字符
    \d <==> [0-9] 大写时表示其补集,就是除此以外的所有字符集
    \w <==> [A-Za-z0-9_] 大写时表示其补集
    \s <==> 表示空白字符,包括空格,制表符,换行符,换页符.
    以上都是特殊字符要使用转义符(\)来转义,\本身转义,所以特殊字符都是两个\\

    ^ 单个字符前表示以字符开始(^[xyz],以xyz之一开头),或字符集前表示其补集([^xyz]除xyz以外的字符)
    $ 表示以字符结束
    {m,n} 出现 m(含)--n(含) 次
    ? 0或1次  <==> {0,1}
    * 0或多次 <==> {0,}
    + 1或多次 <==> {1,}
    | 或者,二选一,表达式要使用()保证优先结合
    . 除换行符为的任意字符
    将以上特殊字符作为普通字符使用,也要使用转移符,也是使用两个\\

    // more: https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Guide/Regular_Expressions

    1 数字：^[0-9]*$

    2 n位的数字：^\d{n}$

    3 至少n位的数字：^\d{n,}$

    4 m-n位的数字：^\d{m,n}$

    5 零和非零开头的数字：^(0|[1-9][0-9]*)$

    6 非零开头的最多带两位小数的数字：^([1-9][0-9]*)+(.[0-9]{1,2})?$

    7 带1-2位小数的正数或负数：^(\-)?\d+(\.\d{1,2})?$

    // more :http://toutiao.com/i6231678548520731137
    */

    private EditFilterUtils() {
    }

    /**
     * 字符串是否匹配给定的正则表达式
     */
    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }

    public enum Type{
        Int,             // 整数
        PositiveInt,     //正整数
        NonNegativeInt,  //非负整数(0和正整数)

    }

    public static void setFilter(EditText editText, Type type) {
        if (editText != null && type != null) {
            editText.setFilters(new InputFilter[]{getEditViewFilter(type)});
        }
    }

    //保留两位的正小数(含0)
    public static final String POSITIVE_FLOAT_RETAIN_2 = "POSITIVE_FLOAT_RETAIN_2";

    //保留两位的正小数(含0) 可输入"/"
    public static final String POSITIVE_FLOAT_RETAIN_2_EX = "POSITIVE_FLOAT_RETAIN_2_EX";

    /**
     * 正则表达式
     */
    // 保留两位小数
    public static final String RETAIN_2_FLOAT = "\\d+(\\.(\\d{0,2})?)?$";

    // 正负整数
    public static final String INT = "-?\\d$";

    // 正负整数 限定三位数
    // public static final String INT_3 = "-?\\d{0,3}$";

    private static final HashMap<Type, InputFilter> filterMap = new HashMap<>();

    private static InputFilter getEditViewFilter(Type type) {
        InputFilter filter = filterMap.get(type);
        if (filter == null) {
            switch (type) {
                case Int:
                    filter = Inner.INT_SIGNED_FILTER;
                    break;
                case PositiveInt:
                    filter = Inner.POSITIVE_INT_FILTER;
                    break;
                case NonNegativeInt:
                    filter = Inner.NOT_NEGATIVE_INT_FILTER;
            }
            filterMap.put(type, filter);
        }
        return filter;
    }



    private static class Inner {

        private static final InputFilter POSITIVE_INT_FILTER = new InputFilter() {
            /**
             * @param source 输入时,是输入的字符,可以是多个.删除时为null
             * @param start  都是0
             * @param end    输入时,是当前的输入字符个数,删除时为0
             * @param dest   不论输入还是删除,是操作前的所有字符串
             * @param dstart 输入时原光标位置,删除时是删除后光标的位置 因为可以点选移动光标,不能用于长度限定
             * @param dend   输出时还是原光标位置,删除时是删除前光标的位置
             * @return 输入时, 返回新输入的字符串, 如果直接返回""则不会显示输入.返回source或null都会显示输入
             * 删除时,返回null即可.
             */
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > 1 && isMatch("\\d+$", source)) return source;

                if (dstart > 0 && isMatch("\\d+$", source)) return source;

                if (dstart == 0 && isMatch("[1-9]+$", source)) return source;

                return "";
            }
        };

        private static final InputFilter INT_SIGNED_FILTER = new InputFilter() {
            /**
             * @param source 输入时,是输入的字符,可以是多个.删除时为null
             * @param start  都是0
             * @param end    输入时,是当前的输入字符个数,删除时为0
             * @param dest   不论输入还是删除,是操作前的所有字符串
             * @param dstart 输入时原光标位置,删除时是删除后光标的位置 因为可以点选移动光标,不能用于长度限定
             * @param dend   输出时还是原光标位置,删除时是删除前光标的位置
             * @return 输入时, 返回新输入的字符串, 如果直接返回""则不会显示输入.返回source或null都会显示输入
             * 删除时,返回null即可.
             */
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuffer sb = new StringBuffer(dest.toString());
                sb.insert(dstart, source);
                if (isMatch(INT, sb.toString())) return source;
                return "";
            }
        };

        private static final InputFilter NOT_NEGATIVE_INT_FILTER = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (isMatch("\\d+$", source)) return source;
                return "";
            }
        };

        private static final InputFilter POSITIVE_FLOAT_RETAIN_2_FILTER = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dstart >= 15) return "";

                if (end > 1 && isMatch("\\d+(\\.\\d)?(\\d?)?$", source)) return source;

                if (isMatch("\\d+\\.\\d{2}$", dest) && dest.toString().length() == dstart)
                    return "";

                if (dest.toString().contains(".") && ".".equals(source)) return "";

                if (dstart == 0 && isMatch("[0-9]", source)) return source;

                if (dest.toString().equals("0")) return ".".equals(source) ? source : "";

                if (dstart > 0 && isMatch("[0-9]|\\.", source)) return source;

                return "";
            }
        };

        private static final InputFilter POSITIVE_FLOAT_RETAIN_2_FILTER_EX = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dstart >= 31) return "";

                if (end > 1) {
                    if (isMatch("\\d+(\\.(\\d{0,2})?)?(/\\d+(\\.(\\d{0,2})?)?)?$", dest.toString() + source)) {
                        return source;
                    } else {
                        return "";
                    }
                }

                String old = dest.toString();
                if (!old.contains("/") && old.length() > 0 && "/".equals(source)) return source;

                StringBuffer sb = new StringBuffer();
                int index = old.indexOf("/");
                int p = dstart;
                if (index != -1 ) {
                    if (index < dstart) {
                        sb.append(old.substring(index + 1));
                        p = dstart-index-1;
                    }else {
                        sb.append(old.substring(0, index));
                    }
                } else {
                    sb.append(old);
                }

                sb.insert(p, source);

                if (isMatch(RETAIN_2_FLOAT, sb.toString())) {
                    return source;
                }

                return "";
            }
        };

        // 汉字: "^[\\u4e00-\\u9fa5]+$";
        private static final InputFilter CHINESE_NAME_FILTER = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > 1) return source;
                if (isMatch("[\\u4e00-\\u9fa5]+", source)) return source;
                return "";
            }
        };

        // 手机号码:    "^[1]\\d{10}$";
        private static final InputFilter MOBILE_NUMBER_FILTER = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > 1 && dest.toString().length() + end <= 11){
                    if(isMatch("\\d{"+end+"}$",source)){
                        return source;
                    }
                }

                if (dstart == 0 && "1".equals(source)) return source;

                if (dstart < 11 && isMatch("\\d", source)) return source;

                return "";
            }
        };

        // 座机号码:    "^0\\d{2,3}[- ]?\\d{7,8}$";
        private static final InputFilter TEL_PHONE_NUMBER_FILTER = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dstart == 0 && "0".equals(source)) return source;
                if (dstart < 3 && isMatch("\\d", source)) {
                    return source;
                }
                if (dstart == 3 && isMatch("\\d|-", source)) return source;
                if (dstart == 4 && (isMatch("\\d", source) || (!dest.toString().contains("-") && "-".equals(source))))
                    return source;
                if (isMatch("\\d", source) && dstart > 4 &&
                        (dest.toString().contains("-") && dstart < 13) || (!dest.toString().contains("-") && dstart < 12))
                    return source;
                return "";
            }
        };

        //开放式兼容 座机号 手机号
        private static final InputFilter CONTACT_NUMBER_FILTER = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                int inputLen = source.length();
                int outputLen = dest.length();

                int max = 15;
                if(inputLen + outputLen >= max) return "";

                String rex = "\\d|\\-|\\+"; // boolean isMatch = (c >= '0' && c <= '9') || (c == '-') || (c == '+');
                StringBuffer sb = new StringBuffer();
                for (int i=0;i<inputLen;i++){
                    char c = source.charAt(i);

                    if (!isMatch(rex, String.valueOf(c))) {
                        return "";
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
        };
    }


}
