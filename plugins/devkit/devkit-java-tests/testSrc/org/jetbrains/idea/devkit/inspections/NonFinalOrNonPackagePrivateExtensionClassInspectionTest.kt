// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.devkit.inspections

import com.intellij.testFramework.TestDataPath
import org.jetbrains.idea.devkit.DevkitJavaTestsUtil
import org.jetbrains.idea.devkit.inspections.quickfix.LightDevKitInspectionFixTestBase

@TestDataPath("\$CONTENT_ROOT/testData/inspections/nonFinalOrNonPackagePrivateExtensionClass")
class NonFinalOrNonPackagePrivateExtensionClassInspectionTest : LightDevKitInspectionFixTestBase() {

  override fun getBasePath() = DevkitJavaTestsUtil.TESTDATA_PATH + "inspections/nonFinalOrNonPackagePrivateExtensionClass/"
  override fun getFileExtension() = "java"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(NonFinalOrNonInternalExtensionClassInspection())
    myFixture.copyDirectoryToProject("resources", "resources")
  }

  fun testMakeFinal() {
    doTest("Make 'MyInspection' final")
  }

  fun testMustBePackagePrivate() {
    doTest("Make 'MyInspection' not public")
  }

  fun testPackagePrivateFinalExtensionClass() {
    doTest()
  }
}