package nothingtolose;

import java.awt.geom.Point2D;

public class Commons {
	public static final double ROBOT_LENGTH = 36;
	public static final double ROBOT_SAFE_LENGTH = ROBOT_LENGTH + 10;

	public static final double RADAR_LOCK_FACTOR = 2;

	public static final double DEFAULT_STEP_LENGTH = 50;
	public static final double DEFAULT_SMALL_TURN_ANGLE = 45;
	public static final double DEFAULT_ROBOT_ENERGY = 100;
	public static final double DEFAULT_BIG_TURN_ANGLE = 90;

	public static final short FORWARD = 1;
	public static final short BACKWARD = -1;

	public static final short HORIZONTAL = 1;
	public static final short VERTICAL = 2;

	public static final int RADAR_BIG_SCAN_INTERVAL = 16;
	public static final int DEFAULT_RADAR_SCAN_NUMS = 8;

	public static double[] getEnemyCoordinates(double heading, double bearing, double x, double y, double distance) {
		// Calculate enemy bearing
		double enemyBearing = heading + bearing;

		// Calculate enemy's position
		double enemyX = x + distance * Math.sin(Math.toRadians(enemyBearing));
		double enemyY = y + distance * Math.cos(Math.toRadians(enemyBearing));

		double[] result = { enemyX, enemyY };
		return result;
	}

	// computes the absolute bearing between two points
	public static double absoluteBearing(double x1, double y1, double x2, double y2) {

		double xo = x2 - x1;
		double yo = y2 - y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}
}
