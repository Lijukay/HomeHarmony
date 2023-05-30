package com.lijukay.famecrew.objects;

import java.util.ArrayList;

public class Exercise {
    private final Member member;
    private final String exName;

    public Exercise(String exName, Member member) {
        this.member = member;
        this.exName = exName;
    }

    public Member getMember() {
        return member;
    }

    public String getExName() {
        return exName;
    }
}
