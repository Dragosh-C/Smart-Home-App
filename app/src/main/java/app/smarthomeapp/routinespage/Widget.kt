package app.smarthomeapp.routinespage

import androidx.room.Entity
import androidx.room.PrimaryKey
var lastIdvalue = 0
@Entity(tableName = "widgets")
data class Widget1(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val title: String,
    val type: String,
    val ifCondition: String,
    val thenAction: String,
    var repeatEveryDay: Boolean = false,
    var isEnabled: Boolean = true,
    var deviceID: String? = null,
)
