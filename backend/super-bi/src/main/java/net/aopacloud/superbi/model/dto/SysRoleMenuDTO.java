package net.aopacloud.superbi.model.dto;

import lombok.Data;

@Data
public class SysRoleMenuDTO {

        private Long id;

        private Long roleId;

        private Long menuId;

        private String creator;

        private String createTime;

        private String operator;

        private String updateTime;
}
