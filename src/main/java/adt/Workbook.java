package adt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import misc.DataTable;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Workbook {
    
    private final HashMap<String, Sheet> sheets = new HashMap<String, Sheet>();
    
    public Workbook() {
    
        // stub; nothing to do here...
    }
    
    public Sheet[] getSheets() {
    
        return sheets.values().toArray(new Sheet[sheets.values().size()]);
    }
    
    public Sheet getSheet(String name) {
    
        return sheets.get(name);
    }
    
    public Workbook importFromWorkbook(HSSFWorkbook workbook, boolean hasHeaders) {
    
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            
            DataTable table = new DataTable();
            
            // Get first/desired sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(i);
            
            // Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            
            readWorkbookToDataTable(table, rowIterator, hasHeaders);
            
            sheets.put(sheet.getSheetName(), new Sheet(sheet.getSheetName(), table));
        }
        
        return this;
    }
    
    public Workbook importFromWorkbook(XSSFWorkbook workbook, boolean hasHeaders) {
    
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            
            DataTable table = new DataTable();
            
            // Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(i);
            
            // Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            
            readWorkbookToDataTable(table, rowIterator, hasHeaders);
            
            sheets.put(sheet.getSheetName(), new Sheet(sheet.getSheetName(), table));
        }
        
        return this;
    }
    
    private DataTable readWorkbookToDataTable(DataTable table, Iterator<Row> rowIterator, boolean hasHeaders) {
    
        boolean headersSet = false;
        
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            // For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            
            ArrayList<Object> newRow = new ArrayList<Object>();
            
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                // Check the cell type and format accordingly
                switch (cell.getCellType())
                {
                    case Cell.CELL_TYPE_NUMERIC:
                        newRow.add(cell.getNumericCellValue());
                        // table.setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        newRow.add(cell.getBooleanCellValue());
                        // table.setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        newRow.add(cell.getStringCellValue());
                        // table.setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getStringCellValue());
                        break;
                }
            }
            
            if (hasHeaders && !headersSet) {
                table.setHeaders(newRow.toArray());
                headersSet = true;
            }
            else {
                table.addRow(newRow);;
            }
        }
        return table;
    }
}
