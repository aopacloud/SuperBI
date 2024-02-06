package net.aopacloud.superbi.common.core.text;

import net.aopacloud.superbi.common.core.utils.StringUtils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * type converter
 */
public class Convert {

    /**
     * convert value to String type.
     * if the value is null, return the default value.
      * @param value
     * @param defaultValue
     * @return
     */
    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * convert value to String type.
     * if the value is null, return null.
      * @param value
     * @return
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }


    /**
     * convert value to Integer type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert value to Integer type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }


    /**
     * convert value to Long type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            // Support for scientific notation
            return new BigDecimal(valueStr.trim()).longValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert value to Long type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * convert value to Double type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            // Support for scientific notation
            return new BigDecimal(valueStr.trim()).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert value to Double type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * convert value to Float type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert value to Float type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * convert value to Boolean type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr) {
            case "true":
            case "yes":
            case "ok":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * convert value to Boolean type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }


    /**
     * convert value to BigDecimal type.
     * if the value is null, return the default value.
     * @param value
     * @param defaultValue
     * @return
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }
        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        }
        if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert value to BigDecimal type.
     * if the value is null, return null.
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * convert object to string specified default charset utf-8
     * 1. Byte arrays and ByteBuffers will be converted to corresponding string arrays
     * 2. Object arrays will call the Arrays.toString method
     *
     * @param obj
     * @return
     */
    public static String utf8Str(Object obj) {
        return str(obj, CharsetKit.CHARSET_UTF_8);
    }


    /**
     * convert object to string specified charset
     * 1. Byte arrays and ByteBuffers will be converted to corresponding string arrays
     * 2. Object arrays will call the Arrays.toString method
     *
     * @param obj
     * @param charset
     * @return
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[] || obj instanceof Byte[]) {
            if (obj instanceof byte[]) {
                return str((byte[]) obj, charset);
            } else {
                Byte[] bytes = (Byte[]) obj;
                int length = bytes.length;
                byte[] dest = new byte[length];
                for (int i = 0; i < length; i++) {
                    dest[i] = bytes[i];
                }
                return str(dest, charset);
            }
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer) obj, charset);
        }
        return obj.toString();
    }


    /**
     * convert byte array to string
     * @param data
     * @param charset
     * @return
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }


    /**
     * convert byte buffer to string
     * @param data
     * @param charset
     * @return
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }


}
