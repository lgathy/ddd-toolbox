package com.doctusoft.ddd.migration;

import com.doctusoft.dataops.Entries;
import com.doctusoft.dataops.JoinOperator;
import com.doctusoft.ddd.migration.MigrationStep.MigrationStepBuilder;
import com.doctusoft.ddd.model.*;
import com.doctusoft.ddd.persistence.GenericPersistence;
import com.doctusoft.java.RandomId;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import static com.doctusoft.java.Failsafe.checkArgument;
import static com.doctusoft.java.Failsafe.checkState;
import static java.util.Objects.*;
import static java.util.logging.Level.*;

@Log
@Value
@Builder
public class MigrationProcessor<S, T extends Entity> {
    
    @NotNull MigrationContext context;
    
    @NotNull Class<S> sourceEntity;
    
    @NotNull Function<? super S, ?> sourceIdFun;
    
    @NotNull Class<T> targetEntity;
    
    @NotNull BiFunction<S, T, T> migratorFun;
    
    boolean hasIncrementalOption;
    
    @Nullable Integer batchSize;
    
    @Nullable Executor transactionalExecutor;
    
    public boolean hasBatchOption() { return batchSize != null; }
    
    public boolean idIsLong() { return EntityWithLongId.class.isAssignableFrom(targetEntity); }
    
    public boolean idIsString() { return EntityWithStringId.class.isAssignableFrom(targetEntity); }
    
    @NotNull
    public MigrationStep initialize() {
        Migration<?> migration = generateMigrationId();
        MigrationStepBuilder builder = MigrationStep.builder().migration(migration);
        if (hasBatchOption()) {
            checkBatchSize();
            builder.batchOption(BatchOption.builder()
                .batchSize(batchSize)
                .build());
        }
        if (hasIncrementalOption) {
            @Nullable Instant lowerBound = context
                .getTargetPersistence()
                .find(IncrementalMigrationLog.createKey(getIncrementalLogId()))
                .map(IncrementalMigrationLog::getLastMigratedAt)
                .orElse(null);
            @Nullable Instant upperBound = context
                .getSourceLoader()
                .queryLastModifiedSince(sourceEntity, lowerBound)
                .orElse(null);
            builder.incrementalOption(new IncrementalOption(lowerBound, upperBound));
        }
        MigrationStep initial = builder.build();
        log.info(() -> "Initialized migration '" + migration.getMigrationId() + "' for " + migration.getTargetEntity().getShortName());
        return initial;
    }
    
    public void pull(List<T> targetEntities) {
        Migration<T> migrationId = generateMigrationId();
        List<Object> idList = targetEntities
            .stream()
            .sorted()
            .map(Entity::getId)
            .collect(Collectors.toList());
        List<S> sourceEntities = context.getSourceLoader().fetch(sourceEntity, idList);
        migrate(migrationId, sourceEntities, targetEntities);
    }
    
    public void pullInBatches(List<T> targetEntities) {
        checkBatchSize();
        int count = targetEntities.size();
        if (count <= 0) return;
        if (count <= batchSize) {
            pull(targetEntities);
        } else {
            int n = count / batchSize;
            for (int i = 0; i < n; ++i) {
                int first = i * batchSize;
                int last = first + batchSize;
                pull(targetEntities.subList(first, last));
            }
            if (count > n * batchSize) {
                pull(targetEntities.subList(n * batchSize, count));
            }
        }
    }
    
