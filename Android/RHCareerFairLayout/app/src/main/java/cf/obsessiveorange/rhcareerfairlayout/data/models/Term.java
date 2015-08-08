package cf.obsessiveorange.rhcareerfairlayout.data.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;

@JsonInclude(Include.NON_NULL)
public class Term implements Comparable<Term> {

    private Integer year;
    private String quarter;
    private Integer layout_Section1;
    private Integer layout_Section2;
    private Integer layout_Section2_PathWidth;
    private Integer layout_Section2_Rows;
    private Integer layout_Section3;
    @JsonIgnore
    private final Long lastUpdateTime;


    public Term(Cursor c) {

        this.year = c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_YEAR));
        this.quarter = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_QUARTER));
        this.setLayout_Section1(c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_LAYOUT_SECTION1)));
        this.setLayout_Section2(c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_LAYOUT_SECTION2)));
        this.setLayout_Section2_PathWidth(c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_LAYOUT_SECTION2_PATHWIDTH)));
        this.setLayout_Section2_Rows(c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_LAYOUT_SECTION2_ROWS)));
        this.setLayout_Section3(c.getInt(c.getColumnIndexOrThrow(DBManager.KEY_LAYOUT_SECTION3)));
        this.lastUpdateTime = c.getLong(c.getColumnIndexOrThrow(DBManager.KEY_LAST_UPDATE_TIME));
    }

    public Term(Integer year, String quarter) {
        this(year, quarter, null, null, null, null, null);
    }

    public Term(@JsonProperty("year") Integer year,
                @JsonProperty("quarter") String quarter,
                @JsonProperty("layout_Section1") Integer layout_Section1,
                @JsonProperty("layout_Section2") Integer layout_Section2,
                @JsonProperty("layout_Section2_PathWidth") Integer layout_Section2_PathWidth,
                @JsonProperty("layout_Section2_Rows") Integer layout_Section2_Rows,
                @JsonProperty("layout_Section3") Integer layout_Section3) {

        this.year = year;
        this.quarter = quarter;
        this.setLayout_Section1(layout_Section1);
        this.setLayout_Section2(layout_Section2);
        this.setLayout_Section2_PathWidth(layout_Section2_PathWidth);
        this.setLayout_Section2_Rows(layout_Section2_Rows);
        this.setLayout_Section3(layout_Section3);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public int compareTo(Term o) {

        int result = this.year.compareTo(o.year);
        if (result == 0) {
            switch (this.quarter) {
                case "Spring":
                    return -1;
                case "Fall":
                    switch (o.quarter) {
                        case "Spring":
                            return 1;
                        case "Winter":
                            return -1;
                        default:
                            return 0;
                    }
                case "Winter":
                    switch (o.quarter) {
                        case "Spring":
                        case "Fall":
                            return 1;
                        default:
                            return 0;
                    }
            }

        }
        return result;
    }

    /**
     * @return the layout_Section1
     */
    public Integer getYear() {
        return year;
    }


    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * @return the layout_Section1
     */
    public String getQuarter() {
        return quarter;
    }


    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    /**
     * @return the layout_Section1
     */
    public Integer getLayout_Section1() {
        return layout_Section1;
    }

    /**
     * @param layout_Section1 the layout_Section1 to set
     */
    public void setLayout_Section1(Integer layout_Section1) {
        this.layout_Section1 = layout_Section1;
    }

    /**
     * @return the layout_Section2
     */
    public Integer getLayout_Section2() {
        return layout_Section2;
    }

    /**
     * @param layout_Section2 the layout_Section2 to set
     */
    public void setLayout_Section2(Integer layout_Section2) {
        this.layout_Section2 = layout_Section2;
    }

    /**
     * @return the layout_Section2_PathWidth
     */
    public Integer getLayout_Section2_PathWidth() {
        return layout_Section2_PathWidth;
    }

    /**
     * @param layout_Section2_PathWidth the layout_Section2_PathWidth to set
     */
    public void setLayout_Section2_PathWidth(Integer layout_Section2_PathWidth) {
        this.layout_Section2_PathWidth = layout_Section2_PathWidth;
    }

    /**
     * @return the layout_Section2_Rows
     */
    public Integer getLayout_Section2_Rows() {
        return layout_Section2_Rows;
    }

    /**
     * @param layout_Section2_Rows the layout_Section2_Rows to set
     */
    public void setLayout_Section2_Rows(Integer layout_Section2_Rows) {
        this.layout_Section2_Rows = layout_Section2_Rows;
    }

    /**
     * @return the layout_Section3
     */
    public Integer getLayout_Section3() {
        return layout_Section3;
    }

    /**
     * @param layout_Section3 the layout_Section3 to set
     */
    public void setLayout_Section3(Integer layout_Section3) {
        this.layout_Section3 = layout_Section3;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Integer getNumTables(){
        return layout_Section1 + (layout_Section2 - 2) * layout_Section2_Rows + 4 + layout_Section3;
    }

    public ContentValues toContentValues() {
        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_YEAR, this.getYear());
        row.put(DBManager.KEY_QUARTER, this.getQuarter());
        row.put(DBManager.KEY_LAYOUT_SECTION1, this.getLayout_Section1());
        row.put(DBManager.KEY_LAYOUT_SECTION2, this.getLayout_Section2());
        row.put(DBManager.KEY_LAYOUT_SECTION2_PATHWIDTH, this.getLayout_Section2_PathWidth());
        row.put(DBManager.KEY_LAYOUT_SECTION2_ROWS, this.getLayout_Section2_Rows());
        row.put(DBManager.KEY_LAYOUT_SECTION3, this.getLayout_Section3());
        row.put(DBManager.KEY_LAST_UPDATE_TIME, this.getLastUpdateTime());

        return row;
    }
}
