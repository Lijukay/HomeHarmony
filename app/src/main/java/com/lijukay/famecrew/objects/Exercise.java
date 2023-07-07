package com.lijukay.famecrew.objects;

import java.util.ArrayList;

/**
 * This class represents a task.
 */
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

    /**
     * Constructor of the task class
     *
     * @param exName         The name of the task
     * @param member         The member, who has to do this task
     * @param isDone         Tells whether the task is done or not
     * @param doneDay        The day, the task was marked as done
     * @param doneMonth      The month, the task was marked as done
     * @param doneYear       The year, the task was marked as done
     * @param doneByMember   The member, that finished the task
     * @param isVoluntary    Tells whether the task is voluntary or not
     * @param subtasks       A list of subtasks
     */
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

    /**
     * Returns the member that finished the task
     *
     * @return The member that finished the task
     */
    public Member getDoneByMember() {
        return doneByMember;
    }

    /**
     * Returns the member that should do this task
     *
     * @return The member that should do the task
     */
    public Member getMember() {
        return member;
    }

    /**
     * Returns the name of the task
     *
     * @return The name of the task
     */
    public String getExName() {
        return exName;
    }

    /**
     * Returns if the task is done or not
     *
     * @return true, if the task is done, else false.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the day, the task was marked as done
     *
     * @return The day, the task was marked as done
     */
    public int getDoneDay() {
        return doneDay;
    }

    /**
     * Returns the month, the task was marked as done
     *
     * @return The month, the task was marked as done
     */
    public int getDoneMonth() {
        return doneMonth;
    }

    /**
     * Returns the year, the task was marked as done
     *
     * @return The year, the task was marked as done
     */
    public int getDoneYear() {
        return doneYear;
    }

    /**
     * Returns if the task is voluntary or not
     *
     * @return true, if the task is voluntary, else false
     */
    public boolean isVoluntary() {
        return isVoluntary;
    }

    /**
     * Returns a list of subtasks
     *
     * @return a list of subtasks
     */
    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }
}
