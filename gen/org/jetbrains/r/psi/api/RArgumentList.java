// This is a generated file. Not intended for manual editing.
package org.jetbrains.r.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RArgumentList extends RArgumentHolder {

  @NotNull
  List<RExpression> getExpressionList();

  @NotNull
  List<RNamedArgument> getNamedArgumentList();

  @NotNull
  List<RNoCommaTail> getNoCommaTailList();

}
