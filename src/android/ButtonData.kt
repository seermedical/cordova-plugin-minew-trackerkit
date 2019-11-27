package com.minew

import androidx.room.*

@Entity(tableName="buttonData")
data class ButtonData(
    @PrimaryKey(autoGenerate=true) @JvmField var id: Int?,
    @ColumnInfo(name="user_id") @JvmField var userId: String,
    @ColumnInfo(name="start_time") @JvmField var start_time: Double,
    @ColumnInfo(name = "created_at") @JvmField var created_at: Long,
    @ColumnInfo(name="timezone") @JvmField var timezone: Float
){
    constructor():this(null,"",0.0,  0, 0.00f)
}