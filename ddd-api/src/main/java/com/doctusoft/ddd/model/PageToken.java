package com.doctusoft.ddd.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Optional;

import static com.doctusoft.java.Failsafe.checkArgument;

@Data
public class PageToken implements Serializable {
    
    public static final PageToken unpaged() { return new PageToken(0, Integer.MAX_VALUE); }
    
    public static final PageToken create(int from, int limit) {
        return new PageToken(from, limit).validate();
    }
    
    public static final PageToken firstPage(int pageSize) {
        return create(0, pageSize);
    }
    
    private int from;
    
    private int limit;
    
    /**
     * The default constructor should never be used directly. Use factory methods instead.
     *
     * @see #create(int, int)
     * @see #firstPage(int)
     * @see #unpaged()
     */
    private PageToken() {}
    
    public PageToken(int from, int limit) {
        this.from = from;
        this.limit = limit;
    }
    
    public void copy(PageToken other) {
        this.from = other.from;
        this.limit = other.limit;
    }
    
    public PageToken copy() { return new PageToken(from, limit); }
    
    public Optional<PageToken> nextPageToken() {
        if (limit <= 0) return Optional.empty();
        long next = (long) from + (long) limit;
        if (next > (long) Integer.MAX_VALUE) return Optional.empty();
        return Optional.of(new PageToken(from + limit, limit));
    }
    
    public PageToken validate() {
        checkArgument(from >= 0, () -> "from: " + from);
        checkArgument(limit > 0, () -> "limit: " + limit);
        return this;
    }
    
}
