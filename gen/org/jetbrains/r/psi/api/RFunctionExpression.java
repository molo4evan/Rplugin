// This is a generated file. Not intended for manual editing.
package org.jetbrains.r.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.r.psi.cfg.RControlFlow;
import org.jetbrains.r.psi.references.RReferenceBase;

public interface RFunctionExpression extends RExpression, RControlFlowHolder {

  @Nullable
  RExpression getExpression();

  @Nullable
  RParameterList getParameterList();

  @Nullable
  String getDocStringValue();

  @NotNull
  RControlFlow getControlFlow();

  @Nullable
  RReferenceBase<?> getReference();

}
