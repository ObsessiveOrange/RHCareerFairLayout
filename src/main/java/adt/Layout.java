package adt;

import java.util.HashMap;
import java.util.Map;

public class Layout {
    
    private int                        section1             = 0;
    private int                        section2             = 0;
    private int                        section2Rows         = 0;
    private int                        section2PathWidth    = 0;
    private int                        section3             = 0;
    private Map<Integer, TableMapping> locationTableMapping = new HashMap<Integer, TableMapping>();
    private Map<Integer, TableMapping> tableLocationMapping = new HashMap<Integer, TableMapping>();
    
    public Layout(HashMap<String, Object> layoutMap) {
    
        this.section1 = Integer.valueOf(layoutMap.get("Layout_Section1").toString());
        this.section2 = Integer.valueOf(layoutMap.get("Layout_Section2").toString());
        this.section2Rows = Integer.valueOf(layoutMap.get("Layout_Section2_Rows").toString());
        this.section2PathWidth = Integer.valueOf(layoutMap.get("Layout_Section2_PathWidth").toString());
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
    
        return section2PathWidth;
    }
    
    public void setSection2PathWidth(int section2PathWidth) {
    
        this.section2PathWidth = section2PathWidth;
    }
    
    public int getSection3() {
    
        return section3;
    }
    
    public void setSection3(int section3) {
    
        this.section3 = section3;
    }
    
    public int getSection2Rows() {
    
        return section2Rows;
    }
    
    public void setSection2Rows(int section2Rows) {
    
        this.section2Rows = section2Rows;
    }
    
    /**
     * @return the locationTableMapping
     */
    public Map<Integer, TableMapping> getLocationTableMapping() {
    
        return locationTableMapping;
    }
    
    /**
     * @return the tableLocationMapping
     */
    public Map<Integer, TableMapping> getTableLocationMapping() {
    
        return tableLocationMapping;
    }
    
    public int getTableCount() {
    
        int tableCount = 0;
        
        // add all section 1 tables
        tableCount += section1;
        
        // add section 2 tables, minus path width
        tableCount += (section2 * section2Rows) - (section2Rows > 2 ? ((section2Rows - 2) * section2PathWidth) : 0);
        tableCount += section3;
        
        return tableCount;
    }
}
