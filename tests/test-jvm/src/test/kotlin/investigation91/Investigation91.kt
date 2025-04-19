package investigation91

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface OneBotListener {
    suspend operator fun invoke() {}
}

data class BotInstance(
    val address: String,
    val accessToken: String,
    val listener: OneBotListener,
    val autoReconnect: Boolean,
    val i: Int,
    val client: InstanceType,
    val string: String,
    val reconnectInterval: Duration,
    val messageExecuteDuration: Duration,
    val logLevel: LogLevel
)

enum class InstanceType {
    Client
}

enum class LogLevel {
    INFO
}

class SomeClass {
    companion object {
        @JvmBlocking(suffix = "JvmBlocking")
        suspend fun createClient(
            address: String,
            accessToken: String,
            listener: OneBotListener = object : OneBotListener {},
            reconnectInterval: Duration = 3.seconds,
            autoReconnect: Boolean = true,
            messageExecuteDuration: Duration = 0.seconds,
            logLevel: LogLevel = LogLevel.INFO
        ): BotInstance {
            delay(1)
            return BotInstance(
                address, accessToken, listener,
                autoReconnect, 0, InstanceType.Client,
                "/", reconnectInterval, messageExecuteDuration,
                logLevel
            )
        }
    }
}

fun run(duration: Duration) {}

class Investigation91 {
    @Test
    fun verifyGeneration() {
        val blockingFun = SomeClass.Companion::class.functions.find { it.name == "createClientJvmBlocking" }
        assertNotNull(blockingFun) { "blocking fun `createClientJvmBlocking` is null" }

        val blockingFunJavaMethod = blockingFun.javaMethod
        assertNotNull(blockingFunJavaMethod) { "blocking fun `createClientJvmBlocking` to java method is null" }

        // 生成的名称并不是 createClientJvmBlocking, 会有一个额外后缀
        assertNotEquals("createClientJvmBlocking", blockingFunJavaMethod.name)
        assertEquals("createClientJvmBlocking-tuv2wNU", blockingFunJavaMethod.name)

        /*
        address: String,
            accessToken: String,
            listener: OneBotListener = object : OneBotListener {},
            reconnectInterval: Duration = 3.seconds,
            autoReconnect: Boolean = true,
            messageExecuteDuration: Duration = 0.seconds,
            logLevel: LogLevel = LogLevel.INFO
         */

        val listenerInstance = object : OneBotListener {}

        val invoked = blockingFunJavaMethod.invoke(
            SomeClass.Companion,
            "address",
            "accessToken",
            listenerInstance,
            0L,
            true,
            0L,
            LogLevel.INFO
        )

        assertIs<BotInstance>(invoked)
        assertEquals("address", invoked.address)
        assertSame(listenerInstance, invoked.listener)
        assertEquals(0.seconds, invoked.reconnectInterval)
        assertEquals(true, invoked.autoReconnect)
        assertEquals(0.seconds, invoked.messageExecuteDuration)
        assertEquals(LogLevel.INFO, invoked.logLevel)

        // invoke original function
        val originalInvoke = runBlocking {
            SomeClass.createClient(
                "address",
                "accessToken",
                listenerInstance,
                0.seconds,
                true,
                0.seconds,
                LogLevel.INFO
            )
        }

        assertEquals(invoked, originalInvoke)
    }
}