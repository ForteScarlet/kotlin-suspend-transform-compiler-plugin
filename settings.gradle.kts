rootProject.name = "kotlin-suspend-transform-compiler-plugin"

include(":compiler:suspend-transform-plugin")
include(":compiler:suspend-transform-plugin-embeddable")

include(":runtime:suspend-transform-annotation")
include(":runtime:suspend-transform-runtime")

include(":plugins:suspend-transform-plugin-gradle")

include(":samples:sample-jvm")
include(":samples:sample-js")
include(":samples:sample-wasmJs")
// include(":plugins:ide:suspend-transform-plugin-idea")
