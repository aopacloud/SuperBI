package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.enums.NotificationTypeEnum;
import lombok.Data;

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
