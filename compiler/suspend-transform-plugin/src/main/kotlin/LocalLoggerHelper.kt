// import java.time.LocalDateTime
// import kotlin.io.path.*
//
// internal object LocalLoggerHelper {
//     private val dir = System.getenv("kstcp.logger.dir")
//     private val userDir = System.getProperty("user.dir")
//     private val path = Path(
//         dir ?: userDir?.plus("/.kstcp-plugin-logger") ?: "~/.kstcp-plugin-logger"
//     ) / Path("${System.currentTimeMillis()}.log")
//
//     init {
//         path.parent.createDirectories()
//         if (path.notExists()) {
//             path.createFile()
//         }
//     }
//
//     fun println(value: Any?) {
//         kotlin.io.println(value)
//         path.appendText("[${LocalDateTime.now()}] $value\n", Charsets.UTF_8)
//     }
// }
