import love.forte.plugin.suspendtrans.sample.ForteScarlet;
import love.forte.plugin.suspendtrans.sample.Scarlet;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final ForteScarlet forte = new ForteScarlet();
        System.out.println(forte.nameBlocking());
        System.out.println(forte.selfAsync().get());
        System.out.println(forte.getAgeBlocking());

        Scarlet s = forte;
        System.out.println(s.selfBlocking());
    }
}
