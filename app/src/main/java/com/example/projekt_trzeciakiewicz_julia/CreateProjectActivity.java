package com.example.projekt_trzeciakiewicz_julia;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateProjectActivity extends AppCompatActivity {
    private EditText etProjectName, etProjectDescription;
    private Button btnStartDate, btnEndDate, btnCreateProject, btnAddMember;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private DatabaseHelper db;
    private String username;
    private Spinner spinnerMembers;
    private List<String> allUsers;
    private List<String> selectedMembers = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private TextView tvSelectedMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        etProjectName = findViewById(R.id.etProjectName);
        etProjectDescription = findViewById(R.id.etProjectDescription);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnCreateProject = findViewById(R.id.btnCreateProject);
        btnAddMember = findViewById(R.id.btnAddMember);
        spinnerMembers = findViewById(R.id.spinnerMembers);
        tvSelectedMembers = findViewById(R.id.tvSelectedMembers);


        allUsers = db.getAllUsers();
        allUsers.remove(username);
        selectedMembers.add(username);


        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allUsers);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMembers.setAdapter(spinnerAdapter);


        btnAddMember.setOnClickListener(v -> {
            String selected = (String) spinnerMembers.getSelectedItem();
            if (selected != null && !selectedMembers.contains(selected)) {
                selectedMembers.add(selected);
                allUsers.remove(selected);
                spinnerAdapter.notifyDataSetChanged();
                updateSelectedMembersList();
            }
        });


        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, month);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateButton();
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, month);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEndDateButton();
        };

        btnStartDate.setOnClickListener(v -> new DatePickerDialog(CreateProjectActivity.this, startDateListener,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnEndDate.setOnClickListener(v -> new DatePickerDialog(CreateProjectActivity.this, endDateListener,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show());


        btnCreateProject.setOnClickListener(v -> createProject());
    }

    private void updateStartDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
    }

    private void updateEndDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void updateSelectedMembersList() {
        StringBuilder sb = new StringBuilder("Członkowie: ");
        for (int i = 0; i < selectedMembers.size(); i++) {
            String member = selectedMembers.get(i);
            sb.append(member);
            if (i != selectedMembers.size() - 1) {
                sb.append(", ");
            }
        }
        tvSelectedMembers.setText(sb.toString());
        tvSelectedMembers.setOnClickListener(v -> showRemoveMemberDialog());
    }
    private void showRemoveMemberDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Usuń członka")
                .setItems(selectedMembers.toArray(new String[0]), (dialog, which) -> {
                    String removed = selectedMembers.remove(which);
                    allUsers.add(removed);
                    spinnerAdapter.notifyDataSetChanged();
                    updateSelectedMembersList();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }


    private void createProject() {
        String projectName = etProjectName.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();

        if (projectName.isEmpty()) {
            Toast.makeText(this, "Nazwa projektu jest wymagana", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(startDateCalendar.getTime());
        String endDate = dateFormat.format(endDateCalendar.getTime());

        long projectId = db.addProject(projectName, description, startDate, endDate);
        if (projectId != -1) {
            for (String member : selectedMembers) {
                int userId = db.getUserId(member);
                if (userId != -1) {
                    db.addProjectMember(projectId, userId);
                }
            }
            Toast.makeText(this, "Sukces", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Błąd", Toast.LENGTH_SHORT).show();
        }
    }
}
