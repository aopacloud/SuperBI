package net.aopacloud.superbi.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;
import net.aopacloud.superbi.listener.event.AuthRoleUserUpdateEvent;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.service.DatasetApplyService;
import net.aopacloud.superbi.service.DatasetAuthorizeService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthRoleUserListener {

    private final DatasetAuthorizeService datasetAuthorizeService;

    private final DatasetApplyService datasetApplyService;

    @EventListener
    public void onAuthRoleUserUpdate(AuthRoleUserUpdateEvent event) {
        log.info("receive event {}", event);
        try {
            List<DatasetAuthorizeDTO> datasetAuthorizes = datasetAuthorizeService.findAuthorizeByRole(event.getRoleId());
            event.getModifiedUsers().forEach(authRoleUser -> {
                String username = authRoleUser.getUsername();
                for(DatasetAuthorizeDTO datasetAuthorize : datasetAuthorizes) {
                    List<DatasetApplyDTO> applies = datasetApplyService.findApplyByDatasetAndUsername(datasetAuthorize.getDatasetId(), username);
                    for(DatasetApplyDTO apply : applies) {
                        apply.setAuthorizeStatus(AuthorizeStatusEnum.AUTHORIZED);
                        datasetApplyService.updateApply(apply);
                        log.info("update apply {} to authorize", apply);
                    }
                }
            });

        } catch (Exception e) {
            log.error("listen authRole user update error", e);
        }
    }

}
