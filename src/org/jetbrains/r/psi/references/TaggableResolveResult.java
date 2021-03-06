// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi.references;

import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Not used yet but may help to do more specific reporting for resovable symbols which are not visible in a given context.
 *
 * @author Holger Brandl
 */
class TaggableResolveResult extends PsiElementResolveResult {

    enum ResolveTag {ForwardReference, ModuleReference}


    private Set<ResolveTag> tags = Sets.newHashSet();


    public TaggableResolveResult(@NotNull PsiElement element) {
        super(element);
    }


    public void addTag(@NotNull ResolveTag tag) {
        tags.add(tag);
    }


    public Set<ResolveTag> getTags() {
        return tags;
    }
}
