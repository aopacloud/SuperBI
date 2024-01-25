package net.aopacloud.superbi.model.domain;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ExcelReader {

    public List<String> readSingleColumn(MultipartFile file) {
        List<String> contents = Lists.newArrayList();
        if(file.isEmpty()){
            log.error("upload file empty");
            return contents;
        }
        try(InputStream inputStream = file.getInputStream()){
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                Cell cell = row.getCell(row.getFirstCellNum());
                String value = convertCellValueToString(cell);
                if(!Strings.isNullOrEmpty(value)){
                    contents.add(value);
                }
            }
        } catch (Exception e){
            log.error("read excel error", e);
        }
        return contents;
    }

    private String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String value = null;
        switch (cell.getCellType()) {
            case NUMERIC:
                Double doubleValue = cell.getNumericCellValue();
                // 格式化科学计数法，取一位整数
                DecimalFormat df = new DecimalFormat("0");
                value = df.format(doubleValue);
                break;
            case STRING:
                value = cell.getStringCellValue().trim();
                break;
            case BOOLEAN:
                Boolean booleanValue = cell.getBooleanCellValue();
                value = booleanValue.toString();
                break;
            case BLANK:
                break;
            case FORMULA:
                value = cell.getCellFormula();
                break;
            case ERROR:
                break;
            default:
                break;
        }
        return value;
    }

}
