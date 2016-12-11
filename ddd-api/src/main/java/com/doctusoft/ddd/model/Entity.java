package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;

import static com.doctusoft.java.Failsafe.checkState;
import static java.util.Objects.*;

/**
 * The Entity API contract requires:
 * <ol>
 * <li>
 * each entity types to be defined by an entity interface derived from one of the direct subinterfaces of the root
 * {@link Entity} interface (see: types of Id)
 * </li>
 * <li>to implement the {@link #getKind()} method returing a reference to the Java class of this entity interface</li>
 * <li>{@link #getKind()} may never return <code>null</code></li>
 * <li>to have a primary identifier {@link #getId()}, which is always unique among all instances of the same kind</li>
 * <li>the id can be <code>null</code> until it's not specified in the entity instance</li>
 * <li>
 * to have an injective (one-to-one) String representation of its Id value returned by the {@link #getIdAsString()},
 * where an empty {@link String} must be returned when the id is <code>null</code>
 * </li>
 * <li>
 * to return an immutable copy of its kind and id packed together into an {@link EntityKey} instance when invoking
 * the {@link #getKey()} method
 * </li>
 * </ol>
 * Permitted Id-types:
 * <ul>
 * <li>
 * The <b>default</b> id type is{@link String}, it should be used wherever possible. The entity interface has to
 * extend the interface {@link EntityWithStringId}.
 * </li>
 * <li>
 * If {@link Long} id is needed, the entity interface has to extend the interface {@link EntityWithLongId}.
 * This should also be used if we only need/allow 32-bit integer ids.
 * </li>
 * <li>
 * In case we need a <b>custom</b> id type, we must extend our entity interface from {@link EntityWithCustomId}.
 * </li>
 * </ul>
 * It is also highly recommended that the actual entity interface:
 * <ul>
 * <li>defines a <b>static</b> <code>createKey(id)</code> method for creating {@link EntityKey} of its kind</li>
 * <li>
 * provides <b>default</b> implementations of {@link #getKind()} and {@link #getKey()} methods with proper generic typing
 * </li>
 * </ul>
 *
 * @author lgathy
 * @see EntityKey
 */
public interface Entity {
    
    String ID = "id";
    
    /**
     * @return The kind and id of the entity packed together and copied into an immutable {@link EntityKey} instance.
     * @throws NullPointerException This is not perfect at all, but throwing NPE in case the id is not yet set on the
     *                              entity or if the Entity API contract is broken by returning <code>null</code> as
     *                              kind is still far more consistent than the alternatives
     */
    @NotNull EntityKey getKey() throws NullPointerException;
    
    /**
     * The mandatory <b>kind</b> attribute of the entity instance, which should always reference to the entity
     * interface's class and should never return <code>null</code>.
     * <p/>
     * It is highly recommended to provide a default implementation on the specific entity interfaces returning the
     * kind with the specific generic typing.
     */
    @NotNull Class<? extends Entity> getKind();
    
    /**
     * The primary identifier of the entity instance, which is always unique among the instances of the same entity kind.
     * It can temporarly hold <code>null</code> value though, but then the instance does not yet identify an entity instance.
     */
    Object getId();
    
    /**
     * The injective (one-to-one) String representation of the entity's Id value. It must return an empty {@link String}
     * when the id is currently <code>null</code>.
     */
    String getIdAsString();
    
    default <T> void checkAttributeEquals(@NotNull String field, @NotNull Supplier<T> getter, @NotNull T expected) {
        requireNonNull(field);
        T actual = getter.get();
        checkState(Objects.equals(actual, expected),
            () -> "Invalid " + field + ": " + actual + " expected: " + expected + " at: " + getKey());
    }
    
    default <T> void checkAttribute(@NotNull String field, @NotNull Supplier<T> getter, @NotNull Predicate<? super T> validation) {
        requireNonNull(field);
        T actual = getter.get();
        checkState(validation.test(actual), () -> "Invalid " + field + ": " + actual + " at: " + getKey());
    }
    
    default <T> void checkAttribute(@NotNull String field, Supplier<T> getter, @NotNull Predicate<? super T> validation, @NotNull String message) {
        requireNonNull(field);
        T actual = getter.get();
        checkState(validation.test(actual), () -> message + " Invalid " + field + ": " + actual + " at: " + getKey());
    }
    
}


