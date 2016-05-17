import GUT.qual.*;

public class B{

    void foo(){
        A a = new  A();
        Object ob = a.o;
    }

    class A{

    Object o = new Object();
    }
}
