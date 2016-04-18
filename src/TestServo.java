import java.io.IOException;

import eu.luminis.devconrob.Servo;

public class TestServo {

    public static void main(String args[]) throws IOException, InterruptedException {

        double deg_0 = 0.58;
        double deg_90 = 1.36;
        double deg_180 = 2.32;

        Servo servo = new Servo(0, deg_0, deg_90, deg_180, 2.33, 100);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    servo.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        servo.moveTo(0);
        servo.moveTo(45);
        servo.moveTo(90);
        servo.moveTo(135);
        servo.moveTo(180);
        
        Thread.sleep(500);

        System.out.println("Bye.");

    }

}
