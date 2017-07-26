package GUTI;
import java.util.*;

public class GenericInGeneric{

    void foo() {
        F f = new F<String, G<String>>();
    }

    class G<T> {
    }

    class F<Y, X extends G<Y>>{

    }

}
