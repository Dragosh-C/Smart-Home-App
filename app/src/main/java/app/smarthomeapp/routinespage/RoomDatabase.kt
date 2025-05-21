package app.smarthomeapp.routinespage

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Dao
interface WidgetDao {
    @Insert
    suspend fun insertWidget(widget: Widget1)

    @Update
    suspend fun updateWidget(widget: Widget1)

    @Delete
    suspend fun deleteWidget(widget: Widget1)
    @Query("SELECT * FROM widgets WHERE id = :id")
    fun getWidgetById(id: Int): Widget1?

    @Query("SELECT * FROM widgets")
    fun getAllWidgets(): LiveData<List<Widget1>>

    @Query("DELETE FROM widgets")
    suspend fun deleteAllWidgets()
}

@Database(entities = [Widget1::class], version = 1)
abstract class WidgetDatabase : RoomDatabase() {
    abstract fun widgetDao(): WidgetDao

    companion object {
        @Volatile private var INSTANCE: WidgetDatabase? = null

        fun getDatabase(context: Context): WidgetDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WidgetDatabase::class.java,
                        "widget_database"
                    ).build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e("WidgetDatabase", "Failed to initialize database", e)
                    throw e
                }
            }
        }
    }
}
