package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

public interface EntityWithLongId extends Entity {
    
    @NotNull Class<? extends EntityWithLongId> getKind();
    
    Long getId();
    
    void setId(Long id);
    
    default String getIdAsString() {
        Long id = getId();
        return id == null ? "" : id.toString(); 
    }
    
    default EntityKey getKey() { return EntityKey.create(getKind(), getId().longValue()); }
    
    default EntityKey nextKey() {
        long idValue = getId().longValue();
        if (idValue == Long.MAX_VALUE) {
            throw new ArithmeticException("overflow");
        }
        return EntityKey.create(getKind(), idValue + 1);
    }
    
}
