import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class Actions {

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

    public static void findLineWithGyro() throws InterruptedException {
        float startingHeading = getHeading();
        float targetLeftHeading = startingHeading - 100;
        float targetRightHeading = startingHeading + 100;

        while (getHeading() > targetLeftHeading) {
            float currentHeading = getHeading();
            float difference = Math.abs(currentHeading - targetLeftHeading);
            if (difference > 12) {
                turnLeft(150);
            } else {
                turnLeft(25);
            }
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                stop();
                return;
            }
            wait(50);
        }

        while (getHeading() < startingHeading) {
            turnRight(50);
            wait(50);
        }

        while (getHeading() < targetRightHeading) {
            float currentHeading = getHeading();
            float difference = Math.abs(currentHeading - targetRightHeading);
            if (difference > 12) {
                turnRight(150);
            } else {
                turnRight(25);
            }
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                stop();
                return;
            }
            wait(50);
        }

        while (getHeading() > startingHeading) {
            turnLeft(40);
            wait(50);
        }

        stop();
        System.out.println("Line not found");
    }


    public static void wiggle() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            forward(250);
            wait(200);
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                stop();
                return;
            }
            turnLeft(70);
            Thread.sleep(10);
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                stop();
                return;
            }
            Thread.sleep(100);
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                stop();
                return;
            }
            turnRight(70);
            Thread.sleep(100);
            if (getLEFTColorID() == Color.BLACK || getRIGHTColorID() == Color.BLACK) {
                forward(250);
                stop();
                return;
            }

            if (getLEFTColorID() != Color.BLACK && getRIGHTColorID() == Color.BLACK) {
                return;
            }

        }
        stop();
    }
}