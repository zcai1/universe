import universe.qual.Peer;
import universe.qual.Bottom;
import universe.qual.Rep;
import universe.qual.*;
import java.util.List;

public class Member {

    void foo(@Rep E e) {
        // :: fixable-error: (assignment.type.incompatible)
        @Rep List<@Rep Object> l = e.le;
    }

    class E {
        @Bottom List<@Peer Object> le;
    }

}
