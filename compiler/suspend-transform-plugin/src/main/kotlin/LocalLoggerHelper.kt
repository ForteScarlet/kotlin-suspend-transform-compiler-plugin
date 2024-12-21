//internal object LocalLoggerHelper {
//    private val dir = System.getenv("kstcp.logger.dir")
//    private val path = Path(dir ?: ".plugin-logger") / Path("${System.currentTimeMillis()}.log")
//    init {
//        path.parent.createDirectories()
//        if (path.notExists()) {
//            path.createFile()
//        }
//    }
//
//    fun println(value: Any?) {
//        kotlin.io.println(value)
//        path.appendText("[${LocalDateTime.now()}] $value\n", Charsets.UTF_8)
//    }
//}
