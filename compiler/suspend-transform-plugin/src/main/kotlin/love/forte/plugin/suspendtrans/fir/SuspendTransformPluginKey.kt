package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.SuspendTransformUserDataFir
import org.jetbrains.kotlin.GeneratedDeclarationKey

data class SuspendTransformPluginKey(val data: SuspendTransformUserDataFir) : GeneratedDeclarationKey() {
    override fun toString(): String {
        return "SuspendTransformPlugin(data=$data)"
    }
}
