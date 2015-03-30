package adt;

public class CachedResult {
    
    private final String cachedResult;
    private final long   cachedDate;
    
    public CachedResult(String cacheResult) {
    
        cachedResult = cacheResult;
        cachedDate = System.currentTimeMillis();
    }
    
    public String getCachedResult() {
    
        return cachedResult;
    }
    
    public long getCacheDate() {
    
        return cachedDate;
    }
}
