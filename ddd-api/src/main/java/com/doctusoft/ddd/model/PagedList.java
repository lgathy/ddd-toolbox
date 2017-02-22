package com.doctusoft.ddd.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
public final class PagedList<T> implements PagedResponse<T>, Serializable {
    
    public static <T> PagedList<T> empty() { return new PagedList<>(Collections.emptyList(), 0); }
    
    private List<T> pageRows;
    
    private int totalRowCount;
    
}
