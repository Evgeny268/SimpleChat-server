package com.evgeny;

import java.util.Objects;

public class User {
    public int iduser = 0;
    public String login;
    public String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(int iduser, String login, String password) {
        this.iduser = iduser;
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
