package net.aopacloud.superbi.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

public class ExcelToImage {
 
    public static String convertExcelToImage(Sheet sheet) throws IOException {

        int rowCount = sheet.getPhysicalNumberOfRows();
        int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
 
        int rowHeight = 20;
        int cellWidth = 150;
        int imageWidth =  colCount * cellWidth;
        int imageHeight = rowCount * rowHeight;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        for (int i = 0; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < colCount; j++) {
                graphics.setFont(new Font("Noto Sans CJK TC", Font.PLAIN, 13));
                Cell cell = row.getCell(j);
                if (cell != null) {
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(j*cellWidth, i*rowHeight, imageWidth, imageHeight);
                    graphics.setColor(Color.BLACK); // 设置边框颜色
                    graphics.drawRect(j*cellWidth, i*rowHeight, imageWidth, imageHeight);
                    String cellValue = getCellValueAsString(cell);
                    graphics.drawString(cellValue, j * cellWidth+10, i * rowHeight + 15);
                }
            }
        }
 
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
 
    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    BigDecimal bigDecimal = new BigDecimal(cell.getNumericCellValue());
                    return bigDecimal.toPlainString();
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
