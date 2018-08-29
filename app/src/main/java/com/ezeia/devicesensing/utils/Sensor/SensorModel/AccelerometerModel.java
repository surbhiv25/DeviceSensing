package com.ezeia.devicesensing.utils.Sensor.SensorModel;

import android.content.Context;
import android.hardware.SensorEvent;

import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;

import edu.mit.media.funf.time.TimeUtil;


/**
 *  AccelerometerModel.java
 *  AndroidSensorApp
 *
 *  © 2016 Zühlke Engineering AG. All rights reserved.
 */

public class AccelerometerModel extends AbstractSensorModel {

    private static AccelerometerModel instance = null;
    private AccelerometerModel() {
    }

    public static synchronized AccelerometerModel getInstance(Context ctx) {
        if (instance == null){
            instance = new AccelerometerModel();
        }
        return instance;
    }

    @Override
    protected LinkedHashMap<String,String> getLastDataEntry(SensorEvent event, String dateTime)
    {
        String xAcc = Float.toString(event.values[0]);
        String yAcc = Float.toString(event.values[1]);
        String zAcc = Float.toString(event.values[2]);

        return calclulateData(event.values[0],event.values[1],event.values[2],event.timestamp,event.accuracy);
    }

    private LinkedHashMap<String,String> calclulateData(Float xAcc, Float yAcc, Float zAcc, Long timeStamp, int accuracy)
    {
        LinkedHashMap<String,String> hmapData = new LinkedHashMap<>();
         double prevSecs = 0;
         double prevFrameSecs = 0;
         double frameTimer = 0;
         double[][] frameBuffer = new double[3][3];
         int frameSamples = 0;
         int frameBufferSize = 0;

         double frameDuration = 1.0;
         int fftSize = 128;
         double[] freqBandEdges = {0,1,3,6,10};

         BigDecimal DIFF_FRAME_SECS;
         double NUM_FRAME_SAMPLES = 0;

        double currentSecs = Double.valueOf(timeStamp);
        double x = xAcc;
        double y = yAcc;
        double z = zAcc;

        if (prevSecs == 0)
        {
            prevSecs = currentSecs;
        }
        double diffSecs = currentSecs - prevSecs;
        prevSecs = currentSecs;

        frameBuffer[frameSamples][0] = x;
        frameBuffer[frameSamples][1] = y;
        frameBuffer[frameSamples][2] = z;
        frameSamples ++;
        frameTimer += diffSecs;

        if ((frameTimer >= frameDuration) || (frameSamples == (frameBufferSize - 1))) {

            JsonObject data = new JsonObject();
            double fN = (double)frameSamples;
            if (prevFrameSecs == 0) {
                prevFrameSecs = currentSecs;
            }

            double diffFrameSecs = currentSecs - prevFrameSecs;
            prevFrameSecs = currentSecs;

            DIFF_FRAME_SECS = new BigDecimal(diffFrameSecs).setScale(TimeUtil.MICRO, RoundingMode.HALF_EVEN);
            NUM_FRAME_SAMPLES = frameSamples;

            double meanX = 0, meanY = 0, meanZ = 0;
            for (int j = 0; j < frameSamples; j ++)
                meanX += frameBuffer[j][0];
            meanX /= fN;

            for (int j = 0; j < frameSamples; j ++)
                meanY += frameBuffer[j][1];
            meanY /= fN;

            for (int j = 0; j < frameSamples; j ++)
                meanZ += frameBuffer[j][2];
            meanZ /= fN;

            double accum, ABSOLUTE_CENTRAL_MOMENT_X, ABSOLUTE_CENTRAL_MOMENT_Y, ABSOLUTE_CENTRAL_MOMENT_Z,
                    STANDARD_DEVIATION_X, STANDARD_DEVIATION_Y, STANDARD_DEVIATION_Z,
                    MAX_DEVIATION_X, MAX_DEVIATION_Y, MAX_DEVIATION_Z;

            // Absolute central moment
            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += Math.abs(frameBuffer[j][0] - meanX);
            ABSOLUTE_CENTRAL_MOMENT_X = accum/fN;

            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += Math.abs(frameBuffer[j][1] - meanY);
            ABSOLUTE_CENTRAL_MOMENT_Y = accum/fN;

            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += Math.abs(frameBuffer[j][2] - meanZ);
            ABSOLUTE_CENTRAL_MOMENT_Z = accum/fN;

            // Standard deviation
            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += (frameBuffer[j][0] - meanX)*(frameBuffer[j][0] - meanX);
            STANDARD_DEVIATION_X = Math.sqrt(accum/fN);

            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += (frameBuffer[j][1] - meanY)*(frameBuffer[j][1] - meanY);
            STANDARD_DEVIATION_Y = Math.sqrt(accum/fN);

            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum += (frameBuffer[j][2] - meanZ)*(frameBuffer[j][2] - meanZ);
            STANDARD_DEVIATION_Z = Math.sqrt(accum/fN);

            // Max deviation
            accum = 0;
            for (int j = 0; j < frameSamples; j ++)
                accum = Math.max(Math.abs(frameBuffer[j][0] - meanX),accum);
            MAX_DEVIATION_X = accum;

            for (int j = 0; j < frameSamples; j ++)
                accum = Math.max(Math.abs(frameBuffer[j][1] - meanY),accum);
            MAX_DEVIATION_Y = accum;

            for (int j = 0; j < frameSamples; j ++)
                accum = Math.max(Math.abs(frameBuffer[j][2] - meanZ),accum);
            MAX_DEVIATION_Z = accum;

            hmapData.put("X: ",xAcc.toString());
            hmapData.put("MEAN OF X: ", String.valueOf(meanX));
            hmapData.put("STANDARD DEVIATION OF X: ", String.valueOf(STANDARD_DEVIATION_X));
            hmapData.put("ABSOLUTE CENTRAL MOMENT OF X: ", String.valueOf(ABSOLUTE_CENTRAL_MOMENT_X));
            hmapData.put("MAX DEVIATION OF X: ", String.valueOf(MAX_DEVIATION_X));
            hmapData.put("Y: ",yAcc.toString());
            hmapData.put("MEAN OF Y: ", String.valueOf(meanY));
            hmapData.put("STANDARD DEVIATION OF Y: ", String.valueOf(STANDARD_DEVIATION_Y));
            hmapData.put("ABSOLUTE CENTRAL MOMENT OF Y: ", String.valueOf(ABSOLUTE_CENTRAL_MOMENT_Y));
            hmapData.put("MAX DEVIATION OF Y: ", String.valueOf(MAX_DEVIATION_Y));
            hmapData.put("Z: ",zAcc.toString());
            hmapData.put("MEAN OF Z: ", String.valueOf(meanZ));
            hmapData.put("STANDARD DEVIATION OF Z: ", String.valueOf(STANDARD_DEVIATION_Z));
            hmapData.put("ABSOLUTE CENTRAL MOMENT OF Z: ", String.valueOf(ABSOLUTE_CENTRAL_MOMENT_Z));
            hmapData.put("MAX DEVIATION OF Z: ", String.valueOf(MAX_DEVIATION_Z));
            hmapData.put("ACCURACY: ", String.valueOf(accuracy));
            hmapData.put("TIMESTAMP: ", String.valueOf(timeStamp));
        }
        else
        {
            hmapData.put("X: ",xAcc.toString());
            hmapData.put("Y: ",yAcc.toString());
            hmapData.put("Z: ",zAcc.toString());
            hmapData.put("ACCURACY: ", String.valueOf(accuracy));
            hmapData.put("TIMESTAMP: ", String.valueOf(timeStamp));
        }
        return hmapData;
    }
}
