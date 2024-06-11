import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class Actions {


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
    //private static EV3UltrasonicSensor ultrasonicSensor;
    //private static SampleProvider ultrasonicProvider;

    static {
        try {
            leftColorSensor = new EV3ColorSensor(SensorPort.S1);
            rightColorSensor = new EV3ColorSensor(SensorPort.S2);
            LEFTcolorProvider = leftColorSensor.getColorIDMode();
            RIGHTcolorProvider = rightColorSensor.getColorIDMode();

            gyroSensor = new EV3GyroSensor(SensorPort.S4);
            gyroAngleProvider = gyroSensor.getAngleMode();

            //ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
            //ultrasonicProvider = ultrasonicSensor.getDistanceMode();
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

    public static void findLineWithGyro() throws InterruptedException {
        resetGyro();
        float startposHeading = getHeading();
        float targetLeftHeading = startposHeading + 100;
        float targetRightHeading = startposHeading - 100;
        if(predictLeft) {
            if(searchLineLeft(targetLeftHeading, startposHeading)) {
                return;
            }
            if(searchLineRight(targetRightHeading, startposHeading)) {
                return;
            }
        } else if(predictRight) {
            if(searchLineRight(targetRightHeading, startposHeading)) {
                return;
            }
            if(searchLineLeft(targetLeftHeading, startposHeading)) {
                return;
            }
        } else {
            if(searchLineLeft(targetLeftHeading, startposHeading)) {
                return;
            }
            if(searchLineRight(targetRightHeading, startposHeading)) {
                return;
            }
        }

        stop();
        System.out.println("Line not found");
    }
    public static boolean searchLineLeft(float targetLeftHeading, float startposHeading) throws InterruptedException {
        while (getHeading() < targetLeftHeading) {
            turnLeft(100);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                stop();
                System.out.println("Line found on the left");
                predictLeft = true;
                predictRight = false;
                return true;
            }
            Thread.sleep(50);

            while (getHeading() > startposHeading) {
                if (Math.abs(getHeading() - startposHeading) < 5)
                    turnRight(turnRightSpeedSlow);
                else
                    turnRight(turnRightSpeedFast);
                Thread.sleep(50);
            }
        }
        return false;
    }

    public static boolean searchLineRight(float targetRightHeading, float startposHeading) throws InterruptedException {
        while (getHeading() > targetRightHeading) {
            turnRight(turnRightSpeedFast);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                stop();
                System.out.println("Line found on the right");
                predictRight = true;
                predictLeft = false;
                return true;
            }
            Thread.sleep(50);

            while (getHeading() < startposHeading) {
                if (Math.abs(getHeading() - startposHeading) < 5)
                    turnLeft(turnLeftSpeedSlow);
                else
                    turnLeft(turnLeftSpeedFast);
                Thread.sleep(50);
            }
        }
        return false;
    }



    public static void wiggle() throws InterruptedException
    {
        for (int i = 0; i < 5; i++) {
            forward(forwardSpeed);
            Thread.sleep(70);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                stop();
                return;
            }
            turnLeft(40);
            Thread.sleep(20);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                stop();
                return;
            }
            Thread.sleep(50);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                stop();
                return;
            }
            turnRight(40);
            Thread.sleep(60);
            if (getLEFTColorID() == lineColor || getRIGHTColorID() == lineColor) {
                forward(forwardSpeed);
                stop();
                return;
            }

            if (getLEFTColorID() != lineColor && getRIGHTColorID() == lineColor)
            {
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
            } else if (Actions.getLEFTColorID() == Color.BLACK && Actions.getRIGHTColorID() != Color.BLACK) {
                Actions.turnLeft(Actions.getTurnLeftSpeedFast());
            } else if (Actions.getLEFTColorID() != Color.BLACK && Actions.getRIGHTColorID() == Color.BLACK) {
                Actions.turnRight(Actions.getTurnRightSpeedFast());
            } else {
                try {
                    wiggleAttempts++;
                    Actions.wiggle();
                    if (wiggleAttempts >= 2) {
                        Actions.findLineWithGyro();
                        wiggleAttempts = 0;
                    }
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

    /*public static float getDistance() {
        float[] sample = new float[ultrasonicProvider.sampleSize()];
        ultrasonicProvider.fetchSample(sample, 0);
        return sample[0];
    }*/

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
