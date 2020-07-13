
package com.vorozhbicky.dmitry.couponsforlove.Model;

import java.io.Serializable;

public class UserInfo implements Serializable {
    public String name;
    public String email;
    public String pairUniqId;

    public UserInfo() {
    }

    public UserInfo(String name, String email, String pairUniqId) {
        this.name = name;
        this.email = email;
        this.pairUniqId = pairUniqId;
    }
}
