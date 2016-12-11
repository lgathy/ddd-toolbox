package com.doctusoft.ddd.model;

import lombok.Data;

import java.io.Serializable;

import static com.doctusoft.java.Failsafe.checkArgument;

@Data
public class PageToken implements Serializable {
    
    public static final PageToken create(int from, int limit) {
        return new PageToken(from, limit).validate();
    }
    
    public static final PageToken firstPage(int pageSize) {
        return create(0, pageSize);
    }
    
    private int from;
    
    private int limit;
    
    public PageToken() {}
    
    public PageToken(int from, int limit) {
        this.from = from;
        this.limit = limit;
    }
    
    public void copy(PageToken other) {
        this.from = other.from;
        this.limit = other.limit;
    }
    
    public PageToken copy() { return new PageToken(from, limit); }
    
    public PageToken nextPageToken() { return new PageToken(from + limit, limit); }
    
    public PageToken validate() {
        checkArgument(from >= 0, () -> "from: " + from);
        checkArgument(limit > 0, () -> "limit: " + limit);
        return this;
    }
    
}
