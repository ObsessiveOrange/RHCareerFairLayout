package misc;

public class Utils {
    
    public static String sanitizeString(String input) {
    
        return input.replaceAll("[^\\p{L}\\p{Nd}]+", "");
    }
}
