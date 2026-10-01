package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.InvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC")
    fun getAllInvoices(): Flow<List<InvoiceItem>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): InvoiceItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invoices: List<InvoiceItem>)

    @Update
    suspend fun update(invoice: InvoiceItem)

    @Delete
    suspend fun delete(invoice: InvoiceItem)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE invoices SET paymentStatus = :newStatus WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, newStatus: String)

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()
}
