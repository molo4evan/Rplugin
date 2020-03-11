/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.intellij.datavis.r.inlays

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.impl.EditorImpl
import java.awt.Component
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JScrollPane


/**
 * Utility class to make smooth scroll of editor with inlay components.
 *
 * The problem is that the editor handles MouseWheel event to scroll as well as Chart and several TextEditors.
 * The event is always caught by the top level element and that's why if we will scroll the editor page with chart,
 * when the mouse will be over Chart, scrolling will immediately stops and Chart zooming will start. This is not the desired behaviour.
 *
 * We are wrapping MouseWheel listener from editor and MouseWheel listeners from all scrollable inlay components.
 * Editor wrapper simply writes the timestamp of scroll event over editor
 * Scrollable wrappers check that the editor scroll time passed check that the editor start scrolling recently
 * and extend the timestamp. If the editor last scrolling mark was set long ago, scrollables process the wheel event itself.
 *
 * ToDo we can also drop editor scroll time on mouse move event over other components.
 */
object MouseWheelUtils {

  private class MouseWheelListenerWrapper(private val component: Component,
                                          private val listeners: Array<MouseWheelListener>) : MouseWheelListener {

    override fun mouseWheelMoved(e: MouseWheelEvent) {

      var cannotScroll = false

      if (component is JScrollPane) {
        val scrollBar = component.verticalScrollBar
        if (e.preciseWheelRotation < 0 && scrollBar.value == scrollBar.minimum ||
            e.preciseWheelRotation > 0 && scrollBar.value == scrollBar.maximum - scrollBar.model.extent) {
          cannotScroll = true
        }
      }

      if (cannotScroll || isEditorOwns()) {
        component.parent.dispatchEvent(e)
      }
      else {
        for (listener in listeners) {
          listener.mouseWheelMoved(e)
        }
      }
    }
  }

  private class EditorMouseWheelListenerWrapper(private val listeners: Array<MouseWheelListener>) : MouseWheelListener {

    override fun mouseWheelMoved(e: MouseWheelEvent) {
      editorScrolls()
      for (listener in listeners) {
        listener.mouseWheelMoved(e)
      }
    }
  }

  private var lastTimestamp: Long = 0

  fun isEditorOwns(): Boolean {
    return System.currentTimeMillis() - lastTimestamp < 800
  }

  fun editorScrolls() {
    lastTimestamp = System.currentTimeMillis()
  }

  fun wrapEditorMouseWheelListeners(editor: EditorImpl) {

    fun addListener(component: Component) {
      val lst = component.mouseWheelListeners.toCollection(ArrayList())

      for (listener in lst) {
        component.removeMouseWheelListener(listener)
      }

      lst.removeIf { it is EditorMouseWheelListenerWrapper }

      component.addMouseWheelListener(EditorMouseWheelListenerWrapper(lst.toTypedArray()))
    }

    addListener(editor.scrollPane)

    LafManager.getInstance().addLafManagerListener( LafManagerListener { addListener(editor.scrollPane) }, editor.disposable)
  }

  fun wrapMouseWheelListeners(component: Component, disposable: Disposable?) {

    fun addListener(component: Component) {
      val lst = component.mouseWheelListeners.toCollection(ArrayList())

      for (listener in lst) {
        component.removeMouseWheelListener(listener)
      }

      lst.removeIf { it is MouseWheelListenerWrapper }

      component.addMouseWheelListener(MouseWheelListenerWrapper(component, lst.toTypedArray()))
    }

    addListener(component)

    if (disposable != null) {
      LafManager.getInstance().addLafManagerListener(LafManagerListener { addListener(component) }, disposable)
    }
  }
}