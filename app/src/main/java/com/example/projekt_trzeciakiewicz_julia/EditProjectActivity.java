package com.example.projekt_trzeciakiewicz_julia;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditProjectActivity extends AppCompatActivity {
    private EditText etProjectName, etProjectDescription;
    private Button btnStartDate, btnEndDate, btnSave;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private DatabaseHelper db;
    private int projectId;

    private ListView lvCurrentMembers;
    private Spinner spinnerAvailableUsers;
    private Button btnAddMember, btnRemoveMember;
    private ArrayAdapter<String> membersAdapter;
    private ArrayAdapter<String> usersAdapter;
    private List<String> currentMembers = new ArrayList<>();
    private List<String> availableUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        db = new DatabaseHelper(this);
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        initViews();
        initMemberViews();
        loadProjectData();
        loadMembersData();
        setupDateButtons();
        setupSaveButton();
        setupMemberButtons();
    }

    private void initViews() {
        etProjectName = findViewById(R.id.etProjectName);
        etProjectDescription = findViewById(R.id.etProjectDescription);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSave = findViewById(R.id.btnSave);
    }

    private void initMemberViews() {
        lvCurrentMembers = findViewById(R.id.lvCurrentMembers);
        spinnerAvailableUsers = findViewById(R.id.spinnerAvailableUsers);
        btnAddMember = findViewById(R.id.btnAddMember);
        btnRemoveMember = findViewById(R.id.btnRemoveMember);

        membersAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, currentMembers);
        lvCurrentMembers.setAdapter(membersAdapter);
        lvCurrentMembers.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableUsers);
        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailableUsers.setAdapter(usersAdapter);
    }

    private void loadProjectData() {
        Cursor cursor = db.getProjectDetails(projectId);
        if (cursor.moveToFirst()) {
            etProjectName.setText(cursor.getString(0));
            etProjectDescription.setText(cursor.getString(1));

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date startDate = sdf.parse(cursor.getString(2));
                Date endDate = sdf.parse(cursor.getString(3));

                if (startDate != null) startDateCalendar.setTime(startDate);
                if (endDate != null) endDateCalendar.setTime(endDate);

                updateDateButtons();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
    }

    private void setupDateButtons() {
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, month);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtons();
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, month);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtons();
        };

        btnStartDate.setOnClickListener(v -> new DatePickerDialog(
                EditProjectActivity.this,
                startDateListener,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnEndDate.setOnClickListener(v -> new DatePickerDialog(
                EditProjectActivity.this,
                endDateListener,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());
    }

    private void updateDateButtons() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        btnEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void loadMembersData() {
        currentMembers.clear();
        currentMembers.addAll(db.getProjectMembers(projectId));

        availableUsers.clear();
        availableUsers.addAll(db.getAllUsers());
        availableUsers.removeAll(currentMembers);

        membersAdapter.notifyDataSetChanged();
        usersAdapter.notifyDataSetChanged();
    }

    private void setupMemberButtons() {
        btnAddMember.setOnClickListener(v -> {
            String selectedUser = (String) spinnerAvailableUsers.getSelectedItem();
            if (selectedUser != null) {
                currentMembers.add(selectedUser);
                availableUsers.remove(selectedUser);
                updateAdapters();
            }
        });

        btnRemoveMember.setOnClickListener(v -> {
            int position = lvCurrentMembers.getCheckedItemPosition();
            if (position != ListView.INVALID_POSITION) {
                String removedUser = currentMembers.remove(position);
                availableUsers.add(removedUser);
                updateAdapters();
                lvCurrentMembers.clearChoices();
            }
        });
    }

    private void updateAdapters() {
        membersAdapter.notifyDataSetChanged();
        usersAdapter.notifyDataSetChanged();
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String name = etProjectName.getText().toString().trim();
            String description = etProjectDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etProjectName.setError("Project name is required");
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());

            if (db.updateProject(projectId, name, description, startDate, endDate)) {

                db.deleteProjectMembers(projectId);


                for (String member : currentMembers) {
                    int userId = db.getUserId(member);
                    if (userId != -1) {
                        db.addProjectMember(projectId, userId);
                    }
                }

                Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update project", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}