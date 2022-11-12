import org.gradle.api.InvalidUserCodeException
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainerWithPresets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset

private val coroutinesUnsupportedTargets = setOf(
    "androidNativeArm32",
    "androidNativeArm64",
    "androidNativeX64",
    "androidNativeX86",
    "linuxArm32Hfp",
    "linuxArm64",
    "linuxMips32",
    "linuxMipsel32",
    "mingwX86",
    "wasm32",
)

fun KotlinMultiplatformExtension.configAllNativeTargets(
    filter: KotlinNativeTargetPreset.() -> Boolean = { true },
    configureEach: KotlinNativeTarget.() -> Unit = { }
) {
    presets.all {
        if (this !is KotlinNativeTargetPreset || !filter(this)) return@all
        configureOrCreate(name, this, configureEach)
    }
}

fun KotlinMultiplatformExtension.configAllNativeTargetsCoroutinesSupported(configureEach: KotlinNativeTarget.() -> Unit = { }) {
    configAllNativeTargets(filter = { name !in coroutinesUnsupportedTargets }, configureEach = configureEach)
}

internal fun KotlinTarget.isProducedFromPreset(kotlinTargetPreset: KotlinTargetPreset<*>): Boolean =
    preset == kotlinTargetPreset

internal fun <T : KotlinTarget> KotlinTargetsContainerWithPresets.configureOrCreate(
    targetName: String,
    targetPreset: KotlinTargetPreset<T>,
    configure: T.() -> Unit
): T {
    val existingTarget = targets.findByName(targetName)
    when {
        existingTarget?.isProducedFromPreset(targetPreset) ?: false -> {
            @Suppress("UNCHECKED_CAST")
            configure(existingTarget as T)
            return existingTarget
        }

        existingTarget == null -> {
            val newTarget = targetPreset.createTarget(targetName)
            targets.add(newTarget)
            configure(newTarget)
            return newTarget
        }

        else -> {
            throw InvalidUserCodeException(
                "The target '$targetName' already exists, but it was not created with the '${targetPreset.name}' preset. " +
                        "To configure it, access it by name in `kotlin.targets`" +
                        (" or use the preset function '${existingTarget.preset?.name}'."
                            .takeIf { existingTarget.preset != null } ?: ".")
            )
        }
    }
}
