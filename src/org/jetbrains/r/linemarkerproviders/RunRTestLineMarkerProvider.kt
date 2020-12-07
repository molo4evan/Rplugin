/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.linemarkerproviders

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.psi.api.RIdentifierExpression
import org.jetbrains.r.testing.single.RDebugSingleTestAction
import org.jetbrains.r.testing.single.RRunSingleTestAction
import java.util.function.Supplier

class RunRTestLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val parent = element.parent
        if (element is RIdentifierExpression &&
            parent is RCallExpression &&
            parent.expression == element &&
            element.name == "test_that") {
            val testName = parent.argumentList.expressionList[0].name ?: return null
            return RunRTestLinMarkerInfo(element, testName, parent)
        }

        return null
    }

    class RunRTestLinMarkerInfo(identifier: RIdentifierExpression, private val testName: String, private val call: RCallExpression)
        : LineMarkerInfo<PsiElement>(
      identifier,
      identifier.textRange,
      AllIcons.RunConfigurations.TestState.Run,
      Function { "Run test \"$testName\"" },
      null,
      GutterIconRenderer.Alignment.CENTER,
      Supplier { "Run test \"$testName\"" }
    ) {
        override fun createGutterRenderer() = object : LineMarkerGutterIconRenderer<PsiElement>(this) {
            override fun getClickAction(): AnAction? {
                return null
            }

            override fun getPopupMenuActions() = DefaultActionGroup(
              RRunSingleTestAction(call),
              RDebugSingleTestAction(call)
            )

            override fun isNavigateAction() = true
        }

        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
    }

}