plugins {
    `maven-publish`
}

publishing {
    repositories {
        mavenLocal()
    }
    
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            
            // Add source JAR
            val sourcesJar = tasks.register("sourcesJar", Jar::class) {
                archiveClassifier.set("sources")
                from(project.the<SourceSetContainer>().getByName("main").allSource)
            }
            
            // Add javadoc JAR
            val javadocJar = tasks.register("javadocJar", Jar::class) {
                archiveClassifier.set("javadoc")
            }
            
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}
