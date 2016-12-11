package com.doctusoft.ddd.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchOption {
    
    private int batchSize;
    
    @Wither
    @Nullable
    private Object dbCursor;
    
}
