package eu.luminis.devconrob;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static eu.luminis.devconrob.SleepUtil.sleep;

public class Servo {

    int servoblasterId;

    private double msPulse_0_Degrees;
    private double msPulse_90_Degrees;
    private double msPulse_180_Degrees;
    private double msDelayPerDegree;
    private double msDelayPerMove;

    private int currentAngle = 0;
        
    public Servo(int servoblasterId, double msPulse_0_Degrees, double msPulse_90_Degrees, double msPulse_180_Degrees,
            double msDelayPerDegree, double msDelayPerMove) {
        this.servoblasterId = servoblasterId;
        this.msPulse_0_Degrees = msPulse_0_Degrees;
        this.msPulse_90_Degrees = msPulse_90_Degrees;
        this.msPulse_180_Degrees = msPulse_180_Degrees;
        this.msDelayPerDegree = msDelayPerDegree;
        this.msDelayPerMove = msDelayPerMove;
        moveTo(90);
    }

    public void moveTo(int angle) {
        try (OutputStream out = new FileOutputStream("/dev/servoblaster");
                OutputStreamWriter writer = new OutputStreamWriter(out)) {
            
            double pulseInMs = angle >= 90 ? 
                    (this.msPulse_180_Degrees - this.msPulse_90_Degrees) / 90 * (angle-90) + this.msPulse_90_Degrees : 
                    (this.msPulse_90_Degrees - this.msPulse_0_Degrees) / 90 * angle + this.msPulse_0_Degrees;
            
            long pulseInUnitsOf10Us = Math.round(pulseInMs * 100);
            writer.write(servoblasterId + "=" + pulseInUnitsOf10Us + "\n");
            writer.flush();
            
            sleep(Math.round(Math.abs(currentAngle - angle) * this.msDelayPerDegree + this.msDelayPerMove));
            
            currentAngle = angle;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws IOException {
        moveTo(90);
    }
}