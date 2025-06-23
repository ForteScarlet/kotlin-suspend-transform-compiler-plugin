import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

plugins {
    kotlin("multiplatform")
    id("love.forte.plugin.suspend-transform")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

repositories {
    mavenLocal()
}

kotlin {

    jvmToolchain(11)
    jvm()
    js {
        nodejs()

        useEsModules()
        generateTypeScriptDefinitions()
        binaries.executable()

        compilerOptions {
            target = "es2015"
            useEsClasses = true
            freeCompilerArgs.addAll(
                // https://kotlinlang.org/docs/whatsnew20.html#per-file-compilation-for-kotlin-js-projects
                "-Xir-per-file",
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("reflect"))
            implementation(project(":runtime:suspend-transform-annotation"))
            implementation(project(":runtime:suspend-transform-runtime"))
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "11"
    targetCompatibility = "11"

    // see https://kotlinlang.org/docs/gradle-configure-project.html#configure-with-java-modules-jpms-enabled
    // if (moduleName != null) {
    //     options.compilerArgumentProviders.add(
    //         CommandLineArgumentProvider {
    //             // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
    //             // listOf("--patch-module", "$moduleName=${sourceSets["main"].output.asPath}")
    //             val sourceSet = sourceSets.findByName("main") ?: sourceSets.findByName("jvmMain")
    //             if (sourceSet != null) {
    //                 listOf("--patch-module", "$moduleName=${sourceSet.output.asPath}")
    //             } else {
    //                 emptyList()
    //             }
    //             // listOf("--patch-module", "$moduleName=${sourceSets["main"].output.asPath}")
    //         }
    //     )
    // }
}

suspendTransformPlugin {
    includeRuntime = false
    includeAnnotation = false
}

@Suppress("DEPRECATION")
suspendTransformPlugin {
    includeRuntime = false
    includeAnnotation = false
    transformers {
        useJvmDefault()
        addJsPromise {
            addCopyAnnotationExclude {
                from(SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo)
            }
        }
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}
