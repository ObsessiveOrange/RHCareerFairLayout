package misc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;

public class ArrayList2D implements Iterable<ArrayList<Object>>, Serializable {
    
    /**
     * @author ObsessiveOrange
     */
    private static final long            serialVersionUID = 3290508608565161119L;
    
    private ArrayList<ArrayList<Object>> outerArray;
    private Map<String, Integer>         headerArray      = new LinkedHashMap<String, Integer>();
    private int                          totalColumns     = 0;
    
    /****************************************************************************** Constructors ******************************************************************************/
    /**
     * Creates a blank 2-Dimensional ArrayList
     */
    public ArrayList2D() {
    
        outerArray = new ArrayList<ArrayList<Object>>(50);
    }
    
    /**
     * Creates a blank 2-Dimensional ArrayList of specified size
     * 
     * @param size The initial size of the 2D ArrayList to be created
     * @param size Initial rows of array to be created (0 = default [50])
     */
    public ArrayList2D(int size) {
    
        outerArray = new ArrayList<ArrayList<Object>>(size == 0 ? 50 : size);
    }
    
    /**
     * Creates a 2-Dimensional ArrayList with set headers
     * 
     * @param newHeaderArray headers for ArrayList2D
     * @param size Initial rows of array to be created (0 = default [50])
     */
    public ArrayList2D(String[] newHeaderArray, int size) {
    
        outerArray = new ArrayList<ArrayList<Object>>(size == 0 ? 50 : size);
        setHeaders(newHeaderArray);
    }
    
    /**
     * Creates a 2-Dimensional ArrayList with set headers
     * 
     * @param newHeaderArray headers for ArrayList2D
     * @param size Initial rows of array to be created (0 = default [50])
     */
    public ArrayList2D(Map<String, Integer> headerMap, int size) {
    
        outerArray = new ArrayList<ArrayList<Object>>(size == 0 ? 50 : size);
        headerArray = headerMap;
    }
    
    /**
     * Creates a 2-Dimensional ArrayList replicating the input data
     * 
     * @param input_data Data to put into ArrayList
     * @param size Initial rows of array to be created (0 = default [1.5 x size of input data])
     */
    public ArrayList2D(Object[][] input_data, int size) {
    
        outerArray = new ArrayList<ArrayList<Object>>(size == 0 ? (int) (input_data.length * 1.5) : size);
        addRows(input_data);
    }
    
    /**
     * Creates a 2-Dimensional ArrayList replicating the input data
     * 
     * @param newHeaderArray headers for ArrayList2D
     * @param input_data Data to put into ArrayList
     * @param size Initial rows of array to be created (0 = default [1.5 x size of input data])
     */
    public ArrayList2D(String[] newHeaderArray, Object[][] input_data, int size) {
    
        outerArray = new ArrayList<ArrayList<Object>>(size == 0 ? (int) (input_data.length * 1.5) : size);
        setHeaders(newHeaderArray);
        addRows(input_data);
    }
    
    /****************************************************************************** Constructors ******************************************************************************/
    
