package com.example.a7minuteworkout

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(historyEntity: HistoryEntity)

    @Delete
    suspend fun delete(historyEntity: HistoryEntity)

    @Query("Select * from `workout-history`")
    fun fetchHistory():Flow<List<HistoryEntity>>

    @Query("Select * from `workout-history` where id =:id")
    fun fetchHistoryById(id: Int): Flow<HistoryEntity>

}