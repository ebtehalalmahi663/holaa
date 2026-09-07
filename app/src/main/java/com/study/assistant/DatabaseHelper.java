package com.study.assistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_assistant.db";
    private static final int DB_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String COL_USERNAME = "username";
    public static final String COL_COLLEGE = "college";
    public static final String COL_DEPARTMENT = "department";
    public static final String COL_SEMESTER = "semester";
    public static final String COL_COURSE = "course";
    public static final String COL_LECTURE = "lecture";
    public static final String COL_SLIDE = "slide_number";
    public static final String COL_LAST_LOGIN = "last_login";

    public static final String TABLE_COURSES = "courses";
    public static final String COL_COURSE_ID = "id";
    public static final String COL_COURSE_USERNAME = "username";
    public static final String COL_COURSE_COLLEGE = "college";
    public static final String COL_COURSE_DEPARTMENT = "department";
    public static final String COL_COURSE_SEMESTER = "semester";
    public static final String COL_COURSE_NAME = "course_name";
    public static final String COL_COURSE_HOURS = "hours";
    public static final String COL_COURSE_LAB_LANG = "lab_language";

    public static final String TABLE_FILES = "lecture_files";
    public static final String COL_FILE_ID = "id";
    public static final String COL_FILE_COURSE_ID = "course_id";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_FILE_URI = "file_uri";

    public static final String TABLE_EXAM_HISTORY = "exam_history";
    public static final String COL_EH_ID = "id";
    public static final String COL_EH_USERNAME = "username";
    public static final String COL_EH_COLLEGE = "college";
    public static final String COL_EH_DEPARTMENT = "department";
    public static final String COL_EH_SEMESTER = "semester";
    public static final String COL_EH_COURSE = "course_name";
    public static final String COL_EH_CORRECT = "correct_count";
    public static final String COL_EH_TOTAL = "total_count";
    public static final String COL_EH_PERCENTAGE = "percentage";
    public static final String COL_EH_TIMESTAMP = "exam_timestamp";

    public static final String TABLE_REMINDERS = "reminders";
    public static final String COL_REM_ID = "id";
    public static final String COL_REM_USERNAME = "username";
    public static final String COL_REM_COURSE = "course_name";
    public static final String COL_REM_HOUR = "hour";
    public static final String COL_REM_MINUTE = "minute";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_COLLEGE + " TEXT, " +
                COL_DEPARTMENT + " TEXT, " +
                COL_SEMESTER + " TEXT, " +
                COL_COURSE + " TEXT, " +
                COL_LECTURE + " TEXT, " +
                COL_SLIDE + " INTEGER DEFAULT 0, " +
                COL_LAST_LOGIN + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_COURSES + " (" +
                COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COURSE_USERNAME + " TEXT, " +
                COL_COURSE_COLLEGE + " TEXT, " +
                COL_COURSE_DEPARTMENT + " TEXT, " +
                COL_COURSE_SEMESTER + " TEXT, " +
                COL_COURSE_NAME + " TEXT, " +
                COL_COURSE_HOURS + " INTEGER, " +
                COL_COURSE_LAB_LANG + " TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_FILES + " (" +
                COL_FILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FILE_COURSE_ID + " INTEGER, " +
                COL_FILE_NAME + " TEXT, " +
                COL_FILE_URI + " TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_EXAM_HISTORY + " (" +
                COL_EH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EH_USERNAME + " TEXT, " +
                COL_EH_COLLEGE + " TEXT, " +
                COL_EH_DEPARTMENT + " TEXT, " +
                COL_EH_SEMESTER + " TEXT, " +
                COL_EH_COURSE + " TEXT, " +
                COL_EH_CORRECT + " INTEGER, " +
                COL_EH_TOTAL + " INTEGER, " +
                COL_EH_PERCENTAGE + " REAL, " +
                COL_EH_TIMESTAMP + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_REMINDERS + " (" +
                COL_REM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REM_USERNAME + " TEXT, " +
                COL_REM_COURSE + " TEXT, " +
                COL_REM_HOUR + " INTEGER, " +
                COL_REM_MINUTE + " INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXAM_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    public void ensureUserExists(String username) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME},
                COL_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_LAST_LOGIN, System.currentTimeMillis());

        if (!exists) {
            values.put(COL_USERNAME, username);
            db.insert(TABLE_USERS, null, values);
        } else {
            db.update(TABLE_USERS, values, COL_USERNAME + "=?", new String[]{username});
        }
    }

    public Cursor getUserProgress(String username) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
    }

    public void updateUserProgress(String username, String college, String department,
                                    String semester, String course, String lecture, int slide) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COLLEGE, college);
        values.put(COL_DEPARTMENT, department);
        values.put(COL_SEMESTER, semester);
        values.put(COL_COURSE, course);
        values.put(COL_LECTURE, lecture);
        values.put(COL_SLIDE, slide);
        db.update(TABLE_USERS, values, COL_USERNAME + "=?", new String[]{username});
    }

    public long insertCourse(String username, String college, String department, String semester,
                              String courseName, int hours, String labLanguage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_USERNAME, username);
        values.put(COL_COURSE_COLLEGE, college);
        values.put(COL_COURSE_DEPARTMENT, department);
        values.put(COL_COURSE_SEMESTER, semester);
        values.put(COL_COURSE_NAME, courseName);
        values.put(COL_COURSE_HOURS, hours);
        values.put(COL_COURSE_LAB_LANG, labLanguage);
        return db.insert(TABLE_COURSES, null, values);
    }

    public Cursor getCourses(String username, String college, String department, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_COURSES, null,
                COL_COURSE_USERNAME + "=? AND " + COL_COURSE_COLLEGE + "=? AND " +
                        COL_COURSE_DEPARTMENT + "=? AND " + COL_COURSE_SEMESTER + "=?",
                new String[]{username, college, department, semester},
                null, null, COL_COURSE_ID + " DESC");
    }

    public Cursor getAllCourseNames(String username) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(true, TABLE_COURSES, new String[]{COL_COURSE_NAME},
                COL_COURSE_USERNAME + "=?", new String[]{username},
                null, null, COL_COURSE_NAME + " ASC", null);
    }

    public long insertLectureFile(long courseId, String fileName, String fileUri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FILE_COURSE_ID, courseId);
        values.put(COL_FILE_NAME, fileName);
        values.put(COL_FILE_URI, fileUri);
        return db.insert(TABLE_FILES, null, values);
    }

    public Cursor getLectureFiles(long courseId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_FILES, null, COL_FILE_COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)}, null, null, COL_FILE_ID + " DESC");
    }

    public long insertExamResult(String username, String college, String department, String semester,
                                  String courseName, int correctCount, int totalCount, double percentage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EH_USERNAME, username);
        values.put(COL_EH_COLLEGE, college);
        values.put(COL_EH_DEPARTMENT, department);
        values.put(COL_EH_SEMESTER, semester);
        values.put(COL_EH_COURSE, courseName);
        values.put(COL_EH_CORRECT, correctCount);
        values.put(COL_EH_TOTAL, totalCount);
        values.put(COL_EH_PERCENTAGE, percentage);
        values.put(COL_EH_TIMESTAMP, System.currentTimeMillis());
        return db.insert(TABLE_EXAM_HISTORY, null, values);
    }

    public Cursor getExamHistory(String username, String college, String department, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_EXAM_HISTORY, null,
                COL_EH_USERNAME + "=? AND " + COL_EH_COLLEGE + "=? AND " +
                        COL_EH_DEPARTMENT + "=? AND " + COL_EH_SEMESTER + "=?",
                new String[]{username, college, department, semester},
                null, null, COL_EH_TIMESTAMP + " DESC");
    }

    public long insertReminder(String username, String courseName, int hour, int minute) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REM_USERNAME, username);
        values.put(COL_REM_COURSE, courseName);
        values.put(COL_REM_HOUR, hour);
        values.put(COL_REM_MINUTE, minute);
        return db.insert(TABLE_REMINDERS, null, values);
    }

    public Cursor getReminders(String username) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_REMINDERS, null, COL_REM_USERNAME + "=?",
                new String[]{username}, null, null, COL_REM_ID + " DESC");
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REMINDERS, COL_REM_ID + "=?", new String[]{String.valueOf(id)});
    }
}