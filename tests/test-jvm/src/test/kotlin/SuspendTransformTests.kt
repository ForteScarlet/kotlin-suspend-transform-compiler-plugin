/*
 *     Copyright (c) 2024. ForteScarlet.
 *
 *     Project    https://github.com/simple-robot/simpler-robot
 *     Email      ForteScarlet@163.com
 *
 *     This file is part of the Simple Robot Library.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     Lesser GNU General Public License for more details.
 *
 *     You should have received a copy of the Lesser GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
import kotlinx.coroutines.cancel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.Api4J
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.plugin.suspendtrans.sample.AliasTestClass
import love.forte.plugin.suspendtrans.sample.JvmReactiveScopedSamples
import love.forte.plugin.suspendtrans.sample.JvmReactiveSamples
import love.forte.plugin.suspendtrans.sample.NullmarkModeSamples
import org.reactivestreams.Publisher
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod
import kotlin.test.*


@JvmBlocking
@JvmAsync
interface STTrans1 {
    suspend fun run1()
    suspend fun run1(value: String): String
}

@JvmBlocking(suffix = "", asProperty = true)
@JvmAsync(suffix = "Ay", asProperty = true)
interface STPTrans1 {
    suspend fun run1(): Int
    suspend fun run2(): String
}

open class Foo
open class Bar : Foo()

@JvmBlocking(asProperty = true)
@JvmAsync(asProperty = true)
interface ITypedTrans1<T : Foo> {
    suspend fun value(): T
}

@JvmBlocking(asProperty = true)
@JvmAsync(asProperty = true)
interface TypedTrans1Impl<T : Bar> : ITypedTrans1<T> {
    override suspend fun value(): T
}

class STPTrans1Impl : STPTrans1 {
    override suspend fun run1(): Int = 1
    override suspend fun run2(): String = "run2"
}

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTests {

    @Test
    fun javaMethodResolve() {
        val run1Blocking = STTrans1::class.functions.find { it.name == "run1Blocking" }
        assertNotNull(run1Blocking)
        println(run1Blocking)
        println(run1Blocking.javaMethod)
    }

    @Test
    fun `interface suspend trans function test`() {
        with(STTrans1::class.java) {
            val blockingMethod = getMethod("run1Blocking")
            val asyncMethod = getMethod("run1Async")

            assertEquals(Void.TYPE, blockingMethod.returnType)
            assertEquals(CompletableFuture::class.java, asyncMethod.returnType)

            assertFalse(Modifier.isAbstract(blockingMethod.modifiers))
            assertFalse(Modifier.isAbstract(asyncMethod.modifiers))
        }

        with(STTrans1::class.java) {
            val blockingMethod = getMethod("run1Blocking", String::class.java)
            val asyncMethod = getMethod("run1Async", String::class.java)

            assertEquals(String::class.java, blockingMethod.returnType)
            assertEquals(CompletableFuture::class.java, asyncMethod.returnType)

            assertFalse(Modifier.isAbstract(blockingMethod.modifiers))
            assertFalse(Modifier.isAbstract(asyncMethod.modifiers))
        }
    }

    @Test
    fun `interface suspend trans property test`() {
        with(STPTrans1::class.memberProperties) {
            assertTrue(any { it.name == "run1" && it.returnType.classifier == Int::class })
            assertTrue(any { it.name == "run2" && it.returnType.classifier == String::class })
        }

        // run1
        with(STPTrans1::class.java) {
            val blockingPropertyMethod = getMethod("getRun1")
            val asyncPropertyMethod = getMethod("getRun1Ay")

            assertEquals(Int::class.javaPrimitiveType, blockingPropertyMethod.returnType)
            assertEquals(CompletableFuture::class.java, asyncPropertyMethod.returnType)

            assertFalse(Modifier.isAbstract(blockingPropertyMethod.modifiers))
            assertFalse(Modifier.isAbstract(asyncPropertyMethod.modifiers))
        }

        // run2
        with(STPTrans1::class.java) {
            val blockingPropertyMethod = getMethod("getRun2")
            val asyncPropertyMethod = getMethod("getRun2Ay")

            assertEquals(String::class.java, blockingPropertyMethod.returnType)
            assertEquals(CompletableFuture::class.java, asyncPropertyMethod.returnType)

            assertFalse(Modifier.isAbstract(blockingPropertyMethod.modifiers))
            assertFalse(Modifier.isAbstract(asyncPropertyMethod.modifiers))
        }

    }

    @Test
    fun `typed interface test`() {
        with(ITypedTrans1::class) {
            assertTrue(memberProperties.any {
                it.name == "valueBlocking" && with(it.returnType.classifier) {
                    this is KTypeParameter && this.upperBounds.any { b -> b.classifier == Foo::class }
                }
            })
            assertTrue(memberProperties.any {
                it.name == "valueAsync" && it.returnType.classifier == CompletableFuture::class
            })
        }
        with(TypedTrans1Impl::class) {
            assertTrue(memberProperties.any {
                it.name == "valueBlocking" && with(it.returnType.classifier) {
                    this is KTypeParameter && this.upperBounds.any { b -> b.classifier == Bar::class }
                }
            })
            assertTrue(memberProperties.any {
                it.name == "valueAsync" && it.returnType.classifier == CompletableFuture::class
            })
        }
    }

    @Test
    fun `invoke test`() {
        val stp = STPTrans1Impl()

        val run1BlockingMethod = STPTrans1::class.java.getMethod("getRun1")
        val run2BlockingMethod = STPTrans1::class.java.getMethod("getRun2")

        val run1AsyncMethod = STPTrans1::class.java.getMethod("getRun1Ay")
        val run2AsyncMethod = STPTrans1::class.java.getMethod("getRun2Ay")

        assertEquals(1, run1BlockingMethod.invoke(stp))
        assertEquals("run2", run2BlockingMethod.invoke(stp))

        val run1Async = run1AsyncMethod.invoke(stp)
        val run2Async = run2AsyncMethod.invoke(stp)

        assertIs<CompletableFuture<Int>>(run1Async)
        assertIs<CompletableFuture<String>>(run2Async)

        assertEquals(1, run1Async.get())
        assertEquals("run2", run2Async.get())
    }

    @Test
    fun aliasTest() {
        val alias = AliasTestClass()
        val longType = Long::class.javaPrimitiveType!!
        val bigDecimalType = BigDecimal::class.java

        val errorReproduction1BlockingMethod =
            AliasTestClass::class.java.getMethod("errorReproduction1Blocking", longType)
        val errorReproduction2BlockingMethod =
            AliasTestClass::class.java.getMethod("errorReproduction2Blocking", bigDecimalType)
        val errorReproduction1AsyncMethod =
            AliasTestClass::class.java.getMethod("errorReproduction1Async", longType)
        val errorReproduction2AsyncMethod =
            AliasTestClass::class.java.getMethod("errorReproduction2Async", bigDecimalType)

        assertEquals(longType, errorReproduction1BlockingMethod.returnType)
        assertEquals(bigDecimalType, errorReproduction2BlockingMethod.returnType)
        assertEquals(CompletableFuture::class.java, errorReproduction1AsyncMethod.returnType)
        assertEquals(CompletableFuture::class.java, errorReproduction2AsyncMethod.returnType)

        assertEquals(1L, errorReproduction1BlockingMethod.invoke(alias, 1L))
        assertEquals(BigDecimal("2.5"), errorReproduction2BlockingMethod.invoke(alias, BigDecimal("2.5")))

        val errorReproduction1Async = errorReproduction1AsyncMethod.invoke(alias, 3L)
        val errorReproduction2Async = errorReproduction2AsyncMethod.invoke(alias, BigDecimal("4.5"))

        assertIs<CompletableFuture<*>>(errorReproduction1Async)
        assertIs<CompletableFuture<*>>(errorReproduction2Async)

        assertEquals(3L, errorReproduction1Async.join())
        assertEquals(BigDecimal("4.5"), errorReproduction2Async.join())
    }

    @Test
    fun `transform return type generic nullmark mode test`() {
        val functions = NullmarkModeSamples::class.functions.associateBy { it.name }

        fun futureArgumentType(functionName: String): KType {
            val returnType = requireNotNull(functions[functionName]) {
                "Function $functionName was not generated"
            }.returnType

            assertEquals(CompletableFuture::class, returnType.classifier)
            return requireNotNull(returnType.arguments.single().type) {
                "Function $functionName has no CompletableFuture generic argument"
            }
        }

        with(futureArgumentType("stringValueNullableAsync")) {
            assertEquals("kotlin.String?", toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("genericValueNullableAsync")) {
            assertTrue(toString().endsWith("?"), toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("whereGenericValueNullableAsync")) {
            assertTrue(toString().endsWith("?"), toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("nullableStringValueNonNullAsync")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(futureArgumentType("nullableBoundGenericValueNonNullAsync")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(futureArgumentType("nullableWhereGenericValueNonNullAsync")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }
    }

    @OptIn(Api4J::class)
    @Test
    fun `jvm reactive transform test`() {
        val functions = JvmReactiveSamples::class.functions.associateBy { it.name }

        fun publisherArgumentType(functionName: String): KType {
            val returnType = requireNotNull(functions[functionName]) {
                "Function $functionName was not generated"
            }.returnType

            assertEquals(Publisher::class, returnType.classifier)
            return requireNotNull(returnType.arguments.single().type) {
                "Function $functionName has no Publisher generic argument"
            }
        }

        with(publisherArgumentType("stringValueReactive")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("nullableStringValueReactive")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("nullableBoundGenericValueReactive")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("whereGenericValueReactive")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(JvmReactiveSamples::class.memberProperties.single { it.name == "nullablePropertyValueReactive" }) {
            assertEquals(Publisher::class, returnType.classifier)
            val argumentType = requireNotNull(returnType.arguments.single().type)
            assertEquals("kotlin.String", argumentType.toString())
            assertFalse(argumentType.isMarkedNullable)
        }

        val samples = JvmReactiveSamples()
        assertEquals("value", runBlocking { samples.stringValueReactive().awaitFirstOrNull() })
        assertNull(runBlocking { samples.nullableStringValueReactive().awaitFirstOrNull() })

        val scopedSamples = JvmReactiveScopedSamples()
        try {
            assertEquals("scoped", runBlocking { scopedSamples.scopedValueReactive().awaitFirstOrNull() })
        } finally {
            scopedSamples.cancel()
        }
    }
}
