package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class KasirRepository(private val dao: KasirDao) {
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    fun getTodaySales(): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getTotalSalesSince(calendar.timeInMillis)
    }

    suspend fun insertProduct(product: Product) {
        dao.insertProduct(product)
    }

    suspend fun checkout(totalAmount: Double, paymentMethod: String, receiptId: String, cartItems: List<TransactionItem>) {
        val transaction = Transaction(
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            receiptId = receiptId
        )
        dao.createTransactionWithItems(transaction, cartItems)
    }

    suspend fun getTransactionItems(transactionId: Int): List<TransactionItem> {
        return dao.getTransactionItems(transactionId)
    }
}
