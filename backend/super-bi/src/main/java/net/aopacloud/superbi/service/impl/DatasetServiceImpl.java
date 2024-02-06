package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.mapper.DatasetFieldMapper;
import net.aopacloud.superbi.mapper.DatasetMapper;
import net.aopacloud.superbi.mapper.DatasetMetaConfigMapper;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.converter.DatasetFieldConverter;
import net.aopacloud.superbi.model.converter.DatasetMetaConfigConverter;
import net.aopacloud.superbi.model.domain.ExcelBuilder;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.DatasetField;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DatasetQuery;
import net.aopacloud.superbi.model.vo.DatasetVO;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.service.*;
import org.assertj.core.util.Strings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: hu.dong
 * @date: 2021/10/25
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetServiceImpl implements DatasetService {

    private final DatasetMapper datasetMapper;

    private final DatasetFieldMapper fieldMapper;

    private final FolderService folderService;

    private final DatasetMetaConfigMapper metaConfigMapper;

    private final DatasetConverter converter;

    private final DatasetMetaConfigConverter configConverter;

    private final DatasetFieldConverter fieldConverter;

    private final DatasetPrivilegeService datasetPrivilegeService;

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final DatasetApplyService datasetApplyService;

    private final SysUserService sysUserService;

    private final MetaDataService metaDataService;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    @Override
    public DatasetDTO findOneWithoutFields(Long id) {
        Dataset dataset = Optional.ofNullable(datasetMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException("dataset not found"));
        return converter.toDTO(dataset, null, null);
    }

    @Override
    public DatasetDTO findOne(Long id) {
        Dataset dataset = Optional.ofNullable(datasetMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException("dataset not found"));
        Integer version = dataset.getVersion();
        return findOne(id, version);
    }

    @Override
    public DatasetDTO findOne(Long id, Integer version) {
        Dataset dataset = Optional.ofNullable(datasetMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException(LocaleMessages.getMessage(MessageConsist.DATASET_NOT_FOUND_ERROR)));

        DatasetMetaConfig config = metaConfigMapper.selectOneByDatasetAndVersion(id, version);

        List<DatasetField> fields = fieldMapper.selectByDatasetAndVersion(id, version);

        DatasetDTO datasetDTO = converter.toDTO(dataset, config, fields);

        TablePartition latestPartition = Optional.ofNullable(metaDataService).map(metaDataService -> metaDataService.getLatestPartition(datasetDTO)).orElse(new TablePartition());
        datasetDTO.setLatestPartitionValue(latestPartition.getPartitionValue());

        PermissionEnum datasetMixedPermission = datasetPrivilegeService.findDatasetMixedPermission(datasetDTO);
        datasetDTO.setPermission(datasetMixedPermission);

        datasetPrivilegeService.fillDatasetFieldAuthorize(datasetDTO, LoginContextHolder.getUsername());

        FullFolderDTO folder = folderService.findByTarget(datasetDTO.getId(), PositionEnum.DATASET);
        if (!Objects.isNull(folder)) {
            datasetDTO.setFolder(folder);
            datasetDTO.setFolderId(folder.getId());
        }

        datasetDTO.setApplying(datasetApplyService.isApplying(dataset.getId()));

        return datasetDTO;
    }

    @Override
    public DatasetDTO findLastEditVersion(Long id) {
        Dataset dataset = Optional.ofNullable(datasetMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException(LocaleMessages.getMessage(MessageConsist.DATASET_NOT_FOUND_ERROR)));
        Integer version = dataset.getLastEditVersion();
        return findOne(id, version);
    }

    public PageVO<DatasetVO> search(DatasetQuery datasetQuery) {

        PageInfo<DatasetDTO> pageInfo = find(datasetQuery);

        List<DatasetDTO> datasetDTOS = pageInfo.getList().stream().map(dataset -> {
                    PermissionEnum permission = datasetPrivilegeService.findDatasetMixedPermission(dataset);
                    dataset.setPermission(permission);
                    DatasetMetaConfig datasetMetaConfig = metaConfigMapper.selectOneByDatasetAndVersion(dataset.getId(), dataset.getVersion());
                    String source = Optional.ofNullable(datasetMetaConfig).map(config -> String.format("%s.%s", config.getDbName(), config.getTableName())).orElse(StringUtils.EMPTY);
                    dataset.setSource(source);

                    FullFolderDTO folder = null;
                    if (datasetQuery.getFolderType() == FolderTypeEnum.ALL) {
                        folder = folderService.findByTarget(dataset.getId(), PositionEnum.DATASET);
                    } else {
                        folder = folderService.findPersonalByTarget(dataset.getId(), PositionEnum.DATASET, LoginContextHolder.getUsername());
                    }
                    if (!Objects.isNull(folder)) {
                        dataset.setFolder(folder);
                        dataset.setFolderId(folder.getId());
                    }

                    dataset.setApplying(datasetApplyService.isApplying(dataset.getId()));

                    return dataset;
                }).filter(dataset -> datasetQuery.getFolderType() == FolderTypeEnum.PERSONAL || (dataset.getPermission() != PermissionEnum.NONE || dataset.isEnableApply()))
                .collect(Collectors.toList());

        return new PageVO<>(converter.toVOList(datasetDTOS), pageInfo.getTotal());
    }

    private PageInfo<DatasetDTO> find(DatasetQuery datasetQuery) {

        ConditionQuery condition = ConditionQuery.from(datasetQuery);
        condition.setFolderType(datasetQuery.getFolderType());
        condition.setSuperAdmin(sysUserService.isSuperAdmin(datasetQuery.getUsername()));
        FolderEnum folderEnum = FolderEnum.ROOT_FOLDER;
        if (!Objects.isNull(datasetQuery.getFolderId())) {
            folderEnum = FolderEnum.ofFolderId(datasetQuery.getFolderId());
        } else {
            if (datasetQuery.getFolderType() == FolderTypeEnum.ALL) {
                folderEnum = FolderEnum.ROOT_FOLDER;
            } else {
                folderEnum = FolderEnum.ALL;
            }
        }

        if (!Strings.isNullOrEmpty(datasetQuery.getKeyword())) {
            List<SysUserDTO> filterUser = sysUserService.filter(datasetQuery.getKeyword());
            condition.setSearchUsernames(filterUser.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
        }


        if (!Objects.isNull(folderEnum)) {
            switch (folderEnum) {
                case AUTHORIZED:
                    Set<String> resourceCodes = workspaceUserResourceService.getResourceCodes(datasetQuery.getWorkspaceId());
                    if (resourceCodes.contains(BiConsist.ALL_WORKSPACE_ANALYSIS_CODE) || condition.isSuperAdmin()) {
                        PageUtils.startPage();
                        condition.setFolderType(FolderTypeEnum.PERSONAL);
                        List<DatasetDTO> root = datasetMapper.findList(condition);
                        return new PageInfo<>(root);
                    } else {
                        List<DatasetAuthorizeDTO> activedAuthorize = datasetAuthorizeMapper.findActivedAuthorize(datasetQuery.getUsername(), datasetQuery.getWorkspaceId());
                        Set<Long> datasetIds = activedAuthorize.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());
                        List<DatasetDTO> creatorByMe = datasetMapper.findMyCreate(condition);
                        datasetIds.addAll(creatorByMe.stream().map(DatasetDTO::getId).collect(Collectors.toSet()));
                        if (datasetIds.isEmpty()) {
                            return new PageInfo<>(Lists.newArrayList());
                        }
                        condition.setIds(datasetIds);
                        PageUtils.startPage();
                        List<DatasetDTO> hasPermissionDatasets = datasetMapper.findHasPermission(condition);
                        return new PageInfo<>(hasPermissionDatasets);
                    }
                case FAVORITE:
                    PageUtils.startPage();
                    List<DatasetDTO> favoriteDatasets = datasetMapper.findMyFavorite(condition);
                    return new PageInfo<>(favoriteDatasets);

                case CREATE:
                    PageUtils.startPage();
                    List<DatasetDTO> myCreate = datasetMapper.findMyCreate(condition);
                    return new PageInfo<>(myCreate);

                case UN_CLASSIFIED:
                    PageUtils.startPage();
                    List<DatasetDTO> unclassified = datasetMapper.findUnclassified(condition);
                    return new PageInfo<>(unclassified);

                case ALL:
                    Set<String> codes = workspaceUserResourceService.getResourceCodes(datasetQuery.getWorkspaceId());
                    if (codes.contains(BiConsist.ALL_WORKSPACE_ANALYSIS_CODE)) {
                        condition.setSuperAdmin(Boolean.TRUE);
                    }
                    List<DatasetAuthorizeDTO> authorize = datasetAuthorizeMapper.findActivedAuthorize(datasetQuery.getUsername(), datasetQuery.getWorkspaceId());
                    Set<Long> dIds = authorize.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());
                    condition.setIds(dIds);
                    PageUtils.startPage();
                    List<DatasetDTO> myAll = datasetMapper.findMyAll(condition);
                    return new PageInfo<>(myAll);
                case ROOT_FOLDER:
                    PageUtils.startPage();
                    condition.setFolderType(FolderTypeEnum.ALL);
                    List<DatasetDTO> root = datasetMapper.findList(condition);
                    return new PageInfo<>(root);
                case NORMAL:
                    FolderNode folderTree = folderService.findTreeByFolder(datasetQuery.getFolderId());
                    List<Long> folderIds = FolderNode.allNodeId(folderTree);
                    condition.setFolderIds(folderIds);
                    condition.setFolderType(folderTree.getType());
                    PageUtils.startPage();
                    List<DatasetDTO> list = datasetMapper.findList(condition);
                    return new PageInfo<>(list);
                default:
                    break;
            }
        }

        PageUtils.startPage();
        List<DatasetDTO> datasetDTOS = datasetMapper.findList(condition);
        return new PageInfo<>(datasetDTOS);
    }

    @Override
    public List<DatasetDTO> listByDatasourceAndTable(Long datasourceId, String tableName) {
        return datasetMapper.selectByDatasourceAndTable(datasourceId, tableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatasetDTO save(DatasetDTO datasetDTO) {

        try {
            Dataset dataset = converter.toEntity(datasetDTO);
            dataset.setVersion(1);
            dataset.setLastEditVersion(1);
            dataset.setCreator(LoginContextHolder.getUsername());
            dataset.setOperator(LoginContextHolder.getUsername());
            dataset.setStatus(StatusEnum.UN_PUBLISH);
            datasetMapper.save(dataset);

            DatasetMetaConfigDTO configDTO = datasetDTO.getConfig();
            DatasetMetaConfig config = configConverter.toEntity(configDTO);
            config.setDatasetId(dataset.getId());
            config.setVersion(1);
            metaConfigMapper.insert(config);


            saveFields(dataset, datasetDTO.getFields());

            return findOne(dataset.getId(), dataset.getLastEditVersion());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
    }

    @Override
    public DatasetDTO delete(Long id) {

        DatasetDTO datasetDTO = findOne(id);

        datasetMapper.deleteById(id);

        return datasetDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatasetDTO update(DatasetDTO datasetDTO, Long id) {

        try {
            Dataset dataset = datasetMapper.selectById(id);
            Integer lastEditVersion = dataset.getLastEditVersion() + 1;
            dataset.setLastEditVersion(lastEditVersion);

            datasetDTO.setId(id);
            datasetDTO.setLastEditVersion(lastEditVersion);
            datasetDTO.setOperator(LoginContextHolder.getUsername());
            datasetMapper.update(converter.toEntity(datasetDTO));

            DatasetMetaConfigDTO configDTO = datasetDTO.getConfig();
            DatasetMetaConfig config = configConverter.toEntity(configDTO);
            config.setDatasetId(dataset.getId());
            config.setVersion(lastEditVersion);
            metaConfigMapper.insert(config);

            saveFields(dataset, datasetDTO.getFields());

            return findOne(id, lastEditVersion);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
    }

    private List<DatasetFieldDTO> saveFields(Dataset dataset, List<DatasetFieldDTO> fields) {
        return fields.stream().map(fieldDTO -> {
            DatasetField field = fieldConverter.toEntity(fieldDTO);
            field.setDatasetId(dataset.getId());
            field.setVersion(dataset.getLastEditVersion());
            fieldMapper.insert(field);
            return fieldConverter.entityToDTO(field);
        }).collect(Collectors.toList());
    }

    @Override
    public void offline(Long id) {
        datasetMapper.updateStatus(id, StatusEnum.OFFLINE);
    }

    @Override
    public void online(Long id) {
        datasetMapper.updateStatus(id, StatusEnum.ONLINE);
    }

    @Override
    public DatasetDTO publish(Long id, Integer version) {
        Dataset dataset = datasetMapper.selectById(id);
        if (version == null || version == 0) {
            version = dataset.getLastEditVersion();
        }
        dataset.setVersion(version);
        dataset.setStatus(StatusEnum.ONLINE);
        datasetMapper.update(dataset);
        return findOne(id);
    }

    @Override
    public void setDataset(Long id, DatasetDTO datasetDTO) {

        int enableApply = datasetDTO.getEnableApply();

        datasetMapper.updateApplyEnable(id, enableApply);

    }

    @Override
    public void downloadDataset(Long id) {
        try {
            DatasetDTO dataset = findOne(id);
            if (dataset == null) {
                throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DATASET_NOT_FOUND_ERROR));
            }
            String fileName = String.format("%s.%s", dataset.getName(), BiConsist.EXCEL_SUFFIX);

            ExcelBuilder excelBuilder = new ExcelBuilder(fileName);
            // set title
            List<String> titles = Lists.newArrayList(BiConsist.DATASET_DOWNLOAD_TITLE.split(","));
            excelBuilder.setTitle(titles);

            // write data
            List<Object[]> rows = dataset.getFields().stream().map(field -> {
                List<Object> items = Lists.newArrayList();
                items.add(field.getName());
                items.add(field.getDisplayName());
                items.add(field.getDataType().getMsg());
                items.add(field.getCategory().getMsg());
                items.add(field.getDataFormat());
                items.add(field.getAggregator());
                items.add(field.getDescription());
                items.add(field.getStatus());
                items.add(field.getExpression());

                return items.toArray();
            }).collect(Collectors.toList());
            excelBuilder.setData(rows);

            // download
            excelBuilder.writeToResponse();
        } catch (Exception e) {
            throw new RuntimeException("downloadExcelFile Exception", e);
        }
    }

    @Override
    public List<Long> selectIdsByWorkspaceAndCreator(Long workspaceId, String creator) {
        return datasetMapper.selectIdsByWorkspaceAndCreator(workspaceId, creator);
    }

    @Override
    public Integer countByTable(Long workspaceId, String datasourceName, String tableName) {
        return datasetMapper.countByTable(workspaceId, datasourceName, tableName);
    }

    @Override
    public Integer countByUsername(String username) {
        return datasetMapper.countByUsername(username);
    }

    @Override
    public void handOver(String fromUsername, String toUsername) {
        datasetMapper.updateCreator(fromUsername, toUsername);
    }

    @Override
    public List<DatasetDTO> findDatasetCanAuthorize(Long workspaceId, String username) {

        boolean superAdmin = sysUserService.isSuperAdmin(username);
        WorkspaceUserResourceDTO workspaceUserResourceDTO = workspaceUserResourceService.get(workspaceId);
        boolean isAllWorkspaceAuth = workspaceUserResourceDTO.getResourceCodes().contains(BiConsist.ALL_WORKSPACE_MANAGE_CODE);

        if (superAdmin || isAllWorkspaceAuth) {
            return datasetMapper.findListByWorkspace(workspaceId);
        }

        List<DatasetAuthorizeDTO> datasetAuthorizeDTOS = datasetAuthorizeMapper.selectWriteAuthorize(username, workspaceId);
        Set<Long> datasetIds = datasetAuthorizeDTOS.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());

        List<DatasetDTO> datasetDTOS = datasetMapper.selectByIdsOrCreator(datasetIds, username);

        return datasetDTOS;
    }

    @Override
    public List<DatasetDTO> findByDashboard(Long dashboardId) {

        return datasetMapper.findByDashboard(dashboardId);
    }
}
