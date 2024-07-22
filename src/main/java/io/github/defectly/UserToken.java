package io.github.defectly;

import java.util.HashMap;
import java.util.List;

public class UserToken {

    public HashMap<User, List<TokenDate>> Content;

    public UserToken() {
        Content = new HashMap<>();
    }

}
