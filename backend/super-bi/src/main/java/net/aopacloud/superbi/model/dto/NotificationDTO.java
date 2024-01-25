package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 17:04 2023/9/25
 */
@Data
public class NotificationDTO {

    private Integer id;

    private String username;

    private Integer readed;

    private String type;

    private String resourceType;

    private Integer resourceId;

    private String resourceName;

    private Date createTime;

    private String content;

}
