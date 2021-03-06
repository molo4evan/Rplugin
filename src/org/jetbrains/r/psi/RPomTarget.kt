/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.pom.PomTarget
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PomTargetPsiElementImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.r.console.RConsoleManager
import org.jetbrains.r.debugger.RDebuggerUtil
import org.jetbrains.r.psi.api.RFile
import org.jetbrains.r.psi.api.RFunctionExpression
import org.jetbrains.r.psi.api.RPsiElement
import org.jetbrains.r.rinterop.*
import org.jetbrains.r.run.visualize.VisualizeTableHandler
import org.jetbrains.r.skeleton.psi.RSkeletonAssignmentStatement

private class RPomTargetPsiElementImpl(pomTarget: PomTarget, project: Project): PomTargetPsiElementImpl(project, pomTarget), RPsiElement

abstract class RPomTarget: PomTarget {
  override fun canNavigate(): Boolean = true

  override fun canNavigateToSource(): Boolean = true

  override fun isValid(): Boolean = true

  abstract fun navigateAsync(requestFocus: Boolean): Promise<Unit>

  override fun navigate(requestFocus: Boolean) {
    navigateAsync(requestFocus)
  }

  companion object {
    fun createPsiElementByRValue(rVar: RVar): RPsiElement = RPomTargetPsiElementImpl(
      createPomTarget(rVar), rVar.project)

    fun createSkeletonParameterTarget(assignment: RSkeletonAssignmentStatement, name: String): RPsiElement =
      RPomTargetPsiElementImpl(RSkeletonParameterPomTarget(assignment, name), assignment.project)

    fun createPomTarget(rVar: RVar): RPomTarget = when (val value = rVar.value) {
      is RValueFunction -> createFunctionPomTarget(rVar)
      is RValueDataFrame -> createDataFramePomTarget(rVar)
      is RValueGraph -> createGraphPomTarget(rVar)
      is RValueMatrix -> {
        if (value.dim.size == 2) {
          createDataFramePomTarget(rVar)
        } else {
          createVariablePomTarget(rVar)
        }
      }
      is RValueError -> throw IllegalStateException("Error: ${value.text}")
      else -> createVariablePomTarget(rVar)
    }

  }
}

private fun createDataFramePomTarget(rVar: RVar): RPomTarget = DataFramePomTarget(rVar)

private fun createGraphPomTarget(rVar: RVar): RPomTarget = GraphPomTarget(rVar)

private fun createVariablePomTarget(rVar: RVar): RPomTarget = VariablePomTarget(rVar)

private fun createFunctionPomTarget(rVar: RVar): RPomTarget = FunctionPomTarget(rVar)

internal class FunctionPomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return rVar.ref.rInterop.executeTask {
      rVar.ref.functionSourcePositionWithText()?.let {
        ApplicationManager.getApplication().invokeLater {
          RDebuggerUtil.navigateAndCheckSourceChanges(rVar.project, it)
        }
      }
      Unit
    }
  }
}

internal class VariablePomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    val promise = AsyncPromise<Unit>()
    ApplicationManager.getApplication().invokeLater {
      RConsoleManager.getInstance(rVar.project).currentConsoleOrNull?.debuggerPanel?.navigate(rVar)
      promise.setResult(Unit)
    }
    return promise
  }
}

internal class DataFramePomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return VisualizeTableHandler.visualizeTable(rVar.ref.rInterop, rVar.ref, rVar.project, rVar.name)
  }
}

internal class GraphPomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return rVar.ref.evaluateAsTextAsync().then { Unit }
  }
}

internal class RSkeletonParameterPomTarget(val assignment: RSkeletonAssignmentStatement,
                                           val name: String) : RPomTarget() {

  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return RConsoleManager.getInstance(assignment.project).runAsync { console ->
      val rVar = assignment.createRVar(console)
      val virtualFile = rVar.ref.functionSourcePosition()?.file ?: return@runAsync
      runReadAction {
        val psiFile = PsiManager.getInstance(assignment.project).findFile(virtualFile)
        if (psiFile !is RFile) return@runReadAction
        val rFunctionExpression = PsiTreeUtil.findChildOfAnyType(psiFile, RFunctionExpression::class.java) ?: return@runReadAction
        val parameter = rFunctionExpression.parameterList?.parameterList?.firstOrNull { it.name == name } ?: return@runReadAction
        invokeLater {
          parameter.navigate(true)
        }
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as RSkeletonParameterPomTarget
    if (assignment != other.assignment) return false
    if (name != other.name) return false
    return true
  }

  override fun hashCode(): Int {
    var result = assignment.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }
}