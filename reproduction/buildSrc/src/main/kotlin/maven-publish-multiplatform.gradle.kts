plugins {
    `maven-publish`
}

publishing {
    repositories {
        mavenLocal()
    }
    
    // Publications are automatically created by the Kotlin multiplatform plugin
    // We don't need to create them manually
}
