package com.doctusoft.ddd.model;

public interface Pageable {
    
    PageToken getPageToken();
    
    void setPageToken(PageToken pageToken);
    
}
