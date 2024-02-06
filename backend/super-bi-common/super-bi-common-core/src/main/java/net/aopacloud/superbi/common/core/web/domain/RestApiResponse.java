package net.aopacloud.superbi.common.core.web.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.common.core.constant.HttpStatus;

/**
 * Rest api return response
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Data
@Accessors(chain = true)
public class RestApiResponse<T> {

    /**
     * status code
     */
    private int statusCode;

    /**
     * prompt message
     */
    private String message;

    /**
     * biz code
     */
    private int bizCode;

    /**
     * return data
     */
    private T data;

    public static RestApiResponse success() {
        return new RestApiResponse().setStatusCode(HttpStatus.SUCCESS);
    }

    public static <T> RestApiResponse<T> success(T data) {
        return new RestApiResponse().setStatusCode(HttpStatus.SUCCESS).setData(data);
    }

    /**
     * alert message, show directly in the front end
     *
     * @param message
     * @return
     */
    public static RestApiResponse warn(String message) {
        return new RestApiResponse().setStatusCode(HttpStatus.WARN).setMessage(message);
    }

    /**
     * error message
     *
     * @param message
     * @return
     */
    public static RestApiResponse error(String message) {
        return new RestApiResponse().setStatusCode(HttpStatus.ERROR).setMessage(message);
    }
}
