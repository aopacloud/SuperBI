package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.AuthRoleDTO;
import net.aopacloud.superbi.model.entity.AuthRole;
import net.aopacloud.superbi.model.query.AuthRoleQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthRoleMapper {

    List<AuthRoleDTO> selectAllByWorkspace(@Param("workspaceId") Long workspaceId, @Param("keyword") String keyword);

    AuthRole selectByNameByWorkspace(@Param("name") String name, @Param("workspaceId") Long workspaceId);

    void insert(AuthRole authRole);

    void update(AuthRole authRole);

    AuthRole selectOneById(Long id);

    void deleteById(Long id);

    Integer countByWorkspace(Long workspaceId);

    List<AuthRoleDTO> search(@Param("keyword") String keyword);

    List<AuthRoleDTO> searchByUsername(@Param("usernames") List<String> usernames);

    List<AuthRoleDTO> searchByQuery(AuthRoleQuery query);
}