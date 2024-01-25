package net.aopacloud.superbi.common.core.web.domain;

import net.aopacloud.superbi.common.core.constant.HttpStatus;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Data
@Accessors(chain = true)
public class RestApiResponse<T> {

    /**
     * 返回状态码
     */
    private int statusCode;

    /**
     * 消息提示内容
     */
    private String message;

    /**
     * 业务代码
     */
    private int bizCode;

    /**
     * 返回数据
     */
    private T data;

    public static RestApiResponse success() {
        return new RestApiResponse().setStatusCode(HttpStatus.SUCCESS);
    }

    public static <T> RestApiResponse<T> success(T data) {
        return new RestApiResponse().setStatusCode(HttpStatus.SUCCESS).setData(data);
    }

    /**
     * 警告信息，前端直接弹窗显示
     *
     * @param message
     * @return
     */
    public static RestApiResponse warn(String message) {
        return new RestApiResponse().setStatusCode(HttpStatus.WARN).setMessage(message);
    }

    public static RestApiResponse error(String message) {
        return new RestApiResponse().setStatusCode(HttpStatus.ERROR).setMessage(message);
    }
}
