/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.references

import com.intellij.psi.PsiElement
import com.intellij.util.SmartList
import org.jetbrains.kotlin.analysis.api.descriptors.references.base.KtFe10Reference
import org.jetbrains.kotlin.analysis.api.descriptors.references.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.idea.references.readWriteAccess
import org.jetbrains.kotlin.load.java.descriptors.JavaPropertyDescriptor
import org.jetbrains.kotlin.plugin.references.SimpleNameReferenceExtension
import org.jetbrains.kotlin.psi.KtImportAlias
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.ImportedFromObjectCallableDescriptor
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor


class KtFe10SimpleNameReference(expression: KtSimpleNameExpression) : KtSimpleNameReference(expression), KtFe10Reference {

    override fun canBeReferenceTo(candidateTarget: PsiElement): Boolean {
        return element.containingFile == candidateTarget.containingFile ||
                KtFe10ReferenceResolutionHelper.getInstance().isInProjectOrLibSource(element, includeScriptsOutsideSourceRoots = true)
    }

    override fun isReferenceToImportAlias(alias: KtImportAlias): Boolean {
        return super<KtFe10Reference>.isReferenceToImportAlias(alias)
    }

    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        return SmartList<DeclarationDescriptor>().apply {
            // Replace Java property with its accessor(s)
            for (descriptor in expression.getReferenceTargets(context)) {
                val sizeBefore = size

                if (descriptor !is JavaPropertyDescriptor) {
                    add(descriptor)
                    continue
                }

                val readWriteAccess = expression.readWriteAccess(true)
                descriptor.getter?.let {
                    if (readWriteAccess.isRead) add(it)
                }
                descriptor.setter?.let {
                    if (readWriteAccess.isWrite) add(it)
                }

                if (size == sizeBefore) {
                    add(descriptor)
                }
            }
        }
    }

    override fun isReferenceToViaExtension(element: PsiElement): Boolean {
        for (extension in element.project.extensionArea.getExtensionPoint(SimpleNameReferenceExtension.EP_NAME).extensions) {
            if (extension.isReferenceTo(this, element)) return true
        }
        return false
    }

    override fun getImportAlias(): KtImportAlias? {
        fun DeclarationDescriptor.unwrap() = if (this is ImportedFromObjectCallableDescriptor<*>) callableFromObject else this

        val name = element.getReferencedName()
        val file = element.containingKtFile
        val importDirective = file.findImportByAlias(name) ?: return null
        val fqName = importDirective.importedFqName ?: return null
        val helper = KtFe10ReferenceResolutionHelper.getInstance()
        val importedDescriptors = helper.resolveImportReference(file, fqName).map { it.unwrap() }
        if (getTargetDescriptors(helper.partialAnalyze(element)).any {
                it.unwrap().getImportableDescriptor() in importedDescriptors
            }) {
            return importDirective.alias
        }
        return null
    }
}
