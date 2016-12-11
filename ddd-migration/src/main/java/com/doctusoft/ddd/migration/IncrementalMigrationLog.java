package com.doctusoft.ddd.migration;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.model.EntityWithStringId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface IncrementalMigrationLog extends EntityWithStringId {
    
    @NotNull static EntityKey<IncrementalMigrationLog> createKey(String id) {
        return EntityKey.create(IncrementalMigrationLog.class, id);
    }
    
    @NotNull default EntityKey<IncrementalMigrationLog> getKey() { return createKey(getId()); }
    
    @NotNull default Class<IncrementalMigrationLog> getKind() { return IncrementalMigrationLog.class; }
    
    Instant getLastMigratedAt();
    
    void setLastMigratedAt(Instant lastMigratedAt);
    
}
