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
public class MigrationStep {
    
    private Migration<?> migration;
    
    @Nullable
    private IncrementalOption incrementalOption;
    
    @Wither
    @Nullable
    private BatchOption batchOption;
    
}
