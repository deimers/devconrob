import java.io.IOException;

import eu.luminis.devconrob.DistanceSensor;
import eu.luminis.devconrob.LED;
import eu.luminis.devconrob.RingBuffer;
import eu.luminis.devconrob.Servo;
import eu.luminis.devconrob.Sensor;

public class MainMapper {

    static class AngleDistanceTuple {
        
        AngleDistanceTuple(int angle, Double distance) {
            this.angle = angle;
            this.distance = distance;
        }

        Double distance;
        int angle;
    }

    static class SweepingHCSR04 extends Sensor<AngleDistanceTuple> {

        Servo servoMotor;
        DistanceSensor distanceSensor;

        public SweepingHCSR04() throws IOException {
            servoMotor = new Servo(0, 0.58, 1.36, 2.32, 2.33, 100);
            distanceSensor = new DistanceSensor(19, 16);
        }

        private static final int SWEEP_PATTERN[] = { 0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180, 
            165, 150, 135, 120, 105, 90, 75, 60, 45, 30, 15 };
        private int currentSweep = 0;

        @Override
        public AngleDistanceTuple sense() throws IOException {
            int direction = SWEEP_PATTERN[currentSweep];

            servoMotor.moveTo(direction);

            Double distance = distanceSensor.sense(40);

            if (currentSweep++ >= SWEEP_PATTERN.length - 1) {
                currentSweep = 0;
            }

            return new AngleDistanceTuple(direction, distance);
        }

        @Override
        public void shutdown() throws IOException {
            super.shutdown();
            distanceSensor.shutdown();
            servoMotor.shutdown();
        }

    }

    public static void main(String args[]) throws IOException, InterruptedException {

        LED led = new LED(21);
        SweepingHCSR04 sweepingDistanceSensor = new SweepingHCSR04();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    sweepingDistanceSensor.shutdown();
                    led.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sweepingDistanceSensor.addSensorCallback((AngleDistanceTuple dd) -> {
            return dd.distance != null && dd.distance <= 50;
        }, (AngleDistanceTuple dd) -> {

            led.blink(50, 1);
            
            try {
                showMap(100, 50, dd);
            } catch (Throwable t) {
                    
            }
            
        });

        sweepingDistanceSensor.start();

        System.out.println("Warming up..");
        System.out.println("Go Go GO!..");
        Thread.sleep(120 * 1000);

        System.out.println("Awake..");
    }

    
    static RingBuffer<AngleDistanceTuple> buffer = new RingBuffer<>(5);
    static void showMap(int width, int height, AngleDistanceTuple ad) {
        buffer.add(ad);
        String[][] array = new String[height + 1][width + 1];
        int origin = width / 2;

        for(AngleDistanceTuple add: buffer.elements()) {
            int px = (int) Math.round(Math.cos(Math.toRadians(add.angle)) * add.distance) + origin;
            int py = (int) Math.round(Math.sin(Math.toRadians(add.angle)) * add.distance);
            array[py][px] = "x";
        }

        String map = "";
        for (int x = 0; x < array.length; x++) {
            String line = "";
            for (int y = 0; y < array[x].length; y++) {
                line += (array[x][y] == null ? "." : array[x][y]);
            }
            map = line + System.lineSeparator() + map;
        }

        System.out.println(map);

    }

}
