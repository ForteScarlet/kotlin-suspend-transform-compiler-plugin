import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class UpdateDocsVersionTask : DefaultTask() {
    /**
     * Update version in docs/src/version.json file with current project version
     */
    @TaskAction
    fun action() {
        val currentVersion = project.version.toString()
        
        // Update version in docs JSON file
        val versionJsonFile = project.file("docs/src/version.json")
        
        // Ensure parent directory exists
        versionJsonFile.parentFile.mkdirs()
        
        // Directly write the JSON content with current version
        val jsonContent = """{"version": "$currentVersion"}"""
        versionJsonFile.writeText(jsonContent)
        
        project.logger.info("Updated version to {} in {}", currentVersion, versionJsonFile.path)
    }
}