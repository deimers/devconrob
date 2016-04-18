package eu.luminis.devconrob;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import static eu.luminis.devconrob.SleepUtil.sleep;

public class DistanceSensor extends Sensor<Double> {

    private int PULSE_NS = 10000;
    private int SPEEDOFSOUND_CM_S = 34000;

    private GPIOPin triggerPin;
    private GPIOPin echoPin;

    public DistanceSensor(int triggerPinNumber, int echoPinNumber) throws IOException {
        triggerPin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, triggerPinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
        echoPin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, echoPinNumber,
                GPIOPinConfig.DIR_INPUT_ONLY, GPIOPinConfig.MODE_INPUT_PULL_UP, GPIOPinConfig.TRIGGER_NONE, false));
    }

    @Override
    public Double sense() throws IOException {

        // trigger sensor
        triggerPin.setValue(true);
        sleep(0, PULSE_NS);
        triggerPin.setValue(false);

        // measure start and stop of the signal on echo line 
        //  with a safe guard against looping forever
        long signalStart = 0;
        for (int i = 0; i < 1000 && !echoPin.getValue(); i++) {
            signalStart = System.nanoTime();
        }
        long signalStop = 0;
        for (int i = 0; i < 1000 && echoPin.getValue(); i++) {
            signalStop = System.nanoTime();
        }

        if (signalStop == 0 || signalStart == 0) {
            return null;
        }

        long distance = (signalStop - signalStart) * SPEEDOFSOUND_CM_S;

        return new Double(distance / 2.0 / (1000000000L)); // cm/s
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Double sense(long msTimeout) {
        Callable<Double> callableSense = this::sense;
      
        Future<Double> future = executorService.submit(callableSense);
        
        try {
            return future.get(msTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    @Override
    public void shutdown() throws IOException {
        echoPin.close();
        triggerPin.close();
        executorService.shutdownNow();
        super.shutdown();
    }

}