    public Optional<MigrationStep> run(@NotNull MigrationStep migrationStep) {
        requireNonNull(migrationStep);
        log.fine(() -> "Running " + migrationStep);
        Migration migration = migrationStep.getMigration();
        BatchOption batchOption = migrationStep.getBatchOption();
        IncrementalOption incrementalOption = migrationStep.getIncrementalOption();
        
        List<S> sourceEntities = loadFromSource(batchOption, incrementalOption);
        
        if (sourceEntities.isEmpty()) {
            return finish(incrementalOption);
        }
        
        List idList = sourceEntities
            .stream()
            .map(validatedSourceIdFun())
            .collect(Collectors.toList());
        
        List<T> targetEntities = context.getTargetPersistence().loadByIdAsc(targetEntity, idList);
        migrate(migration, sourceEntities, targetEntities);
        
        if (hasBatchOption()) {
            Object lastSourceEntityId = idList.get(idList.size() - 1);
            BatchOption nextBatch = batchOption.withDbCursor(lastSourceEntityId);
            return Optional.of(migrationStep.withBatchOption(nextBatch));
        } else {
            return finish(incrementalOption);
        }
    }
    
    public void runFullMigration() {
        MigrationStep initialStep = initialize();
        Optional<MigrationStep> nextStep = run(initialStep);
        while (nextStep.isPresent()) {
            nextStep = run(nextStep.get());
        }
    }
    
    private void migrate(Migration<T> migration, List<S> sourceEntities, List<T> targetEntities) {
        TargetActions targetActions = new TargetActions(migration, context, sourceEntities);
        JoinOperator<Object> joinOperator = (JoinOperator) JoinOperator.natural();
        joinOperator.join(
            Entries.indexValues(sourceEntities, sourceIdFun),
            Entries.indexValues(targetEntities, Entity::getId),
            
            (source, existing, id) -> {
                try {
                    T targetEntity = migratorFun.apply(source, existing);
                    if (targetEntity != null) {
                        Object targetEntityId = targetEntity.getId();
                        if (!id.equals(targetEntityId)) {
                            throw new IllegalStateException("targetEntity.id: " + String.valueOf(targetEntityId));
                        }
                        if (existing == null) {
                            targetActions.addInsert(targetEntity);
                        } else {
                            targetActions.addUpdate(targetEntity);
                        }
                    } else {
                        targetActions.addIgnore();
                    }
                } catch (RuntimeException exception) {
                    targetActions.catchMigrationError(id, exception, existing);
                }
            }
        );
        Executor executor = transactionalExecutor == null ? DEFAULT_EXECUTOR : transactionalExecutor;
        try {
            executor.execute(() -> {
                List errors = targetActions.filterEntities(TargetActions.ErrorLogAction.class);
                List newEntities = targetActions.filterEntities(TargetActions.InsertAction.class);
                List entityUpdates = targetActions.filterEntities(TargetActions.UpdateAction.class);
                GenericPersistence targetPersistence = context.getTargetPersistence();
                targetPersistence.insertMany(MigrationError.class, errors);
                targetPersistence.insertMany(targetEntity, newEntities);
                targetPersistence.updateMany(targetEntity, entityUpdates);
            });
        } catch (RuntimeException batchException) {
            
            if (transactionalExecutor == null) {
                log.log(SEVERE, "Migration batch failed!");
                throw batchException;
            } else {
                log.log(INFO, "Retrying failed migration batch...");
                targetActions.retryOneByOneWith(transactionalExecutor);
            }
        }
        targetActions.logStatistics();
    }
    
    private Optional<MigrationStep> finish(@Nullable IncrementalOption incrementalOption) {
        if (hasIncrementalOption && incrementalOption != null && incrementalOption.getUpperBound() != null) {
            String entryId = getIncrementalLogId();
            Instant latest = incrementalOption.getUpperBound();
            GenericPersistence persistence = context.getTargetPersistence();
            IncrementalMigrationLog entry = persistence.selectForUpdate(IncrementalMigrationLog.createKey(entryId));
            if (entry == null) { // must be the first run ever
                entry = context.getInstantiator().instantiate(IncrementalMigrationLog.class);
                entry.setId(entryId);
                entry.setLastMigratedAt(latest);
                persistence.insert(entry);
            } else if (entry.getLastMigratedAt().isBefore(latest)) {
                entry.setLastMigratedAt(latest);
                persistence.update(entry);
            }
        }
        log.info(() -> targetEntity.getSimpleName() + " migration finished");
        return Optional.empty();
    }
    
