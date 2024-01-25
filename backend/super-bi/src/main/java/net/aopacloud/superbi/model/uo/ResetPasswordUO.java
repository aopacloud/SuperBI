package net.aopacloud.superbi.model.uo;

import lombok.Data;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/10 19:10
 */
@Data
public class ResetPasswordUO {
    private String oldPassword;
    private String newPassword;
    private String uuid;
    private String code;
}
