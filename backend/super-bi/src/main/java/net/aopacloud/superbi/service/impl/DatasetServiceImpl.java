package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.listener.event.AutoAuthorizeUpdateEvent;
import net.aopacloud.superbi.listener.event.DatasetDeleteEvent;
import net.aopacloud.superbi.listener.event.DatasetOfflineEvent;
import net.aopacloud.superbi.listener.event.DatasetUpdateEvent;
import net.aopacloud.superbi.mapper.*;
import net.aopacloud.superbi.model.converter.DatasetAuthorizeConverter;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.converter.DatasetFieldConverter;
import net.aopacloud.superbi.model.converter.DatasetMetaConfigConverter;
import net.aopacloud.superbi.model.domain.ExcelBuilder;
import net.aopacloud.superbi.model.domain.TableDescriptor;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.*;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DatasetQuery;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.DatasetVO;
import net.aopacloud.superbi.model.vo.FieldPreviewVO;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.queryEngine.DataTypeConverter;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssemblerFactory;
import net.aopacloud.superbi.queryEngine.sql.join.TableJoinSQLGenerator;
import net.aopacloud.superbi.service.*;
import org.assertj.core.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    private final DatasetExtraConfigMapper extraConfigMapper;

    private final DatasetConverter converter;

    private final DatasetMetaConfigConverter configConverter;

    private final DatasetFieldConverter fieldConverter;

    private final DatasetPrivilegeService datasetPrivilegeService;

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final DatasetApplyService datasetApplyService;

    private final SysUserService sysUserService;

    private final MetaDataService metaDataService;

    private final ResourceMapper resourceMapper;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final ApplicationContext applicationContext;

    private final DatasetAuthorizeConverter datasetAuthorizeConverter;

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

        DatasetMetaConfigDTO configDTO = configConverter.entityToDTO(config);

        DatasetExtraConfig extraConfig = extraConfigMapper.selectOneByDatasetAndVersion(id, version);

        List<DatasetField> fields = fieldMapper.selectByDatasetAndVersion(id, version);

        DatasetDTO datasetDTO = converter.toDTO(dataset, configDTO, fields);

        if (Objects.nonNull(extraConfig)) {
            datasetDTO.setExtraConfig(extraConfig.getContent());
        }

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
//                    DatasetMetaConfig datasetMetaConfig = metaConfigMapper.selectOneByDatasetAndVersion(dataset.getId(), dataset.getVersion());
//                    String source = Optional.ofNullable(datasetMetaConfig).map(config -> String.format("%s.%s", config.getDbName(), config.getTableName())).orElse(StringUtils.EMPTY);
//                    dataset.setSource(source);

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
                })
