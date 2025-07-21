# WasmJS Limitations

<primary-label ref="experimental" xmlns=""/>
<secondary-label ref="version-0.6.0" />

WasmJS support is experimental and has several limitations:

1. **Custom Types Required**: You need to provide your own types and functions
2. **Runtime Not Included**: The plugin doesn't provide runtime support for WasmJS
3. **Type Restrictions**: WasmJS has restrictions on various types that may affect your implementation

Example of custom WasmJS setup:

```kotlin
// You need to provide these yourself
fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
    }
}
```

