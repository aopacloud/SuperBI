package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.uo.HandOverUO;
import net.aopacloud.superbi.service.HandOverService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("handOver")
public class HandOverController {

    private final HandOverService handOverService;

    @GetMapping("/checkResource")
    public RestApiResponse<Boolean> checkResource(String username) {
        boolean result = handOverService.checkUserHasResource(username);
        return RestApiResponse.success(result);
    }

    @PostMapping("/execute")
    public RestApiResponse handOver(@RequestBody HandOverUO handOverUO) {
        handOverService.moveUserResource(handOverUO.getFromUsername(), handOverUO.getToUsername(), handOverUO.getPosition());
        return RestApiResponse.success();
    }

    @PostMapping("/execute/auto")
    public RestApiResponse handOverAuto(@RequestBody HandOverUO handOverUO) {
        if (handOverUO.getIdList() != null) {
            handOverService.moveUserResourceByIdList(handOverUO.getIdList(), handOverUO.getFromUsernameList(), handOverUO.getToUsername(),
                    handOverUO.getPosition(), handOverUO.getAutoTrans());
        } else {
            handOverService.moveUserResourceById(handOverUO.getId(), handOverUO.getFromUsername(), handOverUO.getToUsername(),
                    handOverUO.getPosition(), handOverUO.getAutoTrans());
        }
        return RestApiResponse.success();
    }

}
