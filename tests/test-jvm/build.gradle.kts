plugins {
    `java-library`
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform")
    // id("suspend-transform.jvm-maven-publish")
    // id(project(":suspend-transform-plugin-gradle"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}

repositories {
    mavenLocal()
}

// apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("test-junit5"))
    api(kotlin("reflect"))
    api(project(":runtime:suspend-transform-annotation"))
    api(project(":runtime:suspend-transform-runtime"))
    api(libs.kotlinx.coroutines.core)
}

suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        

        // For blocking
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JTrans"
                }
                baseNameProperty = "blockingBaseName"
                suffixProperty = "blockingSuffix"
                asPropertyProperty = "blockingAsProperty"
                defaultSuffix = "Blocking"
                defaultAsProperty = false
            }

            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }

            // other config...
        }

        // For async
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JTrans"
                }
                baseNameProperty = "asyncBaseName"
                suffixProperty = "asyncSuffix"
                asPropertyProperty = "asyncAsProperty"
                defaultSuffix = "Async"
                defaultAsProperty = false
            }

            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inAsync"
            }
        }
    }
}

/*
>     val blockingBaseName: String = "",
>     val blockingSuffix: String = "Blocking",
>     val blockingAsProperty: Boolean = false,
>
>     val asyncBaseName: String = "",
>     val asyncSuffix: String = "Async",
>     val asyncAsProperty: Boolean = false
 */

tasks.withType<Test> {
    useJUnitPlatform()
}
