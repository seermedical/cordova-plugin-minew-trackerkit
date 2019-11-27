package com.minew

import androidx.room.*

@Dao
interface ButtonDataDao {

    @Query("SELECT * FROM buttonData WHERE user_id == :userId ORDER BY created_at ASC LIMIT 100")
    fun fetch(userId: String): List<ButtonData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(buttonData: ButtonData)

    @Query("DELETE FROM buttonData WHERE id IN (SELECT id FROM buttonData WHERE user_id == :userId ORDER BY created_at ASC LIMIT 100)")
    fun delete(userId: String)
}