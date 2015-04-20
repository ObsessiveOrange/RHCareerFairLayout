package adt;

import java.util.HashMap;
import java.util.Map;

public class LayoutVars {
    
    private int                        section1             = 0;
    private int                        section2             = 0;
    private int                        section2Rows         = 0;
    private int                        section2PathWidth    = 0;
    private int                        section3             = 0;
    private Map<Integer, TableMapping> locationTableMapping = new HashMap<Integer, TableMapping>();
    private Map<Integer, TableMapping> tableLocationMapping = new HashMap<Integer, TableMapping>();
    
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
}
