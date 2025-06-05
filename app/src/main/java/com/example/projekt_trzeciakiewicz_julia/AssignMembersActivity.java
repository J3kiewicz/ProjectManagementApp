package com.example.projekt_trzeciakiewicz_julia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

public class AssignMembersActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private int projectId;
    private TextView tvCurrentMembers;
    private EditText etMemberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_members);

        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        db = new DatabaseHelper(this);

        tvCurrentMembers = findViewById(R.id.tvCurrentMembers);
        Button btnAssignRandom = findViewById(R.id.btnAssignRandom);
        Button btnBack = findViewById(R.id.btnBack);

        loadCurrentMembers();

        btnAssignRandom.setOnClickListener(v -> assignRandomMembers());
        btnBack.setOnClickListener(v -> finish());


    }

    private void loadCurrentMembers() {
        List<String> members = db.getProjectMembers(projectId);
        StringBuilder sb = new StringBuilder("Liczba członków\n");
        for (String member : members) {
            sb.append("• ").append(member).append("\n");
        }
        tvCurrentMembers.setText(sb.toString());
    }

    private void assignRandomMembers() {
        try {
            int count = Integer.parseInt(etMemberCount.getText().toString());
            List<String> projectMembers = db.getProjectMembers(projectId);

            if (projectMembers.isEmpty()) {
                Toast.makeText(this, "Brak członków w projekcie!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count <= 0 || count > projectMembers.size()) {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                return;
            }

            Collections.shuffle(projectMembers);
            List<String> assigned = projectMembers.subList(0, count);

            StringBuilder result = new StringBuilder("Członkowie:\n");
            for (String member : assigned) {
                result.append("• ").append(member).append("\n");
            }

            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
            loadCurrentMembers();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Wprowadź dozwolony znak", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}