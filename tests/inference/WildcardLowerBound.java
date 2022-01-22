import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class WildcardLowerBound {

    private Set _classNames = new LinkedHashSet();

    Set extract() {
        return true ? _classNames : Collections.EMPTY_SET;
    }
}
