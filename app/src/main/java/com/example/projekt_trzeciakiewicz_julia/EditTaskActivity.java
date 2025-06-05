package com.example.projekt_trzeciakiewicz_julia;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {
    private EditText etTaskName, etTaskDescription;
    private Spinner spinnerStatus, spinnerPriority, spinnerAssignedTo;
    private Button btnDueDate, btnSave;
    private Calendar dueDateCalendar = Calendar.getInstance();
    private DatabaseHelper db;
    private long taskId;
    private int projectId;
    private List<String> projectMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        db = new DatabaseHelper(this);
        taskId = getIntent().getLongExtra("TASK_ID", -1);
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        initViews();
        setupSpinners();
        setupAssignedToSpinner();
        loadTaskData();
        setupDateButton();
        setupSaveButton();
    }

    private void initViews() {
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerAssignedTo = findViewById(R.id.spinnerAssignedTo);
        btnDueDate = findViewById(R.id.btnDueDate);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinners() {

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);


        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_priorities, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void setupAssignedToSpinner() {
        projectMembers = db.getProjectMembers(projectId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, projectMembers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignedTo.setAdapter(adapter);
    }

    private void loadTaskData() {
        Cursor cursor = db.getTaskDetails(taskId);
        if (cursor.moveToFirst()) {
            etTaskName.setText(cursor.getString(1));
            etTaskDescription.setText(cursor.getString(2));


            String status = cursor.getString(3);
            ArrayAdapter<CharSequence> statusAdapter = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
            int statusPosition = statusAdapter.getPosition(status);
            if (statusPosition >= 0) {
                spinnerStatus.setSelection(statusPosition);
            }


            String priority = cursor.getString(4);
            ArrayAdapter<CharSequence> priorityAdapter = (ArrayAdapter<CharSequence>) spinnerPriority.getAdapter();
            int priorityPosition = priorityAdapter.getPosition(priority);
            if (priorityPosition >= 0) {
                spinnerPriority.setSelection(priorityPosition);
            }


            String currentAssigned = getCurrentAssignedMember(taskId);
            if (currentAssigned != null && projectMembers.contains(currentAssigned)) {
                int position = projectMembers.indexOf(currentAssigned);
                spinnerAssignedTo.setSelection(position);
            }


            String dueDate = cursor.getString(5);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dueDateCalendar.setTime(sdf.parse(dueDate));
                updateDateButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
    }

    private String getCurrentAssignedMember(long taskId) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT u." + DatabaseHelper.COL_USERNAME +
                        " FROM " + DatabaseHelper.TABLE_USERS + " u" +
                        " JOIN " + DatabaseHelper.TABLE_TASK_ASSIGNMENTS + " ta ON u." + DatabaseHelper.COL_ID + " = ta." + DatabaseHelper.COL_USER_ID +
                        " WHERE ta." + DatabaseHelper.COL_TASK_ID + " = ? LIMIT 1",
                new String[]{String.valueOf(taskId)});

        String assignedTo = null;
        if (cursor.moveToFirst()) {
            assignedTo = cursor.getString(0);
        }
        cursor.close();
        return assignedTo;
    }

    private void setupDateButton() {
        DatePickerDialog.OnDateSetListener dateListener = (view, year, month, dayOfMonth) -> {
            dueDateCalendar.set(Calendar.YEAR, year);
            dueDateCalendar.set(Calendar.MONTH, month);
            dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButton();
        };

        btnDueDate.setOnClickListener(v -> new DatePickerDialog(
                EditTaskActivity.this,
                dateListener,
                dueDateCalendar.get(Calendar.YEAR),
                dueDateCalendar.get(Calendar.MONTH),
                dueDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show());
    }

    private void updateDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnDueDate.setText(dateFormat.format(dueDateCalendar.getTime()));
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();
            String priority = spinnerPriority.getSelectedItem().toString();
            String assignedTo = (String) spinnerAssignedTo.getSelectedItem();
            String dueDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(dueDateCalendar.getTime());

            if (name.isEmpty()) {
                etTaskName.setError("Nazwa zadania jest wymagana");
                return;
            }

            if (db.updateTask(taskId, name, description, status, priority, dueDate)) {
                updateAssignedMember(taskId, assignedTo);
                Toast.makeText(this, "Zaaktualizowano zadanie", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Błąd przy aktualizacji zadania", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAssignedMember(long taskId, String username) {
        int userId = db.getUserId(username);
        if (userId != -1) {
            db.getWritableDatabase().delete(
                    DatabaseHelper.TABLE_TASK_ASSIGNMENTS,
                    DatabaseHelper.COL_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)});


            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_TASK_ID, taskId);
            values.put(DatabaseHelper.COL_USER_ID, userId);
            db.getWritableDatabase().insert(
                    DatabaseHelper.TABLE_TASK_ASSIGNMENTS,
                    null,
                    values);
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}