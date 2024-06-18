import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

import java.io.File;

public class Actions {

    private static boolean lineOnRight;
    private static int linesOnRightFound = 0;

    public static boolean followLine = true;
    private static int wiggleAttempts = 0;

    private static boolean predictLeft;
    private static boolean predictRight;

    private static int lineColor = Color.BLACK;

    private static int forwardSpeed = 400;
    private static int backwardSpeed = 200;

    private static int turnRightSpeedSlow = 50;
    private static int turnLeftSpeedSlow = 50;

    private static int turnRightSpeedFast = 150;
    private static int turnLeftSpeedFast = 150;

    private static EV3ColorSensor leftColorSensor;
    private static EV3ColorSensor rightColorSensor;
    private static SampleProvider LEFTcolorProvider;
    private static SampleProvider RIGHTcolorProvider;
    private static EV3GyroSensor gyroSensor;
    private static SampleProvider gyroAngleProvider;

    static {
        try {
            leftColorSensor = new EV3ColorSensor(SensorPort.S1);
            rightColorSensor = new EV3ColorSensor(SensorPort.S2);
            LEFTcolorProvider = leftColorSensor.getColorIDMode();
            RIGHTcolorProvider = rightColorSensor.getColorIDMode();

            gyroSensor = new EV3GyroSensor(SensorPort.S4);
            gyroAngleProvider = gyroSensor.getAngleMode();
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to initialize color sensors: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void forward(int speed) {
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.B.forward();
        Motor.C.forward();
    }

    public static void backwards(int speed) {
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.B.backward();
        Motor.C.backward();
    }

    public static void turnRight(int speed) {
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.B.forward();
        Motor.C.backward();
    }

    public static void turnLeft(int speed) {
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.B.backward();
        Motor.C.forward();
    }

    public static void stop() {
        Motor.B.stop(true);
        Motor.C.stop(true);
    }

    public static int getLEFTColorID() {
        float[] sample = new float[LEFTcolorProvider.sampleSize()];
        LEFTcolorProvider.fetchSample(sample, 0);
        return (int) sample[0];
    }

    public static int getRIGHTColorID() {
        float[] sample = new float[RIGHTcolorProvider.sampleSize()];
        RIGHTcolorProvider.fetchSample(sample, 0);
        return (int) sample[0];
    }

    private static void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static float getHeading() {
        float[] sample = new float[gyroAngleProvider.sampleSize()];
        gyroAngleProvider.fetchSample(sample, 0);
        return sample[0];
    }

    public static void resetGyro() {
        gyroSensor.reset();
    }

    /*public static void findLineWithGyro() throws InterruptedException {
        resetGyro();
        float startposHeading = getHeading();
        float targetLeftHeading = startposHeading + 100;
        float targetRightHeading = startposHeading - 100;
            if(searchLineLeft(targetLeftHeading, startposHeading)) {
                return;
            }
            if(searchLineRight(targetRightHeading, startposHeading)) {
                return;
            }

        stop();
        System.out.println("Line not found");
    }*/
    public static boolean searchLineLeft(float targetLeftHeading, float startposHeading) throws InterruptedException {
        while (true) {
            turnLeft(turnLeftSpeedFast);
            if (getLEFTColorID() == lineColor) {
                stop();
                System.out.println("Line found on the left");
                return true;
            }
            wait(50);
        }
    }

    public static boolean searchLineRight(float targetRightHeading, float startposHeading) throws InterruptedException {
        while (true) {
            turnRight(turnRightSpeedFast);
            if (getLEFTColorID() == lineColor) {
                stop();
                System.out.println("Line found on the right");
                return true;
            }
            wait(50);
        }
    }



    public static void wiggle() throws InterruptedException
    {
        for (int i = 0; i < 6; i++) {
            forward(forwardSpeed);
            wait(1000);
            if (getLEFTColorID() == lineColor) {
                stop();
                return;
            }
            turnLeft(40);
            wait(50);
            if (getLEFTColorID() == lineColor) {
                stop();
                return;
            }
            wait(50);
            if (getLEFTColorID() == lineColor) {
                stop();
                return;
            }
            turnRight(40);
            Thread.sleep(60);
            if (getLEFTColorID() == lineColor) {
                forward(forwardSpeed);
                stop();
                return;
            }

            if (getLEFTColorID() != lineColor && getRIGHTColorID() == lineColor)
            {
                turnLeft(turnLeftSpeedSlow);
                return;
            }

        }
        stop();
    }

    public static void turnAround()
    {
        backwards(backwardSpeed);
        float heading = getHeading();
        float targetHeading = heading + 180;
        while (getHeading() < targetHeading)
        {
            if (Math.abs(getHeading() - targetHeading) < 5)
            {
                turnLeft(turnLeftSpeedSlow);
            }
            else
            {
                turnLeft(turnLeftSpeedFast);
            }
        }
    }

    public static void followLine()
    {
        followLine = true;

        while (followLine) {
            if (Actions.getLEFTColorID() == Color.BLACK && Actions.getRIGHTColorID() == Color.BLACK) {
                Actions.forward(Actions.getForwardSpeed());
                lineOnRight = true;
            } else if (Actions.getLEFTColorID() == Color.BLACK && Actions.getRIGHTColorID() != Color.BLACK) {
                Actions.forward(Actions.getTurnLeftSpeedFast());
                lineOnRight=false;
                linesOnRightFound++;
            } else if (Actions.getLEFTColorID() != Color.BLACK && Actions.getRIGHTColorID() == Color.BLACK) {
                Actions.turnLeft(Actions.getTurnRightSpeedFast());
                lineOnRight=true;
            } else {
                try {
                    wiggleAttempts++;
                    Actions.wiggle();
                    if (wiggleAttempts >= 4) {
                        resetGyro();
                        float startposHeading = getHeading();
                        float targetLeftHeading = startposHeading + 100;
                        float targetRightHeading = startposHeading - 100;
                        if(lineOnRight) {
                            searchLineRight(targetRightHeading, startposHeading);
                        } else {
                            searchLineLeft(targetLeftHeading, startposHeading);
                        }
                        wiggleAttempts = 0;
                    }
                    wiggleAttempts++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void stopFollowLine()
    {
        followLine = false;
    }



    public static int getTurnRightSpeedSlow() {
        return turnRightSpeedSlow;
    }

    public static void setTurnRightSpeedSlow(int speed) {
        turnRightSpeedSlow = speed;
    }

    public static int getTurnLeftSpeedSlow() {
        return turnLeftSpeedSlow;
    }

    public static void setTurnLeftSpeedSlow(int speed) {
        turnLeftSpeedSlow = speed;
    }

    public static int getTurnRightSpeedFast() {
        return turnRightSpeedFast;
    }

    public static void setTurnRightSpeedFast(int speed) {
        turnRightSpeedFast = speed;
    }

    public static int getTurnLeftSpeedFast() {
        return turnLeftSpeedFast;
    }

    public static void setTurnLeftSpeedFast(int speed) {
        turnLeftSpeedFast = speed;
    }

    public static int getForwardSpeed() {
        return forwardSpeed;
    }

    public static void setForwardSpeed(int speed) {
        forwardSpeed = speed;
    }

    public static int getBackwardSpeed() {
        return backwardSpeed;
    }

    public static void setBackwardSpeed(int backwardSpeed) {
        Actions.backwardSpeed = backwardSpeed;
    }
}