/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.actions

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.r.RFileType
import org.jetbrains.r.console.RConsoleManager
import org.jetbrains.r.console.RConsoleToolWindowFactory
import org.jetbrains.r.notifications.RNotificationUtil
import org.jetbrains.r.packages.build.RPackageBuildUtil

abstract class RRunMultipleTestsActionBase : DumbAwareAction() {

    abstract val isDebug: Boolean

    var runAll: Boolean

    init {
        templatePresentation.text = "Run All R Tests"
        templatePresentation.description = "Run all R tests"
        runAll = true
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        println(e.virtualFile?.name)
        runAll = isAllTestsDir(e.virtualFile)
        e.presentation.isEnabledAndVisible = e.project != null && (isPsiTestFile(e.psiFile) || runAll)
    }

    private fun isAllTestsDir(file: VirtualFile?): Boolean {
        return file != null
               && file.isDirectory
               && (file.name == "tests"
                   || file.name == "testthat" && file.parent?.name == "tests")
    }

    private fun isPsiTestFile(file: PsiFile?): Boolean {
        return file != null
               && isVirtualTestFile(file.virtualFile, file.project)
    }

    private fun isVirtualTestFile(file: VirtualFile, project: Project): Boolean {
        return file.fileType == RFileType
               && RPackageBuildUtil.isPackage(project)
               && file.parent?.name == "testthat"
               && file.parent?.parent?.name == "tests"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        var file: VirtualFile? = null
        val code =  if (runAll) {
            "devtools::test()"
        } else {
            val fileName = e.virtualFile?.name
            file = e.virtualFile
            "devtools::test_file(\"tests/testthat/$fileName\")"
        }

        RConsoleManager.getInstance(project).currentConsoleAsync.onSuccess { console ->
            ConsoleHistoryController.addToHistory(console, code)
            console.executeActionHandler.splitAndExecute(code, isDebug = isDebug, sourceFile = file)
        }.onError { ex -> RNotificationUtil.notifyConsoleError(project, ex.message) }
        RConsoleToolWindowFactory.focusOnCurrentConsole(project)
    }
}

class RRunMultipleTestsAction : RRunMultipleTestsActionBase() {

    override val isDebug = false

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.icon = AllIcons.Actions.Execute
        e.presentation.text = if (runAll) {
            "Run All R Tests"
        } else {
            "Run tests in \"${e.psiFile?.name}\""
        }
    }
}

class RDebugMultipleTestsAction : RRunMultipleTestsActionBase() {

    override val isDebug = true

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.icon = AllIcons.Actions.StartDebugger
        e.presentation.text = if (runAll) {
            "Debug All R Tests"
        } else {
            "Debug tests in \"${e.psiFile?.name}\""
        }
    }
}