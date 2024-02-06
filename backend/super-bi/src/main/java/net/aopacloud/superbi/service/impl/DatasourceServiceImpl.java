package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.DatasourceMapper;
import net.aopacloud.superbi.model.converter.DatasourceConverter;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.Datasource;
import net.aopacloud.superbi.queryEngine.executor.JdbcExecutor;
import net.aopacloud.superbi.queryEngine.executor.QueryExecutorFactory;
import net.aopacloud.superbi.queryEngine.metadata.MetaDataBackend;
import net.aopacloud.superbi.service.DatasetService;
import net.aopacloud.superbi.service.DatasourceService;
import net.aopacloud.superbi.utils.DatasourceHelper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author shinnie
 * @Description
 * @Date 10:54 2023/8/28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatasourceServiceImpl implements DatasourceService {

    private final DatasourceMapper datasourceMapper;

    private final DatasourceConverter datasourceConverter;

    private final MetaDataBackend metaDataBackend;

    private final DatasetService datasetService;

    private final QueryExecutorFactory queryExecutorFactory;

    @Override
    public List<DatasourceDTO> listAll(Long workspaceId, String keyword) {
        return datasourceMapper.selectAllByWorkspace(workspaceId, keyword);
    }

    @Override
    public DatasourceDTO getOne(Long id) {
        DatasourceDTO datasourceDTO = datasourceMapper.selectOneById(id);
        datasourceDTO.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
        return datasourceDTO;
    }

    @Override
    public DatasourceDTO getOneByWorkspaceAndName(Long workspaceId, String name) {
        DatasourceDTO datasourceDTO = Optional.ofNullable(datasourceMapper.selectByNameAndWorkspace(name, workspaceId)).orElseThrow(() -> new ServiceException("Object not found"));
        datasourceDTO.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
        return datasourceDTO;
    }

    @Override
    public DatasourceDTO save(DatasourceDTO datasourceDTO) {
        try {
            datasourceDTO.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
            datasourceDTO.setCreator(LoginContextHolder.getUsername());
            Datasource entity = datasourceConverter.toEntity(datasourceDTO);
            datasourceMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR, datasourceDTO.getName()));
        }
        return datasourceDTO;
    }

    @Override
    public DatasourceDTO update(DatasourceDTO datasourceDTO) {
        try {
            datasourceDTO.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
            datasourceMapper.update(datasourceConverter.toEntity(datasourceDTO));
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR, datasourceDTO.getName()));
        }
        return datasourceDTO;
    }

    @Override
    public DatasourceDTO delete(Long id) {
        DatasourceDTO datasource = datasourceMapper.selectOneById(id);
        datasourceMapper.deleteById(id);
        return datasource;
    }

    @Override
    public ConnectResultDTO connectTest(DatasourceDTO datasourceDTO) {
        JdbcExecutor executor = (JdbcExecutor) queryExecutorFactory.getQueryExecutor(datasourceDTO.getEngine());
        ConnectionParamDTO connectionParamDTO = new ConnectionParamDTO();
        BeanUtils.copyProperties(datasourceDTO, connectionParamDTO);
        connectionParamDTO.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
        try {
            executor.getConnection(connectionParamDTO);
            return ConnectResultDTO.success();
        } catch (Exception e) {
            log.error("connect database error", e);
            return ConnectResultDTO.fail(e.getMessage());
        }
    }

    @Override
    public List<TableInfoDTO> listTableInfo(Long datasourceId) {

        DatasourceDTO datasource = datasourceMapper.selectOneById(datasourceId);
        List<TableSchemaDTO> tableSchemas = metaDataBackend.getTableSchemas(datasource);
        return tableSchemas.stream().map(tableSchema -> {
            TableInfoDTO tableInfoDTO = new TableInfoDTO();
            BeanUtils.copyProperties(tableSchema, tableInfoDTO);
            tableInfoDTO.setDatasetCount(datasetService.countByTable(datasource.getWorkspaceId(), datasource.getName(), tableSchema.getTableName()));
            return tableInfoDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public TableInfoDTO getTableInfo(Long datasourceId, String tableName) {

        DatasourceDTO datasource = datasourceMapper.selectOneById(datasourceId);
        TableSchemaDTO tableSchemaDTO = metaDataBackend.getTableSchema(datasource, tableName);
        TableInfoDTO tableInfoDTO = new TableInfoDTO();
        BeanUtils.copyProperties(tableSchemaDTO, tableInfoDTO);
        return tableInfoDTO;
    }

    @Override
    public List<DatasetDTO> getDatasetByDatasourceAndTable(Long id, String tableName) {
        return datasetService.listByDatasourceAndTable(id, tableName);
    }
}
