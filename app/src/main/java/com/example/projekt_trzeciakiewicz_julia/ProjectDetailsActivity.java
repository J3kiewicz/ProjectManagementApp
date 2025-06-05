package com.example.projekt_trzeciakiewicz_julia;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProjectDetailsActivity extends AppCompatActivity {
    private static final int EDIT_PROJECT_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int CREATE_TASK_REQUEST = 3;

    private DatabaseHelper db;
    private int projectId;
    private String username;
    private TextView tvProjectName, tvProjectDetails;
    private Button btnEditProject, btnDeleteProject;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<TaskAdapter.Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        db = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        loadProjectDetails();
        loadTasks();
        setupButtonListeners();
    }

    private void initViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProjectDetails = findViewById(R.id.tvProjectDetails);
        btnEditProject = findViewById(R.id.btnEditProject);
        btnDeleteProject = findViewById(R.id.btnDeleteProject);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(tasks, db);
        recyclerView.setAdapter(adapter);

        adapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(ProjectDetailsActivity.this, EditTaskActivity.class);
            intent.putExtra("TASK_ID", task.id);
            intent.putExtra("PROJECT_ID", projectId);
            startActivityForResult(intent, EDIT_TASK_REQUEST);
        });
    }

    private void loadProjectDetails() {
        Cursor cursor = db.getProjectDetails(projectId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String description = cursor.getString(1);
            String startDate = cursor.getString(2);
            String endDate = cursor.getString(3);

            tvProjectName.setText(name);

            List<String> members = db.getProjectMembers(projectId);
            String details = "Opis: " + description + "\n\n" +
                    "Data rozpoczęcia: " + startDate + "\n" +
                    "Data zakończenia: " + endDate + "\n\n" +
                    "Członkowie: " + String.join(", ", members);

            tvProjectDetails.setText(details);
        }
        cursor.close();
    }

    private void loadTasks() {
        new Thread(() -> {
            List<TaskAdapter.Task> newTasks = new ArrayList<>();
            Cursor cursor = db.getTasksForProject(projectId);

            while (cursor.moveToNext()) {
                String assignedMembers = getAssignedMembers(cursor.getLong(0));
                boolean isCompleted = cursor.getString(3).equals("Gotowe");

                newTasks.add(new TaskAdapter.Task(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(5),
                        cursor.getString(3),
                        cursor.getString(4),
                        assignedMembers,
                        isCompleted
                ));
            }
            cursor.close();

            runOnUiThread(() -> adapter.updateTasks(newTasks));
        }).start();
    }
    private String getAssignedMembers(long taskId) {
        StringBuilder members = new StringBuilder();
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT u." + DatabaseHelper.COL_USERNAME +
                        " FROM " + DatabaseHelper.TABLE_USERS + " u" +
                        " JOIN " + DatabaseHelper.TABLE_TASK_ASSIGNMENTS + " ta ON u." + DatabaseHelper.COL_ID + " = ta." + DatabaseHelper.COL_USER_ID +
                        " WHERE ta." + DatabaseHelper.COL_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});

        while (cursor.moveToNext()) {
            if (members.length() > 0) members.append(", ");
            members.append(cursor.getString(0));
        }
        cursor.close();
        return members.toString();
    }

    private void setupButtonListeners() {
        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("USERNAME", username);
            startActivityForResult(intent, CREATE_TASK_REQUEST);
        });

        findViewById(R.id.btnBackToProjects).setOnClickListener(v -> finish());

        btnEditProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProjectActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivityForResult(intent, EDIT_PROJECT_REQUEST);
        });

        btnDeleteProject.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Project")
                    .setMessage("Jesteś pewny, że chcesz usunąć projekt??")
                    .setPositiveButton("Usuń", (dialog, which) -> {
                        if (db.deleteProject(projectId)) {
                            Toast.makeText(this, "Usunięto projekt", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });
        RadioGroup rgTaskFilter = findViewById(R.id.rgTaskFilter);
        RadioButton rbShowAll = findViewById(R.id.rbShowAll);
        RadioButton rbShowActive = findViewById(R.id.rbShowActive);

        rgTaskFilter.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showAll = checkedId == rbShowAll.getId();
            adapter.setShowCompleted(showAll);

            loadTasks();
        });

        rbShowActive.setChecked(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_PROJECT_REQUEST) {
                loadProjectDetails();
            } else if (requestCode == EDIT_TASK_REQUEST || requestCode == CREATE_TASK_REQUEST) {
                loadTasks();
            }
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}