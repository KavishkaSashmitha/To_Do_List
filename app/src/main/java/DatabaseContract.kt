import android.provider.BaseColumns

object DatabaseContract {
    // Inner class that defines the table contents
    class TaskEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "tasks"
            const val _ID = "_id"
            const val COLUMN_NAME_TASK = "task"
            const val COLUMN_NAME_TIME = "time" // New column for storing time
        }
    }
}
