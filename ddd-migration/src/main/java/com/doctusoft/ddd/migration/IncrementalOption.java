package com.doctusoft.ddd.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IncrementalOption {
    
    @Nullable private Instant lowerBound;
    
    @Nullable private Instant upperBound;
    
}
