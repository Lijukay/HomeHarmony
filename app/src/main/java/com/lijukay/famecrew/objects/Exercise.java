package com.lijukay.famecrew.objects;

import java.util.ArrayList;

public class Exercise {
    private final Member member;
    private final String exName;
    private final boolean isDone;
    private final int doneDay;
    private final int doneMonth;
    private final int doneYear;
    private final Member doneByMember;
    private final boolean isVoluntary;
    private final ArrayList<Subtask> subtasks;

    public Exercise(String exName, Member member, boolean isDone, int doneDay, int doneMonth, int doneYear, Member doneByMember, boolean isVoluntary, ArrayList<Subtask> subtasks) {
        this.member = member;
        this.exName = exName;
        this.isDone = isDone;
        this.doneDay = doneDay;
        this.doneMonth = doneMonth;
        this.doneYear = doneYear;
        this.doneByMember = doneByMember;
        this.isVoluntary = isVoluntary;
        this.subtasks = subtasks;
    }

    public Member getDoneByMember() {
        return doneByMember;
    }

    public Member getMember() {
        return member;
    }

    public String getExName() {
        return exName;
    }

    public boolean isDone() {
        return isDone;
    }

    public int getDoneDay() {
        return doneDay;
    }

    public int getDoneMonth() {
        return doneMonth;
    }

    public int getDoneYear() {
        return doneYear;
    }

    public boolean isVoluntary() {
        return isVoluntary;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }
}

