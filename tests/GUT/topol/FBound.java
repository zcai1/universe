package GUT.topol;

import GUT.quals.*;

public class FBound<B extends FBound<B>> {

}

class MyComprator<T extends MyComparable<T>> {
    public void compare(T a1, T a2) {
        a1.compareTo(a2);
    }
}

class MyComparable<S> {
    public int compareTo(S a1) { return 0; }
}
