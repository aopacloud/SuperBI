package net.aopacloud.superbi.utils;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 14:43
 */
public class UuidUtils {
    private static final String[] chars = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "a1", "a2"
    };

    /**
     * 生成缩减版的 UUID
     * @return
     */
    public static String simpleUuid() {
        StringBuilder sb = new StringBuilder();
        String uid = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        int length = uid.length();
        // 将16进制数转换成64进制数
        // 2的4次方转化成2的6次方的数据
        // bcb da5 f41 172 4bc 28b 920 b1c 5f4 422 6e
        // 3位转2位
        int i = 0;
        for (; i < length; i = i + 3) {
            // 16进制数转化成10进制数
            int end = i + 3;
            if (end >= length) {
                end = length;
            }
            int t = Integer.parseInt(uid.substring(i, end), 16);
            // 10进制数转化成64进制数
            sb.append(chars[t % 64]);// 第二个数据
            sb.append(chars[t / 64]);// 第一个数据
        }
        return sb.toString();
    }
}
