import java.util.ArrayList;

// Based on the example in the GUT paper
public class Person {

    Person spouse;
    Account savings;
    ArrayList<Person> friends;

    Person() {

        friends = new ArrayList<Person>();
    }

    void marry(Person p) {
        spouse = p;
    }

    void befriend(Person p) {
        friends.add(p);
    }

    public int assets() {
        Account a = spouse.savings;
        return savings.balance + a.balance;
    }

    void demo() {
        Person o1 = new Person();
        Person o2 = new Person();
        this.marry(o1);
        o1.befriend(o2);
    }

    public class Account {
        int balance;
    }
}