    private Migration<T> generateMigrationId() {
        return Migration.<T>builder()
            .targetEntity(EntityClass.of(targetEntity))
            .migrationId(RandomId.sequential())
            .startedAt(Instant.now())
            .build();
    }
    
    private String getIncrementalLogId() { return targetEntity.getSimpleName(); }
    
    private Function<? super S, Object> validatedSourceIdFun() {
        if (idIsLong()) return source -> validateSourceId(source, Long.class);
        if (idIsString()) return source -> validateSourceId(source, String.class);
        throw new IllegalStateException("Invalid Id type on: " + targetEntity);
    }
    
    private Object validateSourceId(S sourceEntity, @NotNull Class<?> expectedType) {
        Object sourceId = sourceIdFun.apply(sourceEntity);
        requireNonNull(sourceId, () -> "Id was null on migration source: " + sourceEntity);
        checkState(expectedType.isInstance(sourceId),
            () -> "Invalid Id '" + sourceId + "': not " + expectedType + " on migration source: " + sourceEntity);
        return sourceId;
    }
    
    private void checkBatchSize() {
        checkArgument(batchSize.intValue() > 0, () -> "Invalid batchSize: " + batchSize);
    }
    
    private List<S> loadFromSource(@Nullable BatchOption batchOption, @Nullable IncrementalOption incrementalOption) {
        
        if (hasBatchOption()) {
            checkArgument(batchOption != null, "No BatchOption available");
        } else if (batchOption != null) {
            log.warning(() -> "Ignoring unnecessary BatchOption: " + batchOption);
        }
        if (!hasIncrementalOption && incrementalOption != null) {
            log.warning(() -> "Ignoring unnecessary IncrementalOption: " + incrementalOption);
        }
        
        List<S> entityList = context
            .getSourceLoader()
            .load(sourceEntity, batchOption, incrementalOption);
        
        log.fine(() -> "Loaded " + entityList.size() + " entities from source");
        return entityList;
    }
    
    public interface MigrateAction extends Runnable {
        
        @NotNull Entity getEntity();
        
        void retrySafely();
        
    }
    
    private static final class TargetActions {
        
        private final Migration<?> migration;
        private final GenericPersistence persistence;
        private final Instantiator instantiator;
        
        private final ArrayList<MigrateAction> actions;
        private final int countSource;
        
        private int countInsert = 0;
        private int countUpdate = 0;
        private int countIgnore = 0;
        private int countFailed = 0;
        
        @Nullable
        private Executor transactionalExecutor;
        
        private TargetActions(Migration<?> migration, MigrationContext context, List sourceEntities) {
            this.migration = requireNonNull(migration, "migration");
            this.persistence = context.getTargetPersistence();
            this.instantiator = context.getInstantiator();
            this.countSource = sourceEntities.size();
            this.actions = new ArrayList<>(countSource);
        }
        
        public void addInsert(@NotNull Entity entity) {
            actions.add(new InsertAction(entity));
            ++countInsert;
        }
        
        public void addUpdate(@NotNull Entity entity) {
            actions.add(new UpdateAction(entity));
            ++countUpdate;
        }
        
        public void addIgnore() { ++countIgnore; }
        
        public void catchMigrationError(@NotNull Object id, @NotNull RuntimeException exception,
            @Nullable Entity existingEntity) {
            actions.add(logError(id, exception, existingEntity));
            ++countFailed;
        }
        
        public List<Entity> filterEntities(Class<? extends MigrateAction> actionClass) {
            return actions
                .stream()
                .filter(actionClass::isInstance)
                .map(MigrateAction::getEntity)
                .collect(Collectors.toList());
        }
        
        public void retryOneByOneWith(@NotNull Executor transactionalExecutor) {
            this.transactionalExecutor = requireNonNull(transactionalExecutor, "transactionalExecutor");
            actions.forEach(MigrateAction::retrySafely);
        }
        
