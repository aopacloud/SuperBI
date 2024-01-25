package net.aopacloud.superbi.listener;

import com.google.common.base.Strings;
import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.ApplyStatusEnum;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.listener.event.DatasetApplyUpdateEvent;
import net.aopacloud.superbi.model.dto.ApprovalNoticeDTO;
import net.aopacloud.superbi.model.entity.DatasetApply;
import net.aopacloud.superbi.service.NotificationService;
import net.aopacloud.superbi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetApplyListener {

    private final SysUserService sysUserService;

    private final NotificationService notificationService;
    @EventListener
    public void onDatasetApplyModify(DatasetApplyUpdateEvent event) {

        log.info("receive event {}", event);
        try {
            DatasetApply datasetApply = event.getDatasetApply();

            if(datasetApply.getAuthorizeStatus() == AuthorizeStatusEnum.REJECTED ){

                ApprovalNoticeDTO noticeDTO = ApprovalNoticeDTO.builder()
                        .status(ApplyStatusEnum.REJECTED)
                        .aliasName(sysUserService.getUserAliasName(SecurityContextHolder.getUserName()))
                        .datasetCreatorAlias(sysUserService.getUserAliasName(datasetApply.getDatasetCreator()))
                        .operator(SecurityContextHolder.getUserName())
                        .reason(datasetApply.getAuthorizeRemark())
                        .applyId(datasetApply.getId())
                        .build();

                notificationService.approvalNotice(datasetApply.getUsername(), PositionEnum.DATASET, datasetApply.getDatasetId(), datasetApply.getDatasetName(), noticeDTO);
                return;
            }

            switch(datasetApply.getApproveStatus()) {
                case REJECTED:
                    ApprovalNoticeDTO rejectedNotice = ApprovalNoticeDTO.builder()
                            .status(ApplyStatusEnum.REJECTED)
                            .aliasName(sysUserService.getUserAliasName(datasetApply.getCurrentReviewer()))
                            .datasetCreatorAlias(sysUserService.getUserAliasName(datasetApply.getDatasetCreator()))
                            .operator(datasetApply.getCurrentReviewer())
                            .reason(datasetApply.getAuthorizeRemark())
                            .applyId(datasetApply.getId())
                            .build();
                    notificationService.approvalNotice(datasetApply.getUsername(), PositionEnum.DATASET, datasetApply.getDatasetId(), datasetApply.getDatasetName(), rejectedNotice);
                    break;
                case PASSED:
                    ApprovalNoticeDTO passNotice = ApprovalNoticeDTO.builder()
                            .status(ApplyStatusEnum.PASSED)
                            .aliasName(sysUserService.getUserAliasName(datasetApply.getCurrentReviewer()))
                            .datasetCreatorAlias(sysUserService.getUserAliasName(datasetApply.getDatasetCreator()))
                            .operator(datasetApply.getCurrentReviewer())
                            .applyId(datasetApply.getId())
                            .build();
                    notificationService.approvalNotice(datasetApply.getUsername(), PositionEnum.DATASET, datasetApply.getDatasetId(), datasetApply.getDatasetName(), passNotice);
                    break;

                case UNDER_REVIEW:
                    ApprovalNoticeDTO initNotice = ApprovalNoticeDTO.builder()
                            .status(ApplyStatusEnum.UNDER_REVIEW)
                            .aliasName(datasetApply.getAliasName())
                            .proposer(datasetApply.getUsername())
                            .datasetCreatorAlias(sysUserService.getUserAliasName(datasetApply.getDatasetCreator()))
                            .reason(datasetApply.getReason())
                            .applyId(datasetApply.getId())
                            .build();
                    String username = Strings.isNullOrEmpty(datasetApply.getCurrentReviewer()) ? datasetApply.getDatasetCreator() : datasetApply.getCurrentReviewer();
                    notificationService.approvalNotice(username, PositionEnum.DATASET, datasetApply.getDatasetId(), datasetApply.getDatasetName(), initNotice);
                default:
                    break;
            }

        }catch (Exception e) {
            log.error("listen dataset apply status error", e);
        }
    }

}
