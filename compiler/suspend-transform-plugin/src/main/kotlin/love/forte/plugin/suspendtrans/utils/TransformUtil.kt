package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.configuration.ClassInfo
import love.forte.plugin.suspendtrans.configuration.FunctionInfo
import love.forte.plugin.suspendtrans.fqn
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name


fun ClassInfo.toClassId(): ClassId =
    ClassId(packageName.fqn, className.fqn, local)

fun FunctionInfo.toCallableId(): CallableId =
    CallableId(packageName.fqn, null, Name.identifier(functionName))
