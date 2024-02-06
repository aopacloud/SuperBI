package net.aopacloud.superbi.model.dto;

import lombok.Data;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 12:15
 */
@Data
public class TokenDTO {
    private String token;
    private String refreshToken;
}
