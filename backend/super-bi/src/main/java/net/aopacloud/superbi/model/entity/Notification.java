package net.aopacloud.superbi.model.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Notification {

    private Integer id;

    private String username;

    private Integer readed;

    private String type;

    private String resourceType;

    private Long resourceId;

    private String resourceName;

    private Date createTime;

    private String content;

}