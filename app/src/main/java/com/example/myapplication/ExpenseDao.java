package com.example.myapplication;
import androidx.room.*;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Delete
    void delete(Expense expense);
    @Update
    void update(Expense expense);


    @Query("SELECT * FROM expenses ORDER BY id DESC")
    List<Expense> getAll();

    @Query("SELECT SUM(amount) FROM expenses")
    float getTotal();
    // Add @Query annotation here
    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category")
    List<CategoryTotal> getTotalAmountByCategory();
}
