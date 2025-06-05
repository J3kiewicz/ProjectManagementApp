package com.example.projekt_trzeciakiewicz_julia;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {
    private EditText etTaskName, etTaskDescription;
    private Spinner spinnerStatus, spinnerPriority;
    private Button btnDueDate, btnAssignMembers, btnCreateTask, btnAssignRandom;
    private Calendar dueDateCalendar = Calendar.getInstance();
    private DatabaseHelper db;
    private int projectId;
    private String username;
    private List<String> selectedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        db = new DatabaseHelper(this);

        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnDueDate = findViewById(R.id.btnDueDate);
        btnAssignMembers = findViewById(R.id.btnAssignMembers);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        btnAssignRandom = findViewById(R.id.btnAssignRandom);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_priorities, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);


        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dueDateCalendar.set(Calendar.YEAR, year);
                dueDateCalendar.set(Calendar.MONTH, month);
                dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDueDateButton();
            }
        };

        btnDueDate.setOnClickListener(v -> new DatePickerDialog(CreateTaskActivity.this, dateListener,
                dueDateCalendar.get(Calendar.YEAR),
                dueDateCalendar.get(Calendar.MONTH),
                dueDateCalendar.get(Calendar.DAY_OF_MONTH)).show());


        btnAssignMembers.setOnClickListener(v -> showMemberSelectionDialog());


        btnAssignRandom.setOnClickListener(v -> assignRandomMember());


        btnCreateTask.setOnClickListener(v -> createTask());
    }

    private void showMemberSelectionDialog() {
        List<String> projectMembers = db.getProjectMembers(projectId);
        if (projectMembers.isEmpty()) {
            Toast.makeText(this, "Brak członków w projekcie", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] checkedItems = new boolean[projectMembers.size()];
        for (int i = 0; i < projectMembers.size(); i++) {
            checkedItems[i] = selectedMembers.contains(projectMembers.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz członków do zadania")
                .setMultiChoiceItems(projectMembers.toArray(new String[0]), checkedItems,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                selectedMembers.add(projectMembers.get(which));
                            } else {
                                selectedMembers.remove(projectMembers.get(which));
                            }
                        })
                .setPositiveButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Wybrani członkowie: " + selectedMembers.size(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void assignRandomMember() {
        List<String> projectMembers = db.getProjectMembers(projectId);
        if (projectMembers.isEmpty()) {
            Toast.makeText(this, "Brak członków w projekcie", Toast.LENGTH_SHORT).show();
            return;
        }


        selectedMembers.clear();


        Collections.shuffle(projectMembers);
        String randomMember = projectMembers.get(0);
        selectedMembers.add(randomMember);


        new AlertDialog.Builder(this)
                .setTitle("Losowanie")
                .setMessage("Wylosowano: " + randomMember)
                .setPositiveButton("OK", null)
                .show();
    }

    private void createTask() {
        String taskName = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String priority = spinnerPriority.getSelectedItem().toString();

        if (taskName.isEmpty()) {
            etTaskName.setError("Nazwa zadania jest wymagana");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dueDate = dateFormat.format(dueDateCalendar.getTime());


        long taskId = db.addTask(projectId, taskName, description, status, priority, dueDate);

        if (taskId == -1) {
            Toast.makeText(this, "Błąd", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!selectedMembers.isEmpty()) {
            for (String member : selectedMembers) {
                int userId = db.getUserId(member);
                db.assignTaskToUser(taskId, userId);
            }
            Toast.makeText(this, "Stworzono i przydzielono do " + selectedMembers.size(), Toast.LENGTH_SHORT).show();
        } else {

            int userId = db.getUserId(username);
            db.assignTaskToUser(taskId, userId);
            Toast.makeText(this, "Stworzono i przydzielono do ", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }

    private void updateDueDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnDueDate.setText(dateFormat.format(dueDateCalendar.getTime()));
    }
}