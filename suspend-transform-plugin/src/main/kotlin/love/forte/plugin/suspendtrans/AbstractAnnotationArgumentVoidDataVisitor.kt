package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.annotations.AnnotationArgumentVisitor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.*


/**
 *
 * @author ForteScarlet
 */
abstract class AbstractAnnotationArgumentVoidDataVisitor<R> : AnnotationArgumentVisitor<R, Nothing?> {
    abstract fun visitLongValue(value: Long): R
    abstract fun visitIntValue(value: Int): R
    abstract fun visitErrorValue(): R
    abstract fun visitShortValue(value: Short): R
    abstract fun visitByteValue(value: Byte): R
    abstract fun visitDoubleValue(value: Double): R
    abstract fun visitFloatValue(value: Float): R
    abstract fun visitBooleanValue(value: Boolean): R
    abstract fun visitCharValue(value: Char): R
    abstract fun visitStringValue(value: String): R
    abstract fun visitNullValue(): R
    abstract fun visitEnumValue(enumClassId: ClassId, enumEntryName: Name): R
    abstract fun visitArrayValue(value: List<ConstantValue<*>>): R
    abstract fun visitAnnotationValue(value: AnnotationDescriptor): R
    abstract fun visitKClassValue(value: KClassValue.Value): R
    abstract fun visitUByteValue(value: Byte): R
    abstract fun visitUShortValue(value: Short): R
    abstract fun visitUIntValue(value: Int): R
    abstract fun visitULongValue(value: Long): R
    
    ///
    
    final override fun visitLongValue(value: LongValue, data: Nothing?): R = visitLongValue(value.value)
    final override fun visitIntValue(value: IntValue, data: Nothing?): R = visitIntValue(value.value)
    final override fun visitErrorValue(value: ErrorValue, data: Nothing?): R = visitErrorValue()
    final override fun visitShortValue(value: ShortValue, data: Nothing?): R = visitShortValue(value.value)
    final override fun visitByteValue(value: ByteValue, data: Nothing?): R = visitByteValue(value.value)
    final override fun visitDoubleValue(value: DoubleValue, data: Nothing?): R = visitDoubleValue(value.value)
    final override fun visitFloatValue(value: FloatValue, data: Nothing?): R = visitFloatValue(value.value)
    final override fun visitBooleanValue(value: BooleanValue, data: Nothing?): R = visitBooleanValue(value.value)
    final override fun visitCharValue(value: CharValue, data: Nothing?): R = visitCharValue(value.value)
    final override fun visitStringValue(value: StringValue, data: Nothing?): R = visitStringValue(value.value)
    final override fun visitNullValue(value: NullValue, data: Nothing?): R = visitNullValue()
    final override fun visitEnumValue(value: EnumValue, data: Nothing?): R =
        visitEnumValue(value.enumClassId, value.enumEntryName)
    
    final override fun visitArrayValue(value: ArrayValue, data: Nothing?): R = visitArrayValue(value.value)
    final override fun visitAnnotationValue(value: AnnotationValue, data: Nothing?): R = visitAnnotationValue(value.value)
    final override fun visitKClassValue(value: KClassValue, data: Nothing?): R = visitKClassValue(value.value)
    final override fun visitUByteValue(value: UByteValue, data: Nothing?): R = visitUByteValue(value.value)
    final override fun visitUShortValue(value: UShortValue, data: Nothing?): R = visitUShortValue(value.value)
    final override fun visitUIntValue(value: UIntValue, data: Nothing?): R = visitUIntValue(value.value)
    final override fun visitULongValue(value: ULongValue, data: Nothing?): R = visitULongValue(value.value)
    
}

abstract class AbstractNullableAnnotationArgumentVoidDataVisitor<R> : AbstractAnnotationArgumentVoidDataVisitor<R?>() {
    override fun visitLongValue(value: Long): R? = null
    override fun visitIntValue(value: Int): R? = null
    override fun visitErrorValue(): R? = null
    override fun visitShortValue(value: Short): R? = null
    override fun visitByteValue(value: Byte): R? = null
    override fun visitDoubleValue(value: Double): R? = null
    override fun visitFloatValue(value: Float): R? = null
    override fun visitBooleanValue(value: Boolean): R? = null
    override fun visitCharValue(value: Char): R? = null
    override fun visitStringValue(value: String): R? = null
    override fun visitNullValue(): R? = null
    override fun visitEnumValue(enumClassId: ClassId, enumEntryName: Name): R? = null
    override fun visitArrayValue(value: List<ConstantValue<*>>): R? = null
    override fun visitAnnotationValue(value: AnnotationDescriptor): R? = null
    override fun visitKClassValue(value: KClassValue.Value): R? = null
    override fun visitUByteValue(value: Byte): R? = null
    override fun visitUShortValue(value: Short): R? = null
    override fun visitUIntValue(value: Int): R? = null
    override fun visitULongValue(value: Long): R? = null
    companion object {
        val stringOnly = object : AbstractNullableAnnotationArgumentVoidDataVisitor<String>() {
            override fun visitStringValue(value: String): String = value
        }
        val booleanOnly = object : AbstractNullableAnnotationArgumentVoidDataVisitor<Boolean>() {
            override fun visitBooleanValue(value: Boolean): Boolean = value
        }
    }
}

