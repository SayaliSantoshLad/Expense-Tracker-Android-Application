package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExpenseAdapter adapter;
    private AppDatabase db;
    private TextView tvTotalExpense;
    private RecyclerView rvExpenses;
    private FloatingActionButton fabAdd;
    private PieChart pieChart; // Moved to field so we can access it easily

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        rvExpenses = findViewById(R.id.rvExpenses);
        fabAdd = findViewById(R.id.fabAdd);
        pieChart = findViewById(R.id.pieChart);

        adapter = new ExpenseAdapter(new ArrayList<>(), new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Expense expense) {
                showEditExpenseDialog(expense);
            }

            @Override
            public void onDeleteClick(Expense expense) {
                deleteExpense(expense);
            }
        });

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);

        // Initialize database asynchronously to avoid null reference
        Executors.newSingleThreadExecutor().execute(() -> {
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "expense-db")
                    .fallbackToDestructiveMigration()
                    .build();

            // Now it's safe to load data
            runOnUiThread(() -> {
                loadExpenses();
                loadPieChartData();
            });
        });

        fabAdd.setOnClickListener(view -> showAddExpenseDialog());
    }

    private void loadExpenses() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Expense> expenses = db.expenseDao().getAll();
            float total = db.expenseDao().getTotal();

            runOnUiThread(() -> {
                adapter.updateExpenses(expenses);
                tvTotalExpense.setText("Total: $" + String.format("%.2f", total));

                // Get references to views
                TextView tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
                PieChart pieChart = findViewById(R.id.pieChart);

                if (expenses.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.GONE); // Hide PieChart if no data
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    pieChart.setVisibility(View.VISIBLE); // Show PieChart if data exists
                    loadPieChartData(); // optional: only if you haven’t already called it
                }
            });
        });
    }
    private void showDatePickerDialog(EditText etDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    etDate.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));





        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Expense")
                .setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    String note = etNote.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();

                    float amount = 0;
                    if (!amtStr.isEmpty()) {
                        amount = Float.parseFloat(amtStr);
                    }

                    // ✅ ADD THIS VALIDATION HERE
                    if (amount <= 0 || note.isEmpty()) {
                        Toast.makeText(this, "Enter valid amount and note", Toast.LENGTH_SHORT).show();
                        return;
                    }



                    if (note.isEmpty() || date.isEmpty() || amtStr.isEmpty() || category.isEmpty()) {
                        Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    try {
                        amount = (float) Double.parseDouble(amtStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Expense expense = new Expense(note, date, amount, category);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.expenseDao().insert(expense);
                        runOnUiThread(() -> {
                            loadExpenses();
                            loadPieChartData();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditExpenseDialog(Expense expense) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        String[] categories = {"Food", "Travel", "Shopping", "Bills", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        etNote.setText(expense.getNote());
        etDate.setText(expense.getDate());
        etAmount.setText(String.valueOf(expense.getAmount()));

        int selectedIndex = java.util.Arrays.asList(categories).indexOf(expense.getCategory());
        if (selectedIndex >= 0) {
            spinnerCategory.setSelection(selectedIndex);
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(dialogView)
                .setPositiveButton("Update", (dialogInterface, i) -> {
                    String note = etNote.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();

                    if (note.isEmpty() || date.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amtStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    expense.setNote(note);
                    expense.setDate(date);
                    expense.setAmount(amount);
                    expense.setCategory(category);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.expenseDao().update(expense);
                        runOnUiThread(() -> {
                            loadExpenses();
                            loadPieChartData();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense(Expense expense) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.expenseDao().delete(expense);
            runOnUiThread(() -> {
                loadExpenses();
                loadPieChartData();
            });
        });
    }

    private void loadPieChartData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoryTotal> categoryTotals = db.expenseDao().getTotalAmountByCategory();

            List<PieEntry> entries = new ArrayList<>();
            for (CategoryTotal ct : categoryTotals) {
                entries.add(new PieEntry(ct.total, ct.category));
            }

            PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(Color.BLACK);

            PieData pieData = new PieData(dataSet);

            runOnUiThread(() -> {
                pieChart.setData(pieData);
                pieChart.setUsePercentValues(true);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Expenses by Category");
                pieChart.animateY(1000);
                pieChart.invalidate();
            });
        });
    }
}
