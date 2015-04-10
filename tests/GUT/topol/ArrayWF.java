package GUT.topol;

import GUT.quals.Any;
import GUT.quals.Peer;
import GUT.quals.Rep;

public class ArrayWF {
    //:: error: (type.invalid)
    @Peer int[] epi;
    //:: error: (type.invalid)
    @Rep int[] eri;
    //:: error: (type.invalid)
    @Any int[] eai;

    //:: error: (type.invalid)
    @Peer @Rep Object[] pro;

    //:: error: (type.invalid)
    @Peer Object @Rep @Peer[] porp;

    //:: error: (type.invalid)
    @Peer Object @Rep[] @Rep @Peer[] porrp;

    //:: error: (type.invalid)
    @Peer Object [] [] @Rep @Peer[] poaarp;
}