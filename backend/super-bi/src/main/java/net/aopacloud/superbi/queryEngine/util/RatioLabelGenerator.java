package net.aopacloud.superbi.queryEngine.util;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.enums.DateTruncEnum;
import net.aopacloud.superbi.queryEngine.enums.RatioPeriodEnum;
import net.aopacloud.superbi.queryEngine.enums.RatioTypeEnum;
import net.aopacloud.superbi.service.FunctionService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class RatioLabelGenerator {

    private static Properties properties;

    static {
        try(InputStream in = FunctionService.class.getClassLoader().getResourceAsStream(BiConsist.RATIO_LABEL_FILE);
            InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8)
        ) {

            properties = new Properties();
            properties.load(reader);

        } catch (Exception e) {
            log.error("load ratio label error", e);
        }
    }

    public static String getLabel(DateTruncEnum dateTrunc, RatioTypeEnum ratioType, RatioPeriodEnum period) {

        if(Objects.isNull(ratioType)) {
            return StringUtils.EMPTY;
        }

        if(Objects.isNull(dateTrunc)) {
            dateTrunc = DateTruncEnum.ORIGIN;
        }

        if(Objects.isNull(period)) {
            period = RatioPeriodEnum.SAME;
        }

        String key = String.format("%s-%s-%s",ratioType.name(), dateTrunc.name(), period.name());
        return properties.getProperty(key, StringUtils.EMPTY);
    }

}
