package net.aopacloud.superbi.model.domain;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.queryEngine.model.QueryColumn;
import net.aopacloud.superbi.util.Downloader;
import net.aopacloud.superbi.util.ExcelToImage;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.assertj.core.util.Strings;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/10/31
 * @description:
 */
@Slf4j
public class ExcelBuilder {

    private SXSSFWorkbook excel;

    private Sheet sheet;

    private String fileName;

    private QueryColumn[] columns;

    private int columnNum;

    public ExcelBuilder(String fileName, List<QueryColumn> columns) {
        excel = new SXSSFWorkbook(1000);
        excel.setCompressTempFiles(true);
        sheet = excel.createSheet();
        this.fileName = fileName;
        this.columns = columns.toArray(new QueryColumn[0]);
    }

    public ExcelBuilder(String fileName) {
        excel = new SXSSFWorkbook(1000);
        excel.setCompressTempFiles(true);
        sheet = excel.createSheet();
        this.fileName = fileName;
    }

    public ExcelBuilder() {
        excel = new SXSSFWorkbook(1000);
        excel.setCompressTempFiles(true);
        sheet = excel.createSheet();
    }

    public void setTitle(List<String> titles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < titles.size(); i++) {
            row.createCell(i).setCellValue(titles.get(i));
        }
        this.columnNum = titles.size();
    }

    public void setData(List<Object[]> data) {
        if (Objects.isNull(this.columns) || columns.length == 0) {
            setDataWithDefaultFormat(data);
        } else {
            setDataWithCustomFormat(data);
        }

    }

    public void setDataWithDefaultFormat(List<Object[]> data) {

        for (int rowNum = 0; rowNum < data.size(); rowNum++) {
            Row row = sheet.createRow(rowNum + 1);
            Object[] rowData = data.get(rowNum);

            for (int columnNum = 0; columnNum < rowData.length; columnNum++) {

                Cell cell = row.createCell(columnNum);
                Object cellData = rowData[columnNum];
                if (Objects.isNull(cellData) || Strings.isNullOrEmpty(cellData.toString())) {
                    cell.setCellValue("");
                    continue;
                }

                if (NumberUtils.isCreatable(cellData.toString())) {
                    try {
                        if (cellData.toString().length() <= 15) {
                            if (cellData.toString().contains(".")) {
                                cell.setCellValue(NumberUtils.toDouble(cellData.toString()));
                            } else {
                                BigDecimal bigDecimal = new BigDecimal(cellData.toString());
                                cell.setCellValue(bigDecimal.setScale(0, BigDecimal.ROUND_DOWN).doubleValue());
                            }
                        } else {
                            cell.setCellValue(cellData.toString());
                        }
                    } catch (Exception e) {
                        cell.setCellValue(cellData.toString());
                    }
                } else {
                    cell.setCellValue(cellData.toString());
                }
            }
        }
    }

    public void setDataUseString(List<Object[]> data) {

        for (int rowNum = 0; rowNum < data.size(); rowNum++) {
            Row row = sheet.createRow(rowNum + 1);
            Object[] rowData = data.get(rowNum);

            for (int columnNum = 0; columnNum < rowData.length; columnNum++) {

                Cell cell = row.createCell(columnNum);
                Object cellData = rowData[columnNum];
                if (Objects.isNull(cellData) || Strings.isNullOrEmpty(cellData.toString())) {
                    cell.setCellValue("");
                    continue;
                }
                cell.setCellValue(cellData.toString());
            }
        }
    }

    public void setDataWithCustomFormat(List<Object[]> data) {

        Map<String, CellStyle> cellStyleCache = Maps.newHashMap();

        CellStyle textCellStyle = excel.createCellStyle();
        textCellStyle.setDataFormat(excel.createDataFormat().getFormat("@"));

        for (int rowNum = 0; rowNum < data.size(); rowNum++) {
            Row row = sheet.createRow(rowNum + 1);
            Object[] rowData = data.get(rowNum);
            for (int columnNum = 0; columnNum < rowData.length; columnNum++) {
                Cell cell = row.createCell(columnNum);
                Object cellData = rowData[columnNum];
                QueryColumn queryColumn = columns[columnNum];
                if (queryColumn.getCategory() == FieldCategoryEnum.MEASURE && Objects.nonNull(queryColumn.getCustomFormatConfig())) {

                    CellStyle style = cellStyleCache.get(queryColumn.getName());
                    if (Objects.isNull(style)) {
                        short format = excel.createDataFormat().getFormat(queryColumn.getCustomFormatConfig().getFormatString());
                        style = excel.createCellStyle();
                        style.setDataFormat(format);
                        cellStyleCache.put(queryColumn.getName(), style);
                    }
                    cell.setCellStyle(style);
                }

                if (queryColumn.getCategory() == FieldCategoryEnum.DIMENSION && !queryColumn.getDataType().isNumber()) {
                    cell.setCellStyle(textCellStyle);
                }

                if (Objects.isNull(cellData) || Strings.isNullOrEmpty(cellData.toString())) {
                    cell.setCellValue("");
                    continue;
                }

//                if (queryColumn.getCategory() == FieldCategoryEnum.DIMENSION && !queryColumn.getDataType().isNumber()) {
//                    cell.setCellValue(cellData.toString());
//                    continue;
//                }
                if (!NumberUtils.isCreatable(cellData.toString())) {
                    cell.setCellValue(cellData.toString());
                    continue;
                }

                if (Objects.nonNull(queryColumn.getCustomFormatConfig())) {
                    cell.setCellValue(NumberUtils.toDouble(cellData.toString()));
                    continue;
                }

                if (cellData.toString().contains(".") && cellData.toString().length() < 20) {
                    cell.setCellValue(NumberUtils.toDouble(cellData.toString()));
                    continue;
                }

                if (cellData.toString().length() > 20) {
                    cell.setCellValue(cellData.toString());
                    continue;
                }
                try{
                    BigDecimal bigDecimal = new BigDecimal(cellData.toString());
                    cell.setCellValue(bigDecimal.setScale(0, BigDecimal.ROUND_DOWN).doubleValue());
                }catch (Exception e){
                    log.error("convert error value={}", cellData, e);
                    cell.setCellValue(cellData.toString());
                }
            }
        }
    }

    public void autoColumnSize() {
        for (int i = 0; i < columnNum; i++) {
//            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, 256 * 15);
        }
    }

    public String convertExcelToImageBase64() {
        try {
            String base64 = ExcelToImage.convertExcelToImage(sheet);
            return base64;
        } catch (IOException e) {
            log.error("convert excel to image error", e);
        } finally {
            try {
                this.excel.close();
            } catch (Exception e) {
                log.error(" close error", e);
            }
        }
        return "";
    }

    public void write(OutputStream stream) throws IOException {
        excel.write(stream);
    }

    public void writeToResponse() {
        try {
            HttpServletResponse response = Downloader.setExcelResponseHeader(fileName);
            excel.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("download error", e);
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DOWNLOAD_ERROR));
        }
    }
}
