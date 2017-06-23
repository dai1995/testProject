package ActorCritic_kato;

import ActorCritic_kato.ActorCritic.Status;

public interface critc {
	public double TDerror(double reward, Status currentStatus, Status futureStatus, double gamma);
	public void learning(Status currentStatus, double td_error, double learning_speed, double UtilityFunction);
	public double getValue(double[] inputs);
	public double getUtility(double TDerror);
	void setInputSize(int InputSize);
	void Log(String str);
	public double[][] display_learned_Critic();
}
