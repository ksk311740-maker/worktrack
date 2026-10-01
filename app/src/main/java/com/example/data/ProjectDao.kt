package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ProjectItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY date DESC, id DESC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<ProjectItem?>

    @Query("SELECT * FROM projects WHERE date LIKE :yearMonthPrefix || '%' ORDER BY date DESC, id DESC")
    fun getProjectsByMonth(yearMonthPrefix: String): Flow<List<ProjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectItem>): List<Long>

    @Update
    suspend fun update(project: ProjectItem)

    @Delete
    suspend fun delete(project: ProjectItem)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int

    @Query("UPDATE projects SET category = :newName WHERE category = :oldName")
    suspend fun updateCategoryName(oldName: String, newName: String)

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}
