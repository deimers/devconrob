import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.luminis.devconrob.DistanceSensor;
import eu.luminis.devconrob.LED;

public class TestDistanceSensor {
	
	public static void main(String args[]) throws IOException, InterruptedException {

		DistanceSensor distanceSensor = new DistanceSensor(19, 16);
		LED led = new LED(21);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown hook called");
				try {
					distanceSensor.shutdown();
					led.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// average of latest 3, remove min and max,
		distanceSensor.enableNoiseReduction(5, values -> {
				Collections.sort(values);
				return values.subList(1, values.size() - 1)
						.stream()
						.mapToDouble(x -> x)
						.average()
						.getAsDouble();
			});

		Predicate<Double> filter = d -> {
			return d != null && d < 50;
		};
		
		Consumer<Double> callback = d -> {
			led.blink(100, 1);
			System.out.println(d);
		};
		
		distanceSensor.addSensorCallback(filter, callback);
		
		distanceSensor.start(100);

		Thread.sleep(25000);
	}

}
