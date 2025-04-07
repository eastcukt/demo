package com.east.demo.service.excel;

import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * excel相关操作
 *
 * @author: east
 * @date: 2023/9/19
 */

@Slf4j
@Service
public class ExcelDemoService {

    /**
     * 获取excel模板，在第三行表头新增同样格式字段，同时将一二行的合并单元格范围扩大
     *
     * @param request request
     * @return return
     */
    public void getTemplate(String request, HttpServletResponse response) {

        try {
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // 文件名中包含中文字符
            String fileName = "导出模板.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
//        String encodedFileName = Base64.getEncoder().encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
//        headers.add("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);

            InputStream inputStream = new ClassPathResource("/template/excel/模板.xlsx").getStream();
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            // 获取要修改的行
            Row head3 = sheet.getRow(2);

            // 获取要一致的样式
            CellStyle cellStyle = head3.getCell(1).getCellStyle();
            // 获取列宽
            int columnWidth = head3.getCell(1).getSheet().getColumnWidth(1);

            // 获取新表头
            List<String> externalHead = new ArrayList<String>() {
                {
                    add("表头4");
                    add("表头5");
                }
            };

            // 添加新表头
            for (String newHead : externalHead) {
                Cell newCell = head3.createCell(head3.getLastCellNum());
                newCell.setCellValue(newHead);
                newCell.setCellStyle(cellStyle);
                newCell.getSheet().setColumnWidth(newCell.getColumnIndex(), columnWidth);
            }

            // 前两行格式合并
            CellRangeAddress mergedHead1 = sheet.getMergedRegions().get(0);
            CellRangeAddress mergedHead2 = sheet.getMergedRegions().get(1);
            mergedHead1.setLastColumn(mergedHead1.getLastColumn() + externalHead.size());
            mergedHead2.setLastColumn(mergedHead2.getLastColumn() + externalHead.size());
            // 使新合并信息生效
            sheet.removeMergedRegion(0);
            sheet.removeMergedRegion(1);
            sheet.addMergedRegion(mergedHead1);
            sheet.addMergedRegion(mergedHead2);

            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
