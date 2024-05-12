package com.example.myapplication

import DatabaseContract
import DatabaseHelper
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val editTextTask = findViewById<EditText>(R.id.editTextTask)
        val buttonAdd = findViewById<Button>(R.id.buttonAdd)
        val recyclerViewTasks = findViewById<RecyclerView>(R.id.recyclerViewTasks)

        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter()
        recyclerViewTasks.adapter = taskAdapter

        loadTasks()

        buttonAdd.setOnClickListener {
            val task = editTextTask.text.toString()
            if (task.isNotEmpty()) {
                addTask(task)
                editTextTask.text.clear()
            }
        }
    }

    private fun addTask(task: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.TaskEntry.COLUMN_NAME_TASK, task)
        }
        val newRowId = db?.insert(DatabaseContract.TaskEntry.TABLE_NAME, null, values)
        newRowId?.let {
            loadTasks()
        }
    }

    private fun deleteTask(task: String) {
        val db = dbHelper.writableDatabase
        db?.delete(
            DatabaseContract.TaskEntry.TABLE_NAME,
            "${DatabaseContract.TaskEntry.COLUMN_NAME_TASK} = ?",
            arrayOf(task)
        )
        loadTasks()
    }

    private fun updateTask(taskId: Long, newTask: String) {
        dbHelper.updateTask(taskId, newTask)
        loadTasks()
    }

    private fun loadTasks() {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(DatabaseContract.TaskEntry._ID, DatabaseContract.TaskEntry.COLUMN_NAME_TASK)
        val cursor = db.query(
            DatabaseContract.TaskEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val tasksList = ArrayList<Pair<Long, String>>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(DatabaseContract.TaskEntry._ID))
                val task = getString(getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_NAME_TASK))
                tasksList.add(id to task)
            }
        }
        taskAdapter.setTasks(tasksList)
    }

    inner class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

        private var tasksList = ArrayList<Pair<Long, String>>()

        inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val taskTextView: TextView = itemView.findViewById(R.id.textViewTask)
            val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_item, parent, false)
            return TaskViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val currentTask = tasksList[position]
            holder.taskTextView.text = currentTask.second
            holder.deleteButton.setOnClickListener {
                deleteTask(currentTask.second)
            }
            holder.taskTextView.setOnClickListener {
                showUpdateDialog(currentTask.first, currentTask.second)
            }
        }

        override fun getItemCount() = tasksList.size

        fun setTasks(tasks: List<Pair<Long, String>>) {
            tasksList.clear()
            tasksList.addAll(tasks)
            notifyDataSetChanged()
        }
    }

    private fun showUpdateDialog(taskId: Long, task: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.setText(task)

        dialogBuilder.setTitle("Update Task")
            .setView(editText)
            .setPositiveButton("Update") { dialog, _ ->
                val updatedTask = editText.text.toString()
                updateTask(taskId, updatedTask)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}
