package com.lijukay.famecrew.objects;

public class Member {
    private final String prename;
    //private final String surname;
    private final String nickname;

    public Member(String prename, String nickname) {
        this.prename = prename;
        //this.surname = surname;
        this.nickname = nickname;
    }

    public String getPrename() {
        return prename;
    }

    /*public String getSurname() {
        return surname;
    }*/

    public String getNickname() {
        return nickname;
    }
}
