package PuddleWorld;
import javax.swing.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Random;
import ActorCritic.*;

public class PuddleWorldTest extends JPanel{

	//ActorCritic AC = new ActorCritic(nd_value_function, nd_actor)
	
	PuddleWorld pw = new PuddleWorld();
	
	double[] point1X = new double[100];
	double[] point1Y = new double[100];
	double[] point2X = new double[100];
	double[] point2Y = new double[100];
	
	public PuddleWorldTest(){
		
		Random r = new Random();
		
		for(int k = 0; k < 100; k++){
			System.out.println();
			System.out.println("Action" + k);
			
			int Action = r.nextInt(4);
			this.point1X[k] = this.pw.getagentPoint().getX();
			this.point1Y[k] = this.pw.getagentPoint().getY();
			System.out.println("X = " + this.point1X[k]);
			System.out.println("X = " + this.point1Y[k]);
			//this.pw.update(Action);
			this.point2X[k] = this.pw.getagentPoint().getX();
			this.point2Y[k] = this.pw.getagentPoint().getY();
			System.out.println("X = " + this.point2X[k]);
			System.out.println("X = " + this.point2Y[k]);
			
		}
		
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		PuddleWorldTest app = new PuddleWorldTest();
		frame.getContentPane().add(app);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(10, 10, 400, 400);
		frame.setTitle("タイトル");
		frame.setVisible(true);
		
	}

/*
	public void paintComponent(Graphics g){	
		Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
							RenderingHints.VALUE_ANTIALIAS_ON);
		g2.draw(new Rectangle2D.Double(this.pw.worldRect.getMinX() * 300, 
									   this.pw.worldRect.getMinY() * 300,
									   this.pw.worldRect.getMaxX() * 300,
									   this.pw.worldRect.getMaxY() * 300));
		
		g2.draw(new Rectangle2D.Double(this.pw.goalRect.getMinX() * 300, 
				   					   this.pw.goalRect.getMinY() * 300,
				   					   30,
				   					   30));
	
		for(int i = 0; i <= 105; i++){
			g2.fillOval(i, 195, 60, 60);
		}
		//g2.drawOval(105, 195, 60, 60);
		//g2.fillOval(105, 205, 60, 60);
		for(int i = 95; i <= 205; i++){
			g2.fillOval(105, i, 60, 60);
		}
		
		g2.setPaint(Color.red);
		BasicStroke wideStroke = new BasicStroke(10.0f);
	    g2.setStroke(wideStroke);
		//g2.draw(new Line2D.Double(5,20,5,20));
				
		g2.draw(new Line2D.Double(this.point1X[0] * 300, this.point1Y[0] * 300,
								  this.point1X[0] * 300, this.point1Y[0] * 300));
		
		
		
		BasicStroke wideStroke1 = new BasicStroke(2.0f);
	    g2.setStroke(wideStroke1);
		for(int k = 0; k < 100; k++){
			//System.out.println("Action" + k);
			//System.out.println(this.point1X[k]);
			g2.draw(new Line2D.Double(this.point1X[k] * 300, this.point1Y[k] * 300,
					this.point2X[k] * 300, this.point2Y[k] * 300));
		}
	}
*/
}
