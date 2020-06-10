// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.apache.commons.lang.StringUtils
import org.jetbrains.r.RLanguage
import org.jetbrains.r.console.RConsoleRuntimeInfo
import org.jetbrains.r.console.RConsoleView
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.editor.completion.*
import org.jetbrains.r.hints.parameterInfo.RParameterInfoUtil
import org.jetbrains.r.interpreter.RInterpreterManager
import org.jetbrains.r.parsing.RElementTypes.*
import org.jetbrains.r.psi.*
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.psi.references.RSearchScopeUtil
import org.jetbrains.r.refactoring.RNamesValidator
import org.jetbrains.r.rinterop.RValueFunction
import org.jetbrains.r.skeleton.psi.RSkeletonAssignmentStatement
import org.jetbrains.r.util.PathUtil
import kotlin.collections.component1
import kotlin.collections.component2

class RCompletionContributor : CompletionContributor() {

  init {
    addTableContextCompletion()
    addGGPlotAesColumnCompletionProvider()
    addStringLiteralCompletion()
    addInstalledPackageCompletion()
    addNamespaceAccessExpression()
    addMemberAccessCompletion()
    addIdentifierCompletion()
  }

  private fun addNamespaceAccessExpression() {
    extend(CompletionType.BASIC, psiElement()
      .withLanguage(RLanguage.INSTANCE)
      .and(RElementFilters.NAMESPACE_REFERENCE_FILTER), NamespaceAccessCompletionProvider())
  }

  private fun addIdentifierCompletion() {
    extend(CompletionType.BASIC, psiElement()
      .withLanguage(RLanguage.INSTANCE)
      .andOr(RElementFilters.IDENTIFIER_FILTER, RElementFilters.OPERATOR_FILTER), IdentifierCompletionProvider())
  }

  private fun addMemberAccessCompletion() {
    extend(CompletionType.BASIC, psiElement().withLanguage(RLanguage.INSTANCE)
      .and(RElementFilters.MEMBER_ACCESS_FILTER), MemberAccessCompletionProvider())
  }

  private fun addInstalledPackageCompletion() {
    extend(CompletionType.BASIC, psiElement().withLanguage(RLanguage.INSTANCE)
      .and(RElementFilters.IMPORT_CONTEXT), InstalledPackageCompletionProvider())
  }

  private fun addTableContextCompletion() {
    extend(CompletionType.BASIC, psiElement().withLanguage(RLanguage.INSTANCE)
      .and(RElementFilters.IDENTIFIER_OR_STRING_FILTER), TableContextCompletionProvider())
  }

  private fun addStringLiteralCompletion() {
    extend(CompletionType.BASIC, psiElement().withLanguage(RLanguage.INSTANCE)
      .and(RElementFilters.STRING_FILTER), StringLiteralCompletionProvider())
  }

  private fun addGGPlotAesColumnCompletionProvider() {
    extend(CompletionType.BASIC, psiElement().withLanguage(RLanguage.INSTANCE)
      .and(AES_PARAMETER_FILTER), GGPlot2AesColumnCompletionProvider())
  }

