package misc;

public class Utils {
    
    public static String sanitizeString(String input) {
    
        return input.replaceAll("\\W", "");
    }
    
    public static boolean validateArgs(Object... objects) {
    
        for (Object o : objects) {
            if (o == null) {
                return false;
            }
            if (o instanceof String && ((String) o).isEmpty()) {
                return false;
            }
            if (o instanceof Double && (((Double) o).isInfinite() || ((Double) o).isNaN())) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean validateTerm(String year, String quarter) {
    
        return (year.matches("\\d{4}") && quarter.matches("(?i:Spring|Fall|Winter)"));
    }
}
