// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.devkit.kotlin.inspections

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.idea.devkit.inspections.DevKitInspectionUtil
import org.jetbrains.idea.devkit.kotlin.DevKitKotlinBundle
import org.jetbrains.idea.devkit.util.locateExtensionsByPsiClass
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.base.resources.KotlinBundle
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

internal class OpenOrNonInternalExtensionClassInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (!DevKitInspectionUtil.isAllowed(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
    return object : KtVisitorVoid() {
      override fun visitClass(klass: KtClass) {
        val nameIdentifier = klass.nameIdentifier ?: return
        val elementName = klass.name ?: return
        val ktLightClass = klass.toLightClass() ?: return
        val openKeyword = klass.modifierList?.getModifier(KtTokens.OPEN_KEYWORD)
        val isOpen = openKeyword != null
        val isInternal = klass.hasModifier(KtTokens.INTERNAL_KEYWORD)
        if (!isOpen && isInternal) return
        if (locateExtensionsByPsiClass(ktLightClass).isEmpty()) return
        if (isOpen) {
          val fix = ChangeModifierFix(elementName, KtTokens.OPEN_KEYWORD, true)
          holder.registerProblem(openKeyword!!, DevKitKotlinBundle.message("inspection.open.or.non.internal.extension.class.should.not.be.open.text"), fix)
        }
        if (!isInternal) {
          val fix = ChangeModifierFix(elementName, KtTokens.INTERNAL_KEYWORD, false)
          holder.registerProblem(nameIdentifier, DevKitKotlinBundle.message("inspection.open.or.non.internal.extension.class.should.be.internal.text"), fix)
        }
      }
    }
  }

  class ChangeModifierFix(
    private val elementName: String,
    @SafeFieldForPreview
    private val modifier: KtModifierKeywordToken,
    private val removeModifier: Boolean = false
  ) : LocalQuickFix {

    override fun getName(): String {
      if (removeModifier) {
        return KotlinBundle.message("remove.modifier.fix", elementName, modifier)
      }
      return KotlinBundle.message("make.0.1", elementName, modifier)
    }

    override fun getFamilyName(): String {
      if (removeModifier) {
        return KotlinBundle.message("remove.modifier.fix.family", modifier)
      }
      return KotlinBundle.message("make.0", modifier)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val modifierListOwner = descriptor.psiElement.getParentOfType<KtModifierListOwner>(true)
                              ?: throw IllegalStateException("Can't find modifier list owner for modifier")
      if (removeModifier) {
        modifierListOwner.removeModifier(modifier)
      }
      else {
        modifierListOwner.addModifier(modifier)
      }
    }
  }
}