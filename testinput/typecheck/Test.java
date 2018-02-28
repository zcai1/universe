import universe.qual.Any;
import universe.qual.Rep;
import universe.qual.Peer;

/*class A<T extends @Any Object> {
    T t;
    A(){

    }
}
public class Test {
    void test() {
        @Rep A<@Rep Object> a = new @Rep A<@Rep Object>();
        @Any Object o = a.t;
    }
}*/
class A {
    //void foo(@Peer Object p) {}
}
public class Test {

    void foo(@Rep A a) {
/*        @Any Object o = new @Rep Object();
        a.foo(o);*/
        Integer[] i = null;
    }
}
