package com.doctusoft.ddd.jpa.persistence;

import com.doctusoft.ddd.model.PojoInstantiator;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;

import static com.doctusoft.java.Failsafe.checkArgument;
import static java.util.Objects.*;

@RequiredArgsConstructor(staticName = "builder")
public final class EntityTypes {
    
    private ImmutableMap.Builder<Class, Constructor> map = ImmutableMap.builder();
    
    public <T> EntityTypes put(Class<T> kind, Class<? extends T> implClass) {
        requireNonNull(kind);
        requireNonNull(implClass);
        checkArgument(kind.isAssignableFrom(implClass), () -> "Incompatible implementation! kind: " + kind + ", implClass: " + implClass);
        try {
            Constructor e = implClass.getConstructor(new Class[0]);
            map.put(kind, e);
            return this;
        } catch (Exception e) {
            throw new IllegalArgumentException("No default constructor! Kind: " + kind + ", implClass:" + implClass);
        }
    }
    
    public PojoInstantiator build() {
        ImmutableMap<Class, Constructor> constructors = map.build();
        return new PojoInstantiator(constructors::get);
    }
    
}
