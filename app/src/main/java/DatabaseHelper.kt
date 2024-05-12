import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${DatabaseContract.TaskEntry.TABLE_NAME} (" +
                    "${DatabaseContract.TaskEntry._ID} INTEGER PRIMARY KEY," +
                    "${DatabaseContract.TaskEntry.COLUMN_NAME_TASK} TEXT)"

        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.TaskEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun updateTask(taskId: Long, newTask: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.TaskEntry.COLUMN_NAME_TASK, newTask)
        }
        db.update(
            DatabaseContract.TaskEntry.TABLE_NAME,
            values,
            "${DatabaseContract.TaskEntry._ID} = ?",
            arrayOf(taskId.toString())
        )
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Tasks.db"
    }
}
