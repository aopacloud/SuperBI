package net.aopacloud.superbi.queryEngine;

import net.aopacloud.superbi.enums.DataTypeEnum;

public class DataTypeConverter {

    public static DataTypeEnum convert(String databaseDataType) {

        if (databaseDataType.startsWith("Decimal")) {
            return DataTypeEnum.NUMBER;
        }

        switch (databaseDataType) {
            case "Int8":
            case "Nullable(Int8)":
            case "Int16":
            case "Nullable(Int16)":
            case "Int32":
            case "Nullable(Int32)":
            case "Int64":
            case "Nullable(Int64)":
            case "UInt8":
            case "Nullable(UInt8)":
            case "UInt16":
            case "Nullable(UInt16)":
            case "UInt32":
            case "Nullable(UInt32)":
            case "UInt64":
            case "Nullable(UInt64)":
            case "Float32":
            case "Nullable(Float32)":
            case "Float64":
            case "Nullable(Float64)":
            case "INT":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "BIGINT":
            case "INT UNSIGNED":
            case "FLOAT":
            case "DOUBLE":
            case "NUMERIC":
            case "DECIMAL":
                return DataTypeEnum.NUMBER;
            case "Date":
            case "Date32":
            case "DATE":
                return DataTypeEnum.TIME_YYYYMMDD;
            case "DateTime":
            case "DateTime64":
            case "TIMESTAMP":
            case "DATETIME":
                return DataTypeEnum.TIME_YYYYMMDD_HHMMSS;
            case "String":
            case "UUID":
            case "CHAR":
            case "VARCHAR":
            default:
                return DataTypeEnum.TEXT;
        }
    }

    public static String dataType2DbConvert(DataTypeEnum dataTypeEnum) {
        switch (dataTypeEnum) {
            case NUMBER:
                return "Float64";
            case TIME:
            case TIME_YYYYMMDD:
                return "Date";
            case TIME_YYYYMMDD_HHMMSS:
                return "DateTime";
            case TEXT:
            default:
                return "String";
        }
    }

}
