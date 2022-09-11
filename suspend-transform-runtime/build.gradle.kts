plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                kotlinOptions {
                    jvmTarget = "1.8"
                    javaParameters = true
                }
            }
        }
    }
    js(IR) {
        browser()
        nodejs()
    }
    
    configAllNativeTargets()
    
    val nativeTargetSourceNames = targets.flatMapTo(mutableSetOf()) { target ->
        if (target.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native) {
            val name = target.name
            listOf("${name}Main", "${name}Test")
        } else {
            emptyList()
        }
    }
    
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        
        getByName("jvmMain") {
            dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
            }
        }
        
        val nativeCommonMain = create("nativeCommonMain") {
            dependsOn(commonMain)
        }
        val nativeCommonTest = create("nativeCommonTest") {
            dependsOn(commonTest)
        }
    
        matching { it.name in nativeTargetSourceNames }.all {
            when {
                name.endsWith("Main") -> {
                    dependsOn(nativeCommonMain)
                }
                name.endsWith("Test") -> {
                    dependsOn(nativeCommonTest)
                }
            }
        }
        
    }
    
}