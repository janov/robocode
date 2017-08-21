package nothingtolose;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

/**
 * SAR robot implementation.
 * 
 * @author SAR
 *
 */
public class SAR2 extends TeamRobot {

	private double moveAmount;

	@Override
	public void broadcastMessage(Serializable message) throws IOException {
		// TODO Auto-generated method stub
		super.broadcastMessage(message);
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
		out.printf("wall hit ai dza, bearing %s, heading %s %s", event.getBearing(), this.getHeading(), System.getProperty("line.separator"));
	}

	@Override
	public void run() {
		this.setColors(new Color(66,13,171), Color.RED, Color.YELLOW);
		
		
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);

		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		
		
		while (true) {
			out.println("action begin");

			setTurnRadarRight(Double.POSITIVE_INFINITY);
			setTurnGunRight(180);
			setTurnLeft(50);
			ahead(moveAmount);
			
			execute();

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
