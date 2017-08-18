package ntl;

import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class RobotA extends Robot {

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// Fire a bullet with maximum power if the gun is ready
		   if (getGunHeat() == 0) {
		       fire(Rules.MAX_BULLET_POWER);
		   }
		   System.out.println("firing");
	}

	@Override
	public void run() {
		turnLeft(getHeading() % 90);
		turnGunRight(90);
		
		while (true) {
			ahead(1000);
			turnRight(90);
		}
	}

}
