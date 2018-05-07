package com.doctusoft.ddd.migration;

import com.doctusoft.ddd.model.CreatedAt;
import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.model.EntityWithStringId;
import org.jetbrains.annotations.NotNull;

public interface MigrationError extends EntityWithStringId, CreatedAt {
    
    @NotNull static EntityKey<MigrationError> createKey(@NotNull String id) {
        return EntityKey.create(MigrationError.class, id);
    }
    
    @NotNull default Class<MigrationError> getKind() { return MigrationError.class; }
    
    @NotNull default EntityKey<MigrationError> getKey() { return createKey(getId()); }
    
    String getMigrationId();
    
    void setMigrationId(String migrationId);
    
    String getTargetEntity();
    
    void setTargetEntity(String targetEntity);
    
    String getEntityId();
    
    void setEntityId(String entityId);
    
    String getExceptionClass();
    
    void setExceptionClass(String exceptionClass);
    
    String getExceptionMessage();
    
    void setExceptionMessage(String exceptionMessage);
    
    String getExceptionStacktrace();
    
    void setExceptionStacktrace(String exceptionStacktrace);
    
    Boolean getExistingEntity();
    
    void setExistingEntity(Boolean existingEntity);
    
}
