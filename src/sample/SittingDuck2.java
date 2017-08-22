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

import java.awt.Color;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
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
public class SittingDuck2 extends AdvancedRobot {
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

		moveToSide();
		
		execute();

		while (true) {
			setTurnRight(90 * Math.random());
			setAhead(100);

			execute();
		}
	}

	private void moveToSide() {
		double dX = getBattleFieldWidth() - getX();
		double dY = getBattleFieldHeight() - getY();
		
		
		double minX = Math.min(dX, getX());
		double minY = Math.min(dY, getY());
		
		if (minX < minY) {
			// move to Y axis
			if (getX() < dX) 
				moveToLeftSide(getX());
//			else
//				moveToRightSide();
		}
		else {
			// move to X axis
//			if (getY() < dY) 
//				moveToBottomSide();
//			else
//				moveToTopSide();
		}
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
