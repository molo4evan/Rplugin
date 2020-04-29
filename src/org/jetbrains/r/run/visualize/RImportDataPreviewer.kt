/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.run.visualize

import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import org.intellij.datavis.r.inlays.components.EmptyComponentPanel
import org.intellij.datavis.r.ui.MaterialTable
import org.intellij.datavis.r.ui.MaterialTableUtils
import org.jetbrains.r.RBundle
import java.awt.*
import javax.swing.*

class RImportDataPreviewer(private val parent: Disposable, emptyComponent: JComponent) {
  private val loadingPanel = JBLoadingPanel(BorderLayout(), parent).apply {
    startLoading()
  }

  private val rootPanel = EmptyComponentPanel(emptyComponent)

  @Volatile
  private var currentViewer: RDataFrameViewer? = null

  val component = rootPanel.component

  val hasPreview: Boolean
    get() = currentViewer != null

  fun showLoading() {
    closePreview()
    rootPanel.contentComponent = loadingPanel
  }

  fun showPreview(viewer: RDataFrameViewer, errorCount: Int) {
    closePreview()
    val component = createViewerComponent(viewer, errorCount)
    rootPanel.contentComponent = component
    Disposer.register(parent, viewer)
    currentViewer = viewer
  }

  fun closePreview() {
    rootPanel.contentComponent = null
    currentViewer?.let { viewer ->
      Disposer.dispose(viewer)
      currentViewer = null
    }
  }

  private fun createViewerComponent(viewer: RDataFrameViewer, errorCount: Int): JComponent {
    val scrollPane = JBScrollPane(createTableFrom(viewer))
    return if (errorCount > 0) {
      JPanel(BorderLayout()).apply {
        val errorBar = createErrorBar(errorCount)
        add(scrollPane, BorderLayout.CENTER)
        add(errorBar, BorderLayout.SOUTH)
      }
    } else {
      scrollPane
    }
  }

  private fun createErrorBar(errorCount: Int): JComponent {
    return JPanel().apply {
      background = HintUtil.getErrorColor()
      border = JBEmptyBorder(ERROR_LABEL_INSETS)
      layout = BoxLayout(this, BoxLayout.LINE_AXIS)
      val errorText = createParsingErrorsMessage(errorCount)
      val errorLabel = JLabel(errorText).apply {
        font = font.deriveFont(Font.BOLD)
      }
      add(errorLabel)
    }
  }

  private fun createTableFrom(viewer: RDataFrameViewer): MaterialTable {
    return RVisualizeTableUtil.createMaterialTableFromViewer(viewer).apply {
      MaterialTableUtils.fitColumnsWidth(this)
      rowSorter = null
    }
  }

  companion object {
    private val ERROR_LABEL_INSETS = JBInsets(4, 4, 4, 4)

    private fun createParsingErrorsMessage(errorCount: Int): String {
      return RBundle.message("import.data.dialog.preview.parsing.errors", errorCount)
    }
  }
}
