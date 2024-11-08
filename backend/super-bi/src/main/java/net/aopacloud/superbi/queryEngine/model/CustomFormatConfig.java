package net.aopacloud.superbi.queryEngine.model;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.common.core.utils.JsonUtils;
import net.aopacloud.superbi.enums.DataFormatEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFormatConfig {

    public static final CustomFormatConfig INTEGER = new CustomFormatConfig(0, false, 0);
    public static final CustomFormatConfig DECIMAL_1 = new CustomFormatConfig(0, false, 1);
    public static final CustomFormatConfig DECIMAL_2 = new CustomFormatConfig(0, false, 2);
    public static final CustomFormatConfig PERCENT = new CustomFormatConfig(1, false, 0);
    public static final CustomFormatConfig PERCENT_DECIMAL_1 = new CustomFormatConfig(1, false, 1);
    public static final CustomFormatConfig PERCENT_DECIMAL_2 = new CustomFormatConfig(1, false, 2);

    /**
     * 0: number, 1: percent
     */
    private int type;

    /**
     *
     */
    private boolean thousand;

    /**
     * retain
     */
    private int digit;


    public boolean isNumber() {
        return type == 0;
    }

    public String getFormatString() {

        StringBuilder builder = new StringBuilder();
        if (thousand) {
            builder.append("#,##");
        }

        builder.append("0");

        if (digit > 0) {
            builder.append(".");
            for (int i = 0; i < digit; i++) {
                builder.append("0");
            }
        }

        if (!isNumber()) {
            builder.append("%");
        }

        return builder.toString();
    }

    public String format(Double value) {
        if(!isNumber()) {
            value = value * 100;
        }
        String formatStr = String.format("%%.%df", digit);
        return isNumber() ? String.format(formatStr, value) : String.format("%s%%", String.format(formatStr, value));
    }

    public static CustomFormatConfig of(DatasetFieldDTO field) {
        DataFormatEnum dataFormat = DataFormatEnum.ORIGINAL;
        if (Strings.isNullOrEmpty(field.getDataFormat())) {
            dataFormat = DataFormatEnum.ORIGINAL;
        } else {
            dataFormat = DataFormatEnum.valueOf(field.getDataFormat());
        }
        if (dataFormat.hasFormat()) {
            if(dataFormat == DataFormatEnum.CUSTOM) {
                if (!Strings.isNullOrEmpty(field.getCustomFormatConfig())) {
                    CustomFormatConfig config = JsonUtils.fromJson(field.getCustomFormatConfig(), CustomFormatConfig.class);
                    return config;
                }
            } else {
                dataFormat.getFormatConfig();
            }
        }
        return dataFormat.getFormatConfig();
    }
}
