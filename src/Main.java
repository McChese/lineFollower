import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.Color;

public class Main {

    private static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
    private static int wiggleAttempts = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting main loop");

        while (true) {
            int leftColor = Actions.getLEFTColorID();
            int rightColor = Actions.getRIGHTColorID();
            System.out.println("Left Color ID: " + leftColor + ", Right Color ID: " + rightColor);

            if (leftColor == Color.BLACK && rightColor == Color.BLACK) {
                System.out.println("Both sensors detect black, moving forward");
                Actions.forward(200);
                Thread.sleep(100);
            } else if (leftColor == Color.BLACK && rightColor != Color.BLACK) {
                System.out.println("Left sensor detects black, right sensor does not, turning left");
                Actions.turnLeft(200);
            } else if (leftColor != Color.BLACK && rightColor == Color.BLACK) {
                System.out.println("Right sensor detects black, left sensor does not, turning right");
                Actions.turnRight(200);
            } else {
                System.out.println("Neither sensor detects black, trying to wiggle");
                try {
                    wiggleAttempts++;
                    Actions.wiggle();
                    if (wiggleAttempts >= 2) {
                        System.out.println("Wiggle attempts exceeded, trying to find line with gyro");
                        Actions.findLineWithGyro();
                        wiggleAttempts = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
