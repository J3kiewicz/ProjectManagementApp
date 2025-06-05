package com.example.projekt_trzeciakiewicz_julia;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> allTasks;
    private final List<Task> displayedTasks;
    private final DatabaseHelper dbHelper;
    private boolean showCompleted = false;
    private OnTaskClickListener taskClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }

    public TaskAdapter(List<Task> tasks, DatabaseHelper dbHelper) {
        this.allTasks = new ArrayList<>(tasks);
        this.displayedTasks = new ArrayList<>(tasks);
        this.dbHelper = dbHelper;
        filterTasks();
    }

    public void setShowCompleted(boolean show) {
        this.showCompleted = show;
        filterTasks();
    }

    private void filterTasks() {
        List<Task> newDisplayedTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (showCompleted || !task.isCompleted) {
                newDisplayedTasks.add(task);
            }
        }

        displayedTasks.clear();
        displayedTasks.addAll(newDisplayedTasks);

        new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = displayedTasks.get(position);
        holder.bind(task);


        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted);

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {

            holder.itemView.post(() -> {

                for (Task t : allTasks) {
                    if (t.id == task.id) {
                        t.isCompleted = isChecked;
                        break;
                    }
                }

                updateTaskInDatabase(task.id, isChecked);

                if (!showCompleted && isChecked) {
                    filterTasks();
                }
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClick(task);
            }
        });
    }

    private void updateTaskInDatabase(long taskId, boolean isCompleted) {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COL_STATUS, isCompleted ? "Gotowe" : "Nie rozpoczęto");

                db.update(DatabaseHelper.TABLE_TASKS,
                        values,
                        DatabaseHelper.COL_ID + " = ?",
                        new String[]{String.valueOf(taskId)});
            } finally {
                db.close();
            }
        }).start();
    }
    private void updateTaskAppearance(TaskViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return displayedTasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        allTasks.clear();
        allTasks.addAll(newTasks);
        filterTasks();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbCompleted;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvDueDate;
        final TextView tvStatus;
        final TextView tvPriority;
        final TextView tvAssignedTo;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
        }

        void bind(Task task) {

            cbCompleted.setOnCheckedChangeListener(null);

            tvName.setText(task.name);
            tvDescription.setText("Opis: " + task.description);
            tvDueDate.setText("Termin: " + task.dueDate);
            tvStatus.setText("Status: " + task.status);
            tvPriority.setText("Priorytet: " + task.priority);
            tvAssignedTo.setText("Przypisano do: " + task.assignedTo);
            cbCompleted.setChecked(task.isCompleted);

            if (task.isCompleted) {
                tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(0.6f);
            } else {
                tvName.setPaintFlags(tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                itemView.setAlpha(1f);
            }
        }
    }

    public static class Task {
        public final long id;
        public final String name;
        public final String description;
        public final String dueDate;
        public final String status;
        public final String priority;
        public final String assignedTo;
        public boolean isCompleted;

        public Task(long id, String name, String description, String dueDate,
                    String status, String priority, String assignedTo, boolean isCompleted) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.dueDate = dueDate;
            this.status = status;
            this.priority = priority;
            this.assignedTo = assignedTo;
            this.isCompleted = isCompleted;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Task)) return false;
            Task task = (Task) o;
            return id == task.id;
        }

    }
}
