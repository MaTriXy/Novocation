package robolectricsetup;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

public class NovocationTestRunner extends RobolectricTestRunner {
    public NovocationTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("src/test/resources/robolectricsetup"));
    }
}
