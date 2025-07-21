# 示例

本指南提供了全面的示例，展示插件如何转换您的代码以及不同配置的效果。

## 基本转换示例

### 简单函数转换

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
import love.forte.plugin.suspendtrans.annotation.*


class ApiService {
    @JvmBlocking
    @JvmAsync
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched successfully"
    }
}
```

```kotlin
import love.forte.plugin.suspendtrans.annotation.*
import kotlin.jvm.JvmSynthetic

class ApiService {

    @JvmSynthetic
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched successfully"
    }

    @Api4J
    fun fetchDataBlocking(): String =
        runInBlocking { fetchData() }

    @Api4J
    fun fetchDataAsync(): java.util.concurrent.CompletableFuture<out String> =
        runInAsync(block = { fetchData() }, scope = this as? CoroutineScope)
}
```

</compare>

## 接口和实现示例

### 带有挂起函数的接口

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
import love.forte.plugin.suspendtrans.annotation.*


@JvmBlocking
@JvmAsync
interface UserRepository {

    suspend fun findById(id: String): User?


    suspend fun save(user: User): User

    @JvmBlocking(asProperty = true)
    suspend fun count(): Long
}
```

```kotlin
import love.forte.plugin.suspendtrans.annotation.*
import kotlin.jvm.JvmSynthetic

@JvmBlocking
@JvmAsync
interface UserRepository {
    @JvmSynthetic
    suspend fun findById(id: String): User?

    @JvmSynthetic
    suspend fun save(user: User): User

    @JvmSynthetic
    suspend fun count(): Long

    @Api4J
    fun findByIdBlocking(id: String): User?

    @Api4J
    fun findByIdAsync(id: String): java.util.concurrent.CompletableFuture<User?>

    @Api4J
    fun saveBlocking(user: User): User

    @Api4J
    fun saveAsync(user: User): java.util.concurrent.CompletableFuture<out User>

    @Api4J
    val countBlocking: Long

    @Api4J
    fun countAsync(): java.util.concurrent.CompletableFuture<Long>
}
```

</compare>

### 实现类

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
@JvmBlocking
@JvmAsync
class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: String): User? {
        delay(100)
        return User(id, "John Doe")
    }


    override suspend fun save(user: User): User {
        delay(200)
        return user.copy(id = generateId())
    }

    @JvmBlocking(asProperty = true)
    override suspend fun count(): Long {
        delay(50)
        return 42L
    }


    suspend fun deleteById(id: String): Boolean {
        delay(150)
        return true
    }
}
```

```kotlin
@JvmBlocking
@JvmAsync
class UserRepositoryImpl : UserRepository {
    @JvmSynthetic
    override suspend fun findById(id: String): User? {
        delay(100)
        return User(id, "John Doe")
    }

    @JvmSynthetic
    override suspend fun save(user: User): User {
        delay(200)
        return user.copy(id = generateId())
    }

    @JvmSynthetic
    override suspend fun count(): Long {
        delay(50)
        return 42L
    }

    @JvmSynthetic
    suspend fun deleteById(id: String): Boolean {
        delay(150)
        return true
    }

    @Api4J
    override fun findByIdBlocking(id: String): User? = runInBlocking { findById(id) }

    @Api4J
    override fun findByIdAsync(id: String): java.util.concurrent.CompletableFuture<User?> =
        runInAsync(block = { findById(id) }, scope = this as? CoroutineScope)

    @Api4J
    override fun saveBlocking(user: User): User = runInBlocking { save(user) }

    @Api4J
    override fun saveAsync(user: User): java.util.concurrent.CompletableFuture<out User> =
        runInAsync(block = { save(user) }, scope = this as? CoroutineScope)

    @Api4J
    override val countBlocking: Long
        get() = runInBlocking { count() }

    @Api4J
    override fun countAsync(): java.util.concurrent.CompletableFuture<Long> =
        runInAsync(block = { count() }, scope = this as? CoroutineScope)

    @Api4J
    fun deleteByIdBlocking(id: String): Boolean = runInBlocking { deleteById(id) }

    @Api4J
    fun deleteByIdAsync(id: String): java.util.concurrent.CompletableFuture<Boolean> =
        runInAsync(block = { deleteById(id) }, scope = this as? CoroutineScope)
}
```

</compare>

## JavaScript 示例

### 基本 JS Promise 转换

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
import love.forte.plugin.suspendtrans.annotation.JsPromise

class ApiClient {
    @JsPromise
    suspend fun fetchUser(id: String): User {
        delay(500)
        return User(id, "Jane Doe")
    }

    @JsPromise
    suspend fun updateUser(user: User): User {
        delay(300)
        return user
    }
}
```

