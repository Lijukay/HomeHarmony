package com.lijukay.famecrew.objects;

public class Subtask {
    private final String subTaskName;
    private final String subTaskInfo;

    public Subtask(String subTaskName, String subTaskInfo) {
        this.subTaskName = subTaskName;
        this.subTaskInfo = subTaskInfo;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public String getSubTaskInfo() {
        return subTaskInfo;
    }
}
