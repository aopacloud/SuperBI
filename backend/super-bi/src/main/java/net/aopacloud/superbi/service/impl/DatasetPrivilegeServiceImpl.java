package net.aopacloud.superbi.service.impl;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.cache.AuthorizeThreadLocalCache;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.mapper.DatasetMapper;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.dto.DatasetPrivilege;
import net.aopacloud.superbi.model.dto.QueryPrivilege;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.service.DatasetPrivilegeService;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.service.WorkspaceUserResourceService;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: hudong
 * @date: 2023/9/7
 * @description:
 */
@Service
@RequiredArgsConstructor
public class DatasetPrivilegeServiceImpl implements DatasetPrivilegeService {

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final SysUserService sysUserService;

    private final DatasetMapper datasetMapper;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final DatasetConverter datasetConverter;

    @Override
    public PermissionEnum findDatasetMixedPermission(DatasetDTO dataset) {
        return findDatasetMixedPermission(dataset.getId(), LoginContextHolder.getUsername());
    }

    @Override
    public PermissionEnum findDatasetMixedPermission(Long datasetId, String username) {

        boolean isSuperAdmin = sysUserService.isSuperAdmin(username);
        if (isSuperAdmin) {
            return PermissionEnum.WRITE;
        }

        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset.getCreator().equals(username)) {
            return PermissionEnum.WRITE;
        }

        if (hasWorkspaceAllPrivilege(dataset.getWorkspaceId())) {
            return PermissionEnum.READ;
        }

//        List<DatasetAuthorize> datasetAuthorizes = datasetAuthorizeMapper.selectByDatasetAndUsername(datasetId, username);
        List<DatasetAuthorize> datasetAuthorizes = getDatasetAuthorize(dataset, username);

        if (datasetAuthorizes.isEmpty()) {
            return PermissionEnum.NONE;
        }

        boolean isExpire = datasetAuthorizes.stream().allMatch(DatasetAuthorize::isExpire);
        if (isExpire) {
            return PermissionEnum.EXPIRED;
        }

        boolean writePermission = datasetAuthorizes.stream().anyMatch(datasetAuthorize -> datasetAuthorize.getPermission() == PermissionEnum.WRITE);
        if (writePermission) {
            return PermissionEnum.WRITE;
        }