    /****************************************************************************** Header methods ******************************************************************************/
    /**
     * Set the column headers for the 2D ArrayList
     * 
     * @param newHeaderArray Array of headers; the size of this array does not have to match the number of columns in the 2D array
     */
    public void setHeaders(String[] newHeaderArray) {
    
        headerArray = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < newHeaderArray.length; i++) {
            headerArray.put(newHeaderArray[i], i);
        }
        
    }
    
    /**
     * Set the column headers for the 2D ArrayList
     * 
     * @param newHeaderArray Array of headers; the size of this array does not have to match the number of columns in the 2D array
     */
    public void setHeaders(ArrayList<String> newHeaderArray) {
    
        headerArray = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < newHeaderArray.size(); i++) {
            headerArray.put(newHeaderArray.get(i), i);
        }
        
    }
    
    /**
     * Gets the column headers for the 2D ArrayList
     */
    public ArrayList<String> getHeaders() {
    
        String[] returnHeaderArray = new String[headerArray.size()];
        
        for (Entry<String, Integer> entry : headerArray.entrySet()) {
            returnHeaderArray[entry.getValue()] = entry.getKey();
        }
        
        return new ArrayList<String>(Arrays.asList(returnHeaderArray));
    }
    
    /**
     * Gets the column header for index given
     * 
     * @param colID the index of the column to get header of
     */
    public String getHeader(Integer columnID) {
    
        for (Entry<String, Integer> entry : headerArray.entrySet()) {
            if (columnID.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return String.valueOf(headerArray.get(columnID));
    }
    
    /**
     * Checks if headers exist
     * 
     * @param colID the index of the column to get header of
     */
    public Boolean hasHeaders() {
    
        if (headerArray.size() == 0) {
            return false;
        }
        else {
            return true;
        }
    }
    
    /**
     * Checks if headers exist
     * 
     * @param colID the index of the column to get header of (null if not found)
     */
    public int findHeader(Object query) {
    
        Integer result = headerArray.get(query);
        if (result == null) {
            System.out.println("Could not find header: " + query);
        }
        return result;
    }
    
    /****************************************************************************** Header methods ******************************************************************************/
    
    /****************************************************************************** addRow Methods ******************************************************************************/
    /**
     * Add a new row to the end of the 2D arrayList
     * 
     * @param input_data Array of row data
     */
    public void addRow(Object[] input_data) {
    
        addRow(outerArray.size(), input_data);
    }
    
    /**
     * Add a new row to the index given; all subsequent records will have their index increased by 1
     * 
     * @param index Index at which new row should to be inserted
     * @param input_data Array of row data
     */
    public void addRow(int index, Object[] input_data) {
    
        ArrayList<Object> newRow = new ArrayList<Object>();
        for (Object inputDataElement : input_data) {
            newRow.add(inputDataElement);
        }
        outerArray.add(index, newRow);
    }
    
    /**
     * Add a new row to the end of the 2D arrayList
     * 
     * @param newRow Array of row data
     */
    public void addRow(ArrayList<Object> newRow) {
    
        addRow(outerArray.size(), newRow);
    }
    
    /**
     * Add a new row to the index given; all subsequent records will have their index increased by 1
     * 
     * @param index Index at which new row should to be inserted
     * @param newRow Array of row data
     */
    public void addRow(int index, ArrayList<Object> newRow) {
    
        outerArray.add(index, newRow);
    }
    
    /**
     * Add new rows to the end of the array from input_data
     * 
     * @param input_data 2D Array of data
     */
    public void addRows(Object[][] input_data) {
    
        for (int i = 0; i < input_data.length; i++) {
            addRow(input_data[i]);
        }
    }
    
    /**
     * Add new rows to the index given; all subsequent records will have their index increased by the number of rows inserted
     * 
     * @param index Index at which new rows should start to be inserted from
     * @param input_data 2D Array of data
     */
    public void addRows(int startIndex, Object[][] input_data) {
    
        for (int i = 0; i < input_data.length; i++) {
            addRow(startIndex, input_data[i]);
        }
    }
    
    /****************************************************************************** addRow Methods ******************************************************************************/
    
    /****************************************************************************** setValue Methods ******************************************************************************/
    
    public void setValue(Object query, String columnName, Object newValue) {
    
        setValue(findInColumn(query), findHeader(columnName), newValue);
    }
    
    public void setValue(Integer x, String columnName, Object newValue) {
    
        setValue(x, findHeader(columnName), newValue);
    }
    
    public void setValue(Object query, String indexColumnName, String resultColumnName, Object input_value) {
    
        setValue(query, findHeader(indexColumnName), findHeader(resultColumnName), input_value);
    }
    
    public void setValue(Object query, int index_y_index, int result_y_index, Object input_value) {
    
        setValue(findInColumn(query, index_y_index), result_y_index, input_value);
    }
    
    public void setValue(int x, int y, Object newValue) {
    
        while (x >= outerArray.size()) {
            ArrayList<Object> innerArray = new ArrayList<Object>();
            outerArray.add(innerArray);
        }
        while (y >= outerArray.get(x).size()) {
            outerArray.get(x).add("");
        }
        outerArray.get(x).set(y, newValue);
    }
    
    public void setValue(int x, String columnName, Object[] input_data) {
    
        setValue(x, findHeader(columnName), input_data);
    }
    
    public void setValue(int x, int yStart, Object[] input_data) {
    
        for (int i = 0; i < input_data.length; i++) {
            setValue(x, yStart + i, input_data[i]);
        }
    }
    
    public void setValue(int xStart, String columnName, Object[][] input_data) {
    
        setValue(xStart, findHeader(columnName), input_data);
        
    }
    
    public void setValue(int xStart, int yStart, Object[][] input_data) {
    
        for (int i = 0; i < input_data.length; i++) {
            for (int j = 0; j < input_data[i].length; j++) {
                setValue(xStart + i, yStart + j, input_data[i][j]);
            }
        }
    }
    
    /****************************************************************************** setValue Methods ******************************************************************************/
    
    /****************************************************************************** getItem Methods ******************************************************************************/
    public Object getItem(Object query, String columnName) {
    
        return getItem(findInColumn(query, columnName), findHeader(columnName));
    }
    
    public Object getItem(int x, String columnName) {
    
        return getItem(x, findHeader(columnName));
    }
    
    public Object getItem(int x, int y) {
    
        return getItem(x, y, Object.class);
    }
    
    public <T> T getItem(Object query, String columnName, Class<T> returnClass) {
    
        return getItem(findInColumn(query), findHeader(columnName), returnClass);
    }
    
    public <T> T getItem(int x, String columnName, Class<T> returnClass) {
    
        return getItem(x, findHeader(columnName), returnClass);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getItem(int x, int y, Class<T> returnClass) {
    
        if (hasItem(x, y)) {
            
            Object returnItem = outerArray.get(x).get(y);
            
            if (returnItem.getClass().equals(returnClass)) {
                return (T) returnItem;
            }
            else if (returnClass.equals(Integer.class)) {
                return (T) Integer.valueOf(String.valueOf(returnItem));
            }
            else if (returnClass.equals(Long.class)) {
                return (T) Long.valueOf(String.valueOf(returnItem));
            }
            else if (returnClass.equals(Double.class)) {
                return (T) Double.valueOf(String.valueOf(returnItem));
            }
            else if (returnClass.equals(BigDecimal.class)) {
                return (T) new BigDecimal(String.valueOf(returnItem));
            }
            else if (returnClass.equals(Boolean.class)) {
                return (T) Boolean.valueOf(String.valueOf(returnItem));
            }
            else if (returnClass.equals(String.class)) {
                return (T) String.valueOf(returnItem);
            }
            else {
                return returnClass.cast(returnItem);
            }
        }
        return null;
    }
    
    public Object findRowGetItem(Object query, int indexColumn, String resultColumnName) {
    
        return findRowGetItem(query, indexColumn, findHeader(resultColumnName));
    }
    
    public Object findRowGetItem(Object query, int indexColumn, int result_y_index) {
    
        return getItem(findInColumn(query, indexColumn), result_y_index);
    }
    
    // using default column index
    public Object findRowGetItem(Object query, String resultColumnName) {
    
        return findRowGetItem(query, findHeader(resultColumnName));
    }
    
    public Object findRowGetItem(Object query, int result_y_value) {
    
        return findRowGetItem(query, 0, result_y_value);
    }
    
    public <T> T findRowGetItem(Object query, int indexColumn, String resultColumnName, Class<T> returnClass) {
    
        return findRowGetItem(query, indexColumn, findHeader(resultColumnName), returnClass);
    }
    
    public <T> T findRowGetItem(Object query, String indexColumnName, String resultColumnName, Class<T> returnClass) {
    
        return findRowGetItem(query, findHeader(indexColumnName), findHeader(resultColumnName), returnClass);
    }
    
    public <T> T findRowGetItem(Object query, int indexColumn, int result_y_value, Class<T> returnClass) {
    
        int targetRow = findInColumn(query, indexColumn);
        return getItem(targetRow, result_y_value, returnClass);
    }
    
    // using default column index
    public <T> T findRowGetItem(Object query, String resultColumnName, Class<T> returnClass) {
    
        return findRowGetItem(query, findHeader(resultColumnName), returnClass);
    }
    
    public <T> T findRowGetItem(Object query, int result_y_value, Class<T> returnClass) {
    
        return findRowGetItem(query, 0, result_y_value, returnClass);
    }
    
    /****************************************************************************** getItem Methods ******************************************************************************/
    
    /****************************************************************************** Find Methods ******************************************************************************/
    public int[] find(Object query) {
    
        int[] result = find(query, 0, 0);
        return result;
    }
    
    public int[] findLast(Object query) {
    
        int[] result = findLast(query, getRows() - 1, getColumns() - 1);
        return result;
    }
    
    public int[] find(Object query, int startRow, int startCol) {
    
        int[] result = new int[2];
        
        int i = startRow;
        int j = startCol;
        
        while (i < outerArray.size()) {
            while (j < outerArray.get(i).size()) {
                if (outerArray.get(i).get(j).equals(query)) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
                j++;
            }
            j = 0;
            i++;
        }
        result[0] = -1;
        result[1] = -1;
        return result;
    }
    
    public int[] findLast(Object query, int startRow, int startCol) {
    
        int[] result = new int[2];
        
        int i = startRow;
        int j = startCol;
        
        while (i < outerArray.size()) {
            while (j < outerArray.get(i).size()) {
                if (outerArray.get(i).get(j).equals(query)) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
                j--;
            }
            j = outerArray.get(i).size() - 1;
            i--;
        }
        result[0] = -1;
        result[1] = -1;
        return result;
    }
    
    public int findInColumn(Object query) {
    
        return findInColumn(query, 0, 0);
    }
    
    public int findInColumn(Object query, String columnName) {
    
        return findInColumn(query, findHeader(columnName), 0);
    }
    
    public int findInColumn(Object query, int columnID) {
    
        return findInColumn(query, columnID, 0);
    }
    
    public int findInColumn(Object query, String columnName, int startRow) {
    
        return findInColumn(query, findHeader(columnName), startRow);
    }
    
    public int findInColumn(Object query, int columnID, int startRow) {
    
        int result = -1;
        
        for (int i = startRow; i < outerArray.size(); i++) {
            if (outerArray.get(i).get(columnID).equals(query)) {
                result = i;
                return result;
            }
        }
        return result;
    }
    
    public int findLastInColumn(Object query, String columnName) {
    
        return findLastInColumn(query, findHeader(columnName));
    }
    
    public int findLastInColumn(Object query, int columnID) {
    
        int result = -1;
        
        for (int i = outerArray.size() - 1; i >= 0; i--) {
            if (outerArray.get(i).get(columnID).equals(query)) {
                result = i;
                return result;
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllOfClass(Class<T> returnClass) {
    
        List<T> returnList = new ArrayList<T>();
        
        for (int i = 0; i < outerArray.size(); i++) {
            ArrayList<Object> currRow = outerArray.get(i);
            
            for (int j = 0; j < currRow.size(); j++) {
                Object currObj = currRow.get(j);
                
                if (currObj != null && currObj.getClass().equals(returnClass)) {
                    returnList.add((T) currObj);
                }
            }
        }
        return returnList;
    }
    
    public ArrayList2D countOccurrences(Object query) {
    
        ArrayList2D occurrences = new ArrayList2D();
        int[] lastFound = { 0, 0 };
        
        for (int i = 0; true; i++) {
            lastFound = find(query, lastFound[0], lastFound[1] + 1);
            if (lastFound[0] != -1) {
                occurrences.setValue(i, 0, lastFound[0]);
                occurrences.setValue(i, 1, lastFound[1]);
                continue;
            }
            break;
        }
        return occurrences;
    }
    
    /****************************************************************************** Find Methods ******************************************************************************/
    
    /****************************************************************************** Row/Column Methods ******************************************************************************/
    
    /**
     * Returns row ArrayList object
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param rowIndex index of row to return
     * @return ArrayList<Object> of specified row
     */
    public ArrayList<Object> getRow(int rowIndex) {
    
        return outerArray.get(rowIndex);
    }
    
    /**
     * Returns row ArrayList object
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param rowIndex index of row to return
     * @param returnClass class of ArrayList values
     * @return ArrayList<T> of specified row
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getRow(int rowIndex, Class<T> returnClass) {
    
        return (ArrayList<T>) getRow(rowIndex);
    }
    
    /**
     * Returns row ArrayList object
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param rowIndex key of row to return
     * @return ArrayList<Object> of specified row
     */
    public ArrayList<Object> getRow(Object query) {
    
        return outerArray.get(findInColumn(query));
    }
    
    /**
     * Returns row ArrayList object
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param query key of row to return
     * @param returnClass class of ArrayList values
     * @return ArrayList<T> of specified row
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getRow(Object query, Class<T> returnClass) {
    
        return (ArrayList<T>) getRow(query);
    }
    
    /**
     * Returns ArrayList2D object with selected row
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param rowIndex index of row to return
     * @return ArrayList2D with specified row
     */
    @SuppressWarnings("unchecked")
    public ArrayList2D getRowAsArrayList2D(int rowIndex) {
    
        ArrayList2D newArray = new ArrayList2D(headerArray, 0);
        
        newArray.addRow((ArrayList<Object>) getRow(rowIndex).clone());
        
        return newArray;
    }
    
    /**
     * Returns ArrayList2D object with selected row
     * NOT IMMUTABLE. USE WITH CARE.
     * 
     * @param rowIndex index of row to return
     * @return ArrayList2D with specified row
     */
    public ArrayList2D getRowAsArrayList2D(Object query) {
    
        return getRowAsArrayList2D(findInColumn(query));
    }
    
    public <T> ArrayList<T> getColumn(String columnName, Class<T> returnClass) {
    
        return getColumn(findHeader(columnName), returnClass);
    }
    
    public <T> ArrayList<T> getColumn(int columnIndex, Class<T> returnClass) {
    
        ArrayList<T> resultArray = new ArrayList<T>();
        
        for (int i = 0; i < getColumnLength(columnIndex); i++) {
            resultArray.add(getItem(i, columnIndex, returnClass));
        }
        
        return resultArray;
    }
    
    public boolean hasItem(int x, String columnName) {
    
        return hasItem(x, findHeader(columnName));
    }
    
    public boolean hasItem(int x, int y) {
    
        if (x > getRows() - 1 || y > getRow(x, Object.class).size() - 1) {
            return false;
        }
        else {
            Object item = outerArray.get(x).get(y);
            
            if (item instanceof String && item.equals("")) {
                return false;
            }
            else if (item == null) {
                return false;
            }
        }
        return true;
    }
    
    public int getRows() {
    
        return outerArray.size();
    }
    
    public int getColumnLength(String columnName) {
    
        return getColumnLength(findHeader(columnName));
    }
    
    public int getColumnLength(int colID) {
    
        int columnSize = 0;
        for (List<Object> curRow : outerArray) {
            if (curRow.size() >= colID + 1) {
                columnSize++;
            }
            else {
                return columnSize;
            }
        }
        return columnSize;
    }
    
    public int getColumns(int arrayID) {
    
        return outerArray.get(arrayID).size();
    }
    
    public int getColumns() {
    
        totalColumns = 0;
        for (int i = 0; i < getRows(); i++) {
            int currRowColumns = getColumns(i);
            
            if (totalColumns < currRowColumns) {
                totalColumns = currRowColumns;
            }
        }
        
        return totalColumns;
    }
    
    public void deleteRow(int rowID) {
    
        outerArray.remove(outerArray.get(rowID));
        
    }
    
    public void deleteColumn(String columnName) {
    
        deleteColumn(findHeader(columnName));
    }
    
    public void deleteColumn(int colID) {
    
        if (headerArray.size() > colID) {
            headerArray.remove(colID);
        }
        
        for (int i = 0; i < getRows(); i++) {
            if (hasItem(i, colID)) {
                outerArray.remove(outerArray.get(i).remove(colID));
            }
        }
        
    }
    
    /****************************************************************************** Row/Columns Methods ******************************************************************************/
    
    /****************************************************************************** Input/Output Methods ******************************************************************************/
    public String toListString() {
    
        StringBuilder b = new StringBuilder();
        int columnWidth = 20;
        
        if (headerArray.size() > 0) {
            for (Object header : headerArray.keySet()) {
                b.append(String.format("%" + columnWidth + "." + columnWidth + "s", header));
                b.append("|");
            }
            b.append("\n");
        }
        
        for (int i = 0; i < outerArray.size(); i++) {
            for (int j = 0; j < outerArray.get(i).size(); j++) {
                b.append(String.format("%" + columnWidth + "." + columnWidth + "s", outerArray.get(i).get(j)));
                b.append("|");
            }
            b.append("\n");
        }
        
        return b.toString();
    }
    
    public String toJson() {
    
        return new Gson().toJson(outerArray);
    }
    
    public void printList(int columnWidth) {
    
        printHeader(columnWidth);
        
        for (int i = 0; i < outerArray.size(); i++) {
            for (int j = 0; j < outerArray.get(i).size(); j++) {
                System.out.printf("%" + columnWidth + "." + columnWidth + "s", outerArray.get(i).get(j));
                System.out.print("|");
            }
            System.out.println("\n");
        }
    }
    
    public void printHeader(int columnWidth) {
    
        if (headerArray.size() > 0) {
            for (Object header : headerArray.keySet()) {
                System.out.printf("%" + columnWidth + "." + columnWidth + "s", header);
                System.out.print("|");
            }
            System.out.println();
        }
    }
    
    public void printRow(int row, int columnWidth) {
    
        for (int i = 0; i < outerArray.get(row).size(); i++) {
            System.out.printf("%" + columnWidth + "." + columnWidth + "s", outerArray.get(row).get(i));
            System.out.print("|");
        }
        System.out.println("\n");
    }
    
    public ArrayList2D importFromWorkbook(HSSFWorkbook workbook, int sheetIndex) {
    
        // Get first/desired sheet from the workbook
        HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        
        // Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        
        readWorkbook(rowIterator);
        
        return this;
    }
    
    public ArrayList2D importFromWorkbook(XSSFWorkbook workbook, int sheetIndex) {
    
        // Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        
        // Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        
        readWorkbook(rowIterator);
        
        return this;
    }
    
    private void readWorkbook(Iterator<Row> rowIterator) {
    
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            // For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                // Check the cell type and format accordingly
                switch (cell.getCellType())
                {
                    case Cell.CELL_TYPE_NUMERIC:
                        setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        setValue(cell.getRowIndex(), cell.getColumnIndex(), cell.getStringCellValue());
                        break;
                }
            }
        }
    }
    
    public ArrayList2D importFromCSV(String filename, boolean hasHeaders, String removeChars) throws IOException {
    
        return importFromCSV(filename, ",", hasHeaders);
    }
    
    public ArrayList2D importFromCSV(String filename, String separator, boolean hasHeaders, String removeChars) throws IOException {
    
        BufferedReader br = new BufferedReader(new FileReader(filename));
        return importFromFile(br, separator, hasHeaders, removeChars);
    }
    
    public ArrayList2D importFromResourceFile(String filename, String separator, boolean hasHeaders, String removeChars) throws IOException {
    
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        return importFromFile(br, separator, hasHeaders, removeChars);
    }
    
    public ArrayList2D importFromCSV(String filename, boolean hasHeaders) throws IOException {
    
        return importFromCSV(filename, ",", hasHeaders, null);
    }
    
    public ArrayList2D importFromCSV(String filename, String separator, boolean hasHeaders) throws IOException {
    
        BufferedReader br = new BufferedReader(new FileReader(filename));
        return importFromFile(br, separator, hasHeaders, null);
    }
    
    public ArrayList2D importFromResourceFile(String filename, String separator, boolean hasHeaders) throws IOException {
    
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        return importFromFile(br, separator, hasHeaders, null);
    }
    
    public ArrayList2D importFromFile(BufferedReader br, String separator, boolean hasHeaders, String removeChars) throws IOException {
    
        boolean headerRowComplete = false;
        String line = "";
        //
        while ((line = br.readLine()) != null) {
            // use comma as separator
            ArrayList<Object> newRow = new ArrayList<Object>();
            
            if (removeChars != null) {
                line = line.replaceAll(removeChars, "");
            }
            
            while (line.charAt(line.length() - 1) == ',') {
                line = line.substring(0, line.length() - 1);
            }
            
            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter(separator);
            
            while (lineScanner.hasNext()) {
                if (lineScanner.hasNextInt()) {
                    newRow.add(lineScanner.nextInt());
                }
                else if (lineScanner.hasNextLong()) {
                    newRow.add(lineScanner.nextLong());
                }
                else if (lineScanner.hasNextDouble()) {
                    newRow.add(lineScanner.nextDouble());
                }
                else if (lineScanner.hasNextBoolean()) {
                    newRow.add(lineScanner.nextBoolean());
                }
                else {
                    String nextElement = lineScanner.next();
                    if (!nextElement.isEmpty()) {
                        newRow.add(nextElement);
                    }
                    else {
                        newRow.add("");
                    }
                }
            }
            
            lineScanner.close();
            
            if (hasHeaders && !headerRowComplete) {
                ArrayList<String> headerRow = new ArrayList<String>();
                for (Object o : newRow) {
                    headerRow.add((String) o);
                }
                setHeaders(headerRow);
                headerRowComplete = true;
            }
            else if (!newRow.isEmpty()) {
                addRow(newRow);
            }
        }
        
        br.close();
        
        return this;
    }
    
    public void restoreFromFile(String filename) throws IOException, ClassNotFoundException {
    
        // Deserialize data object from file
        ObjectInputStream fileReader = new ObjectInputStream(new FileInputStream(filename));
        
        this.outerArray = ((ArrayList2D) fileReader.readObject()).outerArray;
        fileReader.close();
        
    }
    
    /****************************************************************************** Input/Output Methods ******************************************************************************/
    
    /****************************************************************************** Misc Methods ******************************************************************************/
    public void sort(String columnName, String type) {
    
        sort(findHeader(columnName), type);
    }
    
    public void sort(final int columnID, String type) {
    
        final Comparator<List<Object>> comparatorStringAsc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList1.get(columnID) instanceof String && comparatorList2.get(columnID) instanceof String) {
                    return ((String) comparatorList1.get(columnID)).compareToIgnoreCase((String) comparatorList2.get(columnID));
                }
                return String.valueOf(comparatorList1.get(columnID)).compareToIgnoreCase(String.valueOf(comparatorList2.get(columnID)));
            }
        };
        final Comparator<List<Object>> comparatorStringDesc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList2.get(columnID) instanceof String && comparatorList1.get(columnID) instanceof String) {
                    return ((String) comparatorList2.get(columnID)).compareToIgnoreCase((String) comparatorList1.get(columnID));
                }
                return String.valueOf(comparatorList2.get(columnID)).compareToIgnoreCase(String.valueOf(comparatorList1.get(columnID)));
            }
        };
        final Comparator<List<Object>> comparatorDoubleAsc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList1.get(columnID) instanceof Double && comparatorList2.get(columnID) instanceof Double) {
                    return Double.compare((Double) comparatorList1.get(columnID), (Double) comparatorList2.get(columnID));
                }
                return Double.compare(
                        Double.valueOf(String.valueOf(comparatorList1.get(columnID))),
                        Double.valueOf(String.valueOf(comparatorList2.get(columnID))));
            }
        };
        final Comparator<List<Object>> comparatorDoubleDesc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList2.get(columnID) instanceof Double && comparatorList1.get(columnID) instanceof Double) {
                    return Double.compare((Double) comparatorList2.get(columnID), (Double) comparatorList1.get(columnID));
                }
                return Double.compare(
                        Double.valueOf(String.valueOf(comparatorList2.get(columnID))),
                        Double.valueOf(String.valueOf(comparatorList1.get(columnID))));
            }
        };
        final Comparator<List<Object>> comparatorBigDecimalAsc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList1.get(columnID) instanceof BigDecimal && comparatorList2.get(columnID) instanceof BigDecimal) {
                    return ((BigDecimal) comparatorList1.get(columnID)).compareTo((BigDecimal) comparatorList2.get(columnID));
                }
                return new BigDecimal(String.valueOf(comparatorList1.get(columnID))).compareTo(new BigDecimal(String.valueOf(comparatorList2
                        .get(columnID))));
            }
        };
        final Comparator<List<Object>> comparatorBigDecimalDesc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                if (comparatorList1.get(columnID) instanceof BigDecimal && comparatorList2.get(columnID) instanceof BigDecimal) {
                    return ((BigDecimal) comparatorList2.get(columnID)).compareTo((BigDecimal) comparatorList1.get(columnID));
                }
                return new BigDecimal(String.valueOf(comparatorList2.get(columnID))).compareTo(new BigDecimal(String.valueOf(comparatorList1
                        .get(columnID))));
            }
        };
        final Comparator<List<Object>> comparatorDateAsc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                return ((Date) comparatorList1.get(columnID)).compareTo((Date) comparatorList2.get(columnID));
            }
        };
        final Comparator<List<Object>> comparatorDateDesc = new Comparator<List<Object>>() {
            
            @Override
            public int compare(List<Object> comparatorList1, List<Object> comparatorList2) {
            
                return ((Date) comparatorList2.get(columnID)).compareTo((Date) comparatorList1.get(columnID));
            }
        };
        
        if (type.equals("StringAsc")) {
            Collections.sort(outerArray, comparatorStringAsc);
        }
        if (type.equals("StringDesc")) {
            Collections.sort(outerArray, comparatorStringDesc);
        }
        else if (type.equals("DoubleAsc")) {
            Collections.sort(outerArray, comparatorDoubleAsc);
        }
        else if (type.equals("DoubleDesc")) {
            Collections.sort(outerArray, comparatorDoubleDesc);
        }
        else if (type.equals("BigDecimalAsc")) {
            Collections.sort(outerArray, comparatorBigDecimalAsc);
        }
        else if (type.equals("BigDecimalDesc")) {
            Collections.sort(outerArray, comparatorBigDecimalDesc);
        }
        else if (type.equals("DateAsc")) {
            Collections.sort(outerArray, comparatorDateAsc);
        }
        else if (type.equals("DateDesc")) {
            Collections.sort(outerArray, comparatorDateDesc);
        }
        
    }
    
    /**
     * Extra computation required to calculate average so low that it might as well be calculated at the same time, just in case it's needed.
     */
    public double[] getMaxMinAverage(String columnName) {
    
        return getMaxMinAverage(findHeader(columnName), 0, getRows() - 1);
    }
    
    public double[] getMaxMinAverage(int columnID) {
    
        return getMaxMinAverage(columnID, 0, getRows() - 1);
    }
    
    public double[] getMaxMinAverage(String columnName, int startIndex, int endIndex) {
    
        return getMaxMinAverage(findHeader(columnName), startIndex, endIndex);
    }
    
    public double[] getMaxMinAverage(int columnID, int startIndex, int endIndex) {
    
        Double startItem = getItem(startIndex, columnID, Double.class);
        
        double maxValue;
        double minValue;
        
        if (startItem == null) {
            maxValue = Double.NEGATIVE_INFINITY;
            minValue = Double.POSITIVE_INFINITY;
        }
        else {
            maxValue = startItem;
            minValue = startItem;
        }
        
        double sum = 0;
        double recordsCounted = 0;
        
        for (int i = startIndex; i <= endIndex; i++) {
            Double nextItem = getItem(i, columnID, Double.class);
            if (nextItem == null) {
                continue;
            }
            
            sum += nextItem;
            if (nextItem > maxValue) {
                maxValue = nextItem;
            }
            else if (nextItem < minValue) {
                minValue = nextItem;
            }
            recordsCounted++;
        }
        
        double average = sum / recordsCounted;
        
        return new double[] { maxValue, minValue, average };
    }
    
    public double getAverage(String columnName) {
    
        return getAverage(findHeader(columnName), 0, getRows() - 1);
    }
    
    public double getAverage(int columnID) {
    
        return getAverage(columnID, 0, getRows() - 1);
    }
    
    public double getAverage(String columnName, int startIndex, int endIndex) {
    
        return getAverage(findHeader(columnName), startIndex, endIndex);
    }
    
    public double getAverage(int columnID, int startIndex, int endIndex) {
    
        double total = 0;
        int recordsCounted = 0;
        
        for (int i = startIndex; i <= endIndex; i++) {
            total += getItem(i, columnID, Double.class);
            recordsCounted++;
        }
        return (total / recordsCounted);
    }
    
    public double getWeightedAverage(String columnName1, String columnName2) {
    
        return getWeightedAverage(findHeader(columnName1), findHeader(columnName2), 0, getRows() - 1);
    }
    
    public double getWeightedAverage(int columnID1, int columnID2) {
    
        return getWeightedAverage(columnID1, columnID2, 0, getRows() - 1);
    }
    
    public double getWeightedAverage(String columnName1, String columnName2, int startIndex, int endIndex) {
    
        return getWeightedAverage(findHeader(columnName1), findHeader(columnName2), startIndex, endIndex);
    }
    
    public double getWeightedAverage(int columnID1, int columnID2, int startIndex, int endIndex) {
    
        double total = 0;
        double totalWeightFactor = 0;
        
        for (int i = startIndex; i <= endIndex; i++) {
            total += (getItem(i, columnID1, Double.class) * getItem(i, columnID2, Double.class));
            totalWeightFactor += (getItem(i, columnID2, Double.class));
        }
        return total / totalWeightFactor;
    }
    
    public void scrubArray(boolean scrubHeader) {
    
        if (scrubHeader) {
            headerArray = new LinkedHashMap<String, Integer>();
        }
        
        outerArray = new ArrayList<ArrayList<Object>>();
        
    }
    
    public ArrayList2D cloneArray() {
    
        ArrayList2D newArray = new ArrayList2D();
        
        newArray.setHeaders(getHeaders());
        
        for (int i = 0; i < outerArray.size(); i++) {
            for (int j = 0; j < outerArray.get(i).size(); j++) {
                newArray.setValue(i, j, getItem(i, j));
            }
        }
        
        return newArray;
    }
    
    public boolean isEmpty() {
    
        if (getRows() == 0 && getColumns() == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public void truncate(int length) {
    
        while (outerArray.size() > length) {
            outerArray.remove(length);
        }
        
    }
    
    public void truncateFromBottom(int length) {
    
        while (outerArray.size() > length) {
            outerArray.remove(0);
        }
        
    }
    
    /****************************************************************************** Misc Methods ******************************************************************************/
    
    private class Itr implements Iterator<ArrayList<Object>> {
        
        private int currIndex = -1;
        
        @Override
        public boolean hasNext() {
        
            return currIndex + 1 < getRows();
        }
        
        @Override
        public ArrayList<Object> next() {
        
            if (hasNext()) {
                currIndex++;
                return getRow(currIndex, Object.class);
            }
            throw new NoSuchElementException();
        }
        
        @Override
        public void remove() {
        
            outerArray.remove(currIndex);
        }
    }
    
    @Override
    public Iterator<ArrayList<Object>> iterator() {
    
        return new Itr();
    }
}
