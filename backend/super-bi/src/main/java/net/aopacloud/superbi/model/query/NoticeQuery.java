package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.NotificationTypeEnum;

/**
 * @Author shinnie
 * @Description
 * @Date 14:32 2023/9/27
 */
@Data
public class NoticeQuery extends BaseQuery {

    private NotificationTypeEnum type;

    private String username;

}
