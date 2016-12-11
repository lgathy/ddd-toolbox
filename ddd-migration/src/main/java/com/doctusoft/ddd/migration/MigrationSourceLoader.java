package com.doctusoft.ddd.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

/**
 * All methods must return records sorted by Id asc
 */
public interface MigrationSourceLoader {
    
    <S> List<S> fetch(@NotNull Class<S> sourceEntity, @NotNull List<?> idList);
    
    <S> List<S> load(@NotNull Class<S> sourceEntity, @Nullable BatchOption batchOption, @Nullable IncrementalOption incrementalOption);
    
    Optional<Instant> queryLastModifiedSince(@NotNull Class<?> sourceEntity, @Nullable Instant lowerBound);
    
    void evictCache();
    
}
