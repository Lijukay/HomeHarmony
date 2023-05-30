package com.lijukay.famecrew.objects;

public class Rule {
    private final String title;
    private final String message;

     public Rule (String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
