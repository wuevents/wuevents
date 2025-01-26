package org.wuevents;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        try {
           //new Kellerperle().fetch();
           new Cairo().fetch();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
