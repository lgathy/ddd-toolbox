package com.doctusoft.ddd.migration;

import com.doctusoft.ddd.model.Instantiator;
import com.doctusoft.ddd.persistence.GenericPersistence;

public interface MigrationContext {
    
    Instantiator getInstantiator();
    
    MigrationSourceLoader getSourceLoader();
    
    GenericPersistence getTargetPersistence();
    
}
