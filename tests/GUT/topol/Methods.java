package GUT.topol;

import GUT.quals.Any;
import GUT.quals.Peer;
import GUT.quals.Rep;

/**
 *
 * @author wmdietl
 */
public class Methods {

    //:: error: (uts.receiver.annotations.forbidden)
    void receiver(@Any Methods this) {
    }

    @Peer Object m(@Peer Object o) { return null; }

    @Rep Object foo(@Peer Object o) { return null; }

    void ok() {
        @Peer Methods p = new Methods();
        @Peer Object mo = p.m( new @Peer Object() );

        @Any Object ma = p.foo( new @Peer Object() );

        @Rep Methods r = null;
        @Rep Object ro = r.m( new @Rep Object() );

        @Any Object ra = r.foo( new @Rep Object() );
    }

    void errors() {
        @Peer Methods p = null;
        //:: error: (assignment.type.incompatible)
        @Rep Object mo = p.m( new @Peer Object() );
        //:: error: (argument.type.incompatible)
        @Peer Object mo2 = p.m( new @Rep Object() );

        //:: error: (assignment.type.incompatible)
        @Rep Object ma = p.foo( new @Peer Object() );

        @Rep Methods r = null;

        //:: error: (argument.type.incompatible)
        @Any Object ro = r.m( new @Peer Object() );

        //:: error: (assignment.type.incompatible)
        @Peer Object ro2 = r.m( null );

        //:: error: (argument.type.incompatible)
        @Any Object ra = r.foo( new @Peer Object() );

        @Any Methods a = null;
        //:: error: (uts.lost.parameter)
        a.m(new @Peer Object());
    }

    class SubMethods1 extends Methods {
        //:: error: (override.return.invalid)
        @Rep Object m(@Peer Object o) { return null; }
    }

    class SubMethods2 extends Methods {
        //:: error: (override.param.invalid)
        @Peer Object m(@Rep Object o) { return null; }
    }

    class SubMethods3 extends Methods {
        @Peer Object m(@Peer Object o) { return null; }
    }
}
