// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import com.intellij.xml.util.documentation.HtmlDescriptorsTable;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlReferenceVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.codeInsight.Angular2Processor.getHtmlElementClass;

public class Angular2HtmlReferenceVariableImpl extends JSVariableImpl<JSVariableStub<JSVariable>, JSVariable>
  implements Angular2HtmlReferenceVariable {

  public Angular2HtmlReferenceVariableImpl(ASTNode node) {
    super(node);
  }

  public Angular2HtmlReferenceVariableImpl(JSVariableStub<JSVariable> stub) {
    super(stub, Angular2HtmlElementTypes.REFERENCE_VARIABLE);
  }

  @Nullable
  @Override
  protected JSType doGetType() {
    Angular2HtmlReference reference = getReferenceDefinitionAttribute();
    if (reference == null) {
      return null;
    }

    String tagName = ObjectUtils.doIfNotNull(reference.getParent(), XmlTag::getName);

    if (tagName != null
        && HtmlDescriptorsTable.getTagDescriptor(tagName) != null
        && reference.getValueElement() == null) {
      return JSTypeUtils.createType(getHtmlElementClass(getProject(), tagName),
                                    JSTypeSourceFactory.createTypeSource(this, true));
    }
    return null;
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public boolean isExported() {
    return true;
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    final JSClass clazz = Angular2IndexingHandler.findDirectiveClass(this);
    LocalSearchScope localScope;
    if (clazz != null) {
      localScope = new LocalSearchScope(new PsiElement[]{clazz, this.getContainingFile()});
    }
    else {
      localScope = new LocalSearchScope(this.getContainingFile());
    }
    return GlobalSearchScope.filesScope(getProject(), GlobalSearchScopeUtil.getLocalScopeFiles(localScope));
  }

  @Override
  public void delete() throws IncorrectOperationException {
    Angular2HtmlReference ref = getReferenceDefinitionAttribute();
    if (ref != null) {
      ref.delete();
    }
    else {
      super.delete();
    }
  }

  @NotNull
  @Override
  protected JSAttributeList.AccessType calcAccessType() {
    return JSAttributeList.AccessType.PUBLIC;
  }

  @Override
  protected boolean useTypesFromJSDoc() {
    return false;
  }

  @Nullable
  private Angular2HtmlReference getReferenceDefinitionAttribute() {
    return (Angular2HtmlReference)PsiTreeUtil.findFirstParent(
      this, Angular2HtmlReference.class::isInstance);
  }
}