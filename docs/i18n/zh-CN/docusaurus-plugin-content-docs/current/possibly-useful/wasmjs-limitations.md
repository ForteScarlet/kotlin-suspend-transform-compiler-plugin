---
id: wasmjs-limitations
title: WasmJS 限制
---

import { Badge } from '@site/src/components/Snippets';

<Badge type="primary">实验性</Badge>
<Badge type="secondary">版本 0.6.0</Badge>

WasmJS 支持是实验性的，有几个限制：

1. **需要自定义类型**：您需要提供自己的类型和函数
2. **不包含运行时**：插件不为 WasmJS 提供运行时支持
3. **类型限制**：WasmJS 对各种类型有限制，可能会影响您的实现

自定义 WasmJS 设置示例：

```kotlin
// 您需要自己提供这些
fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
    }
}
```
