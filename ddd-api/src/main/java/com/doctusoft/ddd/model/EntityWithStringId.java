package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

public interface EntityWithStringId extends Entity {
    
    @NotNull Class<? extends EntityWithStringId> getKind();
    
    String getId();
    
    void setId(String id);
    
    default String getIdAsString() {
        String id = getId();
        return id == null ? "" : id; 
    }
    
    default EntityKey getKey() { return EntityKey.create(getKind(), getId()); }
    
}
