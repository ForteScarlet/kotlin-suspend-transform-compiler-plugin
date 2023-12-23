import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import kotlin.js.Promise
import kotlinx.coroutines.await

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Promise // keep import
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        var asyncValue by remember { mutableStateOf<JsAny?>(null) }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(ForteScarlet().toString())
            Text(ForteScarlet()::stringToInt.toString())
            Text(asyncValue.toString())


            LaunchedEffect(null) {
                asyncValue = ForteScarlet().stringToIntAsync("20").toPromise().await()
            }

        }
    }

//    println(ForteScarlet().stringToInt("1"))
    //println(ForteScarlet().stringToIntAsync("1"))
}
