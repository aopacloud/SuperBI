package net.aopacloud.superbi.common.core.annotation;

import net.aopacloud.superbi.common.core.utils.poi.ExcelHandlerAdapter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * excel export data annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {
    /**
     * sort when export
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * column name
     */
    String name() default "";

    /**
     * date format (default: yyyy-MM-dd HH:mm:ss)
     */
    String dateFormat() default "";

    /**
     * read and converter value with expression
     * eg. 0=man,1=woman,2=unknown
     */
    String readConverterExp() default "";

    /**
     * separator
     */
    String separator() default ",";

    /**
     * when scale = -1, the value will not be formatted with bigdecimal
     */
    int scale() default -1;

    /**
     * big decimal value round mode default :BigDecimal.ROUND_HALF_EVEN
     */
    int roundingMode() default BigDecimal.ROUND_HALF_EVEN;

    /**
     * cell height
     */
    double height() default 14;

    /**
     * cell width
     */
    double width() default 16;

    /**
     * string value suffix
     */
    String suffix() default "";

    /**
     * when value is null, use default value
     */
    String defaultValue() default "";

    /**
     * prompt information
     */
    String prompt() default "";

    /**
     * only valid for combo box
     */
    String[] combo() default {};

    /**
     * for list type value, merge cells vertically when needMerge is true
     */
    boolean needMerge() default false;

    /**
     * 是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.
     */
    boolean isExport() default true;

    /**
     * get value from another object, support multi-level access, separated by dot. eg "user.name"
     */
    String targetAttr() default "";

    /**
     * append a row of total data at the end when isStatistics is true
     */
    boolean isStatistics() default false;

    /**
     * cell type (0: numeric 1: string)
     */
    ColumnType cellType() default ColumnType.STRING;

    /**
     * header background color
     */
    IndexedColors headerBackgroundColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * header font color
     */
    IndexedColors headerColor() default IndexedColors.WHITE;

    /**
     *  cell background color
     */
    IndexedColors backgroundColor() default IndexedColors.WHITE;

    /**
     * cell font color
     */
    IndexedColors color() default IndexedColors.BLACK;

    /**
     * align type , default center
     */
    HorizontalAlignment align() default HorizontalAlignment.CENTER;

    /**
     * custom data handler
     */
    Class<?> handler() default ExcelHandlerAdapter.class;

    /**
     * custom data handler arguments
     */
    String[] args() default {};

    /**
     * field type ;
     * ALL : effect both export and import
     * EXPORT : only effect export
     * IMPORT : only effect import
     *
     */
    Type type() default Type.ALL;

    enum Type {
        ALL(0), EXPORT(1), IMPORT(2);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    enum ColumnType {
        NUMERIC(0), STRING(1), IMAGE(2);
        private final int value;

        ColumnType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}