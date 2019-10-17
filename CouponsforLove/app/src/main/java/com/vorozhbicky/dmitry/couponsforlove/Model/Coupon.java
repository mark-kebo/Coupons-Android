package com.vorozhbicky.dmitry.couponsforlove.Model;

import java.io.Serializable;

public class Coupon implements Serializable {
    public String description;
    public String image;

    public Coupon() {
    }

    public Coupon(String description, String image) {
        this.description = description;
        this.image = image;
    }
}
