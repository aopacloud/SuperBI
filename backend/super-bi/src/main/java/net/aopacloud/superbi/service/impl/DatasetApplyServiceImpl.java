package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.ApplyStatusEnum;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.listener.event.DatasetApplyUpdateEvent;
import net.aopacloud.superbi.mapper.DatasetApplyMapper;
import net.aopacloud.superbi.mapper.DatasetMapper;
import net.aopacloud.superbi.mapper.DatasetMetaConfigMapper;
import net.aopacloud.superbi.model.converter.DatasetApplyConverter;
import net.aopacloud.superbi.model.dto.ApplyMessageDTO;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.DatasetApply;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import net.aopacloud.superbi.model.query.DatasetApplyQuery;
import net.aopacloud.superbi.service.DatasetApplyService;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.util.DatasetApplyUtils;
import org.assertj.core.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Service
@RequiredArgsConstructor
public class DatasetApplyServiceImpl implements DatasetApplyService {

    private final DatasetApplyMapper datasetApplyMapper;

    private final DatasetApplyConverter converter;

    private final SysUserService sysUserService;

    private final DatasetMapper datasetMapper;

    private final DatasetMetaConfigMapper datasetMetaConfigMapper;

    private final ApplicationContext applicationContext;

    @Override
    public List<DatasetApplyDTO> search(DatasetApplyQuery query) {

        if (!Strings.isNullOrEmpty(query.getKeyword())) {
            List<String> usernames = sysUserService.filter(query.getKeyword()).stream().map(usr -> usr.getUsername()).collect(Collectors.toList());
            if (!usernames.isEmpty()) {
                query.setSearchUsers(usernames);
            }
        }

        if (sysUserService.isSuperAdmin(LoginContextHolder.getUsername())) {
            query.setIsSuperAdmin(Boolean.TRUE);
        }

        List<DatasetApplyDTO> datasetApplyDTOS = datasetApplyMapper.search(query);

        datasetApplyDTOS.stream().forEach(apply -> apply.setDatasetCreatorAliasName(sysUserService.getUserAliasName(apply.getDatasetCreator())));

        return datasetApplyDTOS;
    }

    @Override
    public DatasetApplyDTO findOne(Long id) {

        DatasetApply datasetApply = datasetApplyMapper.selectOneById(id);
        DatasetApplyDTO datasetApplyDTO = converter.entityToDTO(datasetApply);
        datasetApplyDTO.setDatasetCreatorAliasName(sysUserService.getUserAliasName(datasetApply.getDatasetCreator()));
        datasetApplyDTO.setReviewStateJson(DatasetApplyUtils.buildReviewStateJson(datasetApplyDTO));

        return datasetApplyDTO;
    }

