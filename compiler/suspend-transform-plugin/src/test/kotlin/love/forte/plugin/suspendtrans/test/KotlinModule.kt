package love.forte.plugin.suspendtrans.test

import com.bennyhuo.kotlin.compiletesting.extensions.source.Entry
import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.bennyhuo.kotlin.compiletesting.extensions.utils.captureStdOut
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.*
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import javax.annotation.processing.AbstractProcessor

class KotlinModule(
    val name: String,
    args: Map<String, String>,
    val sourceFiles: List<SourceFile>,
    val dependencyNames: List<String>,
    val entries: List<Entry>,
    componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
    kaptProcessors: Collection<AbstractProcessor> = emptyList(),
    kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList()
) {
    constructor(
        sourceModuleInfo: SourceModuleInfo,
        componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
        kaptProcessors: Collection<AbstractProcessor> = emptyList(),
        kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList()
    ) : this(
        sourceModuleInfo.name,
        sourceModuleInfo.args,
        sourceModuleInfo.sourceFileInfos.map { sourceFileInfo ->
            SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
        },
        sourceModuleInfo.dependencies,
        sourceModuleInfo.entries,
        componentRegistrars, kaptProcessors, kspProcessorProviders
    )

    private val classpath = ArrayList<File>()

    val compilation = newCompilation()

    private val kspCompilation = if (kspProcessorProviders.isNotEmpty()) {
        newCompilation {
            symbolProcessorProviders = kspProcessorProviders.toList()
            kspArgs.putAll(args)
        }
    } else {
        null
    }

    private val classesDir: File = compilation.classesDir

    val dependencies = ArrayList<KotlinModule>()

    var isCompiled = false
        private set

    var compileResult: KotlinCompilation.Result? = null

    val generatedSourceDirs: List<File> = listOfNotNull(
        compilation.kaptSourceDir,
        compilation.kaptKotlinGeneratedDir,
        kspCompilation?.kspSourcesDir
    )

    init {
        compilation.compilerPlugins += componentRegistrars

        compilation.annotationProcessors += kaptProcessors
        compilation.kaptArgs.putAll(args)
    }

    fun resolveDependencies(kotlinModuleMap: Map<String, KotlinModule>) {
        dependencyNames.mapNotNull {
            kotlinModuleMap[it]
        }.forEach {
            dependsOn(it)
        }
    }

    fun compile() {
        if (isCompiled) return
        ensureDependencies()
        isCompiled = true

        if (kspCompilation != null) {
            val compileResult = kspCompilation.compile()
            if (compileResult.exitCode != KotlinCompilation.ExitCode.OK) {
                this.compileResult = compileResult
                return
            }

            compilation.sources += kspCompilation.kspSourcesDir.walkTopDown()
                .filter { !it.isDirectory }
                .map {
                    SourceFile.new(it.name, it.readText())
                }
        }

        compileResult = compilation.compile()
    }

    fun runJvm(): Map<String, String> {
        if (!isCompiled) {
            compile()
        }

        if (entries.isEmpty()) {
            return emptyMap()
        }

        val classLoader = URLClassLoader(
            (classpath + classesDir).map { it.toURI().toURL() }.toTypedArray(),
            this.javaClass.classLoader
        )

        return entries.associate {
            it.fileName to captureStdOut {
                val entryClass = classLoader.loadClass(it.className)
                val entryFunction = entryClass.getDeclaredMethod(it.functionName)
                if (!Modifier.isStatic(entryFunction.modifiers)) {
                    throw IllegalArgumentException("entry function $entryFunction must be static.")
                }
                entryFunction.invoke(null)
            }
        }
    }

    private fun ensureDependencies() {
        dependencies.forEach {
            it.compile()
            classpath += it.classesDir
            classpath += it.classpath
        }
    }

    private fun dependsOn(module: KotlinModule) {
        dependencies += module
    }

    private fun newCompilation(block: KotlinCompilation.() -> Unit = {}) = KotlinCompilation().also { compilation ->
        compilation.verbose = false
        compilation.inheritClassPath = true
        compilation.classpaths = classpath
        compilation.sources = sourceFiles
        compilation.moduleName = name

        compilation.block()
    }

    override fun toString() =
        "$name: $isCompiled >> ${compileResult?.exitCode} ${compileResult?.messages}"
}

fun Collection<KotlinModule>.resolveAllDependencies() {
    val moduleMap = this.associateBy { it.name }
    forEach {
        it.resolveDependencies(moduleMap)
    }
}

fun Collection<KotlinModule>.compileAll() {
    forEach {
        it.compile()
    }
}