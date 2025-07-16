plugins {
    `java`
    kotlin("jvm")
    id("suspend-transform.maven-publish")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    implementation(kotlin("compiler"))
    compileOnly(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(kotlin("compiler"))
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("compiler-internal-test-framework"))
    testImplementation(kotlin("reflect"))
    testRuntimeOnly(kotlin("test"))
    testRuntimeOnly(kotlin("script-runtime"))
    testRuntimeOnly(kotlin("annotations-jvm"))
    testImplementation(project(":reproduce:annotation"))
    testImplementation(libs.kotlinx.coroutines.core)
}

kotlin {
    jvmToolchain(8)
}

sourceSets {
    test {
        kotlin.srcDir("src/test")
        kotlin.srcDir("src/test-gen")
        java.srcDir("src/test-gen")
    }
}

task<JavaExec>("generateTest") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.example.plugin.test.GenerateTestsKt")
}

tasks.test {
    useJUnitPlatform()
    doFirst {
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
    }
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = project.configurations
        .testRuntimeClasspath.get()
        .files
        .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}
