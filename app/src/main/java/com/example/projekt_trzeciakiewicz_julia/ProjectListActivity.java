package com.example.projekt_trzeciakiewicz_julia;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import static com.example.projekt_trzeciakiewicz_julia.DatabaseHelper.COL_PROJECT_ID;
import static com.example.projekt_trzeciakiewicz_julia.DatabaseHelper.COL_USER_ID;
import static com.example.projekt_trzeciakiewicz_julia.DatabaseHelper.TABLE_PROJECT_MEMBERS;
public class ProjectListActivity extends AppCompatActivity {
    private ListView lvProjects;
    private Button btnBackToDashboard;
    private DatabaseHelper db;
    private String username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);



        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        lvProjects = findViewById(R.id.lvProjects);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        int userId = db.getUserId(username);
        if (userId == -1) {
            Toast.makeText(this, "Nie znaleziono użytkownika", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<String> projects = db.getUserProjects(username);

        if (projects.isEmpty()) {
            Toast.makeText(this, "Nie jesteś członkiem żadnego projektu", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, projects);
        lvProjects.setAdapter(adapter);

        lvProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedProject = (String) parent.getItemAtPosition(position);
                int projectId = Integer.parseInt(selectedProject.split("ID: ")[1].replace(")", ""));

                if (isUserProjectMember(userId, projectId)) {
                    startActivity(new Intent(ProjectListActivity.this, ProjectDetailsActivity.class)
                            .putExtra("PROJECT_ID", projectId)
                            .putExtra("USERNAME", username));
                } else {
                    Toast.makeText(ProjectListActivity.this,
                            "Nie masz dostępu do tego projektu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBackToDashboard.setOnClickListener(v -> finish());
    }

    private boolean isUserProjectMember(int userId, int projectId) {
        SQLiteDatabase db = this.db.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_PROJECT_MEMBERS +
                " WHERE " + COL_PROJECT_ID + " = ? AND " + COL_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId), String.valueOf(userId)});
        boolean isMember = cursor.getCount() > 0;
        cursor.close();
        return isMember;
    }
}