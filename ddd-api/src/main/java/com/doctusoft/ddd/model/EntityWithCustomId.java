package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

public interface EntityWithCustomId<T extends CustomId> extends Entity {
    
    @NotNull Class<? extends EntityWithCustomId> getKind();
    
    T getId();
    
    default EntityKey getKey() { return EntityKey.custom(getKind(), getId()); }
    
}
