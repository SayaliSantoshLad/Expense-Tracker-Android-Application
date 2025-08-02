package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;
    private OnItemClickListener listener;

    // Listener interface with edit and delete callbacks
    public interface OnItemClickListener {
        void onEditClick(Expense expense);
        void onDeleteClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, OnItemClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView expenseNote, expenseDate, expenseAmount;
        ImageView expenseIcon, iconEdit, iconDelete;
        View iconBackground;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            expenseNote = itemView.findViewById(R.id.expenseNote);
            expenseDate = itemView.findViewById(R.id.expenseDate);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expenseIcon = itemView.findViewById(R.id.expenseIcon);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconEdit = itemView.findViewById(R.id.iconEdit);
            iconDelete = itemView.findViewById(R.id.iconDelete);

            iconEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick((Expense) itemView.getTag());
                }
            });

            iconDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick((Expense) itemView.getTag());
                }
            });
        }
    }

    @NonNull
    @Override
    public ExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view, listener);
    }




    @Override
    public void onBindViewHolder(@NonNull ExpenseAdapter.ViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.expenseNote.setText(expense.getNote());
        holder.expenseDate.setText(expense.getDate());
        holder.expenseAmount.setText(String.format("$%.2f", expense.getAmount()));

        CategoryIconHelper.IconColor iconColor = CategoryIconHelper.getIconColorForCategory(expense.getCategory());
        holder.iconBackground.setBackgroundColor(iconColor.color);
        holder.expenseIcon.setImageResource(iconColor.iconRes);

        holder.itemView.setTag(expense);
    }

    @Override
    public int getItemCount() {
        return expenses == null ? 0 : expenses.size();
    }

    // Helper class for category icons and colors
    public static class CategoryIconHelper {

        public static class IconColor {
            public final int iconRes;
            public final int color;

            public IconColor(int iconRes, int color) {
                this.iconRes = iconRes;
                this.color = color;
            }
        }

        public static IconColor getIconColorForCategory(String category) {
            if (category == null) category = "";
            category = category.trim().toLowerCase();

            switch (category) {
                case "food":
                    return new IconColor(R.drawable.circle_background, 0xFFE53935); // Red
                case "travel":
                    return new IconColor(R.drawable.ic_travel, 0xFF1E88E5); // Blue
                case "shopping":
                    return new IconColor(R.drawable.ic_shopping, 0xFF43A047); // Green
                case "bills":
                    return new IconColor(R.drawable.ic_bills, 0xFFF4511E); // Orange
                default:
                    return new IconColor(R.drawable.ic_others, 0xFF757575); // Grey
            }
        }
    }
}
