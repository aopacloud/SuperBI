package net.aopacloud.superbi.queryEngine.sql;

import com.google.common.base.Strings;
import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;

/**
 * sql segment.
 *
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
public class Segment {

    /**
     * sql segment name.
     */
    private String name;

    private String aggregator;

    /**
     * sql segment expression that will be used in sql.
     */
    private String expression;

    private String alias;

    public Segment(String name, String aggregator, String expression, String alias) {
        this.name = name;
        this.aggregator = aggregator;
        this.expression = expression;
        this.alias = alias;
    }
    public Segment(String name, String aggregator, String expression) {
        this.name = name;
        this.aggregator = aggregator;
        this.expression = expression;
    }

    public Segment(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public Segment(String expression) {
        this.expression = expression;
    }

    public String getAlias() {
        if (!Strings.isNullOrEmpty(alias)) {
            return alias;
        }
        if (aggregator == null) {
            return String.format("`%s%s`", name, BiConsist.FIELD_AGGREGATOR_SEPARATOR);
        }
        return String.format("`%s%s%s`", name, BiConsist.FIELD_AGGREGATOR_SEPARATOR, aggregator);
    }

    public String getExpressionWithAlias() {
        return String.format("%s as %s", expression, getAlias());
    }
}
