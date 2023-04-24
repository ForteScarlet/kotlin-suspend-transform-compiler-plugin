/*
 * Copyright (c) 2023. ForteScarlet.
 *
 * This file is part of simbot-component-tencent-guild.
 *
 * simbot-component-tencent-guild is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * simbot-component-tencent-guild is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with simbot-component-tencent-guild. If not, see <https://www.gnu.org/licenses/>.
 */

import love.forte.gradle.common.core.repository.Repositories
import love.forte.gradle.common.core.repository.SimpleCredentials
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("R.sonatype.userinfo")


private val sonatypeUserInfo0 by lazy {
    val userInfo = sonatypeUserInfoOrNull

//    val userInfo = love.forte.gradle.common.publication.sonatypeUserInfoOrNull
    
    if (userInfo == null) {
        logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
    }
    
    userInfo
}

val sonatypeUsername: String? get() = sonatypeUserInfo0?.sonatypeUsername
val sonatypePassword: String? get() = sonatypeUserInfo0?.sonatypePassword

val ReleaseRepository by lazy {
    Repositories.Central.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
}
val SnapshotRepository by lazy {
    Repositories.Snapshot.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
}
