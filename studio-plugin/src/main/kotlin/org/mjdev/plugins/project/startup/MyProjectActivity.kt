package org.mjdev.plugins.project.startup

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {
    val log
        get() = thisLogger()
    override suspend fun execute(
        project: Project
    ) {
        log.warn(
            "Don't forget to remove all non-needed sample code files with their corresponding " +
                    "registration entries in `plugin.xml`."
        )
    }
}