/**
 * Copyright (c) 2001-2017 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 */
package sample;

import static robocode.util.Utils.normalAbsoluteAngle;
import static robocode.util.Utils.normalRelativeAngle;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

/**
 * SittingDuck - a sample robot by Mathew Nelson.
 * <p/>
 * Along with sitting still doing nothing, this robot demonstrates persistency.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Andrew Magargle (contributor)
 */
public class SittingDuck2 extends TeamRobot {


	/**
	 * if teammate detected:
	 * 		setBack 40
	 *		setTurnRight 45
	 *		setAhead 100
	 *		setTurnRight -45
	 *		moveAlongSide()
	 *
	 *	if enemy detected:
	 *		moveAlongSide()
	 *		keep firing at enemy -> on scanned robot event
	 */
	public void onHitRobot(HitRobotEvent event) {
		// TODO Auto-generated method stub
		
		super.onHitRobot(event);
	}


	/** 
	 * setBack 10
	 * setTurnRight 90
	 */
	public void onHitWall(HitWallEvent event) {
		// TODO Auto-generated method stub
		super.onHitWall(event);
	}

	private boolean onRoundStarted = true;

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		out.printf("detected robot: %s %s", event.getName(), System.getProperty("line.separator"));

		// turn gun towards enemy
		double gunTurn = getHeading() + event.getBearing() - getGunHeading();
		setTurnGunRight(Utils.normalRelativeAngleDegrees(gunTurn));

		// lock radar to enemy
		double radarTurn = getHeading() + event.getBearing() - getRadarHeading();
		setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn) * 2);
	}


	public void run() {


		setBodyColor(Color.yellow);
		setGunColor(Color.yellow);

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// rotate radar forever
		setTurnRadarRight(Double.POSITIVE_INFINITY);

		// rotate gun to some random angle
		setTurnGunRight(Math.random() * 100);

//		moveToSide();

		execute();

		while (true) {
//			setTurnRight(90 * Math.random());
//			setAhead(100);

			execute();
		}
	}

	/**
	 * 
	 * 
	 * Define some special spots on battle:
	 * 		when robot X < 500 -> spotX = (1/2 robot size + 5), otherwise spotX = battle width - ((1/2 robot size + 5))
	 *   	when robot Y < 500 -> spotY = (1/2 robot size + 5), otherwise spotY = battle height - ((1/2 robot size + 5))
	 * 
	 * Get robot X, Y
	 * Calculate absolute(X - spotX), absolute(Y - spotY) -> Pick smaller value, this is the distance in pixel robot needs to move  
	 * 
	 * 
	 * 
	 * 
	 * Case absolute(X - spotX) < absolute(Y - spotY)
	 * 		if 0 < robot heading < 180 : setTurnRight(90 - robot heading), direction -1
	 * 		else if 180 < robot heading < 360 : setTurnRight(240 - robot heading), direction 1
	 * 		Now move robot, setAhead( (X - spotX) * direction )
	 * 
	 *  Case absolute(Y - spotY) <= absolute(X - spotX)
	 * 		if (0 < robot heading < 90) : setTurnRight(0 - robot heading), direction -1
	 * 		else if (270 < robot heading < 360) : setTurnRight(360 - robot heading), direction -1
	 * 		else if 90 < robot heading < 270 : setTurnRight(180 - robot heading), direction 1
	 * 		Now move robot, setAhead( (Y - spotY)  * direction )
	 * 
	 * 
	 * Result: robot heading always points to middle area
	 * 
	 * direction:
	 * 		1 	- forward
	 *		-1	- backward
	 */
	private void moveToClosestSide(double x, double Y, double direction) {

	}
	
	/**
	 * Given: robot heading points to middle area
	 * 
	 * Turn heading before moving:
	 * 		if (0 < robot heading < 90) : setTurnRight(0 - robot heading)
	 * 		else if (270 < robot heading < 360) : setTurnRight(360 - robot heading)
	 * 
	 * Now move ahead with desired distance -> use moveToClosestSide(x, y, 1)
	 * 
	 * Then repeat from start.
	 */
	private void moveAlongSide() {
		
	}
	

	private void moveToLeftSide(double distance) {
		setTurnRight(getHeading() % 90);
		setAhead(distance);
	}

	private void onRoundStarted() {
		double sector = 1;

		while ((360 - sector * Rules.RADAR_TURN_RATE) >= 0) {
			setTurnRadarRight(Rules.RADAR_TURN_RATE);
			execute();
		}

		onRoundStarted = false;
	}

}