        return PermissionEnum.READ;
    }

    @Override
    public List<DatasetFieldDTO> fillDatasetFieldAuthorize(DatasetDTO datasetDTO, String username) {

        List<DatasetFieldDTO> fields = datasetDTO.getFields();
        boolean isSuperAdmin = sysUserService.isSuperAdmin(username);

        // super admin and dataset creator have all permission
        if (isSuperAdmin || datasetDTO.getCreator().equals(username) || hasWorkspaceAllPrivilege(datasetDTO.getWorkspaceId())) {
            fields.stream().forEach(field -> field.setPermission(PermissionEnum.READ));
            return fields;
        }

        List<DatasetAuthorize> datasetAuthorizes = datasetAuthorizeMapper.selectActiveByDatasetAndUsername(datasetDTO.getId(), username);

        if (datasetAuthorizes.isEmpty()) {
            fields.stream().forEach(field -> field.setPermission(PermissionEnum.NONE));
            return fields;
        }

        // has table privilege or row privilege means has all column privilege
        boolean hasTablePrivilege = datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().hasAllColumnPrivilege());
        if (hasTablePrivilege) {
            fields.stream().forEach(field -> field.setPermission(PermissionEnum.READ));
            return fields;
        }

        Set<String> hasPrivilegeFields = datasetAuthorizes.stream().filter(authorize -> !Strings.isNullOrEmpty(authorize.getColumnPrivilege()))
                .flatMap(authorize -> Stream.of(authorize.getColumnPrivilege().split(",")))
                .collect(Collectors.toSet());

        fields.stream().forEach(field -> {
            if (hasPrivilegeFields.contains(field.getName())) {
                field.setPermission(PermissionEnum.READ);
            } else {
                field.setPermission(PermissionEnum.NONE);
            }
        });

        return fields;
    }

    @Override
    public QueryPrivilege checkQueryPrivilege(QueryParam queryParam, DatasetDTO dataset, String username) {

        DatasetPrivilege datasetPrivilege = checkDatasetPrivilege(dataset, username);

        return checkQueryPrivilege(datasetPrivilege, queryParam);
    }



    private DatasetPrivilege checkDatasetPrivilege(DatasetDTO dataset, String username) {

        boolean isSuperAdmin = sysUserService.isSuperAdmin(username);
        boolean hasWorkspaceAllPrivilege = hasWorkspaceAllPrivilege(dataset.getWorkspaceId());

        return checkDatasetPrivilege(dataset, username, isSuperAdmin, hasWorkspaceAllPrivilege);
    }

    public DatasetPrivilege checkDatasetPrivilege(DatasetDTO dataset, String username, boolean isSuperAdmin, boolean hasWorkspaceAllPrivilege) {


        // super admin and dataset creator have all permission
        if (isSuperAdmin || hasWorkspaceAllPrivilege || dataset.getCreator().equals(username)) {
            DatasetPrivilege result = new DatasetPrivilege()
                    .setDataset(dataset)
                    .setPass(Boolean.TRUE)
                    .setPrivilegeType(PrivilegeTypeEnum.TABLE);
            return result;
        }

        return doCheckDatasetPrivilege(dataset, username);
    }

    private DatasetPrivilege doCheckDatasetPrivilege(DatasetDTO dataset, String username) {
        DatasetPrivilege result = new DatasetPrivilege().setDataset(dataset);

//        List<DatasetAuthorize> datasetAuthorizes = datasetAuthorizeMapper.selectActiveByDatasetAndUsername(dataset.getId(), username);
        List<DatasetAuthorize> datasetAuthorizes = getDatasetAuthorize(datasetConverter.toEntity(dataset), username);

        // no dataset privilege
        if (datasetAuthorizes.isEmpty()) {
            result.setPass(Boolean.FALSE);
            return result;
        }

        result.setPass(Boolean.TRUE);
        // have table privilege
        if (datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isTablePrivilege())) {
            result.setPrivilegeType(PrivilegeTypeEnum.TABLE);
            return result;
        }

        // both row and column privilege means table privilege
        if (datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isRowPrivilege())
                && datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isColumnPrivilege())) {

            result.setPrivilegeType(PrivilegeTypeEnum.TABLE);
            return result;
        }

        // have rowColumn privilege
        if (datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isColumnRowPrivilege())) {
            Set<String> columns = datasetAuthorizes.stream()
                    .filter(authorize -> authorize.getPrivilegeType().isColumnRowPrivilege())
                    .flatMap(authorize -> Arrays.stream(authorize.getColumnPrivilege().split(",")))
                    .collect(Collectors.toSet());
            result.addColumns(columns);

            Set<String> rowPrivilege = datasetAuthorizes.stream()
                    .filter(authorize -> authorize.getPrivilegeType().isColumnRowPrivilege())
                    .map(DatasetAuthorize::getRowPrivilege)
                    .collect(Collectors.toSet());
            result.addRows(rowPrivilege);

            result.setPrivilegeType(PrivilegeTypeEnum.COLUMN_ROW);
        }

        // have column privilege means have all row privilege
        if (datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isColumnPrivilege())) {

            Set<String> columns = datasetAuthorizes.stream()
                    .filter(authorize -> authorize.getPrivilegeType().isColumnPrivilege())
                    .flatMap(authorize -> Arrays.stream(authorize.getColumnPrivilege().split(",")))
                    .collect(Collectors.toSet());

            result.addColumns(columns);

            result.getRows().clear();
            result.setPrivilegeType(PrivilegeTypeEnum.COLUMN);
        }

        if (datasetAuthorizes.stream().anyMatch(authorize -> authorize.getPrivilegeType().isRowPrivilege())) {

            Set<String> rowPrivilege = datasetAuthorizes.stream()
                    .filter(authorize -> authorize.getPrivilegeType().isRowPrivilege())
                    .map(DatasetAuthorize::getRowPrivilege)
                    .collect(Collectors.toSet());
            result.addRows(rowPrivilege);

            result.getColumns().clear();
            result.setPrivilegeType(PrivilegeTypeEnum.ROW);
        }
        return result;
    }

    public QueryPrivilege checkQueryPrivilege(DatasetPrivilege datasetPrivilege, QueryParam queryParam) {
        QueryPrivilege queryPrivilege = new QueryPrivilege();
        // no dataset privilege
        if (!datasetPrivilege.isPass()) {
            queryPrivilege.deny();
            return queryPrivilege;
        }

        switch (datasetPrivilege.getPrivilegeType()) {
            case TABLE:
                queryPrivilege.pass();
                break;
            case ROW:
                queryPrivilege.pass();
                queryPrivilege.setRowPrivileges(getRowPrivilegeExpression(datasetPrivilege.getRows()));
                break;
            case COLUMN:
                if (datasetPrivilege.getColumns().containsAll(queryParam.getUsedFieldName())) {
                    queryPrivilege.pass();
                } else {
                    queryPrivilege.deny();
                }
                break;
            case COLUMN_ROW:
                if (datasetPrivilege.getColumns().containsAll(queryParam.getUsedFieldName())) {
                    queryPrivilege.pass();
                    queryPrivilege.setRowPrivileges(getRowPrivilegeExpression(datasetPrivilege.getRows()));
                } else {
                    queryPrivilege.deny();
                }
                break;
            default:
                queryPrivilege.deny();
                break;
        }
        return queryPrivilege;
    }


    private boolean hasWorkspaceAllPrivilege(Long workspaceId) {
        return workspaceUserResourceService.getResourceCodes(workspaceId).contains(BiConsist.ALL_WORKSPACE_ANALYSIS_CODE);
    }

    private String getRowPrivilegeExpression(Set<String> rows) {
        if (Objects.isNull(rows) || rows.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String expression = Joiner.on(" or ").join(rows);
        return String.format("(%s)", expression);
    }


    private List<DatasetAuthorize> getDatasetAuthorize(Dataset dataset, String username) {

        List<DatasetAuthorize> datasetAuthorizeInWorkspace = AuthorizeThreadLocalCache.getDatasetAuthorize(dataset.getWorkspaceId());

        if(Objects.isNull(datasetAuthorizeInWorkspace)) {
             datasetAuthorizeInWorkspace = datasetAuthorizeMapper.selectActiveByWorkspaceAndUsername(dataset.getWorkspaceId(), username);
             AuthorizeThreadLocalCache.setDatasetAuthorize(dataset.getWorkspaceId(), datasetAuthorizeInWorkspace);
        }

        return datasetAuthorizeInWorkspace.stream().filter(item -> item.getDatasetId().equals(dataset.getId())).filter(item -> username.equals(item.getUsername())).collect(Collectors.toList());

    }

}
