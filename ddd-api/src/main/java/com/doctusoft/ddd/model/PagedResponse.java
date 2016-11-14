package com.doctusoft.ddd.model;

import java.util.*;

public interface PagedResponse<T> {
    
    List<T> getPageRows();
    
    int getTotalRowCount();
    
}
