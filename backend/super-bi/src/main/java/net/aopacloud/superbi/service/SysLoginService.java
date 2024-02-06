package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.CaptchaDTO;
import net.aopacloud.superbi.model.dto.TokenDTO;
import net.aopacloud.superbi.model.entity.SysLoginLog;
import net.aopacloud.superbi.model.uo.LoginBodyUO;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 20:22
 */
public interface SysLoginService {

    TokenDTO login(LoginBodyUO loginBodyUO);

    CaptchaDTO captcha();

    void insertSysLoginLog(SysLoginLog sysLoginLog);

}
