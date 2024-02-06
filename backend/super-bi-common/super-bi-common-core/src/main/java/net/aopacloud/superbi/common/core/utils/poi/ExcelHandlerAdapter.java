package net.aopacloud.superbi.common.core.utils.poi;

/**
 * Excel data format processing adapter
 */
public interface ExcelHandlerAdapter {
    /**
     * format data
     *
     * @param value cell value
     * @param args  excel annotation args
     * @return
     */
    Object format(Object value, String[] args);
}
