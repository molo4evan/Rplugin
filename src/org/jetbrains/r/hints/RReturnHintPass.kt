/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.hints

import com.intellij.codeHighlighting.EditorBoundHighlightingPass
import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar
import com.intellij.codeInsight.hints.InlayHintsSettings
import com.intellij.codeInsight.hints.InlayParameterHintsExtension
import com.intellij.diff.util.DiffUtil
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.r.RLanguage
import org.jetbrains.r.psi.RRecursiveElementVisitor
import org.jetbrains.r.psi.api.RFunctionExpression
import org.jetbrains.r.refactoring.RRefactoringUtil
import org.jetbrains.r.rmarkdown.RMarkdownLanguage
import java.util.*

@Suppress("UnstableApiUsage")
class RReturnHintPass(private val file: PsiFile,
                      editor: Editor,
                      private val needForceRepaint: Boolean,
                      private val settings: RReturnHintInlayProvider.Settings)
  : EditorBoundHighlightingPass(editor, file, true) {

  private val functions = mutableListOf<PsiElement>()

  override fun doCollectInformation(progress: ProgressIndicator) {
    if (myDocument == null) return

    val rCodeHintsModel = RReturnHintsModel.getInstance(myProject)
    val provider = InlayParameterHintsExtension.forLanguage(RLanguage.INSTANCE)
    if (provider == null || DiffUtil.isDiffEditor(myEditor)) {
      rCodeHintsModel.clearDocumentInfo(myDocument)
      return
    }

    val actualHints = HashMap<PsiElement, RReturnHint>()
    file.viewProvider.getPsi(RLanguage.INSTANCE).accept(object : RRecursiveElementVisitor() {
      override fun visitFunctionExpression(o: RFunctionExpression) {
        functions.add(o)
        var returns = RRefactoringUtil.collectReturns(myProject, o).toList()
        if (!settings.showImplicitReturn) {
          returns = returns.filter { it !is RRefactoringUtil.ImplicitNullResult }
        }

        if (returns.size < settings.differentReturnExpressions) return

        returns.forEach {
          actualHints[it.returnStatement] = if (it is RRefactoringUtil.CorrectReturnResult) {
            RExplicitReturnHint(o)
          } else {
            RImplicitReturnHint(o)
          }
        }

        o.acceptChildren(this)
      }
    })

    rCodeHintsModel.update(myDocument, actualHints)
  }

  // Information will be painted with org.jetbrains.r.hints.RReturnHintModel$RReturnHintLineExtensionPainter
  override fun doApplyInformationToEditor() {
    if (needForceRepaint) {
      val editorEx = myEditor as? EditorEx ?: return
      for (function in functions) {
        val functionTextRange = function.textRange
        editorEx.repaint(functionTextRange.startOffset, functionTextRange.endOffset)
      }
    }
  }

  companion object {
    class Factory(registrar: TextEditorHighlightingPassRegistrar) : ProjectComponent, TextEditorHighlightingPassFactory {
      private val documentsToForceRepaint = mutableSetOf<Document>()

      init {
        registrar.registerTextEditorHighlightingPass(this, null, null, false, -1)
      }

      fun filterAndUpdateDocumentsToForceRepaint(documents: List<Document>, project: Project): List<Document> {
        val fileDocumentManager = FileDocumentManager.getInstance()
        return documents.filter { PsiUtilCore.findFileSystemItem(project, fileDocumentManager.getFile(it))?.language == RMarkdownLanguage }.also {
          documentsToForceRepaint.addAll(it)
        }
      }

      override fun createHighlightingPass(file: PsiFile, editor: Editor): TextEditorHighlightingPass? {
        // For RFile information collect by org.jetbrains.r.hints.RReturnHintInlayProvider
        if (file.language != RMarkdownLanguage) return null

        val document = editor.document
        val settings = InlayHintsSettings.instance().findSettings(RReturnHintInlayProvider.settingsKey, RLanguage.INSTANCE) {
          RReturnHintInlayProvider.Settings()
        }

        return RReturnHintPass(file, editor, document in documentsToForceRepaint, settings).also {
          documentsToForceRepaint.remove(document)
        }
      }

      companion object {
        fun getInstance(project: Project): Factory {
          return project.getComponent(Factory::class.java)
        }
      }
    }

    private val SPACE_LINE_EXTENSION_INFO = LineExtensionInfo(" ", TextAttributes())
  }
}