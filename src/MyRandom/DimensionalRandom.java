package MyRandom;

import java.util.Random;

import ActorCritic_kato.ACtest;

public class DimensionalRandom {
	double mux, muy, varx, vary, covxy;
	public DimensionalRandom(double mux, double muy, double varx, double vary, double covxy){
		this.mux = mux;
		this.muy = muy;
		this.varx = varx;
		this.vary = vary;
		this.covxy = covxy;
	}
	public double[] Rand(){
		if((this.vary - Math.pow(this.covxy, 2) / this.varx) <= 0){
			System.out.println("分散共分散行列が正定値ではありません");
		}
		double[] rand = new double[2];
		
		MyRandom rnd1 = new MyRandom();
		MyRandom rnd2 = new MyRandom();
		Random rnd = new Random();
		rand[0]= this.mux + Math.sqrt(this.varx) * rnd1.nrnd(rnd);
		rand[1] = this.muy + this.covxy / this.varx * (rand[0] - this.mux) + Math.sqrt(this.vary - this.covxy * this.covxy / this.varx) * rnd2.nrnd(rnd);
		//System.out.println( rand[0] + " " + rand[1]);
		
		return rand;
	}
	/*
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		DimensionalRandom D = new DimensionalRandom(0, 0, 0, 0, 0);
		D.Rand();
	}
	*/

}
	