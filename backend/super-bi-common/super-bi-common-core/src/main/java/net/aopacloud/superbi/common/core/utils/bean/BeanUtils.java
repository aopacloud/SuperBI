package net.aopacloud.superbi.common.core.utils.bean;

import java.util.regex.Pattern;

/**
 * Bean 工具类
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * Bean属性复制工具方法。
     *
     * @param dest 目标对象
     * @param src  源对象
     */
    public static void copyBeanProp(Object dest, Object src) {
        try {
            copyProperties(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
