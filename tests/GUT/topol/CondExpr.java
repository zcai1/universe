package GUT.topol;

import GUT.quals.*;

public class CondExpr {

    void m() {
        @Peer Object o = true ? new @Peer Object() : this;
    }
}