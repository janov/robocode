package nothingtolose;

import java.awt.Color;
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
	
	private EnemyRobot myEnemy = null;
	private ArrayList<EnemyRobot> enemyList = new ArrayList<EnemyRobot>();
	
	private short radarScansRemaining = 0;
	private boolean closestEnemyDetected = false;
	
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {

		if (isTeammate(event.getName())) 
			return;

		// first enemy detected
		if (myEnemy == null) {
			myEnemy = new EnemyRobot(event.getName(), event.getBearing(), event.getDistance());
		}

		out.printf("detected robot: %s - team mate: %b - distance: %s %s ", event.getName(), isTeammate(event.getName()), event.getDistance(), System.getProperty("line.separator"));
		out.printf("enemy var: %s - radarScansRemaining: %s - closestEnemyDetected: %s  %s", myEnemy.name, radarScansRemaining, closestEnemyDetected, System.getProperty("line.separator"));
		
		if (closestEnemyDetected && event.getName().equals(myEnemy.name))
			closestEnemyDetected = false;

		
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
	

	private void doScanAndUpdateClosestEnemy() {
		if (radarScansRemaining == 0) {
			return;
		}
		
		out.printf("[scanAndUpdateClosestEnemy] scanning 360: time %s %s", radarScansRemaining, System.getProperty("line.separator"));
		
		setTurnRadarRight(Rules.RADAR_TURN_RATE);
		
		radarScansRemaining -= 1; // decrease scans remaining by 1
		
		closestEnemyDetected = false;
		
		// 360 scan done
		if (radarScansRemaining == 0) {
			updateClosestEnemy(enemyList);
		}
	}
	
	private void updateClosestEnemy(ArrayList<EnemyRobot> list) {
		out.println("[updateClosestEnemy]");
		
		if (list != null && list.size() > 0) {
			EnemyRobot robot = new EnemyRobot("", 0, Double.MAX_VALUE);
			
			for (int i=0; i < list.size(); i++) {
				if (list.get(i).distance < robot.distance)
					robot = list.get(i);
			}
			
			if (myEnemy == null) {
				myEnemy = new EnemyRobot();
			}
			
			myEnemy.name = robot.name;
			myEnemy.distance = robot.distance;
			myEnemy.bearing = robot.bearing;

			out.printf("[updateClosestEnemy] list size: %s %s", list.size(), System.getProperty("line.separator"));
			out.printf("[updateClosestEnemy] closest enemy: %s - distance: %s %s", myEnemy.name, myEnemy.distance, System.getProperty("line.separator"));
			
			// lock radar to enemy
			lockRadar(getHeading(), myEnemy.bearing, getRadarHeading());
			
			closestEnemyDetected = true;
			list.clear();
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
	private void setMoveToClosestSide(double x, double y) {
		if (!movingEnabled && !turningEnabled) 
		{
		
			double targetX = x < getBattleFieldWidth() / 2 ? Commons.ROBOT_SAFE_LENGTH/2 : getBattleFieldWidth() - Commons.ROBOT_SAFE_LENGTH/2;
			double targetY = y < getBattleFieldHeight() / 2 ? Commons.ROBOT_SAFE_LENGTH/2 : getBattleFieldHeight() - Commons.ROBOT_SAFE_LENGTH/2;
			
			double dX = Math.abs(x - targetX);
			double dY = Math.abs(y - targetY);
			double moveAmount = Math.min(dX, dY); // smaller move amount either on axis X or Y
			
			if (dX < dY) 
			{
				// move horizontal
				double horAngle = targetX < getBattleFieldWidth() / 2 ? 270 : 90; 
				setTurnHeading(Commons.HORIZONTAL, getHeading(), horAngle);
			}
			else
			{
				// move vertical
				double verAngle = targetY < getBattleFieldHeight() / 2 ? 180 : 0;
				setTurnHeading(Commons.VERTICAL, getHeading(), verAngle);
			}
			
			movingEnabled = true;
			this.moveAmount = moveAmount;
		}
	}
	
	
	private void setTurnHeading(short axis, double heading, double angle) {
		double turnDeg = 0;
		
		if (Commons.VERTICAL == axis && angle == 0)
			angle = heading >= 180 ? 360 : 0;

		turnDeg = angle - heading;
		
		turningEnabled = true;
		turnAmount = turnDeg;
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
	private void setMoveAlongSides() {
		
		if (!movingEnabled && !turningEnabled) {
			
			if (getHeading() % 90 == 0) {
				turnAmount = 90;
				turningEnabled = true;
			}

			double nextHeading = (getHeading() + 90) % 360;
			if (nextHeading == 0) {
				moveAmount = getBattleFieldHeight() - getY() - Commons.ROBOT_SAFE_LENGTH / 2;
			} else if (nextHeading == 180) {
				moveAmount = getY() - Commons.ROBOT_SAFE_LENGTH / 2;
			} else if (nextHeading == 90) {
				moveAmount = getBattleFieldWidth() - getX() - Commons.ROBOT_SAFE_LENGTH / 2;
			} else if (nextHeading == 270) {
				moveAmount = getX() - Commons.ROBOT_SAFE_LENGTH / 2;
			}

			movingEnabled = true;
		}
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
		out.printf("[onHitRobot] robot: %s %s", event.getName(), System.getProperty("line.separator"));
		
		if (isTeammate(event.getName())) {
			if (Math.random() >= 0.5) {
				setBack(Commons.DEFAULT_STEP_LENGTH);
				setMoveToClosestSide(getX(), getY());
			}
		}
		else 
		{	
			enemyList.add(new EnemyRobot(event.getName(), event.getBearing(), 0));
			updateClosestEnemy(enemyList);
			out.printf("[onHitRobot] update enemy list, new size: %s %s", enemyList.size(), System.getProperty("line.separator"));
		}
	}


	/** 
	 * setBack 10
	 * setTurnRight 90
	 */
	public void onHitWall(HitWallEvent event) {
		setBack(Commons.DEFAULT_STEP_LENGTH);
		setMoveAlongSides();
	}

	
	private void initRobot() {
		// set color
		setColors(new Color(66, 13, 171), Color.RED, Color.YELLOW);
		
		// let radar and gun turn independently from body
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
	}
	
	public void run() {
		initRobot();

		setMoveToClosestSide(getX(), getY());
		
		setFullScanBattleField();

		while (true) {
			
			// full scan some time
			if (getTime() % Commons.RADAR_BIG_SCAN_INTERVAL == 0) {
				setFullScanBattleField();
			}
			
			doScanAndUpdateClosestEnemy();
			
			
			doTurnHeading();

			doMove();
			
			setMoveAlongSides();
			
			doTurnHeading();

			doMove();
			
			doFire();
			
			execute();
		}
	}
	

	private void doTurnHeading() {
		if (Math.abs(getTurnRemaining()) > 0 || Math.abs(getDistanceRemaining()) > 0)
			return;

		if (turningEnabled) {
			setTurnRight(turnAmount);
			turningEnabled = false;
		}
	}


	private void doMove() {
		if (Math.abs(getTurnRemaining()) > 0 || Math.abs(getDistanceRemaining()) > 0)
			return;
		
		if (movingEnabled) {
			setAhead(moveAmount);
			movingEnabled = false;
		}
	}


	private void lockGun(double heading, double targetBearing, double gunHeading) {
		double gunTurn = heading + targetBearing - gunHeading;
		setTurnGunRight(Utils.normalRelativeAngleDegrees(gunTurn) * Commons.GUN_LOCK_FACTOR);
	}


	private void lockRadar(double heading, double targetBearing, double radarHeading) {
		double radarTurn = heading + targetBearing - radarHeading;
		setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn) * Commons.RADAR_LOCK_FACTOR);
	}
	
	
	private void doFire() {
		if (!firingEnabled || myEnemy == null)
			return;
		
		if (getGunHeat() == 0 && getGunTurnRemaining() < 30) {
			double power = 0;
			
			if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY * 0.1) {
				// few energy left, just move no fire
			}
			else if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY * 0.4) {
				power = Rules.MAX_BULLET_POWER * 0.2;
			}
			else if (getEnergy() < Commons.DEFAULT_ROBOT_ENERGY * 0.7) {
				power = Rules.MAX_BULLET_POWER * 0.7;
			}
			else
				power = Rules.MAX_BULLET_POWER;
			
			if (myEnemy.distance > getBattleFieldWidth() * 0.95)
				power = power * 0.6;
			else if (myEnemy.distance <= 50)
				power = Rules.MAX_BULLET_POWER;
			
			
			out.printf("set fire power: %s %s", power, System.getProperty("line.separator"));
			setFire(power);
		}
	}


	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		out.printf("[onRobotDeath] dead robot: %s  %s", event.getName(), System.getProperty("line.separator"));

		if (myEnemy != null && myEnemy.name.equals(event.getName())) {
			setFullScanBattleField();
			firingEnabled = false;
		}
	}
	
	private void setFullScanBattleField() {
		radarScansRemaining = Commons.DEFAULT_RADAR_SCAN_NUMS + 1;
	}
}
