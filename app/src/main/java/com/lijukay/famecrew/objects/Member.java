package com.lijukay.famecrew.objects;


/**
 * This class represents a member
 */
public class Member {
    private final String prename;
    //private final String surname;
    private final String nickname;

    /**
     *
     * @param prename The prename of the member
     * @param nickname The nickname of the member
     */
    public Member(String prename, String nickname) {
        this.prename = prename;
        this.nickname = nickname;
    }

    /**
     * Returns the prename of a member
     *
     * @return the prename of a member
     */
    public String getPrename() {
        return prename;
    }


    /**
     * Returns the nickname of a member
     *
     * @return the nickname of a member
     */
    public String getNickname() {
        return nickname;
    }
}
