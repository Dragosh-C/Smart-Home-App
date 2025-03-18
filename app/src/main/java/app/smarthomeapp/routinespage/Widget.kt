package app.smarthomeapp.routinespage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class Widget1(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String
)
