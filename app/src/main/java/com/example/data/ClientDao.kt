package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ClientItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<ClientItem>)

    @Update
    suspend fun update(client: ClientItem)

    @Delete
    suspend fun delete(client: ClientItem)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clients")
    suspend fun deleteAll()
}
