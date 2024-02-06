package net.aopacloud.superbi.model.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetFieldValueQueryParam {

    private Long datasetId;

    private String fieldName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetFieldValueQueryParam that = (DatasetFieldValueQueryParam) o;
        return Objects.equals(datasetId, that.datasetId) && Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, fieldName);
    }
}
