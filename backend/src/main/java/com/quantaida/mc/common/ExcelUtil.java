package com.quantaida.mc.common;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class ExcelUtil {
    public byte[] export(List<String> headers, List<Map<String, Object>> rows) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("数据");
            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                head.createCell(i).setCellValue(headers.get(i));
            }
            int r = 1;
            for (Map<String, Object> row : rows) {
                Row data = sheet.createRow(r++);
                for (int i = 0; i < headers.size(); i++) {
                    Object v = row.get(headers.get(i));
                    data.createCell(i).setCellValue(v == null ? "" : String.valueOf(v));
                }
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new BizException(500, "导出失败");
        }
    }
}
