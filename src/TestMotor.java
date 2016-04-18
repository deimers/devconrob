import java.io.IOException;

import eu.luminis.devconrob.Motor;

public class TestMotor {
    public static void main(String args[]) throws IOException, InterruptedException {

        Motor motorLeft = new Motor(17, 23);
        Motor motorRight = new Motor(22, 27);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    motorLeft.shutdown();
                    motorRight.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        System.out.println("Links vooruit..");
        
        motorLeft.forward();
        motorRight.stop();
        Thread.sleep(2000);

        System.out.println("Links achteruit..");
        motorLeft.reverse();
        motorRight.stop();
        Thread.sleep(2000);
        

        System.out.println("Rechts vooruit..");
        motorRight.forward();
        motorLeft.stop();
        Thread.sleep(2000);

        System.out.println("Rechts achteruit..");
        motorRight.reverse();
        motorLeft.stop();
        Thread.sleep(2000);

        System.out.println("Awake..");
    }

}
