package net.aopacloud.superbi.model.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 12:09
 */
@Data
public class TokenVO {
    private String token;
    private String refreshToken;
}
