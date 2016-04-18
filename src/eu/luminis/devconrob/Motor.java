package eu.luminis.devconrob;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

public class Motor {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private GPIOPin forwardPin;
    private GPIOPin reversePin;

    public Motor(int forwardPinNumber, int reversePinNumber) throws IOException {
        this.forwardPin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, forwardPinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        this.reversePin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, reversePinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
    }

    public void stop() {
        executorService.execute(() -> {
            try {
                forwardPin.setValue(false);
                reversePin.setValue(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void forward() {
        executorService.execute(() -> {
            try {
                forwardPin.setValue(true);
                reversePin.setValue(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void reverse() {
        executorService.execute(() -> {
            try {
                forwardPin.setValue(false);
                reversePin.setValue(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void shutdown() throws IOException {
        forwardPin.setValue(false);
        reversePin.setValue(false);
        executorService.shutdownNow();
    }
}