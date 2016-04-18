package eu.luminis.devconrob;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import static eu.luminis.devconrob.SleepUtil.sleep;

public class LED {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private GPIOPin ledPin;
    private Future<?> thread;
    
    public LED(int pinNumber) throws IOException {
        ledPin = (GPIOPin) DeviceManager.open(new GPIOPinConfig(DeviceConfig.DEFAULT, pinNumber,
                GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
    }

    public void blink(long interval, long times) {
        // interrupt active blinking thread
        if (thread != null && !thread.isDone()) {
            thread.cancel(true);
        }

        // start new blinking thread
        thread = executorService.submit(() -> {
        	try {
			    ledPin.setValue(false);
			    LongStream.range(0, times*2).forEach((n) -> {
			        try {
			            ledPin.setValue(!ledPin.getValue());
			        } catch (IOException e) {
			            throw new RuntimeException(e);
			        }
			        sleep(interval);
			    });
			    ledPin.setValue(false);
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
        });
    }

    public void shutdown() throws IOException {
        ledPin.setValue(false);
        ledPin.close();
        executorService.shutdownNow();
    }
}