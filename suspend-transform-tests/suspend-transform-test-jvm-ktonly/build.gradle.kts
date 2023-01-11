import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.FunctionInfo
import love.forte.plugin.suspendtrans.MarkAnnotation

buildscript {
     this@buildscript.repositories {
         mavenLocal()
         mavenCentral()
     }
 }
 
plugins {
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform")
}


//withType<JavaCompile> {
//    sourceCompatibility = "11"
//    targetCompatibility = "11"
//}
//withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "11"
//}


suspendTransform {
    clear()
    
    
    val markAnnotationClassInfo = ClassInfo("suspendtrans.test", "AB")
    
    
    val jvmSyntheticClassInfo = ClassInfo("kotlin.jvm", "JvmSynthetic")
    val jvmSyntheticFunctionIncludeAnnotation = love.forte.plugin.suspendtrans.IncludeAnnotation(
        ClassInfo(
            "love.forte.plugin.suspendtrans.annotation",
            "Api4J"
        )
    )
    val abBlockingTransformer = love.forte.plugin.suspendtrans.Transformer(
        markAnnotation = MarkAnnotation(
            markAnnotationClassInfo,
            baseNameProperty = "blockingBaseName",
            suffixProperty = "blockingSuffix",
            asPropertyProperty = "blockingAsProperty",
            defaultSuffix = "Blocking"
        ),
        transformFunctionInfo = FunctionInfo("love.forte.plugin.suspendtrans.runtime", null, "\$runInBlocking\$"),
        transformReturnType = null,
        transformReturnTypeGeneric = false,
        originFunctionIncludeAnnotations = listOf(love.forte.plugin.suspendtrans.IncludeAnnotation(jvmSyntheticClassInfo)),
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(jvmSyntheticClassInfo),
        syntheticFunctionIncludeAnnotations = listOf(jvmSyntheticFunctionIncludeAnnotation)
    )
    val abAsyncTransformer = love.forte.plugin.suspendtrans.Transformer(
        markAnnotation = MarkAnnotation(
            markAnnotationClassInfo,
            baseNameProperty = "asyncBaseName",
            suffixProperty = "asyncSuffix",
            asPropertyProperty = "asyncAsProperty",
            defaultSuffix = "Async"
        ),
        transformFunctionInfo = FunctionInfo("love.forte.plugin.suspendtrans.runtime", null, "\$runInAsync\$"),
        transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"),
        transformReturnTypeGeneric = true,
        originFunctionIncludeAnnotations = listOf(love.forte.plugin.suspendtrans.IncludeAnnotation(jvmSyntheticClassInfo)),
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(jvmSyntheticClassInfo),
        syntheticFunctionIncludeAnnotations = listOf(jvmSyntheticFunctionIncludeAnnotation)
    )
    
    jvmTransformers(abBlockingTransformer, abAsyncTransformer)
}

kotlin {
    dependencies {
        implementation(kotlin("stdlib"))
        implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:0.2.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    }
}
