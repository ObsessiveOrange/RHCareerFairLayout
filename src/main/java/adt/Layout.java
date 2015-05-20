package adt;

import java.util.ArrayList;
import java.util.HashMap;

public class Layout {
    
    private int                     section1           = 0;
    private int                     section2           = 0;
    private int                     section2_Rows      = 0;
    private int                     section2_PathWidth = 0;
    private int                     section3           = 0;
    private ArrayList<TableMapping> tableMappings      = new ArrayList<TableMapping>();
    
    public Layout(HashMap<String, Object> layoutMap) {
    
        this.section1 = Integer.valueOf(layoutMap.get("Layout_Section1").toString());
        this.section2 = Integer.valueOf(layoutMap.get("Layout_Section2").toString());
        this.section2_Rows = Integer.valueOf(layoutMap.get("Layout_Section2_Rows").toString());
        this.section2_PathWidth = Integer.valueOf(layoutMap.get("Layout_Section2_PathWidth").toString());
        this.section3 = Integer.valueOf(layoutMap.get("Layout_Section3").toString());
    }
    
    public int getSection1() {
    
        return section1;
    }
    
    public void setSection1(int section1) {
    
        this.section1 = section1;
    }
    
    public int getSection2() {
    
        return section2;
    }
    
    public void setSection2(int section2) {
    
        this.section2 = section2;
    }
    
    public int getSection2PathWidth() {
    
        return section2_PathWidth;
    }
    
    public void setSection2PathWidth(int section2PathWidth) {
    
        this.section2_PathWidth = section2PathWidth;
    }
    
    public int getSection3() {
    
        return section3;
    }
    
    public void setSection3(int section3) {
    
        this.section3 = section3;
    }
    
    public int getSection2Rows() {
    
        return section2_Rows;
    }
    
    public void setSection2Rows(int section2Rows) {
    
        this.section2_Rows = section2Rows;
    }
    
    /**
     * @return the locationTableMapping
     */
    public ArrayList<TableMapping> getTableMappings() {
    
        return tableMappings;
    }
    
    public int getTableCount() {
    
        int tableCount = 0;
        
        // add all section 1 tables
        tableCount += section1;
        
        // add section 2 tables, minus path width
        tableCount += (section2 * section2_Rows) - (section2_Rows > 2 ? ((section2_Rows - 2) * section2_PathWidth) : 0);
        tableCount += section3;
        
        return tableCount;
    }
}
