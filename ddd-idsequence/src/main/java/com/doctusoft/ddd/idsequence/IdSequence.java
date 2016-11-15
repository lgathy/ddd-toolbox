package com.doctusoft.ddd.idsequence;

import com.doctusoft.ddd.model.EntityWithStringId;

public interface IdSequence extends EntityWithStringId {
    
    Long getLastValue();
    
    void setLastValue(Long lastValue);
    
    Long getMaxValue();
    
    void setMaxValue(Long maxValue);
    
}