```kotlin
import love.forte.plugin.suspendtrans.annotation.*

class ApiClient {

    suspend fun fetchUser(id: String): User {
        delay(500)
        return User(id, "Jane Doe")
    }


    suspend fun updateUser(user: User): User {
        delay(300)
        return user
    }

    @Api4Js
    fun fetchUserAsync(id: String): Promise<User> =
        runInAsync(block = { fetchUser(id) }, scope = this as? CoroutineScope)

    @Api4Js
    fun updateUserAsync(user: User): Promise<User> =
        runInAsync(block = { updateUser(user) }, scope = this as? CoroutineScope)
}
```

</compare>

## 属性示例

### 属性生成

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
class ConfigService {
    @JvmBlocking(asProperty = true)
    suspend fun serverUrl(): String {
        delay(10)
        return "https://api.example.com"
    }

    @JvmBlocking(asProperty = true)
    @JvmAsync
    suspend fun timeout(): Int {
        delay(5)
        return 30000
    }
}
```

```kotlin
class ConfigService {
    @JvmSynthetic
    suspend fun serverUrl(): String {
        delay(10)
        return "https://api.example.com"
    }


    @JvmSynthetic
    suspend fun timeout(): Int {
        delay(5)
        return 30000
    }

    @Api4J
    val serverUrlBlocking: String
        get() = runInBlocking { serverUrl() }

    @Api4J
    val timeoutBlocking: Int
        get() = runInBlocking { timeout() }

    @Api4J
    fun timeoutAsync(): java.util.concurrent.CompletableFuture<Int> =
        runInAsync(block = { timeout() }, scope = this as? CoroutineScope)
}
```

</compare>

## MarkName 示例

### 自定义函数名称

<compare type="top-bottom" first-title="源代码" second-title="编译后 (JVM)">

```kotlin
class PaymentService {
    @JvmBlocking(markName = "processPaymentSync")
    suspend fun processPayment(amount: Double): PaymentResult {
        delay(2000)
        return PaymentResult.Success(amount)
    }
    
    @JsPromise(markName = "calculateTaxAsync")
    suspend fun calculateTax(amount: Double): Double {
        delay(100)
        return amount * 0.1
    }
}
```

```kotlin
class PaymentService {
    @JvmSynthetic
    suspend fun processPayment(amount: Double): PaymentResult {
        delay(2000)
        return PaymentResult.Success(amount)
    }

    
    suspend fun calculateTax(amount: Double): Double {
        delay(100)
        return amount * 0.1
    }

    @Api4J
    @JvmName("processPaymentSync")
    fun processPaymentBlocking(amount: Double): PaymentResult =
        runInBlocking { processPayment(amount) }
}
```
</compare>

<compare type="top-bottom" first-title="源代码" second-title="编译后 (JS)">

```kotlin
class PaymentService {
    @JvmBlocking(markName = "processPaymentSync")
    suspend fun processPayment(amount: Double): PaymentResult {
        delay(2000)
        return PaymentResult.Success(amount)
    }

    @JsPromise(markName = "calculateTaxAsync")
    suspend fun calculateTax(amount: Double): Double {
        delay(100)
        return amount * 0.1
    }
}
```

```kotlin
class PaymentService {
    
    suspend fun processPayment(amount: Double): PaymentResult {
        delay(2000)
        return PaymentResult.Success(amount)
    }
    
    
    suspend fun calculateTax(amount: Double): Double {
        delay(100)
        return amount * 0.1
    }

    @Api4Js
    @JsName("calculateTaxAsync")
    fun calculateTaxAsync(amount: Double): Promise<Double> =
        runInAsync(block = { calculateTax(amount) }, scope = this as? CoroutineScope)
}
```

</compare>


## 复杂类示例

### 功能完整的服务类

**源代码：**

```kotlin
import love.forte.plugin.suspendtrans.annotation.*

