package universe;

import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

public class UniverseInferenceCheckerTestsLostNo extends CheckerFrameworkPerDirectoryTest {
    public UniverseInferenceCheckerTestsLostNo(List<File> testFiles) {
        super(testFiles, UniverseInferenceChecker.class, "", "-Anomsgtext", "-d", "testTmp");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"typecheck/lostno"};
    }
}
