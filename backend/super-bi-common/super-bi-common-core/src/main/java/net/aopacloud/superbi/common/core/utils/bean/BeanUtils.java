package net.aopacloud.superbi.common.core.utils.bean;

/**
 * Bean utils
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * copy bean properties
      * @param dest
     * @param src
     */
    public static void copyBeanProp(Object dest, Object src) {
        try {
            copyProperties(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
