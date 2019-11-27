package com.minew

import androidx.room.*
import android.content.Context

@Database(entities = arrayOf(ButtonData::class), version = 1)
abstract class SeerDatabase : RoomDatabase() {

    abstract fun buttonDataDao(): ButtonDataDao

    companion object {
        private var INSTANCE: SeerDatabase? = null

        @JvmStatic fun getInstance(context: Context): SeerDatabase? {
            if (INSTANCE == null) {
                synchronized(SeerDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        SeerDatabase::class.java, "seer.db")
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}