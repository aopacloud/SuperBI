package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.PositionEnum;
import lombok.Data;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 11:56 2023/9/13
 */
@Data
public class FavoriteVO {

    private Integer id;

    private PositionEnum position;

    private Integer targetId;

    private String username;

    private Date createTime;

    private Date updateTime;

}
