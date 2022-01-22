package typecheck.topol;

public class Boxing {
    Integer I = 5;

    void m() {
        Integer I = 3;
        int i = 5;
        I = i;
        i = I;
    }

    void mrep() {
        Integer I = 3;
        int i = 5;

        I = i;
        i = I;
    }

    void k(){
        int i;
    }

    void ops() {

        I += 9;
        I += 3;
    }

}
