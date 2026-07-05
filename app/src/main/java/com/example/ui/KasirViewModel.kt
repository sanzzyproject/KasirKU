package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.KasirRepository
import com.example.data.Product
import com.example.data.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val total: Double get() = product.price * quantity
}

class KasirViewModel(private val repository: KasirRepository) : ViewModel() {

    val products = repository.allProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val transactions = repository.allTransactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val todaySales = repository.getTodaySales().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow<String?>(null)
    val checkoutSuccess: StateFlow<String?> = _checkoutSuccess.asStateFlow()

    fun addToCart(product: Product) {
        _cart.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + CartItem(product, 1)
            }
        }
        recalculateTotal()
    }

    fun removeFromCart(product: Product) {
        _cart.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                if (existing.quantity > 1) {
                    current.map {
                        if (it.product.id == product.id) it.copy(quantity = it.quantity - 1) else it
                    }
                } else {
                    current.filter { it.product.id != product.id }
                }
            } else current
        }
        recalculateTotal()
    }
    
    fun clearCart() {
        _cart.value = emptyList()
        recalculateTotal()
    }

    private fun recalculateTotal() {
        _cartTotal.value = _cart.value.sumOf { it.total }
    }

    fun addDummyProductsIfNeeded() {
        viewModelScope.launch {
            if (products.value.isEmpty()) {
                repository.insertProduct(Product(name = "Nasi Goreng Spesial", price = 25000.0, stock = 100, category = "Makanan"))
                repository.insertProduct(Product(name = "Mie Goreng", price = 20000.0, stock = 100, category = "Makanan"))
                repository.insertProduct(Product(name = "Es Teh Manis", price = 5000.0, stock = 50, category = "Minuman"))
                repository.insertProduct(Product(name = "Kopi Hitam", price = 10000.0, stock = 50, category = "Minuman"))
                repository.insertProduct(Product(name = "Ayam Penyet", price = 30000.0, stock = 20, category = "Makanan"))
            }
        }
    }

    fun checkout(paymentMethod: String) {
        if (_cart.value.isEmpty()) return
        val receiptId = "INV-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(4).uppercase()}"
        
        viewModelScope.launch {
            val items = _cart.value.map {
                TransactionItem(
                    transactionId = 0, // Assigned in DAO
                    productId = it.product.id,
                    productName = it.product.name,
                    quantity = it.quantity,
                    price = it.product.price
                )
            }
            repository.checkout(_cartTotal.value, paymentMethod, receiptId, items)
            _checkoutSuccess.value = receiptId
            clearCart()
        }
    }

    fun clearCheckoutSuccess() {
        _checkoutSuccess.value = null
    }
}

class KasirViewModelFactory(private val repository: KasirRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KasirViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KasirViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
