package ActorCritic_kato;

import org.w3c.dom.Node;


import ActorCritic_kato.ActorCritic.Status;

public interface actor {
	public void learning(Status currentStatus, double td_error, double UtilityFunction);
	public double[] getAction(double inputs[], Status currentStatus, int count);
	public double ActionGenerator(double mux, double muy, double varx, double vary, Status currentStatus);
	public double NearestNeighbor(double[] rand);
	void setOutputSize(int OutputSize);
	public void setMaxVariance(double max_variance);
	void setInputSize(int InputSize);
	public void resetBeta(double data);
	void Log(String str);
	public void displayLearnedKnowledge();
}
