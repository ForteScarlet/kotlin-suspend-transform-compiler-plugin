/**
 * 为了让 gradle 插件可以在 buildSrc 之类的地方使用。
 */
fun org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension.configGradleBuildSrcFriendly() {
    coreLibrariesVersion = "1.9.0"
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
    }
}
