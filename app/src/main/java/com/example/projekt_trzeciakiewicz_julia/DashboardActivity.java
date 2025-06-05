package com.example.projekt_trzeciakiewicz_julia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnCreateProject, btnViewProjects, btnLogout;
    private Button btnSaveNote, btnLoadNote, btnDeleteNote, btnNewNote, btnListNotes;
    private EditText etNotes, etNoteName;
    private String username;
    private String currentNoteName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        username = getIntent().getStringExtra("USERNAME");

        tvWelcome = findViewById(R.id.tvWelcome);
        btnCreateProject = findViewById(R.id.btnCreateProject);
        btnViewProjects = findViewById(R.id.btnViewProjects);
        btnLogout = findViewById(R.id.btnLogout);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        btnNewNote = findViewById(R.id.btnNewNote);
        btnListNotes = findViewById(R.id.btnListNotes);
        etNotes = findViewById(R.id.etNotes);
        etNoteName = findViewById(R.id.etNoteName);

        tvWelcome.setText("Witaj, " + username + "!");

        btnCreateProject.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CreateProjectActivity.class)
                        .putExtra("USERNAME", username))
        );

        btnViewProjects.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProjectListActivity.class)
                        .putExtra("USERNAME", username))
        );

        btnNewNote.setOnClickListener(v -> {
            currentNoteName = etNoteName.getText().toString().trim();
            if (currentNoteName.isEmpty()) {
                Toast.makeText(this, "Proszę wprowadzić nazwę", Toast.LENGTH_SHORT).show();
                return;
            }
            etNotes.setText("");
            Toast.makeText(this, "Nowa stworzona notatka: " + currentNoteName, Toast.LENGTH_SHORT).show();
        });

        btnSaveNote.setOnClickListener(v -> {
            if (currentNoteName.isEmpty()) {
                Toast.makeText(this, "Najpierw stwórz lub wybierz notatkę", Toast.LENGTH_SHORT).show();
                return;
            }
            saveNote(currentNoteName);
        });


        btnDeleteNote.setOnClickListener(v -> {
            if (currentNoteName.isEmpty()) {
                Toast.makeText(this, "Proszę wprowadzić nazwę", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteNote(currentNoteName);
        });

        btnListNotes.setOnClickListener(v -> showNotesList());

        btnLogout.setOnClickListener(v -> finish());
    }

    private void saveNote(String noteName) {
        String notesText = etNotes.getText().toString();
        try {
            FileOutputStream fos = openFileOutput(noteName + ".txt", MODE_PRIVATE);
            fos.write(notesText.getBytes());
            fos.close();
            Toast.makeText(this, "Notatka zapisana: " + noteName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd podczas zapisywania", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNote(String noteName) {
        try {
            FileInputStream fis = openFileInput(noteName + ".txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            etNotes.setText(sb.toString());
            fis.close();
            Toast.makeText(this, "Notatka załadowana: " + noteName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            etNotes.setText("");
            Toast.makeText(this, "Nie znaleziono notatki: " + noteName, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote(String noteName) {
        File file = new File(getFilesDir(), noteName + ".txt");
        if (file.exists()) {
            if (file.delete()) {
                etNotes.setText("");
                currentNoteName = "";
                etNoteName.setText("");
                Toast.makeText(this, "Notatka usunięta: " + noteName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nie znaleziono notatki", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getNotesList() {
        List<String> notes = new ArrayList<>();
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".txt")) {
                    notes.add(file.getName().replace(".txt", ""));
                }
            }
        }
        return notes;
    }

    private void showNotesList() {
        List<String> notes = getNotesList();
        if (notes.isEmpty()) {
            Toast.makeText(this, "Nie znaleziono notatki", Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] items = notes.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Wybierz notatke")
                .setItems(items, (dialog, which) -> {
                    currentNoteName = items[which].toString();
                    etNoteName.setText(currentNoteName);
                    loadNote(currentNoteName);
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!currentNoteName.isEmpty()) {
            saveNote(currentNoteName);
        }
    }
}