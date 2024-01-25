package net.aopacloud.superbi.utils;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/1 11:03
 */
public class LogUtils {
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
