package net.aopacloud.superbi.model.domain;

import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.util.Downloader;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.assertj.core.util.Strings;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
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

    public ExcelBuilder(String fileName) {
        excel = new SXSSFWorkbook(1000);
        excel.setCompressTempFiles(true);
        sheet = excel.createSheet();
        this.fileName = fileName;
    }

    public void setTitle(List<String> titles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < titles.size(); i++) {
            row.createCell(i).setCellValue(titles.get(i));
        }
    }

    public void setData(List<Object[]> data) {

        for (int rowNum = 0; rowNum < data.size(); rowNum++) {
            Row row = sheet.createRow(rowNum + 1);
            Object[] rowData = data.get(rowNum);

            for (int columnNum = 0; columnNum < rowData.length; columnNum++) {

                Cell cell = row.createCell(columnNum);
                Object cellData = rowData[columnNum];
                if (Objects.isNull(cellData) || Strings.isNullOrEmpty(cellData.toString())) {
                    cell.setCellValue("");
                }

                if (NumberUtils.isCreatable(cellData.toString())) {
                    try {
                        if (cellData.toString().contains(".")) {
                            cell.setCellValue(NumberUtils.toDouble(cellData.toString()));
                        } else {
                            BigDecimal bigDecimal = new BigDecimal(cellData.toString());
                            cell.setCellValue(bigDecimal.setScale(0, BigDecimal.ROUND_DOWN).doubleValue());
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
