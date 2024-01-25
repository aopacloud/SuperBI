package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.PermissionEnum;
import lombok.Data;

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
