package com.lijukay.famecrew.objects;

/**
 * This class represents a subtask
 */
public class Subtask {
    private final String subTaskName;
    private final String subTaskInfo;

    /**
     *
     * @param subTaskName The name of the subtask
     * @param subTaskInfo The subtasks information
     */
    public Subtask(String subTaskName, String subTaskInfo) {
        this.subTaskName = subTaskName;
        this.subTaskInfo = subTaskInfo;
    }

    /**
     * Returns the name of the subtask
     *
     * @return the name of the subtask
     */
    public String getSubTaskName() {
        return subTaskName;
    }

    /**
     * Returns the information of the subtask
     *
     * @return the information of the subtask
     */
    public String getSubTaskInfo() {
        return subTaskInfo;
    }
}
