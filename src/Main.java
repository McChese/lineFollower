import lejos.hardware.ev3.LocalEV3;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {

    System.out.println("Starting...");
    Actions.followLine();

    if (LocalEV3.get().getKey("Escape").isDown())
    {
        Actions.stopFollowLine();
    }

    }
}
