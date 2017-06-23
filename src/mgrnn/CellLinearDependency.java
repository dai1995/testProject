package mgrnn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import datalogger.dataOutput;


public class CellLinearDependency extends GRNNCell implements Cell {
	//appended by k.yamauchi at 2010.6.30
	double alpha=1D; //Linear Dependency(分母側）
	double t_alpha[]; //Linear Dependency x t(分子側）
	double delta; 
	double distance; //distance between the kernel center and the input
	double averaged_distance;
	double NumberOfLearnedSamples=0;
	double sigma;
	double sum_of_outputs;
	int ID;
	boolean isFix = false; //If isFix==true, this kernel is not removed, replace nor modified. by k.yamauchi 2013.7.6 

	ArrayList<CellLinearDependency> Edge = new ArrayList<CellLinearDependency>();// the list of neighbors
	
	public CellLinearDependency(int NumberOfInput, int NumberOfOutput) {
		super(NumberOfInput, NumberOfOutput);
		this.t_alpha = new double[this.NumberOfOutput];
		this.NumberOfLearnedSamples = 1D;
		this.setStandardDeviation(1.0D);
	}//constructor

	public double exp_output(double input[]) {
		double sum2 = 0D;
		this.distance = 0.0D;
		for (int i = 0; i < this.NumberOfInput; i++) {
			//sum += Math.pow(this.W[i] * (this.T[i] - input[i]), 2D);
			//System.out.println("( >ω^ )< input[" + i + "] = " + input[i] +", T[i] = " + T[i]);
			//System.out.println("( ^ω＜ )< 				Math.pow((this.T[" + i + "] - input[" + i + "]), 2D) = " + Math.pow((this.T[i] - input[i]), 2D));
			this.distance += Math.pow((this.T[i] - input[i]), 2D);
		}
		sum2 = this.distance / (2 * this.sigma); //sigmaはsetStandardDeviationで2乗してある
		this.exp_output_value = Math.exp(-sum2);
		//if (this.exp_output_value<0.1) this.exp_output_value = 0D;
		//this.actual_distance = Math.sqrt(sum);
		this.actual_distance = Math.sqrt(sum2);
		return this.exp_output_value;
	}
	
	//after calculating exp_output()
	public double responsiblity() {
		return this.alpha * this.exp_output_value;
	}
	

	public void setSumOfOutputs(double sum) {
		this.sum_of_outputs = sum;
	}
	
	public double relativeResponsibility() {
		return this.alpha * this.exp_output_value / this.sum_of_outputs;
	}
	
	public double[] output(double input[]) {
		double exp_out = this.exp_output(input);
		double[] outputs = new double[this.NumberOfOutput];
		//this.Log("output() input size is " + input.length + " output size is " + outputs.length + " size t_alpha = " + t_alpha.length + " NumberOfOutputs is " + this.NumberOfOutput);
		for (int i=0; i<this.NumberOfOutput; i++) {
			outputs[i] = this.t_alpha[i] * exp_out;
			//System.out.println("+++++this.t_alpha[" + i + "] = " + this.t_alpha[i] + ", exp_out = " + exp_out);
			//System.out.println("+++++				outputs[" + i + "] = " + outputs[i]);
		}
		return outputs;
	}
	
/*	void Log(String str) {
		System.out.println("mgrnn.CellLinearDependency." + str);
	}
*/
	//initial learning
	//If output is null, t_alpha[] and C[] are set to zero vector.
	//This function is for special procedure to set the new added cell, which is a tentative cell for addeding new input.
	public void learn(double input[], double output[], double alpha) {
		for (int i=0; i<this.NumberOfInput; i++) {
			this.T[i] = input[i];
		}

		for (int j=0; j<this.NumberOfOutput; j++) {
			if (output == null) {
				this.t_alpha[j] = this.C[j] = 0D;
			}else{
				this.t_alpha[j] = this.C[j] = output[j];
			}
		}
		this.alpha = alpha;
		this.NumberOfLearnedSamples = 1D;
		this.NumberOfActivations = 0;
		this.averaged_distance = 0.0D;
	}	
	
