package net.aopacloud.superbi.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.cache.AuthorizeThreadLocalCache;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;
import net.aopacloud.superbi.enums.EventActionEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.listener.event.DatasetAuthorizeUpdateEvent;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.model.converter.DatasetAuthorizeConverter;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.PermissionNoticeDTO;
import net.aopacloud.superbi.service.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Listen dataset authorize modify event.
 * when dataset authorize modify, update dataset apply status.
 *
 * @author: hudong
 * @date: 2023/9/14
 * @description:
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetAuthorizeListener {

    private final DatasetPrivilegeService privilegeService;

    private final DatasetApplyService applyService;

    private final DatasetAuthorizeService authorizeService;

    private final DatasetAuthorizeMapper authorizeMapper;

    private final ApplicationContext applicationContext;

    private final NotificationService notificationService;

    private final DatasetService datasetService;

    private final SysUserService sysUserService;

    private final DatasetAuthorizeConverter datasetAuthorizeConverter;

    @EventListener
    public void onAuthorizeModify(DatasetAuthorizeUpdateEvent event) {
        log.info("receive event {}", event);
        notifyUser(event);
        modifyApplyIfNeed(event);
    }

    private void modifyApplyIfNeed(DatasetAuthorizeUpdateEvent event) {
        try {
            List<DatasetApplyDTO> applies = applyService.findApplyByDatasetAndUsername(event.getDatasetId(), event.getUsername());
            if (applies.isEmpty()) {
                return;
            }
            AuthorizeThreadLocalCache.remove();
            PermissionEnum permission = privilegeService.findDatasetMixedPermission(event.getDatasetId(), event.getUsername());
            switch (permission) {
                case NONE:
                    updateApplyAuthorizeStatus(applies, AuthorizeStatusEnum.NOT_AUTHORIZED);
                    break;
                case EXPIRED:
                    updateApplyAuthorizeStatus(applies, AuthorizeStatusEnum.EXPIRED);
                    break;
                case READ:
                case WRITE:
                    updateApplyAuthorizeStatus(applies, AuthorizeStatusEnum.AUTHORIZED);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("listen authorize modify error", e);
        }
    }

    private void updateApplyAuthorizeStatus(List<DatasetApplyDTO> applies, AuthorizeStatusEnum authorizeStatus) {
        applies.stream().forEach(apply -> {
            apply.setAuthorizeStatus(authorizeStatus);
            applyService.updateApply(apply);
            log.info("update apply {} authorize status to {}", apply.getId(), authorizeStatus);
        });
    }

    public void notifyUser(DatasetAuthorizeUpdateEvent event) {

        AuthorizeStatusEnum authorizeStatus;

        switch (event.getAction()) {
            case CREATE:
                authorizeStatus = AuthorizeStatusEnum.AUTHORIZED;
                break;
            case EXPIRE:
                authorizeStatus = AuthorizeStatusEnum.EXPIRED;
                break;
            default:
                return;

        }
        DatasetDTO datasetDTO = datasetService.findOneWithoutFields(event.getDatasetId());

        PermissionNoticeDTO permissionNoticeDTO = PermissionNoticeDTO.builder()
                .status(authorizeStatus)
                .authorizeId(event.getDatasetAuthorize().getId())
                .datasetId(event.getDatasetId())
                .aliasName(sysUserService.getUserAliasName(LoginContextHolder.getUsername()))
                .operator(LoginContextHolder.getUsername())
                .expireTime(new Date(event.getDatasetAuthorize().getStartTime().getTime() + event.getDatasetAuthorize().getExpireDuration() * 1000))
                .build();

        notificationService.permissionNotice(event.getUsername(), PositionEnum.DATASET, event.getDatasetId(), datasetDTO.getName(), permissionNoticeDTO);
    }

    /**
     * scan expire authorize
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void scanExpireAuthorize() {
        try {
            List<DatasetAuthorizeDTO> maybeExpire = authorizeService.findMaybeExpire();

            maybeExpire.stream().forEach(authorize -> {

                authorizeService.updateAuthorizeExpire(authorize.getId());

                log.info("dataset authorize {} expire, update", authorize);

                applicationContext.publishEvent(new DatasetAuthorizeUpdateEvent(authorize.getUsername(), authorize.getDatasetId(), EventActionEnum.EXPIRE, datasetAuthorizeConverter.toEntity(authorize)));
            });
        } catch (Exception e) {
            log.error("scan expire authorize error", e);
        }
    }

    /**
     * scan delete authorize
     * 每周一一次
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void scanDeleteAuthorize() {
        try {
            List<DatasetAuthorizeDTO> shouldDelete = authorizeService.selectAuthorizeShouldDelete();

            shouldDelete.stream().forEach(authorize -> {
                authorizeMapper.softDelete(authorize.getId());
                log.info("dataset authorize {} auto delete", authorize);
            });
        } catch (Exception e) {
            log.error("scan delete authorize error", e);
        }
    }
}
