package love.forte.plugin.suspendtrans.sample

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking



/**
 *
 * @author ForteScarlet
 */
interface FooInterface {
    suspend fun run1(value: Int): String
    suspend fun run2(): String
}

class FooImpl : FooInterface {
    @JvmBlocking
    @JvmAsync
    override suspend fun run1(value: Int): String = value.toString()

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun run2(): String = ""
}

/**
 *
 * @author ForteScarlet
 */
interface FooInterface2 {
    @JvmBlocking
    suspend fun run1(value: Int): String

    @JvmBlocking(asProperty = true)
    suspend fun run2(): String
}


class FooImpl2 : FooInterface2 {
    @JvmBlocking
    @JvmAsync
    override suspend fun run1(value: Int): String = value.toString()

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun run2(): String = ""
}


/**
 *
 * @author ForteScarlet
 */
interface FooInterface3 {
    suspend fun run1(value: Int): String

    fun run1Blocking(value: Int): String = value.toString()

    suspend fun run2(): String

    val run2: String get() = ""
}


class FooImpl3 : FooInterface3 {
    @JvmBlocking
    @JvmAsync
    override suspend fun run1(value: Int): String = value.toString()

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun run2(): String = ""
}
