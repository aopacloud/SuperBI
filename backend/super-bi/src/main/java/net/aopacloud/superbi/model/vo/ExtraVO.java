package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.PermissionEnum;

/**
 * @author: hudong
 * @date: 2023/9/7
 * @description:
 */
@Data
public class ExtraVO {

    private PermissionEnum permission;

    private boolean favorite;

    private boolean applying;

    private boolean expired;

    private boolean createByMe;

}
