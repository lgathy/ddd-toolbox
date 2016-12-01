package com.doctusoft.ddd.model;

import com.doctusoft.dynabean.SharedDynaBeanFactory;
import com.doctusoft.java.Failsafe;
import org.jetbrains.annotations.NotNull;

public class DynaBeanEntities extends SharedDynaBeanFactory implements Instantiator {
    
    public static final DynaBeanEntities INSTANCE = new DynaBeanEntities();
    
    private DynaBeanEntities() { throw Failsafe.staticClassInstantiated(); }
    
    public <T> @NotNull T instantiate(@NotNull Class<T> kind) {
        return INSTANCE.create(kind);
    }
    
}
