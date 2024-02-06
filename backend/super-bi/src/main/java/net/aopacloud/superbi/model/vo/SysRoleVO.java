package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.SystemRoleTypeEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 10:50 2023/10/11
 */
@Data
public class SysRoleVO {

    private Long id;

    private String name;

    private String description;

    private SystemRoleTypeEnum roleType;

    private String creator;

    private String extra;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
