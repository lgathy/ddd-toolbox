package com.doctusoft.ddd.migration;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Migration<T extends Entity> {
    
    private EntityClass<T> targetEntity;
    
    private String migrationId;
    
    private Instant startedAt;
    
    public String toString() {
        return new StringBuilder(256)
            .append("{targetEntity: ").append(targetEntity.getShortName())
            .append(", migrationId: ").append(migrationId)
            .append(", startedAt: ").append(startedAt)
            .append("}")
            .toString();
    }
    
}
