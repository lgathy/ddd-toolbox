package com.doctusoft.ddd.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import static com.doctusoft.java.Failsafe.checkArgument;
import static java.util.Objects.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(doNotUseGetters = true)
public abstract class EntityClass<T extends Entity> {
    
    public static <T extends Entity> EntityClass<T> of(@NonNull Class<T> kind) {
        if (EntityWithLongId.class.isAssignableFrom(kind)) {
            return new WithLongId(kind);
        }
        if (EntityWithStringId.class.isAssignableFrom(kind)) {
            return new WithStringId(kind);
        }
        throw new IllegalArgumentException("Not an EntityClass: " + kind);
    }
    
    @NonNull private final Class<T> kind;
    
    @NonNull private final Class<?> idClass;
    
    @NotNull public Object checkId(@NotNull Object id) { return idClass.cast(requireNonNull(id)); }
    
    @NotNull public abstract EntityKey<T> toKey(@NotNull Object id);
    
    public boolean hasLongId() { return false; }
    
    public boolean hasStringId() { return false; }
    
    public final String getFullName() { return kind.getName(); }
    
    public final String getShortName() { return kind.getSimpleName(); }
    
    public void checkInstance(Object entity) {
        checkArgument(kind.isInstance(entity), () -> "Not an instance of " + getFullName() + " entity: " + entity);
    }
    
    @ToString(callSuper = true)
    private static class WithLongId<T extends EntityWithLongId> extends EntityClass<T> {
        
        private WithLongId(Class<T> kind) {
            super(kind, Long.class);
        }
        
        @NotNull public EntityKey<T> toKey(@NotNull Object id) {
            long checkedId = (Long) checkId(id);
            return EntityKey.create(getKind(), checkedId);
        }
        
        public boolean hasLongId() { return true; }
    }
    
    @ToString(callSuper = true)
    private static class WithStringId<T extends EntityWithStringId> extends EntityClass<T> {
        
        private WithStringId(Class<T> kind) {
            super(kind, String.class);
        }
        
        @NotNull public EntityKey<T> toKey(@NotNull Object id) {
            String checkedId = (String) checkId(id);
            return EntityKey.create(getKind(), checkedId);
        }
        
        public boolean hasStringId() { return true; }
    }
    
}
