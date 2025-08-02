package com.example.myapplication;

public class CategoryTotal {
    public String category;
    public float total;

    // Required by Room
    public CategoryTotal() {
    }

    public CategoryTotal(String category, float total) {
        this.category = category;
        this.total = total;
    }
}
