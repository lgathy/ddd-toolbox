package com.doctusoft.ddd.model;

import java.time.Instant;

public interface CreatedAt {
    
    String ATTRIBUTE = "createdAt";
    
    Instant getCreatedAt();
    
    void setCreatedAt(Instant createdAt);
    
}
