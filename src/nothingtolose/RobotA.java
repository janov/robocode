package nothingtolose;

import java.io.IOException;
import java.io.Serializable;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class RobotA extends TeamRobot {

	@Override
	public void broadcastMessage(Serializable message) throws IOException {
		// TODO Auto-generated method stub
		super.broadcastMessage(message);
	}

	@Override
	public boolean isTeammate(String name) {
		// TODO Auto-generated method stub
		out.println(CommonValues.PACKAGE_NAME);
		return name.toLowerCase().contains(CommonValues.PACKAGE_NAME.toLowerCase());
	}

	@Override
	public void onMessageReceived(MessageEvent event) {
		// TODO Auto-generated method stub
		super.onMessageReceived(event);
	}

	@Override
	public void sendMessage(String name, Serializable message) throws IOException {
		// TODO Auto-generated method stub
		super.sendMessage(name, message);
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		out.println("");
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		// TODO Auto-generated method stub
		super.onBulletHitBullet(event);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		// TODO Auto-generated method stub
		super.onBulletMissed(event);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		out.printf("hit by bullet, fired by robot '%s', remaining energy: %s\n", event.getName(), this.getEnergy());
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		// TODO Auto-generated method stub
		super.onHitRobot(event);
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		// TODO Auto-generated method stub
		super.onHitWall(event);
	}

	@Override
	public void run() {
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);

		while (true) {
			out.println("action begin");

			turnRadarRight(180);
			turnGunRight(180);
			turnRight(180);

			out.println("action end");
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		out.printf("detected robot: '%s' - team mate: %s %s", event.getName(), isTeammate(event.getName()),
				System.getProperty("line.separator"));

		if (isTeammate(event.getName()))
			return;

		// Fire a bullet with maximum power if the gun is ready
		// if (getGunHeat() == 0) {
		// System.out.println("firing");
		// fireBullet(Rules.MAX_BULLET_POWER);
		// }
	}

}
