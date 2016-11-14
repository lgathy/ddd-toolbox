package com.doctusoft.ddd.model;

import java.time.Instant;

public interface LastModifiedAt {
    
    String ATTRIBUTE = "lastModifiedAt";
    
    Instant getLastModifiedAt();
    
    void setLastModifiedAt(Instant lastModifiedAt);
    
}