  private class MemberAccessCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val file = parameters.originalFile
      val info = file.runtimeInfo ?: return
      val memberAccess = PsiTreeUtil.getParentOfType(position, RMemberExpression::class.java) ?: return
      val leftExpr = memberAccess.leftExpr ?: return
      val noCalls = PsiTreeUtil.processElements(leftExpr) { it !is RCallExpression }
      if (noCalls) {
        info.loadObjectNames(leftExpr.text).forEach { result.consume(rCompletionElementFactory.createNamespaceAccess(it)) }
      }
    }
  }


  private class InstalledPackageCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val interpreter = RInterpreterManager.getInterpreterOrNull(position.project) ?: return
      val installedPackages = interpreter.installedPackages
      installedPackages.filter { it.isUser }.forEach {
        result.consume(rCompletionElementFactory.createPackageLookupElement(it.packageName, true))
      }
    }
  }

  private class NamespaceAccessCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position =
        PsiTreeUtil.getParentOfType(parameters.position, RIdentifierExpression::class.java, false) ?: return
      val namespaceAccess = position.parent as? RNamespaceAccessExpression ?: return
      val namespaceName = namespaceAccess.namespaceName
      val isInternalAccess = namespaceAccess.node.findChildByType(R_TRIPLECOLON) != null
      RPackageCompletionUtil.addNamespaceCompletion(namespaceName, isInternalAccess, parameters, result, rCompletionElementFactory)
    }
  }

  private class IdentifierCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, _result: CompletionResultSet) {
      val probableIdentifier = PsiTreeUtil.getParentOfType(parameters.position, RExpression::class.java, false)
      val position = if (probableIdentifier != null) {
        // operator surrounded by % or identifier
        PsiTreeUtil.findChildOfType(probableIdentifier, RInfixOperator::class.java) ?: probableIdentifier
      }
      else {
        // operator with parser error
        PsiTreeUtil.getParentOfType(parameters.position, RPsiElement::class.java, false) ?: return
      }

      val result =
        if (probableIdentifier == null) _result.withPrefixMatcher("%${_result.prefixMatcher.prefix}")
        else _result
      val parent = position.parent
      val shownNames = HashSet<String>()
      val project = position.project
      val originalFile = parameters.originalFile

      // don't complete parameters name
      if (parent is RParameter && position == parent.variable) {
        return
      }

      val file = parameters.originalFile
      val isHelpFromRConsole = file.getUserData(RConsoleView.IS_R_CONSOLE_KEY)?.let { file.firstChild is RHelpExpression } ?: false
      val elementFactory = if (isHelpFromRConsole) RLookupElementFactory() else rCompletionElementFactory
      addKeywords(position, shownNames, result, isHelpFromRConsole)
      addLocalsFromControlFlow(position, shownNames, result, elementFactory)
      addLocalsFromRuntime(originalFile, shownNames, result, elementFactory)

      // we are completing an assignee, so we don't want to suggest function names here
      if (position is RExpression && position.isAssignee()) {
        return
      }

      RPackageCompletionUtil.addPackageCompletion(position, result)
      addNamedArgumentsCompletion(originalFile, parent, result)
      val prefix = position.name?.let { StringUtil.trimEnd(it, CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED) } ?: ""
      RPackageCompletionUtil.addCompletionFromIndices(project, RSearchScopeUtil.getScope(originalFile),
                                                      parameters.originalFile, prefix, shownNames, result, elementFactory)
    }

    private fun addLocalsFromRuntime(originFile: PsiFile,
                                     shownNames: HashSet<String>,
                                     result: CompletionResultSet,
                                     elementFactory: RLookupElementFactory) {
      originFile.runtimeInfo?.variables?.let { variables ->
        variables.filterKeys { shownNames.add(it) }.forEach { (name, value) ->
          if (value is RValueFunction) {
            val code = "${RNamesValidator.quoteIfNeeded(name)} <- ${value.header} NULL"
            val element =
              RElementFactory.createRPsiElementFromTextOrNull(originFile.project, code) as? RAssignmentStatement ?: return@forEach
            result.consume(elementFactory.createFunctionLookupElement(element, isLocal = true))
          }
          else {
            result.consume(elementFactory.createLocalVariableLookupElement(name, false))
          }
        }
      }
    }

    private fun addKeywords(position: RPsiElement,
                            shownNames: HashSet<String>,
                            result: CompletionResultSet,
                            isHelpFromRConsole: Boolean) {
      for (keyword in BUILTIN_CONSTANTS + KEYWORDS) {
        val necessaryCondition = KEYWORD_NECESSARY_CONDITION[keyword]
        val stringValue = keyword.toString()
        if (isHelpFromRConsole || necessaryCondition == null || necessaryCondition(position)) {
          shownNames.add(stringValue)
          result.addElement(PrioritizedLookupElement.withGrouping(RLookupElement(stringValue, true), GLOBAL_GROUPING))
        }
      }
      for (keyword in KEYWORDS_WITH_BRACKETS) {
        val stringValue = keyword.toString()
        shownNames.add(stringValue)
        val insertHandler = InsertHandler<LookupElement> { context, _ ->
          if (!isHelpFromRConsole) {
            val document = context.document
            document.insertString(context.tailOffset, " ()")
            context.editor.caretModel.moveCaretRelatively(2, 0, false, false, false)
          }
        }
        result.addElement(RLookupElementFactory.createLookupElementWithGrouping(RLookupElement(stringValue, true, tailText = " (...)"),
                                                                                insertHandler, GLOBAL_GROUPING))
      }
    }

    private fun addLocalsFromControlFlow(position: RPsiElement,
                                         shownNames: HashSet<String>,
                                         result: CompletionResultSet,
                                         elementFactory: RLookupElementFactory) {
      val controlFlowHolder = PsiTreeUtil.getParentOfType(position, RControlFlowHolder::class.java)
      controlFlowHolder?.getLocalVariableInfo(position)?.variables?.values?.sortedBy { it.variableDescription.name }?.forEach {
        val name = it.variableDescription.name
        shownNames.add(name)
        val parent = it.variableDescription.firstDefinition.parent
        if (parent is RAssignmentStatement && parent.isFunctionDeclaration) {
          result.consume(elementFactory.createFunctionLookupElement(parent, true))
        }
        else {
          result.consume(elementFactory.createLocalVariableLookupElement(name, parent is RParameter))
        }
      }
    }

    private fun consumeParameter(parameterName: String, shownNames: MutableSet<String>, result: CompletionResultSet) {
      if (shownNames.add(parameterName)) {
        result.consume(rCompletionElementFactory.createNamedArgumentLookupElement(parameterName))
      }
    }

    private fun addNamedArgumentsCompletion(originalFile: PsiFile, parent: PsiElement?, result: CompletionResultSet) {
      if (parent !is RArgumentList && parent !is RNamedArgument) return

      val mainCall = (if (parent is RNamedArgument) parent.parent.parent else parent.parent) as? RCallExpression ?: return
      val shownNames = HashSet<String>()

      val declarations = RPsiUtil.resolveCall(mainCall)
      for (functionDeclaration in declarations) {
        functionDeclaration.parameterNameList.forEach { consumeParameter(it, shownNames, result) }
      }

      val info = originalFile.runtimeInfo
      val mainFunctionName = when (val expression = mainCall.expression) {
        is RNamespaceAccessExpression -> expression.identifier?.name ?: return
        is RIdentifierExpression -> expression.name
        else -> return
      }
      info?.loadInheritorNamedArguments(mainFunctionName)?.forEach { consumeParameter(it, shownNames, result) }

      val singleDeclaration = declarations.singleOrNull()
      val extraNamedArguments =
        when (singleDeclaration) {
          null -> info?.loadExtraNamedArguments(mainFunctionName)
          is RSkeletonAssignmentStatement -> singleDeclaration.stub.extraNamedArguments
          else -> {
            val functionExpression = singleDeclaration.assignedValue as? RFunctionExpression
            if (functionExpression != null) info?.loadExtraNamedArguments(mainFunctionName, functionExpression)
            else info?.loadExtraNamedArguments(mainFunctionName)
          }
        } ?: return

      for (parameter in extraNamedArguments.argumentNames) {
        consumeParameter(parameter, shownNames, result)
      }

      val argumentInfo = RParameterInfoUtil.getArgumentInfo(mainCall, singleDeclaration) ?: return
      for (parameter in extraNamedArguments.functionArgNames) {
        val arg = argumentInfo.getArgumentPassedToParameter(parameter) ?: continue
        if (arg is RFunctionExpression) {
          arg.parameterList?.parameterList?.map { it.name }?.forEach {
            consumeParameter(it, shownNames, result)
          }
        }
        else {
          arg.reference?.multiResolve(false)?.forEach { resolveResult ->
            (resolveResult.element as? RAssignmentStatement)?.let { assignment ->
              val inhNamedArgs = info?.loadInheritorNamedArguments(assignment.name) ?: emptyList()
              (assignment.parameterNameList + inhNamedArgs).forEach {
                consumeParameter(it, shownNames, result)
              }
            }
          }
        }
      }
    }

    companion object {
      private val BUILTIN_CONSTANTS = listOf(R_TRUE, R_FALSE, R_NULL, R_NA, R_INF, R_NAN,
                                             R_NA_INTEGER_, R_NA_REAL_, R_NA_COMPLEX_, R_NA_CHARACTER_)
      private val KEYWORDS_WITH_BRACKETS = listOf(R_IF, R_WHILE, R_FUNCTION, R_FOR)
      private val KEYWORDS = listOf(R_ELSE, R_REPEAT, R_IN)
      private val KEYWORD_NECESSARY_CONDITION = mapOf<IElementType, (PsiElement) -> Boolean>(
        R_IN to { element ->
          if (element.parent !is RForStatement) false
          else {
            val newText = element.parent.text.replace(element.text, R_IN.toString())
            val newElement = RElementFactory
              .buildRFileFromText(element.project, newText).findElementAt(element.textOffset - element.parent.textOffset)
            if (PsiTreeUtil.getParentOfType(newElement, PsiErrorElement::class.java, false) != null) false
            else {
              !isErrorElementBefore(newElement!!)
            }
          }
        },
        R_ELSE to { element ->
          var sibling: PsiElement? = PsiTreeUtil.skipWhitespacesAndCommentsBackward(element)
          sibling is RIfStatement && !sibling.node.getChildren(null).any { it.elementType == R_ELSE }
        }
      )

      private val PsiElement.prevLeafs: Sequence<PsiElement>
        get() = generateSequence({ PsiTreeUtil.prevLeaf(this) }, { PsiTreeUtil.prevLeaf(it) })

      private fun isErrorElementBefore(token: PsiElement): Boolean {
        for (leaf in token.prevLeafs) {
          if (leaf is PsiWhiteSpace || leaf is PsiComment) continue
          if (leaf is PsiErrorElement || PsiTreeUtil.findFirstParent(leaf) { it is PsiErrorElement } != null) return true
          if (leaf.textLength != 0) break
        }
        return false
      }
    }
  }

  private class TableContextCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val runtimeInfo = parameters.originalFile.runtimeInfo ?: return
      val columns = mutableListOf<TableManipulationColumn>()
      addTableCompletion(RDplyrAnalyzer, position, runtimeInfo, columns)
      addTableCompletion(RDataTableAnalyzer, position, runtimeInfo, columns)
      result.addAllElements(columns.distinct().map {
        PrioritizedLookupElement.withPriority(
          RLookupElement(it.name, true, AllIcons.Nodes.Field, packageName = it.type),
          TABLE_MANIPULATION_PRIORITY
        )
      })
    }
  }

  private class StringLiteralCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val stringLiteral = PsiTreeUtil.getParentOfType(parameters.position, RStringLiteralExpression::class.java, false) ?: return
      addTableLiterals(stringLiteral, parameters, result)
      addFilePathCompletion(parameters, stringLiteral, result)
    }

    private fun addTableLiterals(stringLiteral: RStringLiteralExpression,
                                 parameters: CompletionParameters,
                                 result: CompletionResultSet) {
      val parent = stringLiteral.parent as? ROperatorExpression ?: return
      if (!parent.isBinary || (parent.operator?.name != "==" && parent.operator?.name != "!=")) return
      val other = (if (parent.leftExpr == stringLiteral) parent.rightExpr else parent.leftExpr) ?: return
      val runtimeInfo = parameters.originalFile.runtimeInfo ?: return

      val values = mutableListOf<String>()
      addColumnStringValues(RDplyrAnalyzer, stringLiteral, other, runtimeInfo, values)
      addColumnStringValues(RDataTableAnalyzer, stringLiteral, other, runtimeInfo, values)
      val insertHandler = InsertHandler<LookupElement> { insertHandlerContext, _ ->
        insertHandlerContext.file.findElementAt(insertHandlerContext.editor.caretModel.offset)?.let { element ->
          insertHandlerContext.editor.caretModel.moveToOffset(element.textRange.endOffset)
        }
      }
      result.addAllElements(values.distinct().map {
        RLookupElementFactory.createLookupElementWithPriority(RLookupElement(escape(it), true, AllIcons.Nodes.Field, itemText = it),
                                                              insertHandler, TABLE_MANIPULATION_PRIORITY)
      })
    }

    private fun <T : TableManipulationFunction> addColumnStringValues(tableAnalyser: TableManipulationAnalyzer<T>,
                                                                      stringLiteral: RStringLiteralExpression,
                                                                      columnNameIdentifier: RExpression,
                                                                      runtimeInfo: RConsoleRuntimeInfo,
                                                                      result: MutableList<String>) {
      val contextInfo = tableAnalyser.getContextInfo(stringLiteral, runtimeInfo)
      val text = if (contextInfo != null) {
        val columnName =
          if (columnNameIdentifier is RMemberExpression) columnNameIdentifier.rightExpr?.name
          else columnNameIdentifier.name
        val command = StringBuilder()
        command.append("unlist(list(")
        contextInfo.callInfo.passedTableArguments.forEachIndexed { ind, table ->
          if (ind != 0) command.append(", ")
          command.append("(")
          tableAnalyser.transformExpression(table, command, runtimeInfo, true)
          command.append(")$$columnName")
        }
        command.append("))")
        command.toString()
      } else {
        if (!tableAnalyser.isSafe(columnNameIdentifier, runtimeInfo)) return
        "(${columnNameIdentifier.text})"
      }
      result.addAll(runtimeInfo.loadDistinctStrings(text).filter { it.isNotEmpty() })
    }

    private val escape = StringUtil.escaper(true, "\"")::`fun`
  }

  companion object {
    private val rCompletionElementFactory = RLookupElementFactory(RFunctionCompletionInsertHandler)

    private object RFunctionCompletionInsertHandler : RLookupElementInsertHandler {
      override fun getInsertHandlerForAssignment(assignment: RAssignmentStatement): InsertHandler<LookupElement> {
        val noArgs = assignment.functionParameters == "()"
        return InsertHandler<LookupElement> { context, _ ->
          val document = context.document
          document.insertString(context.tailOffset, "()")
          context.editor.caretModel.moveCaretRelatively(if (noArgs) 2 else 1, 0, false, false, false)
        }
      }
    }

    private fun <T : TableManipulationFunction> addTableCompletion(tableAnalyser: TableManipulationAnalyzer<T>,
                                                                   position: PsiElement,
                                                                   runtimeInfo: RConsoleRuntimeInfo,
                                                                   result: MutableList<TableManipulationColumn>) {
      val (tableCallInfo, currentArgument) = tableAnalyser.getContextInfo(position, runtimeInfo) ?: return
      val tableInfos = tableCallInfo.passedTableArguments.map { tableAnalyser.getTableColumns(it, runtimeInfo) }
      val isCorrectTableType = tableInfos.all { it.type == tableAnalyser.tableColumnType }
      val isQuotesNeeded = !isCorrectTableType && tableAnalyser.isSubscription(tableCallInfo.function)
                           || tableCallInfo.function.isQuotesNeeded(tableCallInfo.argumentInfo, currentArgument)
      if (!isQuotesNeeded && position.parent is RStringLiteralExpression) return

      var columns = tableInfos.map { it.columns }.flatten()
        .groupBy { it.name }
        .map { (name, list) ->
          TableManipulationColumn(name, StringUtils.join(list.mapNotNull { it.type }, "/"))
        }

      if (tableAnalyser is RDplyrAnalyzer)
        columns = tableAnalyser.addCurrentColumns(columns, tableCallInfo, currentArgument)

      if (isQuotesNeeded && position.parent !is RStringLiteralExpression) {
        columns = columns.map { TableManipulationColumn("\"${it.name}\"", it.type) }
      }
      result.addAll(columns)
    }

    private fun addFilePathCompletion(parameters: CompletionParameters,
                                      stringLiteral: RStringLiteralExpression,
                                      _result: CompletionResultSet) {
      val reference = parameters.position.containingFile.findReferenceAt(parameters.offset) as? FileReference ?: return
      val filepath = stringLiteral.name?.trim() ?: return
      val filePrefix = PathUtil.toPath(filepath)?.fileName?.toString()?.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "") ?: return
      val result = _result.withPrefixMatcher(filePrefix)
      val variants = reference.variants.map {
        when (it) {
          is LookupElement -> it
          is PsiNamedElement -> LookupElementBuilder.createWithIcon(it)
          else -> LookupElementBuilder.create(it)
        }
      }

      for (lookup in variants) {
        if (result.prefixMatcher.prefixMatches(lookup)) {
          result.addElement(lookup)
        }
      }
    }
  }
}