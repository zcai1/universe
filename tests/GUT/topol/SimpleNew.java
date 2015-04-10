package GUT.topol;

import GUT.quals.Any;
import GUT.quals.Peer;
import GUT.quals.Rep;

/**
 *
 * @author wmdietl
 */
public class SimpleNew {
    // OK
    @Peer Object p = new @Peer Object();
    @Rep Object r = new @Rep Object();

    //:: error: (uts.new.ownership)
    @Any Object a = new @Any Object();
}
