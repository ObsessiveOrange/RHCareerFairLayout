package adt;

public class Term implements Comparable<Term> {
    
    private final Integer year;
    private final String  quarter;
    
    public Term(String year, String quarter) {
    
        this.year = Integer.valueOf(year);
        this.quarter = quarter;
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
     * @return the year
     */
    public Integer getYear() {
    
        return year;
    }
    
    /**
     * @return the quarter
     */
    public String getQuarter() {
    
        return quarter;
    }
    
}
