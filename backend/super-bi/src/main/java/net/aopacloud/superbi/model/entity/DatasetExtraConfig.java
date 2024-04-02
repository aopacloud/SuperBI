package net.aopacloud.superbi.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetExtraConfig {

    private Long id;

    private Long datasetId;

    private Integer version;

    private String content;

    private Date createTime;

    private Date updateTime;

    public DatasetExtraConfig(Long datasetId, Integer version, String content) {
        this.datasetId = datasetId;
        this.version = version;
        this.content = content;
    }
}
