package fr.eni.concurrent.examples.misc;

/**
 * Created by ljoyeux on 26/06/2017.
 */
public class FinalParam {
    private int a;

    public void setA(int a) {
        this.a = a;
    }

    public void userFinal(final FinalParam p) {
        p.setA(4);

        //p = new FinalParam(); // error
    }
}
