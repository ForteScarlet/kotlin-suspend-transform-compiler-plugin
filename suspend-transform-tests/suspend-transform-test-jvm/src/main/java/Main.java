import suspendtrans.test.JvmFoo;

public class Main {
    public static void main(String[] args) {
        JvmFoo foo = new JvmFoo();
        System.out.println(foo);
        System.out.println(foo.getValueBlocking());
        System.out.println(foo.getValueAsync());
    }
}
