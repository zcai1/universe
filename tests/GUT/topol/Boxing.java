package GUT.topol;

import GUT.quals.*;

public class Boxing {

    void m() {
        Integer I = new Integer(3);
        int i = 5;

        I = i;
        i = I;
    }

    void mrep() {
        @Rep Integer I = new @Rep Integer(3);
        int i = 5;

        I = i;
        i = I;
    }

    void ops() {
        Integer I = 5;
        I += 9;
        //:: error: (compound.assignment.type.incompatible)
        I += new @Rep Integer(3);
    }
}