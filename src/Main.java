import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.Color;

public class Main {

    private static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
    private static int wiggleAttempts = 0;

    public static void main(String[] args) throws InterruptedException {

        while (true) {
            if (Actions.getLEFTColorID() == Color.BLACK && Actions.getRIGHTColorID() == Color.BLACK) {
                Actions.forward(300);
            } else if (Actions.getLEFTColorID() == Color.BLACK && Actions.getRIGHTColorID() != Color.BLACK) {
                Actions.turnLeft(200);
            } else if (Actions.getLEFTColorID() != Color.BLACK && Actions.getRIGHTColorID() == Color.BLACK) {
                Actions.turnRight(200);
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
}
