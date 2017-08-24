/**
 * Copyright (c) 2001-2017 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 */
package nothingtolose;

import java.awt.Color;
import java.awt.Robot;
import java.util.ArrayList;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
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
	private boolean turningEnabled = false;
	private double turnAmount = 0;
	
	private boolean movingEnabled = false;
	private double moveAmount = 0;
	
	private boolean firingEnabled = false;
	
	private EnemyRobot enemy = null;
	private ArrayList<EnemyRobot> enemyList = new ArrayList<EnemyRobot>();
	
	private short radarScansRemaining = 0;
	private boolean closestEnemyDetected = false;
	
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
	 * 		else if 180 < robot heading < 360 : setTurnRight(270 - robot heading), direction 1
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
	private void moveToClosestSide(double x, double y, double direction) {
		double targetX = x < getBattleFieldWidth() / 2 ? Commons.ROBOT_SAFE_LENGTH/2 : getBattleFieldWidth() - Commons.ROBOT_SAFE_LENGTH/2;
		double targetY = y < getBattleFieldHeight() / 2 ? Commons.ROBOT_SAFE_LENGTH/2 : getBattleFieldHeight() - Commons.ROBOT_SAFE_LENGTH/2;
		
		double dX = Math.abs(x - targetX);
		double dY = Math.abs(y - targetY);
		double moveAmount = Math.min(dX, dY); // smaller move amount either on axis X or Y
		
		if (dX < dY) 
		{
			// move horizontal
			double horAngle = targetX < getBattleFieldWidth() / 2 ? 270 : 90; 
			doTurnHeading(Commons.HORIZONTAL, getHeading(), horAngle);
		}
		else
		{
			// move vertical
			double verAngle = targetY < getBattleFieldHeight() / 2 ? 180 : 0;
			doTurnHeading(Commons.VERTICAL, getHeading(), verAngle);
		}
		
		movingEnabled = true;
		this.moveAmount = moveAmount;
	}
	
	
	private void doTurnHeading(short axis, double heading, double angle) {
		double turnDeg = 0;
		
		if (Commons.VERTICAL == axis && angle == 0)
			angle = heading >= 180 ? 360 : 0;

		turnDeg = angle - heading;
		
		turningEnabled = true;
		turnAmount = turnDeg;
	}


	private void moveToClosestSide(double x, double y) {
		moveToClosestSide(x, y, Commons.FORWARD);
	}
	
	private void init() {
		// set color
		setColors(new Color(66, 13, 171), Color.RED, Color.YELLOW);
		
		// let radar and gun turn independently from body
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		
		radarScansRemaining = Commons.DEFAULT_RADAR_SCAN_NUMS;
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
		if (isTeammate(event.getName())) {
			setBack(Commons.DEFAULT_STEP_LENGTH);
			setTurnRight(Commons.DEFAULT_SMALL_TURN_ANGLE);
			setAhead(Commons.DEFAULT_STEP_LENGTH);
			setTurnRight(-Commons.DEFAULT_SMALL_TURN_ANGLE);
			moveAlongSide();
		}
		else 
		{
			moveAlongSide();
			lockRadar(getHeading(), event.getBearing(), getRadarHeading());
			lockGun(getHeading(), event.getBearing(), getGunHeading());
			
			firingEnabled = true;
			enemy.distance = 0;
		}
	}


	/** 
	 * setBack 10
	 * setTurnRight 90
	 */
	public void onHitWall(HitWallEvent event) {
		setBack(Commons.DEFAULT_STEP_LENGTH);
		setTurnRight(Commons.DEFAULT_BIG_TURN_ANGLE);
	}


	@Override
	public void onScannedRobot(ScannedRobotEvent event) {

		if (isTeammate(event.getName())) 
			return;

		// first enemy detected
		if (enemy == null) {
			enemy = new EnemyRobot(event.getName(), event.getBearing(), event.getDistance());
		}

		if (event.getName().equals(enemy.name))
			closestEnemyDetected = false;

		out.printf("detected robot: %s - team mate: %b - distance: %s %s ", event.getName(), isTeammate(event.getName()), event.getDistance(), System.getProperty("line.separator"));
		out.printf("enemy var: %s - radarScansRemaining: %s - closestEnemyDetected: %s  %s", enemy.name, radarScansRemaining, closestEnemyDetected, System.getProperty("line.separator"));
		
		if (closestEnemyDetected)
			return;
		
		// aim and fire when not scanning 360
		if (radarScansRemaining == 0) {		
			// lock radar to enemy
			lockRadar(getHeading(), event.getBearing(), getRadarHeading());
			
			// lock gun at enemy
			lockGun(getHeading(), event.getBearing(), getGunHeading());
			
			// fire
			firingEnabled = true;
		}
		else
		{
			out.printf("[add enemy to list] robot: %s - distance: %s %s", event.getName(), event.getDistance(), System.getProperty("line.separator"));
			enemyList.add(new EnemyRobot(event.getName(), event.getBearing(), event.getDistance()));
		}
	}
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		out.printf("[onRobotDeath] dead robot: %s  %s", event.getName(), System.getProperty("line.separator"));

		radarScansRemaining = Commons.DEFAULT_RADAR_SCAN_NUMS;

		firingEnabled = false;
		
	}
	

	private void scanAndUpdateClosestEnemy() {
		if (radarScansRemaining == 0) {
			return;
		}
		
		out.printf("[scanAndUpdateClosestEnemy] scanning 360: time %s %s", radarScansRemaining, System.getProperty("line.separator"));
		
		setTurnRadarRight(Rules.RADAR_TURN_RATE);
		
		radarScansRemaining -= 1; // decrease scans remaining by 1
		
		closestEnemyDetected = false;
		
		// 360 scan done
		if (radarScansRemaining == 0) {
			updateClosestEnemy();
			
			enemyList.clear();
		}
	}
	
	private void updateClosestEnemy() {
		out.println("[updateClosestEnemy]");
		
		if (enemyList.size() > 0) {
			EnemyRobot robot = new EnemyRobot("", 0, Double.MAX_VALUE);
			
			for (int i=0; i < enemyList.size(); i++) {
				if (enemyList.get(i).distance < robot.distance)
					robot = enemyList.get(i);
			}
			
			if (enemy == null) {
				enemy = new EnemyRobot("", 0, 0);
			}
			
			enemy.name = robot.name;
			enemy.distance = robot.distance;
			enemy.bearing = robot.bearing;

			out.printf("[updateClosestEnemy] enemyList size: %s %s", enemyList.size(), System.getProperty("line.separator"));
			out.printf("[updateClosestEnemy] closest enemy: %s - distance: %s %s", enemy.name, enemy.distance, System.getProperty("line.separator"));
			
			// lock radar to enemy
			lockRadar(getHeading(), enemy.bearing, getRadarHeading());
			
			closestEnemyDetected = true;
		}
	}

	private void doFire() {
		if (!firingEnabled)
			return;
		
		if (getGunHeat() == 0 && getGunTurnRemaining() < 30) {
			double power = 0;
			
//			if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY / 10) {
//				// few energy left, just move no fire
//			}
//			else if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY / 5)
//				power = Rules.MAX_BULLET_POWER / 5;
//			else if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY / 3)
//				power = Rules.MAX_BULLET_POWER / 3;
//			else {
//				power = Rules.MAX_BULLET_POWER;
//			}
//			
//			if (enemy.distance > getBattleFieldWidth() * (4/5)) 
//				power /= 2;
			
			if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY / 10) {
			// few energy left, just move no fire
			}
			else 
				power = Rules.MAX_BULLET_POWER;
			
			out.printf("set fire power: %s %s", power, System.getProperty("line.separator"));
			setFire(power);
		}
	}


	private void lockGun(double heading, double targetBearing, double gunHeading) {
		double gunTurn = heading + targetBearing - gunHeading;
		setTurnGunRight(Utils.normalRelativeAngleDegrees(gunTurn) * Commons.RADAR_LOCK_FACTOR);
	}


	private void lockRadar(double heading, double targetBearing, double radarHeading) {
		double radarTurn = heading + targetBearing - radarHeading;
		setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn) * Commons.RADAR_LOCK_FACTOR);
	}


	public void run() {
		init();

		moveToClosestSide(getX(), getY());

		while (true) {
			// scan 360 some time
			if (getTime() % Commons.RADAR_BIG_SCAN_INTERVAL == 0) {
				radarScansRemaining = Commons.DEFAULT_RADAR_SCAN_NUMS;
			}
			
			scanAndUpdateClosestEnemy();
			
			doTurnHeading();

			doMove();
			
//			doFire();
			
			execute();
		}
	}
	

	private void doTurnHeading() {
		if (Math.abs(getTurnRemaining()) > 0 || Math.abs(getDistanceRemaining()) > 0)
			return;

		if (turningEnabled) {
			setTurnRight(turnAmount);
			turningEnabled = !turningEnabled;
		}
	}


	private void doMove() {
		if (Math.abs(getTurnRemaining()) > 0 || Math.abs(getDistanceRemaining()) > 0)
			return;
		
		if (movingEnabled) {
			setAhead(moveAmount);
			movingEnabled = !movingEnabled;
		}
	}


}
