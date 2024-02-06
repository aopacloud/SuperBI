package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 11:57 2023/9/13
 */
@Data
public class FavoriteDTO {

    private Integer id;

    private PositionEnum position;

    private Integer targetId;

    private String username;

    private Date createTime;

    private Date updateTime;

}
