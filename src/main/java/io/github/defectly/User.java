package io.github.defectly;

import com.google.gson.annotations.Expose;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public String Username;
    public String Password;

    public User(String username, String password) {
        Username = username;
        Password = password;
    }
}
