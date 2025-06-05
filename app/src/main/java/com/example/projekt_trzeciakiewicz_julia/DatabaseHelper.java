package com.example.projekt_trzeciakiewicz_julia;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Patterns;
import java.util.ArrayList;
import java.util.List;



public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ProjectManagement.db";
    public static final int DATABASE_VERSION = 2;
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_PROJECT_MEMBERS = "project_members";
    public static final String TABLE_TASK_ASSIGNMENTS = "task_assignments";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";

    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email";


    public static final String COL_PROJECT_ID = "project_id";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";


    public static final String COL_TASK_ID = "task_id";
    public static final String COL_STATUS = "status";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_DUE_DATE = "due_date";


    public static final String COL_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COL_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);


        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NAME + " TEXT NOT NULL,"
                + COL_DESCRIPTION + " TEXT,"
                + COL_START_DATE + " TEXT,"
                + COL_END_DATE + " TEXT)";
        db.execSQL(CREATE_PROJECTS_TABLE);


        String CREATE_PROJECT_MEMBERS_TABLE = "CREATE TABLE " + TABLE_PROJECT_MEMBERS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PROJECT_ID + " INTEGER NOT NULL,"
                + COL_USER_ID + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COL_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COL_ID + "),"
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "))";
        db.execSQL(CREATE_PROJECT_MEMBERS_TABLE);


        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PROJECT_ID + " INTEGER NOT NULL,"
                + COL_NAME + " TEXT NOT NULL,"
                + COL_DESCRIPTION + " TEXT,"
                + COL_STATUS + " TEXT DEFAULT 'Nie rozpoczęto',"
                + COL_PRIORITY + " TEXT DEFAULT 'Średni',"
                + COL_DUE_DATE + " TEXT,"
                + "FOREIGN KEY(" + COL_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COL_ID + "),"
                + "UNIQUE(" + COL_PROJECT_ID + ", " + COL_NAME + ") ON CONFLICT REPLACE)";

        db.execSQL(CREATE_TASKS_TABLE);


        String CREATE_TASK_ASSIGNMENTS_TABLE = "CREATE TABLE " + TABLE_TASK_ASSIGNMENTS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TASK_ID + " INTEGER NOT NULL,"
                + COL_USER_ID + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COL_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_ID + "),"
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "))";
        db.execSQL(CREATE_TASK_ASSIGNMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_ASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    public boolean registerUser(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_ID};
        String selection = COL_USERNAME + " = ?" + " AND " + COL_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_ID};
        String selection = COL_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public long addProject(String name, String description, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_START_DATE, startDate);
        values.put(COL_END_DATE, endDate);
        return db.insert(TABLE_PROJECTS, null, values);
    }


