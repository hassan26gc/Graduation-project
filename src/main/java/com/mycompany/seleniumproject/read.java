package com.mycompany.seleniumproject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class read {

    public String[][] read_sheet() throws IOException, InvalidFormatException {
        File myfile = new File(".\\testdata.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(myfile);
        XSSFSheet mySheet = wb.getSheet("Sheet1");
        int number_rows = mySheet.getPhysicalNumberOfRows();
        int number_column = mySheet.getRow(0).getLastCellNum();
        String[][] myarray = new String[number_rows - 1][number_column];

        for (int i = 1; i < number_rows; i++) {
            XSSFRow row = mySheet.getRow(i);
            for (int j = 0; j < number_column; j++) {
                myarray[i - 1][j] = getCellValue(row, j);
            }
        }

        return myarray;
    }

    private String getCellValue(XSSFRow row, int columnIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        CellType type = cell.getCellType();

        switch (type) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
