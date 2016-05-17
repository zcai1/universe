import GUT.qual.*;
public class Test<T extends @Bottom Number>{
    @Bottom Object o;
    @Bottom int a;
    @Bottom Object foo(){
        return null;
    }
    @Bottom int bar(){
        return 3;
    }
}
