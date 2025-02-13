

package love.forte.plugin.suspendtrans.runners;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link love.forte.plugin.suspendtrans.GenerateTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("src/testData/codegen")
@TestDataPath("$PROJECT_ROOT")
public class CodeGenTestRunnerGenerated extends AbstractCodeGenTestRunner {
  @Test
  public void testAllFilesPresentInCodegen() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("src/testData/codegen"), Pattern.compile("^(.+)\\.kt$"), null, true);
  }

  @Test
  @TestMetadata("asProperty.kt")
  public void testAsProperty() {
    runTest("src/testData/codegen/asProperty.kt");
  }

  @Test
  @TestMetadata("basic.kt")
  public void testBasic() {
    runTest("src/testData/codegen/basic.kt");
  }

  @Test
  @TestMetadata("override.kt")
  public void testOverride() {
    runTest("src/testData/codegen/override.kt");
  }

  @Test
  @TestMetadata("typeAttr.kt")
  public void testTypeAttr() {
    runTest("src/testData/codegen/typeAttr.kt");
  }

  @Test
  @TestMetadata("typeAttrNested.kt")
  public void testTypeAttrNested() {
    runTest("src/testData/codegen/typeAttrNested.kt");
  }

  @Test
  @TestMetadata("opt.kt")
  public void testOpt() {
    runTest("src/testData/codegen/opt.kt");
  }

  @Test
  @TestMetadata("implOverriden.kt")
  public void testImplOverriden() {
    runTest("src/testData/codegen/implOverriden.kt");
  }

  @Test
  @TestMetadata("implOverridenGeneric.kt")
  public void testImplOverridenGeneric() {
    runTest("src/testData/codegen/implOverridenGeneric.kt");
  }

  @Test
  @TestMetadata("alias.kt")
  public void testAlias() {
    runTest("src/testData/codegen/alias.kt");
  }

  @Test
  @TestMetadata("varargParam.kt")
  public void testVarargParam() {
    runTest("src/testData/codegen/varargParam.kt");
  }
}
