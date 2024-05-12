package com.example.myapplication

import DatabaseContract
import DatabaseHelper
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val editTextTask = findViewById<EditText>(R.id.editTextTask)
        val editTextTime = findViewById<TextInputEditText>(R.id.editTextTime)
        val buttonAdd = findViewById<Button>(R.id.buttonAdd)
        val recyclerViewTasks = findViewById<RecyclerView>(R.id.recyclerViewTasks)

        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter()
        recyclerViewTasks.adapter = taskAdapter

        loadTasks()

        editTextTime.setOnClickListener {
            showTimePickerDialog(editTextTime)
        }

        buttonAdd.setOnClickListener {
            val task = editTextTask.text.toString()
            val time = editTextTime.text.toString()
            if (task.isNotEmpty()) {
                addTask(task, time)
                editTextTask.text.clear()
                editTextTime.text?.clear()
            }
        }
    }

    private fun showTimePickerDialog(editTextTime: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editTextTime.setText(selectedTime)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }


    private fun addTask(task: String, time: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.TaskEntry.COLUMN_NAME_TASK, task)
            time?.let {
                put(DatabaseContract.TaskEntry.COLUMN_NAME_TIME, it)
            }
        }
        val newRowId = db?.insert(DatabaseContract.TaskEntry.TABLE_NAME, null, values)
        newRowId?.let {
            loadTasks()
        }
    }

    private fun deleteTask(taskId: Long) {
        val db = dbHelper.writableDatabase
        db?.delete(
            DatabaseContract.TaskEntry.TABLE_NAME,
            "${DatabaseContract.TaskEntry._ID} = ?",
            arrayOf(taskId.toString())
        )
        loadTasks()
    }

    private fun updateTask(taskId: Long, newTask: String, newTime: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.TaskEntry.COLUMN_NAME_TASK, newTask)
            newTime?.let {
                put(DatabaseContract.TaskEntry.COLUMN_NAME_TIME, it)
            }
        }

        db.update(
            DatabaseContract.TaskEntry.TABLE_NAME,
            values,
            "${DatabaseContract.TaskEntry._ID} = ?",
            arrayOf(taskId.toString())
        )

        loadTasks()
    }


    private fun loadTasks() {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            DatabaseContract.TaskEntry._ID,
            DatabaseContract.TaskEntry.COLUMN_NAME_TASK,
            DatabaseContract.TaskEntry.COLUMN_NAME_TIME
        )
        val cursor = db.query(
            DatabaseContract.TaskEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val tasksList = ArrayList<Triple<Long, String, String?>>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(DatabaseContract.TaskEntry._ID))
                val task = getString(getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_NAME_TASK))
                val time = getString(getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_NAME_TIME))
                tasksList.add(Triple(id, task, time))
            }
        }
        taskAdapter.setTasks(tasksList)
    }

    inner class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

        private var tasksList = ArrayList<Triple<Long, String, String?>>()

        inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val taskTextView: TextView = itemView.findViewById(R.id.textViewTask)
            val timeTextView: TextView = itemView.findViewById(R.id.textViewTime)
            val deleteButton: ImageView = itemView.findViewById(R.id.buttonDelete)
            val editButton: ImageView = itemView.findViewById(R.id.buttonEdit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_item, parent, false)
            return TaskViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val currentTask = tasksList[position]
            holder.taskTextView.text = currentTask.second
            holder.timeTextView.text = currentTask.third ?: "No time specified"
            holder.deleteButton.setOnClickListener {
                deleteTask(currentTask.first)
            }
            holder.editButton.setOnClickListener {
                showUpdateDialog(currentTask.first, currentTask.second, currentTask.third)
            }
        }

        override fun getItemCount() = tasksList.size

        fun setTasks(tasks: List<Triple<Long, String, String?>>) {
            tasksList.clear()
            tasksList.addAll(tasks)
            notifyDataSetChanged()
        }
    }

    private fun showUpdateDialog(taskId: Long, task: String, time: String?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_task, null)
        val editTextTask = dialogView.findViewById<EditText>(R.id.editTextTask)
        val editTextTime = dialogView.findViewById<TextInputEditText>(R.id.editTextTime)

        editTextTask.setText(task)
        editTextTime.setText(time)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)
            .setTitle("Update Task")
            .setPositiveButton("Update") { dialog, _ ->
                val updatedTask = editTextTask.text.toString()
                val updatedTime = editTextTime.text.toString()
                updateTask(taskId, updatedTask, updatedTime)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

}