@JvmBlocking
@JvmAsync
class OrderService : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    suspend fun createOrder(order: Order): Order {
        validateOrder(order)
        delay(500) // 模拟数据库操作
        return order.copy(id = generateOrderId(), status = OrderStatus.CREATED)
    }

    suspend fun findOrder(id: String): Order? {
        delay(200)
        return mockDatabase.find(id)
    }

    @JvmBlocking(asProperty = true)
    suspend fun orderCount(): Long {
        delay(100)
        return mockDatabase.count()
    }

    suspend fun updateOrderStatus(id: String, status: OrderStatus): Order? {
        delay(300)
        return mockDatabase.updateStatus(id, status)
    }

    private suspend fun validateOrder(order: Order) {
        delay(50)
        if (order.items.isEmpty()) {
            throw IllegalArgumentException("Order must have at least one item")
        }
    }
}
```

**编译结果：**

> _从反编译结果简化而来。_

```kotlin
import love.forte.plugin.suspendtrans.annotation.*
import kotlin.jvm.JvmSynthetic

@JvmBlocking
@JvmAsync
class OrderService : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    @Api4J
    val orderCountBlocking: Long
        get() = runInBlocking { orderCount() }

    @JvmSynthetic
    suspend fun createOrder(order: Order): Order {
        validateOrder(order)
        delay(500)
        return order.copy(id = generateOrderId(), status = OrderStatus.CREATED)
    }

    @Api4J
    fun createOrderAsync(order: Order): java.util.concurrent.CompletableFuture<out Order> =
        runInAsync(block = { createOrder(order) }, scope = this)

    @Api4J
    fun createOrderBlocking(order: Order): Order = runInBlocking { createOrder(order) }

    @JvmSynthetic
    suspend fun findOrder(id: String): Order? {
        delay(200)
        return mockDatabase.find(id)
    }

    @Api4J
    fun findOrderAsync(id: String): java.util.concurrent.CompletableFuture<Order?> =
        runInAsync(block = { findOrder(id) }, scope = this)

    @Api4J
    fun findOrderBlocking(id: String): Order? = runInBlocking { findOrder(id) }

    @JvmSynthetic
    @JvmBlocking
    suspend fun orderCount(): Long {
        delay(100)
        return mockDatabase.count()
    }

    @Api4J
    fun orderCountAsync(): java.util.concurrent.CompletableFuture<Long> =
        runInAsync(block = { orderCount() }, scope = this)

    @JvmSynthetic
    suspend fun updateOrderStatus(id: String, status: OrderStatus): Order? {
        delay(300)
        return mockDatabase.updateStatus(id, status)
    }

    @Api4J
    fun updateOrderStatusAsync(id: String, status: OrderStatus): java.util.concurrent.CompletableFuture<Order?> =
        runInAsync(block = { updateOrderStatus(id, status) }, scope = this)

    @Api4J
    fun updateOrderStatusBlocking(id: String, status: OrderStatus): Order? =
        runInBlocking { updateOrderStatus(id, status) }

    private suspend fun validateOrder(order: Order) {
        delay(50)
        if (order.items.isEmpty()) {
            throw IllegalArgumentException("Order must have at least one item")
        }
    }
}
```

## 从 Java 使用

### 从 Java 调用生成的函数

```java
public class JavaClient {
    private final OrderService orderService = new OrderService();

    public void demonstrateUsage() {
        // 使用阻塞变体
        Order order = new Order(/* ... */);
        Order createdOrder = orderService.createOrderBlocking(order);

        Long count = orderService.getOrderCountBlocking();

        // 使用异步变体
        CompletableFuture<Order> futureOrder = orderService.createOrderAsync(order);
        futureOrder.thenAccept(result -> {
            System.out.println("Order created: " + result.getId());
        });

        CompletableFuture<Order> findFuture = orderService.findOrderAsync("order-123");
        Order foundOrder = findFuture.join(); // 阻塞等待
    }
}
```

## 从 JavaScript 使用

### 从 JavaScript 调用生成的函数

```javascript
// 假设已导入 Kotlin/JS 模块
const apiClient = new ApiClient();

// 使用基于 Promise 的变体
apiClient.fetchUserAsync("user-123")
    .then(user => {
        console.log("User fetched:", user.name);
        return apiClient.updateUserAsync(user);
    })
    .then(updatedUser => {
        console.log("User updated:", updatedUser.name);
    })
    .catch(error => {
        console.error("Error:", error);
    });

// 使用 async/await
async function handleUser() {
    try {
        const user = await apiClient.fetchUserAsync("user-456");
        const updatedUser = await apiClient.updateUserAsync(user);
        console.log("Process completed:", updatedUser);
    } catch (error) {
        console.error("Error:", error);
    }
}
```