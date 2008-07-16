/*
 * Copyright 2005 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.xpath.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.lang.xpath.XPathTokenTypes;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.context.VariableContext;
import org.intellij.lang.xpath.psi.PrefixedName;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.psi.XPathVariable;
import org.intellij.lang.xpath.psi.XPathVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public class XPathVariableReferenceImpl extends XPathElementImpl implements XPathVariableReference {
  private static final Object[] EMPTY_ARRAY = ArrayUtil.EMPTY_OBJECT_ARRAY;
    private static final TokenSet QNAME_FILTER = TokenSet.create(XPathTokenTypes.VARIABLE_PREFIX, XPathTokenTypes.VARIABLE_NAME);

    private final VariableContext resolver;

    public XPathVariableReferenceImpl(ASTNode node) {
        super(node);
        resolver = ContextProvider.getContextProvider(this).getVariableContext();
    }

    @NotNull
    public String getReferencedName() {
        return getText().substring(1);
    }

    @NotNull
    public XPathType getType() {
        final XPathVariable xPathVariable = resolve();
        if (xPathVariable != null) {
            return xPathVariable.getType();
        }
        return XPathType.UNKNOWN;
    }

    public PsiReference getReference() {
        return this;
    }

    public PsiElement getElement() {
        return this;
    }

    public int getTextOffset() {
        return getTextRange().getStartOffset() + 1;
    }

    public TextRange getRangeInElement() {
        return TextRange.from(1, getTextLength() - 1);
    }

    @Nullable
    public XPathVariable resolve() {
        if (resolver != null) {
            return resolver.resolve(this);
        }
        return null;
    }

    public String getCanonicalText() {
        return getText();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        renameTo(newElementName);
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        renameTo(((PsiNamedElement)element).getName());
        return this;
    }

    private void renameTo(String newElementName) {
        final XPathVariableReference child = XPathChangeUtil.createVariableReference(this, newElementName);

        final PrefixedNameImpl newName = ((PrefixedNameImpl)child.getQName());
        final PrefixedNameImpl oldName = ((PrefixedNameImpl)getQName());
        assert newName != null;
        assert oldName != null;

        final ASTNode localNode = newName.getLocalNode();
        getNode().replaceChild(oldName.getLocalNode(), localNode);
    }

    public boolean isReferenceTo(PsiElement element) {
        if (element instanceof XPathVariable) {
            final XPathVariable resolved = resolve();
            if (getReferencedName().equals(((XPathVariable)element).getName())) {
                return element.equals(resolved);
            }
        } else if (resolver != null) {
            return resolver.isReferenceTo(element, this);
        }
        return false;
    }

    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    public boolean isSoft() {
        return true;
    }

    @Nullable
    public PrefixedName getQName() {
        final ASTNode[] nodes = getNode().getChildren(QNAME_FILTER);
        if (nodes.length == 1) {
            return new PrefixedNameImpl(nodes[0]);
        } else if (nodes.length == 2) {
            return new PrefixedNameImpl(nodes[0], nodes[1]);
        }
        return null;
    }

    public int hashCode() {
        final QName name = ContextProvider.getContextProvider(this).getQName(this);
        return name != null ? name.hashCode() : getReferencedName().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        final XPathVariableReference ref = (XPathVariableReference)obj;
        final XPathVariable variable = resolve();
        if (variable != null) return variable == ref.resolve();

        final QName name = ContextProvider.getContextProvider(this).getQName(this);
        if (name != null) {
            return Comparing.equal(name, ContextProvider.getContextProvider(ref).getQName(ref));
        }
        return Comparing.equal(getReferencedName(), ref.getReferencedName());
    }
}