/*
 *  Copyright (c) 2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simply-robot (或称 simple-robot 3.x 、simbot 3.x ) 的一部分。
 *
 *  simply-robot 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simply-robot 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 *
 */

import org.gradle.api.Project
import java.net.URI

@Suppress("ClassName")
sealed class Sonatype {
    abstract val name: String
    abstract val url: String
    fun Project.uri(): URI = uri(url)
    
    object Central : Sonatype() {
        const val NAME = "central"
        const val URL = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        override val name: String get() = NAME
        override val url: String get() = URL
    }
    
    object Snapshot : Sonatype() {
        const val NAME = "snapshot"
        const val URL = "https://oss.sonatype.org/content/repositories/snapshots/"
        override val name: String get() = NAME
        override val url get() = URL
    }
}