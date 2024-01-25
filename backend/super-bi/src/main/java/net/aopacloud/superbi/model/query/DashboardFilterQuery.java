package net.aopacloud.superbi.model.query;

import lombok.Data;

import java.util.Objects;

@Data
public class DashboardFilterQuery {

    private Long datasetId;

    private String fieldName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardFilterQuery that = (DashboardFilterQuery) o;
        return Objects.equals(datasetId, that.datasetId) && Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, fieldName);
    }
}
