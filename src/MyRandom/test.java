package MyRandom;

import java.awt.geom.Point2D;

public class test {

	public static void main(String[] args) {
		int Action = 0;
		double x, y;
		x = -1.1;
		y = -1.2;
		double nearest;
		double[] distance = new double[4];
		Point2D right = new Point2D.Double(1.5, -1.5);
		Point2D left = new Point2D.Double(-1.5, 1.5);
		Point2D up = new Point2D.Double(1.5, 1.5);
		Point2D down = new Point2D.Double(-1.5, -1.5);
		
		distance[0] = right.distanceSq(x, y);
		distance[1] = left.distanceSq(x, y);
		distance[2] = up.distanceSq(x, y);
		distance[3] = down.distanceSq(x, y);
		nearest = distance[0];
		for(int i = 1; i < 4; i++){
			if(nearest > distance[i]){
				nearest = distance[i];
				Action = i;
			}
		}
		System.out.println("Action = " + Action);
	}

}
