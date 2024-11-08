package net.aopacloud.superbi.listener;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.cache.AuthorizeThreadLocalCache;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.listener.event.AutoAuthorizeUpdateEvent;
import net.aopacloud.superbi.listener.event.DatasetAuthorizeUpdateEvent;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.mapper.DatasetFieldMapper;
import net.aopacloud.superbi.model.converter.DatasetAuthorizeConverter;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.PermissionNoticeDTO;
import net.aopacloud.superbi.model.entity.DatasetField;
import net.aopacloud.superbi.service.*;
import org.assertj.core.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: yan.zu
 * @date: 2024/8/2
 * @description:
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AutoAuthorizeListener {

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final DatasetAuthorizeConverter datasetAuthorizeConverter;

    private final DatasetFieldMapper datasetFieldMapper;

    @EventListener
    public void onAutoAuthorizeUpdate(AutoAuthorizeUpdateEvent event) {
        log.info("receive event {}", event);
        DatasetDTO datasetDTO = event.getDatasetDTO();
        //新增字段自动授权逻辑
        try {
            if (datasetDTO != null & datasetDTO.getId() != null) {
                //找到行权限、且新增字段自动授权的数据集授权记录
                autoUpdateAuth(datasetDTO , PrivilegeTypeEnum.COLUMN);
                //找到行列权限、且新增字段自动授权的数据集授权记录
                autoUpdateAuth(datasetDTO , PrivilegeTypeEnum.COLUMN_ROW);
            }
        } catch (Exception e) {
            log.error("listen autoAuthorizeUpdate error", e);
        }
    }

    private  void autoUpdateAuth(DatasetDTO datasetDTO, PrivilegeTypeEnum privilegeTypeEnum) {
        List<DatasetAuthorizeDTO> datasetAuthorizeDTOList = datasetAuthorizeMapper.selectAuthorizeByDataset(datasetDTO.getId());
        List<DatasetAuthorizeDTO> autoAuthList = datasetAuthorizeDTOList.stream().filter(datasetAuthorizeDTO ->
                        datasetAuthorizeDTO.getPrivilegeType().equals(privilegeTypeEnum) && datasetAuthorizeDTO.getAutoAuth())
                .collect(Collectors.toList());
        autoAuthList.forEach(datasetAuthorizeDTO -> {
            StringJoiner columnJoiner = new StringJoiner(",");
            Set<String> columnSet = Sets.newHashSet();
            List<String> oldFieldList = datasetFieldMapper.selectByDatasetAndVersion(datasetDTO.getId(), datasetAuthorizeDTO.getVersion())
                    .stream().map(DatasetField::getName).collect(Collectors.toList());
            List<String> newFieldList = datasetFieldMapper.selectByDatasetAndVersion(datasetDTO.getId(), datasetDTO.getVersion())
                    .stream().map(DatasetField::getName).collect(Collectors.toList());
            newFieldList.removeAll(oldFieldList);
            columnSet.addAll(newFieldList);

            if (!Strings.isNullOrEmpty(datasetAuthorizeDTO.getColumnPrivilege())) {
                columnSet.addAll(Arrays.stream(datasetAuthorizeDTO.getColumnPrivilege().split(",")).collect(Collectors.toList()));
            }
            columnSet.forEach(columnName -> columnJoiner.add(columnName));
            datasetAuthorizeDTO.setColumnPrivilege(columnJoiner.toString());
            datasetAuthorizeMapper.update(datasetAuthorizeConverter.toEntity(datasetAuthorizeDTO));
        });
    }
}
