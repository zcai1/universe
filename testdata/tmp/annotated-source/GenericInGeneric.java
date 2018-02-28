import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Self;
import universe.qual.Rep;
import java.util.*;

public class GenericInGeneric{

    void foo() {
        @Rep F f = new @Rep F<@Bottom String, @Self G<@Bottom String>>();
    }

    class G<@Bottom T extends @Bottom Object> {
    }

    class F<@Any Y extends @Any Object, @Bottom X extends @Any G<@Any Y>>{

    }

}
