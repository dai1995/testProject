package Delta;

import java.io.File;

public class Learner {
	double[] input;
	int NumberofInput;
	int NumberofKernels;
	double sigma;
	double[][] centers;
	double[] answer;
	int Kernelcount;
	double err;
	int count;

	public Learner(int NumberofKernels, int NumberofInput,  double sigma){
		this.NumberofInput = NumberofInput;
		this.NumberofKernels = NumberofKernels;
		this.sigma = sigma;
	    this.centers = new double[NumberofKernels][NumberofInput];
	    this.answer = new double[NumberofKernels];
	    this.Kernelcount = 0;
	    this.err = 0;
	    this.count = 1;
	}


	public void output(double x[],double y){
		double deruta = 0;
		Deruta de = new Deruta(this.NumberofKernels,this.NumberofInput,this.sigma);
		this.input = x;
		double output = calcurateoutput(this.centers,answer);
		double cumerr = err(output,y);
		double hantei = hantei(this.centers,this.input);
		if(this.count <= this.NumberofKernels){
			this.count++;
			if(hantei == 0){  //hanteiの値が0ならば学習。0でなければ入力と同じものが学習されている。
				learn(x,y);
				if(this.Kernelcount>=3){
					deruta = de.deruta(this.input,this.centers,this.Kernelcount);

					if(deruta < 0.99){							//δ<しきい値ならば新しい入力の削除
						for(int d=0;d<this.NumberofInput;d++){
							centers[this.Kernelcount][d] = 0;
						}
						answer[this.Kernelcount] = 0;
						this.Kernelcount -= 1;
					}
				}
			}
		}

		System.out.println("output" + output);
		System.out.println("cumerr:" + cumerr);
		System.out.println("learningkernelcount:" + this.Kernelcount);
		System.out.println("deruta:" + deruta);

/*
		//出力結果のテキストファイルへの書き出し
		try{
		      File file = new File("\\C:\\pleiades\\workspace\\LGRNN\\src\\result\\servo\\result\\0.99-sigma0.5.txt"); //サーバーでの実行:/home/ylabo/baba/result/データセット名/結果内容/ファイル名

		      if (checkBeforeWritefile(file)){
		        FileWriter filewriter = new FileWriter(file, true);

		        filewriter.write("学習器からの出力" + output + "\r\n");
		        filewriter.write("累積誤差:" + cumerr + "\r\n");
		        filewriter.write("学習したカーネル数:" + this.Kernelcount + "\r\n");
		        filewriter.write("δ=" + deruta + "\r\n");

		        filewriter.close();
		      }else{
		        System.out.println("ファイルに書き込めません");
		      }
		    }catch(IOException e){
		      System.out.println(e);
		    }


		//累積誤差のテキストファイルへの書き出し
		try{
		      File file = new File("/home/ylabo/baba/result/ConcreteData/cumerr/cumerr-deruta0.9.txt"); //サーバーでの実行:/home/ylabo/baba/result/データセット名/結果内容/ファイル名

		      if (checkBeforeWritefile(file)){
		        FileWriter filewriter = new FileWriter(file, true);

		        filewriter.write(cumerr + "\r\n");
		        filewriter.close();
		      }else{
		        System.out.println("ファイルに書き込めません");
		      }
		    }catch(IOException e){
		      System.out.println(e);
		    }


		//δの値のテキストファイルへの書き出し
		try{
		      File file = new File("/home/ylabo/baba/result/ConcreteData/deruta/deruta-deruta0.9.txt"); //サーバーでの実行:/home/ylabo/baba/result/データセット名/結果内容/ファイル名

		      if (checkBeforeWritefile(file)){
		        FileWriter filewriter = new FileWriter(file, true);

		        filewriter.write(deruta + "\r\n");

		        filewriter.close();
		      }else{
		        System.out.println("ファイルに書き込めません");
		      }
		    }catch(IOException e){
		      System.out.println(e);
		    }

		//学習しているカーネル数のテキストファイルへの書き出し
		try{
		      File file = new File("/home/ylabo/baba/result/ConcreteData/kernelcount/kernelcount-deruta0.9.txt"); //サーバーでの実行:/home/ylabo/baba/result/データセット名/結果内容/ファイル名

		      if (checkBeforeWritefile(file)){
		        FileWriter filewriter = new FileWriter(file, true);

		        filewriter.write(this.Kernelcount + "\r\n");

		        filewriter.close();
		      }else{
		        System.out.println("ファイルに書き込めません");
		      }
		    }catch(IOException e){
		      System.out.println(e);
		    }*/
	}



	//学習器の出力計算
	public double calcurateoutput(double input[][],double answer[]){
		int index=0;
		double y=0;
		double bunsi=0;
		double bunbo=0;
		double sigma2 = 2 * this.sigma * this.sigma;
		if(this.Kernelcount == 0){
			y = 0;
		}

		else{
			for(int i=0; i<this.Kernelcount;i++){
				double diff = 0;

				for(int j=0; j<this.NumberofInput; j++){
					diff += Math.pow(this.input[j]-this.centers[index][j], 2);
				}


				bunsi += this.answer[index]*Math.exp(-diff/sigma2);
				bunbo += Math.exp(-diff/sigma2);
				index++;
			}

			y=bunsi/bunbo;

		}
		return y;
	}


	//累積誤差の計算
	public double err(double output, double y){
		this.err += Math.pow(output-y,2);
		return  this.err;
	}

	//入力の学習
	public void learn(double x[],double y){
		centers[this.Kernelcount] = x;
		answer[this.Kernelcount] = y;
		this.Kernelcount++;
			}

	//入力と学習しているカーネルが同じであるかの判断
	public double hantei(double centers[][],double input[]){	//学習するかの判定
		int count=0;
		int index=0;
		for(int i=0; i<this.Kernelcount;i++){
			double dis=0;
			for(int j=0; j<this.NumberofInput; j++){
				dis += Math.pow(this.input[j]-this.centers[index][j], 2);
			}
			if(dis == 0){   //disが0ならば入力と同じカーネルが学習されている。
			count++;		//disが0ならばcount
			}
			index++;
		}
		return count;
	}

	//テキストファイルへの書き出しに必要
	private static boolean checkBeforeWritefile(File file){
	    if (file.exists()){
	      if (file.isFile() && file.canWrite()){
	        return true;
	      }
	    }

	    return false;
	  }


}
