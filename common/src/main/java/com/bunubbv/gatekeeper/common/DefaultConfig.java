package com.bunubbv.gatekeeper.common;

public class DefaultConfig {
    public String answer;
    public String question;
    public String welcomeMessage;
    public String correctMessage;
    public String incorrectMessage;
    public String kickMessage;
    public int kickDelay;

    public static DefaultConfig defaultConfig() {
        DefaultConfig c = new DefaultConfig();
        c.answer = "16";
        c.question = "Evaluate the definite integral of four with respect to x, from zero to four.";
        c.welcomeMessage = "Welcome! Please answer the following question to join the server:";
        c.correctMessage = "Correct! You can now play on the server.";
        c.incorrectMessage = "Incorrect answer. Please try again.";
        c.kickMessage = "You didn't respond within the time limit.";
        c.kickDelay = 3;
        return c;
    }
}
