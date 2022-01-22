package universe;

import java.io.File;
import java.util.List;

import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class UniverseInferenceCheckerTestsLostNo extends CheckerFrameworkPerDirectoryTest {
    public UniverseInferenceCheckerTestsLostNo(List <File> testFiles) {
        super(testFiles, UniverseInferenceChecker.class, "", "-Anomsgtext", "-d", "testTmp");
    }

    @Parameters
    public static String [] getTestDirs(){
        return new String[]{"typecheck/lostno"};
    }
}
