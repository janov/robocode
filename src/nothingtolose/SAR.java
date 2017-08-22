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
import robocode.util.Utils;

/**
 * SAR robot implementation.
 * 
 * @author SAR
 *
 */
public class SAR extends TeamRobot {

	private double moveAmount;
	private boolean radarLocked;

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
		out.printf("wall hit ai dza, bearing %s, heading %s %s", event.getBearing(), this.getHeading(),
				System.getProperty("line.separator"));
	}

	@Override
	public void run() {
		this.setColors(new Color(66, 13, 171), Color.RED, Color.YELLOW);

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		while (true) {
			out.println("action begin");

			
//			setTurnLeft(50);
//			ahead(moveAmount);

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

		boolean isCurrentTarget = true;
		double absoluteBearing = getHeadingRadians() + event.getBearingRadians();
		if (isCurrentTarget && getGunHeat() < 0.5) // Lock for 5 ticks
		{
			setTurnRadarRightRadians(3.5 * Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
			setTurnGunRightRadians(3.5 * Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
			
			// Fire a bullet with maximum power if the gun is ready
			if (getGunHeat() == 0) {
				setFire(2);
			}
		}
		else
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		
	}

}