    @Override
    public DatasetApplyDTO rejectApply(DatasetApplyDTO datasetApplyDTO) {
        DatasetApply datasetApply = datasetApplyMapper.selectOneById(datasetApplyDTO.getId());

        if (datasetApply.getAuthorizeStatus() == AuthorizeStatusEnum.REJECTED) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.APPLY_REJECT_DUPLICATE_ERROR));
        }

        datasetApply.setAuthorizeStatus(AuthorizeStatusEnum.REJECTED);
        datasetApply.setAuthorizeRemark(datasetApplyDTO.getAuthorizeRemark());
        datasetApplyMapper.updateApply(datasetApply);
        applicationContext.publishEvent(new DatasetApplyUpdateEvent(datasetApply));
        return converter.entityToDTO(datasetApply);
    }

    @Override
    public DatasetApplyDTO revokeApply(Long id) {

        DatasetApply datasetApply = datasetApplyMapper.selectOneById(id);
        datasetApply.setApproveStatus(ApplyStatusEnum.DELETE);
        datasetApplyMapper.updateApply(datasetApply);
        applicationContext.publishEvent(new DatasetApplyUpdateEvent(datasetApply));
        return converter.entityToDTO(datasetApply);
    }

    @Override
    public DatasetApplyDTO updateApply(DatasetApplyDTO datasetApplyDTO) {
        datasetApplyMapper.updateApply(converter.toEntity(datasetApplyDTO));
        return datasetApplyDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatasetApplyDTO postApply(DatasetApplyDTO datasetApplyDTO) {

        List<DatasetApplyDTO> datasetApplyDTOS = datasetApplyMapper.selectByDatasetAndUsername(datasetApplyDTO.getDatasetId(), LoginContextHolder.getUsername());
        boolean hasApplying = datasetApplyDTOS.stream().anyMatch(datasetApply -> !datasetApply.getApproveStatus().isFinished());
        if (hasApplying) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.APPLY_DUPLICATE_ERROR));
        }

        Dataset dataset = datasetMapper.selectById(datasetApplyDTO.getDatasetId());
        datasetApplyDTO.setWorkspaceId(dataset.getWorkspaceId());
        datasetApplyDTO.setDatasetName(dataset.getName());
        datasetApplyDTO.setDatasetCreator(dataset.getCreator());
        DatasetApply datasetApply = converter.toEntity(datasetApplyDTO);
        datasetApply.setUsername(LoginContextHolder.getUsername());
        datasetApply.setAliasName(sysUserService.getUserAliasName(LoginContextHolder.getUsername()));
        datasetApply.setApproveStatus(ApplyStatusEnum.UNDER_REVIEW);
        datasetApply.setCurrentReviewer(dataset.getCreator());
        datasetApply.setAuthorizeStatus(AuthorizeStatusEnum.NOT_AUTHORIZED);
        if (Strings.isNullOrEmpty(datasetApply.getDatasource())) {
            DatasetMetaConfig datasetMetaConfig = datasetMetaConfigMapper.selectOneByDatasetAndVersion(dataset.getId(), dataset.getVersion());
            datasetApply.setDatasource(datasetMetaConfig.getRefTables());
        }
        datasetApplyMapper.save(datasetApply);
        applicationContext.publishEvent(new DatasetApplyUpdateEvent(datasetApply));
        return converter.entityToDTO(datasetApply);
    }

    @Override
    public DatasetApplyDTO passApply(Long id) {

        DatasetApply datasetApply = datasetApplyMapper.selectOneById(id);

        if (datasetApply.getApproveStatus() == ApplyStatusEnum.PASSED) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.APPLY_PASS_DUPLICATE_ERROR));
        }

        datasetApply.setApproveStatus(ApplyStatusEnum.PASSED);
        datasetApplyMapper.updateApply(datasetApply);
        applicationContext.publishEvent(new DatasetApplyUpdateEvent(datasetApply));
        return converter.entityToDTO(datasetApply);
    }

    @Override
    public List<DatasetApplyDTO> findApplyByDatasetAndUsername(Long datasetId, String username) {
        return datasetApplyMapper.selectByDatasetAndUsername(datasetId, username);
    }

    @Override
    public ApplyMessageDTO getApplyMessage() {

        int applyingCount = datasetApplyMapper.selectApplyingCount(LoginContextHolder.getUsername());

        int reviewCount = datasetApplyMapper.selectReviewCount(LoginContextHolder.getUsername());

        int operationCount = datasetApplyMapper.selectOperationCount(LoginContextHolder.getUsername());

        return new ApplyMessageDTO().setApplyingCount(applyingCount).setReviewCount(reviewCount).setOperationCount(operationCount);
    }

    @Override
    public List<DatasetApplyDTO> findUnFinished() {
        return datasetApplyMapper.selectUnFinished();
    }

    @Override
    public boolean isApplying(Long datasetId) {

        String currentUser = LoginContextHolder.getUsername();

        boolean isApplying = datasetApplyMapper.selectByDatasetAndUsername(datasetId, currentUser)
                .stream()
                .anyMatch(datasetApply -> !datasetApply.getApproveStatus().isFinished());

        return isApplying;
    }

    @Override
    public DatasetApplyDTO disagree(DatasetApplyDTO datasetApplyDTO) {
        DatasetApply datasetApply = datasetApplyMapper.selectOneById(datasetApplyDTO.getId());

        if (datasetApply.getApproveStatus() == ApplyStatusEnum.REJECTED) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.APPLY_REJECT_DUPLICATE_ERROR));
        }

        datasetApply.setApproveStatus(ApplyStatusEnum.REJECTED);
        datasetApply.setAuthorizeRemark(datasetApplyDTO.getAuthorizeRemark());
        datasetApplyMapper.updateApply(datasetApply);
        applicationContext.publishEvent(new DatasetApplyUpdateEvent(datasetApply));
        return converter.entityToDTO(datasetApply);

    }
}
