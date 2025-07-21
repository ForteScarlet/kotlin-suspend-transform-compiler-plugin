import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class UpdateWritersideVersionTask : DefaultTask() {
    /**
     * Update `<var name="version" value="xxx" />`'s value with current version
     * in docs_en/v.list` and `docs_zh/v.list`.
     */
    @TaskAction
    fun action() {
        val currentVersion = IProject.version
        
        // Update version in both Writerside v.list files
        val vListFiles = listOf(
            project.file("docs_en/v.list"),
            project.file("docs_zh/v.list")
        )
        
        vListFiles.forEach { file ->
            if (file.exists()) {
                val content = file.readText()
                // Use regex to replace only the version variable's value attribute
                val updatedContent = content.replaceFirst(
                    Regex("""(<var\s+name="version"\s+value=")[^"]*("\s*/>)"""),
                    "$1$currentVersion$2"
                )
                file.writeText(updatedContent)
                project.logger.info("Updated version to {} in {}", currentVersion, file.path)
            } else {
                project.logger.warn("Warning: {} does not exist", file.path)
            }
        }
    }
}