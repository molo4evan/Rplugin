/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.testing.single

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import org.jetbrains.r.actions.editor
import org.jetbrains.r.console.RConsoleManager
import org.jetbrains.r.console.RConsoleToolWindowFactory
import org.jetbrains.r.notifications.RNotificationUtil
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.rinterop.ExecuteCodeRequest
import javax.swing.Icon

abstract class RRunSingleTestActionBase(
    private val call: RCallExpression,
    text: String,
    description: String,
    icon: Icon
) : DumbAwareAction(text, description, icon) {

    abstract val isDebug: Boolean

    override fun actionPerformed(e: AnActionEvent) {
        val text = call.text
        val project = e.project ?: return
        val editor = e.editor ?: return
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        RConsoleManager.getInstance(project).currentConsoleAsync.onSuccess { console ->
            ConsoleHistoryController.addToHistory(console, text)
            console.executeActionHandler.splitAndExecute(text, isDebug = isDebug, sourceFile = virtualFile,
                                                         sourceStartOffset = call.textRange.startOffset,
                                                         firstDebugCommand = ExecuteCodeRequest.DebugCommand.CONTINUE)
        }.onError { ex -> RNotificationUtil.notifyConsoleError(project, ex.message) }
        RConsoleToolWindowFactory.focusOnCurrentConsole(project)
    }
}

class RRunSingleTestAction(call: RCallExpression) : RRunSingleTestActionBase(
    call,
    "Run test",
    "Run selected test",
    AllIcons.Actions.Execute
) {

    override val isDebug = false
}

class RDebugSingleTestAction(call: RCallExpression) : RRunSingleTestActionBase(
    call,
    "Debug test",
    "Debug selected test",
    AllIcons.Actions.StartDebugger
) {

    override val isDebug = true
}