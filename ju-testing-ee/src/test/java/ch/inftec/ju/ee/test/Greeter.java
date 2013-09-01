package ch.inftec.ju.ee.test;

import java.io.PrintStream;

/**
 * Greeter class from the Arquillain getting started guide.
 * @author Martin
 *
 */
public class Greeter {
    public void greet(PrintStream to, String name) {
        to.println(createGreeting(name));
    }

    public String createGreeting(String name) {
        return "Hello, " + name + "!";
    }
}