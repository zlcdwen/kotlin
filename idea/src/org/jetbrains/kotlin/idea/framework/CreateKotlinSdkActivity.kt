/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.framework

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.kotlin.idea.configuration.getBuildSystemType
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.jetbrains.kotlin.platform.impl.CommonIdePlatformKind
import org.jetbrains.kotlin.platform.impl.JsIdePlatformKind
import org.jetbrains.kotlin.statistics.KotlinJPSTargetTrigger
import org.jetbrains.kotlin.statistics.KotlinStatisticsTrigger

/**
 * This StartupActivity creates KotlinSdk for projects containing JS or Common modules.
 * This activity is work-around required until the issue IDEA-203655 is fixed. The major case is to create
 * Kotlin SDK when the KotlinSourceRootType is created
 */
class CreateKotlinSdkActivity : StartupActivity {

    override fun runActivity(project: Project) {
        val kotlinSdkIsRequired = project.allModules().any {
            it.platform is JsIdePlatformKind.Platform || it.platform is CommonIdePlatformKind.Platform
        }
        project.allModules().forEach {
            val buildSystem = it.getBuildSystemType().toString()
            if (listOf("jps", "Jps", "JPS").any { buildSystem.contains(it) }) {
                KotlinStatisticsTrigger.trigger(
                        KotlinJPSTargetTrigger::class.java,
                        it.platform.toString()
                )
            }
        }
        project.allModules()[1].moduleTypeName
        if (kotlinSdkIsRequired) {
            KotlinSdkType.setUpIfNeeded()
        }
    }
}