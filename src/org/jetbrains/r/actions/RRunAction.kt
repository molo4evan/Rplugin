/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.actions

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AppUIUtil
import icons.org.jetbrains.r.RBundle
import icons.org.jetbrains.r.notifications.RNotificationUtil
import org.jetbrains.r.console.RConsoleManager
import org.jetbrains.r.console.RConsoleToolWindowFactory
import org.jetbrains.r.console.RConsoleView
import org.jetbrains.r.debugger.exception.RDebuggerException

abstract class RRunActionBase : REditorActionBase() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.virtualFile ?: return
    AppUIUtil.invokeOnEdt {
      FileDocumentManager.getInstance().saveAllDocuments()
      RConsoleToolWindowFactory.getRConsoleToolWindows(project)?.show {}
    }
    RConsoleManager.getInstance(project).currentConsoleAsync.onSuccess { console ->
      if (REditorActionUtil.isRunningCommand(console)) {
        RNotificationUtil.notifyConsoleError(project, RBundle.message("notification.console.busy"))
      }
      doExecute(console, file)
    }.onError {
      RNotificationUtil.notifyConsoleError(project, it.message)
    }
  }

  abstract fun doExecute(console: RConsoleView, file: VirtualFile)

  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.isEnabled = e.presentation.isEnabled && !REditorActionUtil.isRunningCommand(e.project)
  }
}

class RRunAction : RRunActionBase() {
  override fun doExecute(console: RConsoleView, file: VirtualFile) {
    console.rInterop.sourceFile(file)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    e.virtualFile?.let { e.presentation.text = RBundle.message("run.file.action.text", it.name) }
  }
}

class RDebugAction : RRunActionBase() {
  override fun doExecute(console: RConsoleView, file: VirtualFile) {
    try {
      console.debugger.executeDebugSource(file)
    } catch (e: RDebuggerException) {
      if (e.message != null) console.print(e.message + "\n", ConsoleViewContentType.ERROR_OUTPUT)
    }
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    e.virtualFile?.let { e.presentation.text = RBundle.message("debug.file.action.text", it.name) }
  }
}