public class GyrosensorTest {
    public static void main(String[] args) {

        float heading = Actions.getHeading();

        System.out.println("Starting");

        while (true) {
            float currentHeading = Actions.getHeading();
            float difference = Math.abs(currentHeading - heading);

            if (currentHeading > heading) {
                if (difference > 5) {
                    Actions.turnRight(300);  // Faster turn
                } else {
                    Actions.turnRight(40);   // Slower turn
                }
            } else if (currentHeading < heading) {
                if (difference > 5) {
                    Actions.turnLeft(300);   // Faster turn
                } else {
                    Actions.turnLeft(40);    // Slower turn
                }
            }

            if (currentHeading == heading) {
                System.out.println("At the start position");
                Actions.stop();  // Stop the motors when the heading is at the target
            }
        }
    }
}
