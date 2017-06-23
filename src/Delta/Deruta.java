package Delta;


public class Deruta {

	int NumberofKernels;
	int NumberofInput;
	double sigma;

	public Deruta(int NumberofKernels,int NumberofInput,double sigma){
		this.NumberofKernels = NumberofKernels;
		this.NumberofInput = NumberofInput;
		this.sigma = sigma;
		//this.kernelcount = 0;
	}



	 public double deruta(double input[], double centers[][],int kernelcount){
		double deruta[] = new double[kernelcount];
		double minimumderuta = 0;
		double a[] = new double[kernelcount]; 									//a
		double k[][] = new double[kernelcount][kernelcount]; 				//行列K
		double r[][] = new double[kernelcount][kernelcount];
		double tanni[][] = new double[kernelcount][kernelcount];
		double fai[] = new double[kernelcount];								//Φ
		double gyakugyouretuk[][] = new double[kernelcount][kernelcount]; //逆行列
		double buf;
		double dis1 = 0;
		double dis2 = 0;
		double diff1 = 0;
		double diff2 = 0;
		double kdis = 0;
		double faidis = 0;




		for(int m=0;m<kernelcount;m++){				//mは最小となるδiの添え字 *下記の配列にiを使用のためmを使用
		//行列Kの作成
			for(int i=0; i<m;i++){
				for(int j=0; j<m; j++){
					kdis = 0;
					for(int d=0;d<this.NumberofInput;d++){
						kdis += Math.pow(centers[i][d]-centers[j][d], 2);
					}


					k[i][j] = Math.exp(-kdis/(2 * this.sigma * this.sigma));
				}
			}


			//Φの作成
			for(int i=0; i<m;i++){
				faidis = 0;
				for(int d=0;d<this.NumberofInput;d++){
					faidis += Math.pow(centers[m][d]-centers[i][d], 2);
				}
				fai[i] = Math.exp(-faidis/(2 * this.sigma * this.sigma));
			}

			//逆行列Kの作成
			//単位行列の作成
			for(int i=0;i<m;i++){
				for(int j=0;j<m;j++){
					gyakugyouretuk[i][j] = (i==j) ?1.0:0.0;
				}
			}

			//掃き出し法
			for(int i=0;i<m;i++){
				buf = 1 / k[i][i];
					for(int j=0;j<m;j++){
						k[i][j] *= buf;
						gyakugyouretuk[i][j]*=buf;
					}
					for(int s=0;s<m;s++){
						if(i != s){
							buf = k[s][i];
							for(int t=0;t<m;t++){
								k[s][t] -= k[i][t] * buf;
								gyakugyouretuk[s][t] -= gyakugyouretuk[i][t] * buf;
							}
						}
					}
			}

			for(int i=0;i<m;i++){
				for(int j=0;j<m;j++){
					tanni[i][j]=0;
					for(int d=0;d<m;d++){
						tanni[i][j] += r[i][d] * gyakugyouretuk[d][j];
					}
				}
			}


			//aの作成
			for(int i=0; i<m;i++){
				for(int j=0; j<m;j++){
					a[i] += gyakugyouretuk[i][j] * fai[i];
				}
			}

			//真ん中部分の作成
			for(int i=0; i<m;i++){
				dis1 = 0;
				for(int d=0;d<this.NumberofInput;d++){
					dis1 += Math.pow(input[d]-centers[i][d], 2);
				}
				diff1 += a[i] * Math.exp(-dis1/(2 * this.sigma * this.sigma));
			}

			//最後部分の作成
			for(int i=0; i<m;i++){
				for(int j=0; j<m;j++){
					dis2 = 0;
					for(int d=0;d<this.NumberofInput;d++){
						dis2 += Math.pow(centers[i][d]-centers[j][d], 2);
					}
					diff2 += a[i] * a[j] * Math.exp(-dis2/(2 * this.sigma * this.sigma));
				}
			}


			deruta[m] = 1 - (2 * diff1) + diff2;

		}

		//最小となるδ
		minimumderuta = deruta[0];
		for(int i=1;i<kernelcount-1;i++){
			if(minimumderuta>deruta[i])
				minimumderuta = deruta[i];
		}

		return minimumderuta;
	}
}


