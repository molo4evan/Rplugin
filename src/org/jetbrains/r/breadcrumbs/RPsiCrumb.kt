/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.breadcrumbs

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.intellij.xml.breadcrumbs.BreadcrumbsPanel
import com.intellij.xml.breadcrumbs.BreadcrumbsXmlWrapper
import com.intellij.xml.breadcrumbs.CrumbPresentation
import org.jetbrains.r.actions.RActionUtil

class RPsiCrumb(element: PsiElement, provider: BreadcrumbsProvider, presentation: CrumbPresentation?) :
  CommonPsiCrumb(element, provider, presentation) {

  private val file = element.containingFile

  override fun navigate(editor: Editor, withSelection: Boolean) {
    unmuteCaretChangeUpdate(editor)
    RActionUtil.executeActionById("FileStructurePopup", file.project)
  }

  private fun unmuteCaretChangeUpdate(editor: Editor) {
    // BreadcrumbsXmlWrapper does not track caret changes just after Crumb navigation
    // But in this custom implementation we need to track it
    val breadcrumbsComponent = BreadcrumbsXmlWrapper.getBreadcrumbsComponent(editor) ?: return
    val trackCaretChangeFlag = BreadcrumbsPanel::class.java.getDeclaredField("myUserCaretChange")
    trackCaretChangeFlag.isAccessible = true
    trackCaretChangeFlag.set(breadcrumbsComponent, true)
  }
}
