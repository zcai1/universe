import universe.qual.*;

public class Array {

    int @Rep[] pi;
    // :: fixable-error: (assignment.type.incompatible)
    int [] xpi = pi;

}
