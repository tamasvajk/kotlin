/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.references.base

import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtSymbolBasedReference
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.idea.references.unwrappedTargets
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtImportAlias
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext

interface KtFe10Reference : KtReference, KtSymbolBasedReference {
    override val resolver: ResolveCache.PolyVariantResolver<KtReference>
        get() = KtFe10PolyVariantResolver

    fun resolveToDescriptors(bindingContext: BindingContext): Collection<DeclarationDescriptor> = getTargetDescriptors(bindingContext)

    fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor>

    fun isReferenceToImportAlias(alias: KtImportAlias): Boolean {
        val importDirective = alias.importDirective ?: return false
        val importedFqName = importDirective.importedFqName ?: return false
        val helper = KtFe10ReferenceResolutionHelper.getInstance()
        val importedDescriptors = helper.resolveImportReference(importDirective.containingKtFile, importedFqName)
        val importableTargets = unwrappedTargets.mapNotNull {
            when {
                it is KtConstructor<*> -> it.containingClassOrObject
                it is PsiMethod && it.isConstructor -> it.containingClass
                else -> it
            }
        }

        val project = element.project
        val resolveScope = element.resolveScope

        return importedDescriptors.any {
            helper.findPsiDeclarations(it, project, resolveScope).any { declaration ->
                declaration in importableTargets
            }
        }
    }

    override fun KtAnalysisSession.resolveToSymbols(): Collection<KtSymbol> {
        require(this is KtFe10AnalysisSession)
        val bindingContext = KtFe10ReferenceResolutionHelper.getInstance().partialAnalyze(element)
        return getTargetDescriptors(bindingContext).mapNotNull { descriptor ->
            descriptor.toKtSymbol(analysisContext)
        }
    }
}
