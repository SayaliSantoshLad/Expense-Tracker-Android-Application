package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String note;
    private String date;
    private double amount;
    private String category;   // <-- category field

    public Expense() {}  // Default constructor required by Room

    public Expense(String note, String date, double amount,String category) {
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.category = category;
    }

    // Getter and setter for id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }


    // Getter and setter for category
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
}
