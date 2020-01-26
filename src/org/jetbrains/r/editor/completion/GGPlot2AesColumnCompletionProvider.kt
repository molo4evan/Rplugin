/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.editor.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.editor.RLookupElement
import org.jetbrains.r.editor.TABLE_MANIPULATION_COLUMNS_GROUPING
import org.jetbrains.r.hints.parameterInfo.RParameterInfoUtil
import org.jetbrains.r.psi.RDataTableUtil
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.api.*

class GGPlot2AesColumnCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val expression = PsiTreeUtil.getParentOfType(parameters.position, RExpression::class.java, false) ?: return
    val parent = expression.parent
    val aesCall = (if (parent is RNamedArgument) parent.parent.parent else parent.parent) as RCallExpression
    val ggplotCall = (if (aesCall.parent is RNamedArgument) aesCall.parent.parent.parent else aesCall.parent.parent) as RCallExpression
    val assignment = RPsiUtil.resolveCall(ggplotCall).singleOrNull() ?: return
    val parameterNameList = assignment.parameterNameList
    val argumentList = ggplotCall.argumentList
    val argumentsPermutation = RParameterInfoUtil.getArgumentsPermutation(parameterNameList, argumentList).first
    val dataParameter = argumentList.expressionList[argumentsPermutation.indexOf(0).takeIf { it != -1 } ?: return] // data argument
    val data = if (dataParameter is RNamedArgument) dataParameter.assignedValue ?: return else dataParameter
    val aes = argumentList.expressionList[argumentsPermutation.indexOf(1).takeIf { it != -1 } ?: return] // aes argument
    if (!(aes is RNamedArgument && aes.assignedValue == aesCall || aes == aesCall)) return
    val runtimeInfo = parameters.originalFile.runtimeInfo ?: return
    val tableInfo = RDataTableUtil.getTableColumns(data, runtimeInfo)
    result.addAllElements(tableInfo.columns.map {
      PrioritizedLookupElement.withGrouping(RLookupElement(it.name, true, AllIcons.Nodes.Field, packageName = it.type),
                                            TABLE_MANIPULATION_COLUMNS_GROUPING)
    })
  }
}

val AES_PARAMETER_FILTER = FilterPattern(AesFilter())

class AesFilter : ElementFilter {
  override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
    val expression = PsiTreeUtil.getParentOfType(context, RExpression::class.java, false)
    if (expression !is RIdentifierExpression) return false
    val parent = expression.parent ?: return false
    if (!(parent is RNamedArgument && parent.assignedValue == expression || parent is RArgumentList)) return false
    val aesCall = if (parent is RNamedArgument) parent.parent.parent else parent.parent
    if (aesCall !is RCallExpression) return false
    val aesIdentifier = aesCall.expression
    if (aesIdentifier !is RIdentifierExpression || aesIdentifier.name != "aes") return false
    val ggplot = if (aesCall.parent is RNamedArgument) aesCall.parent.parent.parent else aesCall.parent.parent
    if (ggplot !is RCallExpression) return false
    val ggplotIdentifier = ggplot.expression
    if (ggplotIdentifier !is RIdentifierExpression || ggplotIdentifier.name != "ggplot") return false
    return true
  }

  override fun isClassAcceptable(hintClass: Class<*>?): Boolean = true
}