//    public List<String> getAllProjects() {
//        List<String> projects = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(TABLE_PROJECTS, new String[]{COL_ID, COL_NAME}, null, null, null, null, null);
//        if (cursor.moveToFirst()) {
//            do {
//                projects.add(cursor.getString(1) + " (ID: " + cursor.getInt(0) + ")");
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return projects;
//    }


    public long addTask(int projectId, String name, String description,
                        String status, String priority, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_PROJECT_ID, projectId);
            values.put(COL_NAME, name);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_STATUS, status);
            values.put(COL_PRIORITY, priority);
            values.put(COL_DUE_DATE, dueDate);

            Log.d("DatabaseHelper", "Dodano zadanie: " + name + " do projektu " + projectId);

            return db.insertWithOnConflict(TABLE_TASKS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

    public boolean assignTaskToUser(long taskId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_ID, taskId);
        values.put(COL_USER_ID, userId);
        long result = db.insert(TABLE_TASK_ASSIGNMENTS, null, values);
        return result != -1;
    }


    public Cursor getProjectDetails(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_PROJECTS,
                new String[]{COL_NAME, COL_DESCRIPTION, COL_START_DATE, COL_END_DATE},
                COL_ID + " = ?",
                new String[]{String.valueOf(projectId)},
                null, null, null);
    }

    public boolean updateProject(int projectId, String name, String description, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_START_DATE, startDate);
        values.put(COL_END_DATE, endDate);

        int rowsAffected = db.update(TABLE_PROJECTS, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(projectId)});
        return rowsAffected > 0;
    }

    public boolean deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_TASK_ASSIGNMENTS,
                COL_TASK_ID + " IN (SELECT " + COL_ID + " FROM " + TABLE_TASKS +
                        " WHERE " + COL_PROJECT_ID + " = ?)",
                new String[]{String.valueOf(projectId)});
        db.delete(TABLE_TASKS, COL_PROJECT_ID + " = ?",
                new String[]{String.valueOf(projectId)});

        db.delete(TABLE_PROJECT_MEMBERS, COL_PROJECT_ID + " = ?",
                new String[]{String.valueOf(projectId)});

        int rowsAffected = db.delete(TABLE_PROJECTS, COL_ID + " = ?",
                new String[]{String.valueOf(projectId)});
        return rowsAffected > 0;
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USERNAME},
                null, null, null, null, COL_USERNAME + " ASC");

        while (cursor.moveToNext()) {
            users.add(cursor.getString(0));
        }
        cursor.close();
        return users;
    }

    public List<String> getProjectMembers(long projectId) {
        List<String> members = new ArrayList<>();
        String query = "SELECT u." + COL_USERNAME + " FROM " + TABLE_USERS + " u " +
                "JOIN " + TABLE_PROJECT_MEMBERS + " pm ON u." + COL_ID + " = pm." + COL_USER_ID + " " +
                "WHERE pm." + COL_PROJECT_ID + " = ? " +
                "ORDER BY u." + COL_USERNAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});

        while (cursor.moveToNext()) {
            members.add(cursor.getString(0));
        }
        cursor.close();
        return members;
    }

    public boolean deleteProjectMembers(long projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_PROJECT_MEMBERS,
                COL_PROJECT_ID + " = ?",
                new String[]{String.valueOf(projectId)});
        return rowsDeleted > 0;
    }

    public boolean addProjectMember(long projectId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, projectId);
        values.put(COL_USER_ID, userId);
        long result = db.insert(TABLE_PROJECT_MEMBERS, null, values);
        db.close();
        return result != -1;
    }

    public Cursor getTaskDetails(long taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TASKS,
                new String[]{COL_ID, COL_NAME, COL_DESCRIPTION, COL_STATUS, COL_PRIORITY, COL_DUE_DATE},
                COL_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null, null, null);
    }

    public Cursor getTasksForProject(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TASKS,
                new String[]{COL_ID, COL_NAME, COL_DESCRIPTION, COL_STATUS, COL_PRIORITY, COL_DUE_DATE},
                COL_PROJECT_ID + " = ?",
                new String[]{String.valueOf(projectId)},
                null, null, COL_DUE_DATE + " ASC");
    }

    public boolean updateTask(long taskId, String name, String description,
                              String status, String priority, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, name);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_STATUS, status);
            values.put(COL_PRIORITY, priority);
            values.put(COL_DUE_DATE, dueDate);

            int rowsAffected = db.update(TABLE_TASKS, values,
                    COL_ID + " = ?",
                    new String[]{String.valueOf(taskId)});

            return rowsAffected > 0;
        } finally {
            db.close();
        }
    }


    public List<String> getUserProjects(String username) {
        List<String> projects = new ArrayList<>();
        int userId = getUserId(username);

        if (userId == -1) {
            return projects;
        }

        String query = "SELECT p." + COL_ID + ", p." + COL_NAME +
                " FROM " + TABLE_PROJECTS + " p" +
                " JOIN " + TABLE_PROJECT_MEMBERS + " pm ON p." + COL_ID + " = pm." + COL_PROJECT_ID +
                " WHERE pm." + COL_USER_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
            projects.add(name + " (ID: " + id + ")");
        }
        cursor.close();
        return projects;
    }
//    public boolean isUserProjectMember(int userId, int projectId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String query = "SELECT 1 FROM " + TABLE_PROJECT_MEMBERS +
//                " WHERE " + COL_PROJECT_ID + " = ? AND " + COL_USER_ID + " = ?";
//
//        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId), String.valueOf(userId)});
//        boolean isMember = cursor.getCount() > 0;
//        cursor.close();
//        return isMember;
//    }
}
