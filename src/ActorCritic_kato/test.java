package ActorCritic_kato;

import DataLoad.MultipleDataload;
import ParameterReader.ParameterReader;

public class test {
	public test(String parametername){
		ParameterReader pr = new ParameterReader(parametername);
		double test;
		double[][] inputs = new double[441][2]; 
		double[][] outputs = new double[441][8];
		MultipleDataload tes = new MultipleDataload(pr.Reader("init"));
		tes.read(0);
		System.out.println("happy");
		for(int i = 0; i < 441; i++){
			for(int j = 0; j < 6; j++){
				test = tes.learning_input_patterns[i][0][j];
				if(j < 2){
					inputs[i][j] = tes.learning_input_patterns[i][0][j];
					System.out.print(inputs[i][j] + " ");
				}
				else{
					outputs[i][j - 2] = tes.learning_input_patterns[i][0][j];
					System.out.print(outputs[i][j - 2] + " ");
				}
					
				
			}
			System.out.println();
		}
		
	}
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		new test(args[0]);
	}

}
