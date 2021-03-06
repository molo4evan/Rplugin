// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.intentions

import org.intellij.lang.annotations.Language
import org.jetbrains.r.RUsefulTestCase

abstract class AbstractRIntentionTest : RUsefulTestCase() {
  protected abstract val intentionName: String

  @Throws(Exception::class)
  public override fun setUp() {
    super.setUp()
    val intentionDataPath = testDataPath + "/intentions/" + javaClass.simpleName.replace("Test", "")
    myFixture.testDataPath = intentionDataPath
  }

  protected fun doTest() {
    myFixture.configureByFile(getTestName(false) + ".before.R")

    val intention = myFixture.findSingleIntention(intentionName)
    myFixture.launchAction(intention)
    myFixture.checkResultByFile(getTestName(false) + ".after.R")
  }


  protected fun doExprTest(@Language("R") before: String, @Language("R") after: String) {
    myFixture.configureByText("a.R", before.trimIndent())

    val intention = myFixture.findSingleIntention(intentionName)
    myFixture.launchAction(intention)

    myFixture.checkResult(after.trimIndent())
  }
}