//                .filter(dataset -> datasetQuery.getFolderType() == FolderTypeEnum.PERSONAL || (dataset.getPermission() != PermissionEnum.NONE || dataset.isEnableApply()))
                .collect(Collectors.toList());

        return new PageVO<>(converter.toVOList(datasetDTOS), pageInfo.getTotal());
    }

    ;

    public PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery) {
        PageUtils.startPage();
        List<RecycleVO> recycleVOS = datasetMapper.searchByRecycle(recycleQuery);
        recycleVOS.forEach(recycleVO -> {
            recycleVO.setCreatorAlias(sysUserService.getUserAliasName(recycleVO.getCreator()));
            recycleVO.setPosition(recycleQuery.getPosition());
        });
        PageInfo<RecycleVO> pageInfo = new PageInfo<>(recycleVOS);
        return new PageVO<>(recycleVOS, pageInfo.getTotal());
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

        Set<Long> authorizeDatasetIds = getAuthorizeDatasetIds(datasetQuery, condition);
        condition.setAuthorizeIds(authorizeDatasetIds);


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

                        if (authorizeDatasetIds.isEmpty()) {
                            return new PageInfo<>(Lists.newArrayList());
                        }

                        condition.setIds(authorizeDatasetIds);
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
            config.setRefTables(configDTO.getContent().getRefTables());
            config.setVersion(1);
            metaConfigMapper.insert(config);

            String extraConfigContent = datasetDTO.getExtraConfig();
            if (!Strings.isNullOrEmpty(extraConfigContent)) {
                DatasetExtraConfig extraConfig = new DatasetExtraConfig(dataset.getId(), dataset.getVersion(), extraConfigContent);
                extraConfigMapper.insert(extraConfig);
            }

            saveFields(dataset, datasetDTO.getFields());

            return findOne(dataset.getId(), dataset.getLastEditVersion());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DATASET_DUPLICATE_NAME_ERROR));
        }
    }

    @Override
    public DatasetDTO delete(Long id) {

        DatasetDTO datasetDTO = findOne(id);

        datasetMapper.updateOperatorById(id, LoginContextHolder.getUsername());
        datasetMapper.deleteById(id);
        resourceMapper.delete(id, PositionEnum.DATASET);

        applicationContext.publishEvent(new DatasetDeleteEvent(datasetDTO, LoginContextHolder.getUsername()));

        return datasetDTO;
    }

    @Override
    public void recycleDelete(Long id) {
        datasetMapper.recycleDelete(id);
    }

    @Override
    public void restore(Long id) {
        datasetMapper.restore(id);
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
            config.setRefTables(configDTO.getContent().getRefTables());
            metaConfigMapper.insert(config);

            String extraConfigContent = datasetDTO.getExtraConfig();
            if (!Strings.isNullOrEmpty(extraConfigContent)) {
                DatasetExtraConfig extraConfig = new DatasetExtraConfig(dataset.getId(), lastEditVersion, extraConfigContent);
                extraConfigMapper.insert(extraConfig);
            }

            saveFields(dataset, datasetDTO.getFields());
            if (datasetDTO.getName() != null) {
                //更新资源列表
                updateResourceName(dataset.getId(), PositionEnum.DATASET, datasetDTO.getName());
            }

            return findOne(id, lastEditVersion);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DATASET_DUPLICATE_NAME_ERROR));
        }
    }

    private List<DatasetFieldDTO> saveFields(Dataset dataset, List<DatasetFieldDTO> fields) {
        return fields.stream().map(fieldDTO -> {
            DatasetField field = fieldConverter.toEntity(fieldDTO);
            field.setDatasetId(dataset.getId());
            field.setVersion(dataset.getLastEditVersion());
            if (!fieldDTO.getType().isNewAdd()) {
                field.setOriginDataType(DataTypeConverter.convert(field.getDatabaseDataType()));
            }
            fieldMapper.insert(field);
            return fieldConverter.entityToDTO(field);
        }).collect(Collectors.toList());
    }

    @Override
    public void offline(Long id) {
        datasetMapper.updateStatus(id, StatusEnum.OFFLINE);
        updateResource(id, PositionEnum.DATASET, StatusEnum.OFFLINE);
        DatasetDTO datasetDTO = findOne(id);
        applicationContext.publishEvent(new DatasetOfflineEvent(datasetDTO, LoginContextHolder.getUsername()));
    }

    private void updateResource(Long id, PositionEnum position, StatusEnum status) {
        Resource resource = new Resource();
        resource.setSourceId(id);
        resource.setPosition(position);
        resource.setStatus(status);
        resourceMapper.update(resource);
    }

    private void updateResourceName(Long id, PositionEnum position, String name) {
        Resource resource = new Resource();
        resource.setSourceId(id);
        resource.setPosition(position);
        resource.setName(name);
        resourceMapper.update(resource);
        resourceMapper.updateDatasetName(name, id);
    }

    @Override
    public void online(Long id) {
        datasetMapper.updateStatus(id, StatusEnum.ONLINE);
        updateResource(id, PositionEnum.DATASET, StatusEnum.ONLINE);
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
        updateResource(id, PositionEnum.DATASET, StatusEnum.ONLINE);

        DatasetDTO datasetDTO = findOne(id);
        applicationContext.publishEvent(new DatasetUpdateEvent(datasetDTO));
        applicationContext.publishEvent(new AutoAuthorizeUpdateEvent(datasetDTO));
        return datasetDTO;
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
    public void handOverById(Long id, String fromUsername, String toUsername) {
        datasetMapper.updateCreatorById(id, fromUsername, toUsername);
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

    @Override
    public List<DatasetDTO> findOnlineDataset(Long workspaceId) {
        return datasetMapper.selectOnlineDataset(workspaceId);
    }

    public String previewSql(DatasetDTO dataset) {


        ConnectionParamDTO connection = metaDataService.getDatasetConnection(dataset);

        QueryContext context = QueryContext.ofQuery(null, dataset, connection);
        AbstractSqlAssembler sqlAssembler = (AbstractSqlAssembler) SqlAssemblerFactory.getSqlAssembler(context);

        TableJoinSQLGenerator generator = new TableJoinSQLGenerator(dataset.getConfig().getContent(), dataset, sqlAssembler);

        return generator.producePreviewSql();

    }

    @Override
    public FieldPreviewVO previewField(Long datasetId, List<TableDescriptor> tables, List<DatasetFieldDTO> fields) {

        boolean isSaved = Objects.nonNull(datasetId);

        Set<String> duplicateColumns = Sets.newHashSet();
        Set<String> allColumns = Sets.newHashSet();
        for (TableDescriptor table : tables) {
            List<String> columns = table.getColumns();
            if (allColumns.isEmpty()) {
                allColumns.addAll(columns);
                continue;
            }

            for (String col : columns) {
                if (allColumns.contains(col)) {
                    duplicateColumns.add(col);
                }
            }

            allColumns.addAll(columns);
        }

        Map<String, String> fieldMap = fields
                .stream()
                .filter(field -> !field.getType().isNewAdd())
                .collect(Collectors
                        .toMap(f -> String.format("%s.%s", f.getTableAlias(), Objects.isNull(f.getSourceFieldName()) ? f.getName() : f.getSourceFieldName()),
                                f -> f.getName()));

        Set<String> newAddFieldNames = fields.stream().filter(field -> field.getType().isNewAdd()).map(DatasetFieldDTO::getName).collect(Collectors.toSet());

        FieldPreviewVO preview = new FieldPreviewVO();

        for (TableDescriptor table : tables) {
            List<String> columns = table.getColumns();
            for (String col : columns) {
                String tableAndColumn = String.format("%s.%s", table.getAlias(), col);
                if (isSaved && fieldMap.containsKey(tableAndColumn)) {
                    preview.putField(table.getAlias(), col, fieldMap.get(tableAndColumn));
                    continue;
                }
                if (duplicateColumns.contains(col) || newAddFieldNames.contains(col)) {
                    preview.putField(table.getAlias(), col, String.format("%s_%s", col, table.getAlias()));
                } else {
                    preview.putField(table.getAlias(), col, col);
                }
            }

            allColumns.addAll(columns);
        }

        return preview;
    }

    private Set<Long> getAuthorizeDatasetIds(DatasetQuery datasetQuery, ConditionQuery condition) {
        if (sysUserService.isSuperAdmin(LoginContextHolder.getUsername())) {
            List<DatasetDTO> datasetDTOList = datasetMapper.selectOnlineDataset(datasetQuery.getWorkspaceId());
            return datasetDTOList.stream().map(DatasetDTO::getId).collect(Collectors.toSet());
        }
        WorkspaceUserResourceDTO workspaceUserResourceDTO = workspaceUserResourceService.get(datasetQuery.getWorkspaceId());
        if (workspaceUserResourceDTO.getResourceCodes().contains("DATASET:ANALYSIS:ALL:WORKSPACE")) {
            List<DatasetDTO> datasetDTOList = datasetMapper.selectOnlineDataset(datasetQuery.getWorkspaceId());
            return datasetDTOList.stream().map(DatasetDTO::getId).collect(Collectors.toSet());
        }

        List<DatasetAuthorizeDTO> activedAuthorize = datasetAuthorizeMapper.findActivedAuthorize(datasetQuery.getUsername(), datasetQuery.getWorkspaceId());
        Set<Long> datasetIds = activedAuthorize.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());
        List<DatasetDTO> creatorByMe = datasetMapper.findMyCreate(condition);
        datasetIds.addAll(creatorByMe.stream().map(DatasetDTO::getId).collect(Collectors.toSet()));
        return datasetIds;

    }
}
