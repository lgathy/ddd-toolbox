package com.doctusoft.ddd.persistence;

import com.doctusoft.ddd.model.Task;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class ExecuteTaskDispatcher implements TaskDispatcher {
    
    private final TaskHandler taskHandler;
    
    @Inject
    public ExecuteTaskDispatcher(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }
    
    public void dispatch(@NotNull Task task) {
        taskHandler.execute(task);
    }
    
}
