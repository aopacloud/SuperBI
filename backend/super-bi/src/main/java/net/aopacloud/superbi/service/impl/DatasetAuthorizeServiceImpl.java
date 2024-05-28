package net.aopacloud.superbi.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.EventActionEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.listener.event.DatasetAuthorizeUpdateEvent;
import net.aopacloud.superbi.mapper.AuthRoleMapper;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.model.converter.DatasetAuthorizeConverter;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.entity.AuthRole;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;
import net.aopacloud.superbi.model.query.DatasetAuthorizeBatchQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeSaveVO;
import net.aopacloud.superbi.model.vo.DatasetAuthorizeVO;
import net.aopacloud.superbi.queryEngine.enums.LogicalEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.OperatorParam;
import net.aopacloud.superbi.service.AuthRoleUserService;
import net.aopacloud.superbi.service.DatasetAuthorizeService;
import net.aopacloud.superbi.service.DatasetService;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.util.JSONUtils;
import org.assertj.core.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetAuthorizeServiceImpl implements DatasetAuthorizeService {

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final DatasetAuthorizeConverter converter;

    private final SysUserService sysUserService;

    private final AuthRoleMapper authRoleMapper;

    private final ApplicationContext applicationContext;

    private final DatasetService datasetService;

    private final AuthRoleUserService authRoleUserService;

    @Override
    public List<DatasetAuthorizeDTO> searchUserAuthorize(DatasetAuthorizeQuery query) {

        List<DatasetAuthorizeDTO> authorizes = datasetAuthorizeMapper.searchUserAuthorize(query);
        authorizes.stream().forEach(authorize -> authorize.setAliasName(sysUserService.getUserAliasName(authorize.getUsername())));
        return authorizes;
    }

    @Override
    public List<DatasetAuthorizeDTO> searchRoleAuthorize(DatasetAuthorizeQuery query) {
        List<DatasetAuthorizeDTO> authorizes = datasetAuthorizeMapper.searchRoleAuthorize(query);
        return authorizes;
    }

    @Override
    public List<DatasetAuthorizeDTO> save(DatasetAuthorizeSaveVO datasetAuthorizeSaveVO) {

        List<DatasetAuthorize> authorizes = Lists.newArrayList();
        List<String> usernames = datasetAuthorizeSaveVO.getUsernames();
        if (!Objects.isNull(usernames) && !usernames.isEmpty()) {
            usernames.stream().forEach(username -> {
                DatasetAuthorize authorize = create(datasetAuthorizeSaveVO, AuthorizeScopeEnum.USER, username, null);
                authorizes.add(authorize);
            });
        }

        List<Long> roleIds = datasetAuthorizeSaveVO.getRoleIds();
        if (!Objects.isNull(roleIds) && !roleIds.isEmpty()) {
            roleIds.stream().forEach(roleId -> {
                DatasetAuthorize authorize = create(datasetAuthorizeSaveVO, AuthorizeScopeEnum.ROLE, null, roleId);
                authorizes.add(authorize);
            });
        }

        authorizes.stream().forEach(datasetAuthorize -> datasetAuthorizeMapper.insert(datasetAuthorize));
        authorizes.stream().forEach(datasetAuthorize -> {
            if (datasetAuthorize.getScope() == AuthorizeScopeEnum.ROLE) {
                List<String> users = authRoleUserService.getUserByRole(datasetAuthorize.getRoleId());
                for (String user : users) {
                    applicationContext.publishEvent(DatasetAuthorizeUpdateEvent.of(user, datasetAuthorize, EventActionEnum.CREATE));
                }
            } else {
                applicationContext.publishEvent(DatasetAuthorizeUpdateEvent.of(datasetAuthorize, EventActionEnum.CREATE));
            }
        });

        return converter.entityToDTOList(authorizes);
    }

    @Override
    public DatasetAuthorizeDTO save(DatasetAuthorizeDTO datasetAuthorizeDTO) {
        DatasetAuthorize datasetAuthorize = converter.toEntity(datasetAuthorizeDTO);
        datasetAuthorizeMapper.insert(datasetAuthorize);

        return converter.entityToDTO(datasetAuthorize);
    }

    @Override
    public DatasetAuthorizeDTO update(DatasetAuthorizeDTO datasetAuthorizeDTO) {

        DatasetAuthorize datasetAuthorize = converter.toEntity(datasetAuthorizeDTO);

        DatasetAuthorize oldAuthorize = datasetAuthorizeMapper.selectOneById(datasetAuthorizeDTO.getId());
        oldAuthorize.setColumnPrivilege(datasetAuthorize.getColumnPrivilege());
        oldAuthorize.setPrivilegeType(datasetAuthorize.getPrivilegeType());
        oldAuthorize.setExpireDuration(datasetAuthorize.getExpireDuration());
        oldAuthorize.setRowParam(datasetAuthorize.getRowParam());

        if (!Objects.isNull(datasetAuthorizeDTO.getStartTime())) {
            oldAuthorize.setStartTime(datasetAuthorizeDTO.getStartTime());
        }

        if (oldAuthorize.isExpire()) {
            oldAuthorize.setStartTime(new Date());
        }
        if (datasetAuthorize.getPrivilegeType().hasRowPrivilege()) {
            DatasetDTO dataset = datasetService.findOne(datasetAuthorize.getDatasetId());
            oldAuthorize.setRowPrivilege(parseRowPrivilege(JSONUtils.parseObject(datasetAuthorizeDTO.getRowParam(), DatasetAuthorizeVO.Rows.class), dataset));
        }

        datasetAuthorizeMapper.update(oldAuthorize);

        applicationContext.publishEvent(DatasetAuthorizeUpdateEvent.of(oldAuthorize, EventActionEnum.UPDATE));
        return datasetAuthorizeDTO;
    }

    @Override
    public DatasetAuthorizeDTO delete(Long id) {

        DatasetAuthorizeDTO datasetAuthorizeDTO = findOne(id);

        datasetAuthorizeMapper.softDelete(id);

        applicationContext.publishEvent(DatasetAuthorizeUpdateEvent.of(converter.toEntity(datasetAuthorizeDTO), EventActionEnum.DELETE));

        return datasetAuthorizeDTO;
    }

    @Override
    public DatasetAuthorizeDTO renewed(DatasetAuthorizeDTO datasetAuthorizeDTO) {
        DatasetAuthorize oldAuthorize = datasetAuthorizeMapper.selectOneById(datasetAuthorizeDTO.getId());
        Long expiredTime = oldAuthorize.getStartTime().getTime() + (datasetAuthorizeDTO.getExpireDuration() * 1000);
        Date startTime = new Date(Math.max(expiredTime, new Date().getTime()));
        datasetAuthorizeDTO.setStartTime(startTime);

        return update(datasetAuthorizeDTO);
    }

    @Override
    public DatasetAuthorizeDTO findOne(Long id) {

        DatasetAuthorize datasetAuthorize = datasetAuthorizeMapper.selectOneById(id);
        DatasetAuthorizeDTO datasetAuthorizeDTO = converter.entityToDTO(datasetAuthorize);
        if (datasetAuthorize.getScope() == AuthorizeScopeEnum.USER) {
            datasetAuthorizeDTO.setAliasName(sysUserService.getUserAliasName(datasetAuthorizeDTO.getUsername()));
        } else {
            AuthRole authRole = authRoleMapper.selectOneById(datasetAuthorizeDTO.getRoleId());
            datasetAuthorizeDTO.setRoleName(authRole.getName());
        }
        return datasetAuthorizeDTO;
    }

    @Override
    public DatasetAuthorizeDTO findLastOne(Long datasetId, String username) {
        List<DatasetAuthorize> authorizes = datasetAuthorizeMapper.selectUserAuthorizeByDatasetAndUsername(datasetId, username);
        return authorizes.stream().findFirst().map(authorize -> converter.entityToDTO(authorize)).orElse(null);
    }

    @Override
    public List<DatasetAuthorizeDTO> findMaybeExpire() {
        return datasetAuthorizeMapper.selectMaybeExpire();
    }

    @Override
    public void updateAuthorizeExpire(Long id) {
        datasetAuthorizeMapper.updateAuthorizeExpire(id);
    }

    @Override
    public List<DatasetAuthorizeDTO> findActivedAuthorize(String username, Long workspaceId) {
        return datasetAuthorizeMapper.findActivedAuthorize(username, workspaceId);
    }

    @Override
    public DatasetAuthorizeDTO updatePermission(DatasetAuthorizeDTO datasetAuthorizeDTO) {

        DatasetAuthorizeDTO oldDatasetAuthorize = findOne(datasetAuthorizeDTO.getId());
        oldDatasetAuthorize.setPermission(datasetAuthorizeDTO.getPermission());

        datasetAuthorizeMapper.update(converter.toEntity(oldDatasetAuthorize));
        return oldDatasetAuthorize;
    }

    @Override
    public List<DatasetAuthorizeDTO> findAuthorizeByRole(Long roleId) {
        return datasetAuthorizeMapper.selectAuthorizeByRole(roleId);
    }

    @Override
    public List<DatasetAuthorizeDTO> findAuthorizeByUsername(String username) {
        return datasetAuthorizeMapper.selectAuthorizeByUsername(username);
    }

    @Override
    public List<DatasetAuthorizeDTO> findAuthorizeByDataset(Long datasetId) {
        return datasetAuthorizeMapper.selectAuthorizeByDataset(datasetId);
    }

    @Override
    public List<DatasetAuthorizeDTO> search(DatasetAuthorizeBatchQuery query) {
        return datasetAuthorizeMapper.search(query);
    }

    @Override
    public List<DatasetAuthorizeDTO> findAuthorizeByDatasetAndUsernameAndRole(Set<Long> datasetIds, String username, Long roleId) {
        if(Objects.isNull(datasetIds) || datasetIds.isEmpty()) {
            return Lists.newArrayList();
        }

        if(!Strings.isNullOrEmpty(username)) {
            return datasetAuthorizeMapper.selectAuthorizeByDatasetsAndUsername(datasetIds, username);
        } else if(Objects.nonNull(roleId)) {
            return datasetAuthorizeMapper.selectAuthorizeByDatasetsAndRole(datasetIds, roleId);
        } else {
            return Lists.newArrayList();
        }

    }

    @Override
    public void deleteByUsername(String username) {
       log.warn("delete {} all dataset authorize", username);
       datasetAuthorizeMapper.deleteByUsername(username);
    }

    public DatasetAuthorize create(DatasetAuthorizeSaveVO datasetAuthorizeSaveVO, AuthorizeScopeEnum scope, String username, Long roleId) {

        DatasetAuthorize authorize = new DatasetAuthorize();
        authorize.setPermission(PermissionEnum.READ);
        authorize.setDatasetId(datasetAuthorizeSaveVO.getDatasetId());
        authorize.setPrivilegeType(datasetAuthorizeSaveVO.getPrivilegeType());
        authorize.setColumnPrivilege(datasetAuthorizeSaveVO.getColumnPrivilege());
        authorize.setExpireDuration(datasetAuthorizeSaveVO.getExpireDuration());
        if (datasetAuthorizeSaveVO.getPrivilegeType().hasRowPrivilege()) {
            authorize.setRowParam(JSONUtils.toJsonString(datasetAuthorizeSaveVO.getRows()));
            DatasetDTO datasetDTO = datasetService.findOne(datasetAuthorizeSaveVO.getDatasetId());
            authorize.setRowPrivilege(parseRowPrivilege(datasetAuthorizeSaveVO.getRows(), datasetDTO));
        }
        authorize.setStartTime(new Date());
        if (!Strings.isNullOrEmpty(username)) {
            authorize.setUsername(username);
        }
        if (!Objects.isNull(roleId)) {
            authorize.setRoleId(roleId);
        }
        authorize.setScope(scope);
        authorize.setOperator(LoginContextHolder.getUsername());

        return authorize;
    }

    public String parseRowPrivilege(DatasetAuthorizeVO.Rows rows, DatasetDTO datasetDTO) {

        Map<String, DatasetFieldDTO> fieldMap = datasetDTO.getFields().stream().collect(Collectors.toMap(field -> field.getName(), field -> field));

        LogicalEnum relation = LogicalEnum.valueOf(rows.getRelation().toUpperCase());

        List<String> wideExpression = rows.getChildren().stream().map(row -> {

            LogicalEnum childRelation = LogicalEnum.valueOf(row.getRelation().toUpperCase());

            List<String> expressions = row.getChildren().stream().map(condition -> {
                List<String> value = condition.getValue();

                DatasetFieldDTO field = fieldMap.get(condition.getField());

                String fieldName = field.getType().isNewAdd() ? String.format("[%s]", field.getName()) : field.getName();

                OperatorParam param = new OperatorParam(value, fieldName, field);
                FunctionalOperatorEnum operator = condition.getOperator();
                if ("ENUM".equals(condition.getType())) {
                    operator = FunctionalOperatorEnum.IN;
                }
                String expression = operator.getOperator().apply(param);

                return String.format("( %s )", expression);
            }).collect(Collectors.toList());

            return String.format(" ( %s ) ", Joiner.on(childRelation.getExpression()).join(expressions));
        }).collect(Collectors.toList());
        return Joiner.on(relation.getExpression()).join(wideExpression);
    }
}