        private void retryInTransaction(@NotNull MigrateAction action, @Nullable Entity existingEntity) {
            requireNonNull(action, "action");
            try {
                transactionalExecutor.execute(action);
                
            } catch (RuntimeException exception) {
                ++countFailed;
                if (existingEntity == null) {
                    --countInsert;
                } else {
                    --countUpdate;
                }
                log.warning("Retry transaction failed: check MigrationError table for details!");
                try {
                    Object id = action.getEntity().getId();
                    ErrorLogAction logAction = logError(id, exception, existingEntity);
                    transactionalExecutor.execute(logAction);
                } catch (RuntimeException logFailed) {
                    log.log(SEVERE, logFailed, () -> "Unexpected: failed to create error log entry! " + logFailed.getMessage());
                    log.log(SEVERE, exception, () -> "Retrying " + action + " failed with: " + exception.getMessage());
                    throw logFailed;
                }
            }
        }
        
        @Getter
        @RequiredArgsConstructor
        private final class InsertAction implements MigrateAction {
            
            @NotNull private final Entity entity;
            
            public void run() {
                persistence.insert(entity);
            }
            
            public void retrySafely() {
                retryInTransaction(this, null);
            }
            
            public String toString() {
                return "InsertAction(" + entity.getKey().toString() + ")";
            }
        }
        
        @Getter
        @RequiredArgsConstructor
        private final class UpdateAction implements MigrateAction {
            
            @NotNull private final Entity entity;
            
            public void run() {
                persistence.update(entity);
            }
            
            public void retrySafely() {
                retryInTransaction(this, entity);
            }
            
            public String toString() {
                return "UpdateAction(" + entity.getKey().toString() + ")";
            }
        }
        
        @Getter
        @RequiredArgsConstructor
        private final class ErrorLogAction implements MigrateAction {
            
            @NotNull private final MigrationError entity;
            
            public void run() {
                persistence.insert(entity);
            }
            
            public void retrySafely() {
                try {
                    transactionalExecutor.execute(this);
                } catch (RuntimeException exception) {
                    log.log(SEVERE, exception, () -> "Unexpected: failed to create error log entry! " + exception.getMessage());
                    log.severe(() -> " migrationId: " + entity.getMigrationId() +
                        "\n targetEntity: " + entity.getTargetEntity() +
                        "\n entityId: " + entity.getEntityId() +
                        "\n exceptionClass: " + entity.getExceptionClass() +
                        "\n exceptionMessage: " + entity.getExceptionMessage() +
                        "\n exceptionStacktrace: " + entity.getExceptionStacktrace() +
                        "\n existingEntity: " + entity.getExistingEntity());
                    throw exception;
                }
            }
        }
        
        public ErrorLogAction logError(@NotNull Object id, @NotNull RuntimeException exception,
            @Nullable Entity existingEntity) {
            
            MigrationError errorLog = instantiator.instantiate(MigrationError.class);
            errorLog.setId(UUID.randomUUID().toString());
            errorLog.setCreatedAt(Instant.now());
            errorLog.setMigrationId(migration.getMigrationId());
            errorLog.setTargetEntity(migration.getTargetEntity().getShortName());
            errorLog.setEntityId(id.toString());
            errorLog.setExceptionClass(exception.getClass().getName());
            errorLog.setExceptionMessage(exception.getMessage());
            StringWriter stackTraceBuffer = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTraceBuffer));
            errorLog.setExceptionStacktrace(stackTraceBuffer.toString());
            errorLog.setExistingEntity(existingEntity != null);
            return new ErrorLogAction(errorLog);
        }
        
        public void logStatistics() {
            log.info(() -> "Migration batch stats: count(sourceEntities)=" + countSource + ", inserted=" + countInsert 
                + ", updated=" + countUpdate + ", ignored=" + countIgnore + ", failed=" + countFailed);
        }
    }
    
    private static final Executor DEFAULT_EXECUTOR = action -> action.run();
    
}
