package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.*;

/**
 * Abstraction on how to instantiate new empty instances of interface where an abstraction over the implementation 
 * classes is desired. Typical use-cases are domain models and {@link Entity entities}.
 */
public interface Instantiator {
    
    /**
     * Returns a new instance of <code>kind</code>.
     *
     * @param kind the interface class of the instance we want to create.
     * @param <T>  the generic parameter is not restricted to {@link Entity} types.
     */
    @NotNull <T> T instantiate(@NotNull Class<T> kind);
    
    @NotNull default <T> Class<? extends T> getImplementationClass(@NotNull Class<T> kind) {
        requireNonNull(kind, "kind");
        return (Class<? extends T>) instantiate(kind).getClass();
    }
    
}
