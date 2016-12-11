package com.doctusoft.ddd.persistence;

import com.doctusoft.ddd.model.EntityKey;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

@Value
@ToString(callSuper = false, of = "key", doNotUseGetters = true)
@EqualsAndHashCode(callSuper = false, of = "key", doNotUseGetters = true)
public class EntityNotFoundException extends IllegalStateException {
    
    @NotNull EntityKey<?> key;
    
    public EntityNotFoundException(@NotNull EntityKey<?> key) {
        super("Entity does not exist: " + requireNonNull(key, "entityKey").toString());
        this.key = key;
    }
    
}
