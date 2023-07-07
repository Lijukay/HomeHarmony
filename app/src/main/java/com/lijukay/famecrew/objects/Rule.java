package com.lijukay.famecrew.objects;

/**
 * This class represents a rule
 */
public class Rule {
    private final String title;
    private final String message;

    /**
     *
     * @param title The title of the rule
     * @param message The rule itself
     */
     public Rule (String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Returns the title of the rule
     *
     * @return the title of the rule
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the rule
     *
     * @return the rule
     */
    public String getMessage() {
        return message;
    }
}
