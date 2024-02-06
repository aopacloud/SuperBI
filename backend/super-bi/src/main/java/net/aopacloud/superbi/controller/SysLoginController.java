package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.annotation.Log;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.CaptchaConverter;
import net.aopacloud.superbi.model.converter.TokenConverter;
import net.aopacloud.superbi.model.uo.LoginBodyUO;
import net.aopacloud.superbi.model.vo.CaptchaVO;
import net.aopacloud.superbi.model.vo.TokenVO;
import net.aopacloud.superbi.service.SysLoginService;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/7 18:01
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SysLoginController {
    private final SysLoginService sysLoginService;
    private final TokenConverter tokenConverter;
    private final CaptchaConverter captchaConverter;

    @PostMapping("/login")
    @Log(moduleName = "登录")
    public RestApiResponse<TokenVO> login(@RequestBody LoginBodyUO loginBodyUO) {
        TokenVO tokenVO = tokenConverter.toVO(sysLoginService.login(loginBodyUO));
        return RestApiResponse.success(tokenVO);
    }

    @GetMapping("/captchaImage")
    public RestApiResponse<CaptchaVO> captcha() {
        CaptchaVO captchaVO = captchaConverter.toVO(sysLoginService.captcha());
        return RestApiResponse.success(captchaVO);
    }


}
