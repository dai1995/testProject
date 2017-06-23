package PuddleWorld;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Puddle {
	Line2D centerLine;
	double puddleRadius;
	public Puddle(double x1, double y1, double x2, double y2, double puddelRadius){
		Point2D start = new Point2D.Double(x1, y1);
		Point2D end = new Point2D.Double(x2, y2);
		this.centerLine = new Line2D.Double(start, end);
		this.puddleRadius = puddelRadius;		
	}
	
	//水たまりに入った場合のreward 1ステップの報酬はまた別
	public double getReward(Point2D agentPosition){
		double distance = centerLine.ptSegDist(agentPosition);
		//System.out.println("距離 = " + distance);
		if(distance < puddleRadius){
			return -400.0d * (puddleRadius - distance);
		}
		
		return 0.0;
	}
}
