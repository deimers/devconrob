import java.io.IOException;

import eu.luminis.devconrob.LED;

public class TestLED {
    public static void main(String args[]) throws IOException, InterruptedException {

        LED led = new LED(21);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    led.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        led.blink(250, 5);
        
        System.out.println("Blinking..");
        
        Thread.sleep(5000);

        System.out.println("Bye.");
        
    }

}
