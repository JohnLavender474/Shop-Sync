package edu.uga.cs.shopsync.exceptions;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;

public class TaskFailureException extends RuntimeException {

    private final Task<?> task;

    public TaskFailureException(Task<?> task, String message) {
        super(message);
        this.task = task;
    }

    public Task<?> getTask() {
        return task;
    }

    @NonNull
    @Override
    public String toString() {
        return "TaskFailureException{" +
                "task=" + task +
                '}';
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\nTask exception: " + task.getException();
    }
}
