package eu.luminis.devconrob;
import java.io.IOException;

import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

public class StepperMotor {

    GPIOPin in1Pin;
    GPIOPin in2Pin;
    GPIOPin in3Pin;
    GPIOPin in4Pin;

    private static int STEPS_PER_REVOLUTION = 2048;

    public StepperMotor(int in1PinNumber, int in2PinNumber, int in3PinNumber, int in4PinNumber) throws IOException {
        in1Pin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, in1PinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        in1Pin.setValue(false);
        in2Pin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, in2PinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        in2Pin.setValue(false);
        in3Pin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, in3PinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        in3Pin.setValue(false);
        in4Pin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, in4PinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        in4Pin.setValue(false);
    }

    // only sequence that seems to work: https://arduino-info.wikispaces.com/SmallSteppers
    private boolean[][] stepSequence = new boolean[][] {
            // {false, false, false, true },
            { false, false, true, true },
            // {false, false, true, false},
            { false, true, true, false },
            // {false, true, false, false},
            { true, true, false, false },
            // {true, false, false, false},
            { true, false, false, true } };

    public int position = 0;

    public int turn(int steps, boolean forward) throws IOException, InterruptedException {
        int sequenceCount = stepSequence.length;
        int direction = forward ? 1 : -1;

        in1Pin.setValue(false);
        in2Pin.setValue(false);
        in3Pin.setValue(false);
        in4Pin.setValue(false);

        for (int i = 0; i < steps; i++) {

            boolean[] step = stepSequence[position % sequenceCount];

            in1Pin.setValue(step[0]);
            in2Pin.setValue(step[1]);
            in3Pin.setValue(step[2]);
            in4Pin.setValue(step[3]);

            position += direction;

            if (position >= STEPS_PER_REVOLUTION) {
                position = 0;
            } else if (position < 0) {
                position = STEPS_PER_REVOLUTION-1;
            }
            Thread.sleep(2); //28byj-48 skips steps if delay is too short

        }
        return position;
    }

    public void reset() throws IOException, InterruptedException {
        if (position > STEPS_PER_REVOLUTION / 2) {
            turn(STEPS_PER_REVOLUTION - position, true);
        } else {
            turn(position, false);
        }

        
    }
    public void shutdown() throws IOException {
        
        try {
            reset();
        } catch (InterruptedException e) {
        }

        in1Pin.setValue(false);
        in2Pin.setValue(false);
        in3Pin.setValue(false);
        in4Pin.setValue(false);
    }
}