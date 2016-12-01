package com.doctusoft.ddd.model;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.function.*;

import static com.doctusoft.java.Failsafe.checkArgument;
import static java.util.Objects.*;

public class PojoInstantiator implements Instantiator {
    
    private final Function<Class, Constructor> constructorFun;
    
    public PojoInstantiator(Function<Class, Constructor> constructorFun) {
        this.constructorFun = requireNonNull(constructorFun);
    }
    
    public <T> @NotNull T instantiate(Class<T> kind) {
        Constructor<T> constructor = lookup(kind);
        try {
            return constructor.newInstance(NO_ARGS);
        } catch (Exception e) {
            Class implClass = constructor.getDeclaringClass();
            throw new IllegalArgumentException("Failed to instantiate class: " + implClass.getName() + " " + e.getMessage(), e);
        }
    }
    
    public <T> @NotNull Class<? extends T> getImplementationClass(Class<T> kind) {
        return lookup(kind).getDeclaringClass();
    }
    
    private <T> Constructor<T> lookup(Class<T> kind) {
        requireNonNull(kind, "kind");
        Constructor constructor = constructorFun.apply(kind);
        checkArgument(constructor != null, "Not registered kind: " + kind);
        return constructor;
    }
    
    private static final Object[] NO_ARGS = new Object[0];
    
}
