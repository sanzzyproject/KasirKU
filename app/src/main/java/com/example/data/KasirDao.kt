package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KasirDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: Int): List<TransactionItem>

    @RoomTransaction
    suspend fun createTransactionWithItems(transaction: Transaction, items: List<TransactionItem>) {
        val transactionId = insertTransaction(transaction).toInt()
        val itemsWithTransactionId = items.map { it.copy(transactionId = transactionId) }
        insertTransactionItems(itemsWithTransactionId)
        
        // Update product stock
        for (item in items) {
            updateProductStock(item.productId, item.quantity)
        }
    }

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, quantity: Int)

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalSalesSince(startTime: Long): Flow<Double?>
}
