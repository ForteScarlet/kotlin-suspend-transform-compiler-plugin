// FIR_DUMP
// ISSUE: Symbol not found for T in generic annotation

import com.example.annotation.GenericAnnotation

class TestClass {
    @GenericAnnotation<Boolean>(name = "foo1", enabled = true)
    fun foo1(): Boolean = true
    @GenericAnnotation<T>(name = "foo2", enabled = true)
    fun <T> foo2(): Boolean = true
}
