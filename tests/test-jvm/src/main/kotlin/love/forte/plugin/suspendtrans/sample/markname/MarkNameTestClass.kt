package love.forte.plugin.suspendtrans.sample.markname

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@Suppress("RedundantSuspendModifier")
class MarkNameTestClass {
    @JvmBlocking(markName = "fooB")
    @JvmAsync(markName = "fooA")
    @JvmName("_foo")
    suspend fun foo(): Int = 1

    @JvmName("aName")
    fun named(): String = ""
}

@Suppress("RedundantSuspendModifier")
@JvmBlocking(markName = "fooB")
@JvmAsync(markName = "fooA")
class MarkNameOnTypeTestClass {
    @JvmName("_foo")
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
@JvmBlocking(markName = "fooB")
@JvmAsync(markName = "fooA")
class MarkNameSubOverwriteTestClass {
    @JvmBlocking(markName = "fooB1")
    @JvmAsync(markName = "fooA1")
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
class MarkNameTestClassAsProperty {
    @JvmBlocking(asProperty = true, markName = "fooB")
    @JvmAsync(asProperty = true, markName = "fooA")
    @JvmName("_foo")
    suspend fun foo(): Int = 1

    @JvmName("aName")
    fun named(): String = ""
}

@Suppress("RedundantSuspendModifier")
@JvmBlocking(asProperty = true, markName = "fooB")
@JvmAsync(asProperty = true, markName = "fooA")
class MarkNameOnTypeTestClassAsProperty {
    @JvmName("_foo")
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
@JvmBlocking(markName = "fooB")
@JvmAsync(markName = "fooA")
class MarkNameSubOverwriteTestClassAsProperty {
    @JvmBlocking(asProperty = true, markName = "fooB1")
    @JvmAsync(asProperty = true, markName = "fooA1")
    suspend fun foo(): Int = 1
}
