
package com.sakshi.worklist;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText taskInput;
    Button addTaskButton;
    LinearLayout taskContainer;
    TextView emptyMessage;

    DatabaseHelper databaseHelper;

    String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnMorningRoutine).setOnClickListener(v -> {
            android.content.Intent intent =
                    new android.content.Intent(
                            MainActivity.this,
                            ReminderSettingsActivity.class
                    );

            startActivity(intent);
        });

        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        taskContainer = findViewById(R.id.taskContainer);
        emptyMessage = findViewById(R.id.emptyMessage);

        databaseHelper = new DatabaseHelper(this);

        addTaskButton.setOnClickListener(v -> addTask());

        findViewById(R.id.btnAll).setOnClickListener(v -> {
            currentFilter = "all";
            loadTasks();
        });

        findViewById(R.id.btnPending).setOnClickListener(v -> {
            currentFilter = "pending";
            loadTasks();
        });

        findViewById(R.id.btnCompleted).setOnClickListener(v -> {
            currentFilter = "completed";
            loadTasks();
        });

        loadTasks();
    }

    private void addTask() {
        String title = taskInput.getText().toString().trim();

        if (title.isEmpty()) {
            taskInput.setError("Please enter a task");
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TITLE, title);
        values.put(DatabaseHelper.COL_COMPLETED, 0);

        long result = db.insert(
                DatabaseHelper.TABLE_TASKS,
                null,
                values
        );

        if (result != -1) {
            taskInput.setText("");

            Toast.makeText(
                    this,
                    "Task saved successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            loadTasks();
        } else {
            Toast.makeText(
                    this,
                    "Unable to save task",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadTasks() {
        taskContainer.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection = null;

        if (currentFilter.equals("pending")) {
            selection = DatabaseHelper.COL_COMPLETED + " = 0";
        } else if (currentFilter.equals("completed")) {
            selection = DatabaseHelper.COL_COMPLETED + " = 1";
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASKS,
                null,
                selection,
                null,
                null,
                null,
                DatabaseHelper.COL_ID + " DESC"
        );

        try {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_ID
                        )
                );

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_TITLE
                        )
                );

                int completed = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_COMPLETED
                        )
                );

                // Create task row
                LinearLayout taskRow = new LinearLayout(this);
                taskRow.setOrientation(LinearLayout.HORIZONTAL);
                taskRow.setGravity(Gravity.CENTER_VERTICAL);
                taskRow.setPadding(8, 4, 8, 4);

                // Create checkbox
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(title);
                checkBox.setTextSize(16);
                checkBox.setPadding(4, 8, 4, 8);
                checkBox.setChecked(completed == 1);

                LinearLayout.LayoutParams checkBoxParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );

                taskRow.addView(checkBox, checkBoxParams);

                // Create Edit button
                Button editButton = new Button(this);
                editButton.setText("Edit");
                taskRow.addView(editButton);

                // Create Delete button
                Button deleteButton = new Button(this);
                deleteButton.setText("Delete");
                taskRow.addView(deleteButton);

                taskContainer.addView(taskRow);

                // Mark task completed or pending
                checkBox.setOnCheckedChangeListener(
                        (buttonView, isChecked) -> {

                            SQLiteDatabase writeDb =
                                    databaseHelper.getWritableDatabase();

                            ContentValues values = new ContentValues();
                            values.put(
                                    DatabaseHelper.COL_COMPLETED,
                                    isChecked ? 1 : 0
                            );

                            writeDb.update(
                                    DatabaseHelper.TABLE_TASKS,
                                    values,
                                    DatabaseHelper.COL_ID + " = ?",
                                    new String[]{String.valueOf(id)}
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    isChecked
                                            ? "Task completed!"
                                            : "Task marked pending",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Refresh the current filter
                            loadTasks();
                        });

                // Edit task
                editButton.setOnClickListener(v -> {

                    EditText editInput = new EditText(
                            MainActivity.this
                    );

                    editInput.setText(title);
                    editInput.setSelection(
                            editInput.getText().length()
                    );

                    editInput.setSingleLine(true);

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Edit task")
                            .setView(editInput)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Save", (dialog, which) -> {

                                String updatedTitle =
                                        editInput.getText()
                                                .toString()
                                                .trim();

                                if (updatedTitle.isEmpty()) {
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Task cannot be empty",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                ContentValues values =
                                        new ContentValues();

                                values.put(
                                        DatabaseHelper.COL_TITLE,
                                        updatedTitle
                                );

                                SQLiteDatabase writeDb =
                                        databaseHelper
                                                .getWritableDatabase();

                                int rowsUpdated = writeDb.update(
                                        DatabaseHelper.TABLE_TASKS,
                                        values,
                                        DatabaseHelper.COL_ID + " = ?",
                                        new String[]{
                                                String.valueOf(id)
                                        }
                                );

                                if (rowsUpdated > 0) {
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Task updated",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Unable to update task",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                loadTasks();
                            })
                            .show();
                });

                // Delete task with confirmation
                deleteButton.setOnClickListener(v -> {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete task")
                            .setMessage(
                                    "Are you sure you want to delete this task?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        SQLiteDatabase writeDb =
                                                databaseHelper
                                                        .getWritableDatabase();

                                        int rowsDeleted = writeDb.delete(
                                                DatabaseHelper.TABLE_TASKS,
                                                DatabaseHelper.COL_ID + " = ?",
                                                new String[]{
                                                        String.valueOf(id)
                                                }
                                        );

                                        if (rowsDeleted > 0) {
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    "Task deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }

                                        loadTasks();
                                    })
                            .show();
                });
            }

            emptyMessage.setVisibility(
                    cursor.getCount() == 0
                            ? TextView.VISIBLE
                            : TextView.GONE
            );

        } finally {
            cursor.close();
        }
    }
}