	//Incremental learning of new outputs based on the LinearDependency
	public void Increment(double t[], double LinearDependency, double NumberOfSamples) {
		this.alpha += LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] += t[o] * LinearDependency;
		}
		this.NumberOfLearnedSamples += NumberOfSamples;
	}
	
	//Incremental learning of new outputs based on the LinearDependency
	public void Increment(double t[], double LinearDependency) {
		this.alpha += LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] += t[o] * LinearDependency;
		}
	}	
	
	//Decremental learning is also available
	public void Decrement(double t[], double LinearDependency) {
		this.alpha -= LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] -= t[o] * LinearDependency;
		}
	}
	

	public void setAveragedDistance() {
		this.inc_activation_number();
		this.averaged_distance += 
				(1/(double)this.getNumberOfActivations())*(this.distance-this.averaged_distance);
	}
	
	public double getAveragedDistance() {
		return this.averaged_distance;
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}


	/**
	 * @return the t_alpha
	 */
	@Override
	public double[] getT_alpha() {
		return t_alpha;
	}


	/**
	 * @param tAlpha the t_alpha to set
	 */
	public void setT_alpha(double[] tAlpha) {
		t_alpha = tAlpha;
	}
	
	public double[] get_normalized_t_alpha() {
		double[] n_alpha_t = new double[this.NumberOfOutput]; 
		for (int i=0; i<this.NumberOfOutput; i++) {
			n_alpha_t[i] = this.t_alpha[i]/this.alpha;
		}
		return n_alpha_t;
	}
	
	public void setStandardDeviation(double r) {
		this.R = r;
		this.sigma = Math.pow(0.3 * this.R, 2.0);
	}
	
	//実質的な重みを返す(C[]を返すだけではダメで、ちゃんと以下の計算をしないと正しい出力に対応した重みが出ないことに注意）
	public double[] ActualWeight() {
		double actual_weight[] = new double[this.NumberOfOutput];
		for (int o=0; o<this.NumberOfOutput; o++) {
			actual_weight[o] = this.t_alpha[o]/this.alpha;
		}
		return actual_weight;
	}

	//Following functions are for manipulating Edges.
	// Answer whether or not the NeighborCandidate is included in the Edge.
	public boolean isConnected(CellLinearDependency NeighborCandidate) {
		CellLinearDependency target_unit;
		Iterator<CellLinearDependency> edge = this.Edge.iterator();
		while (edge.hasNext()) {
			target_unit = edge.next();
			if (target_unit.equals(NeighborCandidate)) {
				return true;
			}
		}
		return false;
	}

	//remove the specified edge, which corresponds to the Neighbor.
	public void removeEdge(CellLinearDependency Neighbor) {
		CellLinearDependency target_unit;
		for (int i=0; i<this.Edge.size(); i++) {
			target_unit = this.Edge.get(i);
			if (target_unit.equals(Neighbor)) {
				this.Edge.remove(i);
				break;
			}
		}
	}

	//remove all Edges
	public void removeAllEdges() {
		while (this.Edge.size()>0) {
			this.Edge.remove(this.Edge.size()-1);
		}
	}
	

	
	/**
	 * @return the iD
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(int iD) {
		ID = iD;
	}
	
	/**
	 * @return the numberOfLearnedSamples
	 */
	public double getNumberOfLearnedSamples() {
		return NumberOfLearnedSamples;
	}
	

	/**
	 * @param numberOfLearnedSamples the numberOfLearnedSamples to set
	 */
	public void IncrementNumberOfLearnedSamples(double AddNumberOfLearnedSamples) {
		NumberOfLearnedSamples += AddNumberOfLearnedSamples;
	}

	/**
	 * @return the sigma
	 */
	public double getSigma() {
		return sigma;
	}

	/**
	 * @return the delta
	 */
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		this.delta = delta;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @return the isFix
	 */
	public boolean isFix() {
		return isFix;
	}

	/**
	 * @param isFix the isFix to set
	 */
	public void setFix(boolean isFix) {
		this.isFix = isFix;
	}
	
	public double[] display_hidden_unit_parameters(int num) {
		//System.out.println("---------kernel: " + num + " ---------");
		//System.out.print("kernel center: ");
		double[] karnel_parameter = new double[this.NumberOfInput + this.NumberOfOutput]; 
		int i;
		for (i=0; i<this.NumberOfInput; i++) {
			karnel_parameter[i] = this.T[i];
			//System.out.printf("%1.2f ", this.T[i]); 
		}
		//System.out.print("output: ");
		double[] normalized_w = this.get_normalized_t_alpha();
		for (int o=0; o<this.NumberOfOutput; o++) {
			
			karnel_parameter[i] = normalized_w[o];
			
			//System.out.printf(" %1.2f ", normalized_w[o]);
		}
//		System.out.println();
		return karnel_parameter;
	}

	
	public void display_hidden_unit_parameters(dataOutput dout, int num) {
		
		dout.put("---------kernel: " + num + " ---------");
		dout.put("kernel center: ");
		for (int i=0; i<this.NumberOfInput; i++) {
			dout.put(" " + this.T[i]); 
		}
		dout.put("output: ");
		double[] normalized_w = this.get_normalized_t_alpha();
		for (int o=0; o<this.NumberOfOutput; o++) {
			dout.put(" " + normalized_w[o]);
		}
	}		
	  public void save_parameter(int LGRNNid, Connection con) throws SQLException {
	        //LGRNNidのカーネルデータをデータベースから一旦消す
	        String query1 = "delete from kernel where lgrnn_id = ?";
	        PreparedStatement pstmt = con.prepareStatement(query1);;
	        pstmt.setInt(1, LGRNNid);
	        pstmt.executeUpdate();
	       
	        //パラメータをkernelに送るqueryを作る
	        String query2 = "insert into kernel (w1, w2, r,";
	        for (int i=0; i<this.NumberOfInput; i++) {
	            query2 += "u" + (i+1) + ", ";
	        }
	        query2 += " lgrnn_id) values (";
	        for (int o=0; o<this.NumberOfOutput; o++) {
	            query2 += this.t_alpha[o] + ", ";
	        }
	        query2 += this.alpha + ", " + LGRNNid + ")";
	        this.Log("save_parameter() query=" + query2);//デバッグのためのログ出力
	        PreparedStatement pstmt2 = con.prepareStatement(query2);
	        pstmt2.executeUpdate();
	    }
	   
	    void Log(String log) {
	        System.out.println("CellLinearDependency." + log);
	    }
	
}
