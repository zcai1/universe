package universe;

import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

public class UniverseInferenceCheckerTestsTopology extends CheckerFrameworkPerDirectoryTest {
    public UniverseInferenceCheckerTestsTopology(List<File> testFiles) {
        super(
                testFiles,
                UniverseInferenceChecker.class,
                "",
                "-Anomsgtext",
                "-AconservativeUninferredTypeArguments",
                "-d",
                "testTmp");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"typecheck/topol"};
    }
}
