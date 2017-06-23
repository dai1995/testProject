package matrix;

/**
 * タイトル:  Matrix
 * 説明:    行列演算ライブラリ
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author
 * @version 1.0
 */

public class MatrixCalc {

  public MatrixCalc() {
  }

  // return A*B
  public MatrixObj MultiplyMatrix_M(MatrixObj A, MatrixObj B) {
  	double[][] result = new double[A.L][B.M];//2007.2.4 修正(間違っていた。正方行列以外うまく働いていなかったはず)
  	MatrixObj result_matrix = new MatrixObj(A.L, B.M);
  	this.MultiplyMatrix(A.L, A.M, B.M, A.getMatrix(), B.getMatrix(), result);
  	result_matrix.set_array_data(result);
  	return result_matrix;
  }
  public MatrixObj MultiplyMatrix_M(DiagonalMatrixObj A, MatrixObj B) {
  	return MultiplyMatrix_M(this.Convert(A), B);
  }
  public MatrixObj MultiplyMatrix_M(MatrixObj A, DiagonalMatrixObj B) {
  	return MultiplyMatrix_M(A, this.Convert(B));
  }
  public DiagonalMatrixObj MultiplyMatrix_M(DiagonalMatrixObj A, DiagonalMatrixObj B) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(A.L);
  	for (int i=0; i<A.L; i++) {
  		result.set_data(i, A.getData(i,i) * B.getData(i,i));
  	}
  	return result;
  }
  
  MatrixObj Convert(DiagonalMatrixObj A) {
  	MatrixObj result = new MatrixObj(A.L, A.L);
  	for (int i=0; i<A.L; i++) {
  		result.set_data(i,i,A.getData(i,i));
  	}
  	return result;
  }

  
  // return A*B (サイズを指定するversion:行列の一部の要素だけを使って積を計算するときに使用する)
  public MatrixObj MultiplyMatrix_M(MatrixObj A, MatrixObj B, int sizeAL, int sizeAM, int sizeBL, int sizeBM) {
  	double[][] result = new double[sizeAL][sizeBL];//2007.2.4 修正(間違っていた。正方行列以外うまく働いていなかったはず)
  	MatrixObj result_matrix = new MatrixObj(sizeAL, sizeBM);
  	this.MultiplyMatrix(sizeAL, sizeAM, sizeBM, A.getMatrix(), B.getMatrix(), result);
  	result_matrix.set_array_data(result);
  	return result_matrix;
  }
  
  
  // return A*b (bはスカラー)
  public MatrixObj MultiplyMatrix_M(MatrixObj A, double b) {
  	MatrixObj result_matrix = new MatrixObj(A.L, A.M);
  	for (int i=0; i<A.L; i++) {
  		for (int j=0; j<A.M; j++) {
  			result_matrix.set_data(i,j, A.getData(i,j)*b);
  		}
  	}
  	return result_matrix;
  }

  public MatrixObj MultiplyMatrix_M(DiagonalMatrixObj A, double b) {
  	MatrixObj result_matrix = new MatrixObj(A.L, A.M);
  	for (int i=0; i<A.L; i++) {
  			result_matrix.set_data(i,i, A.getData(i,i)*b);
  	}
  	return result_matrix;
  }
  

  // return A*b (bはスカラー) のサイズ指定版
  public MatrixObj MultiplyMatrix_M(MatrixObj A, double b, int sizeAL, int sizeAM) {
  	MatrixObj result_matrix = new MatrixObj(sizeAL, sizeAM);
  	for (int i=0; i<sizeAL; i++) {
  		for (int j=0; j<sizeAM; j++) {
  			result_matrix.set_data(i,j, A.getData(i,j)*b);
  		}
  	}
  	return result_matrix;
  }

  
  // C<-A*B
  // A:L*M, B:M*N, C: L*N
  public void MultiplyMatrix(int L, int M, int N,
                            double A[][], double B[][], double C[][]) {
    double sum;
    for (int n=0; n<N; n++) {
      for (int l=0; l<L; l++) {
        sum = 0D;
        for (int m=0; m<M; m++) {
          sum += A[l][m] * B[m][n];
        }/* for m */
        C[l][n] = sum;
      }/*for l */
    }/* for n */
  }/* MultiplyMatrix */

  //N=1のときは特に多次元配列を使わなくて良いので。
  public void MultiplyMatrix(int L, int M,
                            double A[][], double B[], double C[]) {
    double sum;
    for (int l=0; l<L; l++) {
        sum = 0D;
        for (int m=0; m<M; m++) {
          sum += A[l][m] * B[m];
          System.out.println("A(" + l + ", " + m + ")=" + A[l][m]);
        }/* for m */
        C[l] = sum;
      }/*for l */
  }/* MultiplyMatrix */

  // return A+B
  public MatrixObj AddMatrix_M(MatrixObj A, MatrixObj B) {
  	MatrixObj result_matrix = new MatrixObj(A.L, A.M);
  	//System.out.println("A.L="+A.L+" A.M="+A.M);
  	for (int i=0; i<A.L; i++) {
  		for (int j=0; j<A.M; j++) {
  			result_matrix.set_data(i,j,A.getData(i,j)+B.getData(i,j));
  		}
  	}
  	return result_matrix;
  }

  public MatrixObj AddMatrix_M(DiagonalMatrixObj A, MatrixObj B) {
  	return AddMatrix_M(Convert(A), B);
  }
  
  public MatrixObj AddMatrix_M(MatrixObj A, DiagonalMatrixObj B) {
  	return AddMatrix_M(A, Convert(B));
  }
  
  public DiagonalMatrixObj AddMatrix_M(DiagonalMatrixObj A, DiagonalMatrixObj B) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(A.L);
  	for (int i=0; i<A.L; i++) {
  		result.set_data(i, A.getData(i,i) + B.getData(i,i));
  	}
  	return result;
  }

  
  //C<-A+B
  //A,B,C:L*M
  public void AddMatirx(int L, int M,
                        double A[][], double B[][], double C[][]) {
      for (int l=0; l<L; l++) {
        for (int m=0; m<M; m++) {
          C[l][m] = A[l][m] + B[l][m];
        }/* for m */
      }/*for l */
  }/* AddMatrix */

  
  // return A-B
  public MatrixObj DiffMatrix_M(MatrixObj A, MatrixObj B) {
  	MatrixObj result_matrix = new MatrixObj(A.L, A.M);
  	for (int i=0; i<A.L; i++) {
  		for (int j=0; j<A.M; j++) {
  			result_matrix.set_data(i,j,A.getData(i,j)-B.getData(i,j));
  		}
  	}
  	return result_matrix;
  }
  public MatrixObj DiffMatrix_M(DiagonalMatrixObj A, MatrixObj B) {
  	return DiffMatrix_M(this.Convert(A), B);
  }
  public MatrixObj DiffMatrix_M(MatrixObj A, DiagonalMatrixObj B) {
  	return DiffMatrix_M(A, this.Convert(B));
  }
  public DiagonalMatrixObj DiffMatrix(DiagonalMatrixObj A, DiagonalMatrixObj B) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(A.L);
  	for (int i=0; i<A.L; i++) {
  		result.set_data(i, A.getData(i,i)-B.getData(i,i));
  	}
  	return result;
  }
  
  //C<-A-B
  //A,B,C:L*M
  public void DiffMatrix(int L, int M,
                        double A[][], double B[][], double C[][]) {
      for (int l=0; l<L; l++) {
        for (int m=0; m<M; m++) {
          C[l][m] = A[l][m] - B[l][m];
        }/* for m */
      }/*for l */
  }/* DiffMatrix */

  //err[l] -> c[l][l]
  public void MakeCovarianceMatrix(int l, double err[], double C[][]) {
    for (int i=0; i<l; i++) {
      for (int j=0; j<l; j++) {
        C[i][j] = err[i]*err[j];
      }
    }
  }/* MakeCovarianceMatrix() */

  
  //転置  
  public MatrixObj TransPortMatrix_M(MatrixObj A) {
  	double[][] result = new double[A.M][A.L];//sizeが逆になっていたので修正2007.2.7
  	MatrixObj result_matrix = new MatrixObj(A.M, A.L);
  	this.TransportMatrix(A.L, A.M, A.getMatrix(), result);
  	result_matrix.set_array_data(result);
  	return result_matrix;
  }
  public DiagonalMatrixObj TransPortMatrix_M(DiagonalMatrixObj A) {
  	return A;
  }
  
  //転置
  public void TransportMatrix(int L, int M, double A[][], double C[][]) {
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        C[m][l] = A[l][m];
      }
    }
  }
  
  //トレース
  public double Trace(MatrixObj A) {
  	double sum=0D; 
  	if (A.getL()!=A.getM()) {//正方行列じゃない場合
  		System.err.println("MatrixCalc: Trace(A)  A.L must be the same as A.M!");
  		System.exit(1);
  	}
  	for (int i=0; i<A.getL(); i++) {
  		sum += A.getData(i,i);
  	}
  	return sum;
  }
  
  public MatrixObj InverseMatrix_M(MatrixObj A) throws MatrixException {
  	MatrixObj result = new MatrixObj(A.L, A.M);
  	double[][] ainv = new double[A.L][A.M];
  	this.InverseMatrix(A.L, A.matrix, ainv);
  	result.set_array_data(ainv);
  	return result;
  }
  
  public DiagonalMatrixObj InverseMatrix_M(DiagonalMatrixObj A) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(A.L);
  	for (int i=0; i<A.L; i++) {
  		result.set_data(i, 1/(double)A.getData(i,i));
  	}
  	return result;
  }

  //LxL行列の逆行列を求める
  public void InverseMatrix(int L, double A[][], double Ainv[][]) throws MatrixException {
    int Ipivot[] = new int[L];
    double LU[][] = new double[L][L];
    double T;
    int j,k;
    LUdecompose(L,A,LU,Ipivot);
    for (int i=0; i<L; i++) {
      for (j=0; j<L; j++) {
        if (Ipivot[j] == i) {
          T=1D;
        }else{
          T=0D;
        }
        for (k=0; k<=j-1; k++) {
          T -= LU[Ipivot[j]][k] * Ainv[k][i];
        }
        Ainv[j][i] = T;
      }
      for (j=L-1; j>=0; j--) {
        T = Ainv[j][i];
        for (k=j+1; k<L; k++) {
          T -= LU[Ipivot[j]][k] * Ainv[k][i];
        }
        Ainv[j][i] = T / LU[Ipivot[j]][j];
      }
    }
  }/* InverseMatrix() */
  
  MatrixObj PseudoInverse(MatrixObj A) throws MatrixException {
	  MatrixObj result = A;
	  result = result.Transport().multiply(this.InverseMatrix_M(A.multiply(A.Transport())));
	  return result;
  }

  //LU分解
  void LUdecompose(int L, double A[][], double LU[][], int Ipivot[]) throws MatrixException {
    final double Epsilon=1.0E-50;
    int I,J;
    double Pivot, T;
    this.SetMatrixA2B(L,L,A,LU);
    for (int i=0; i<L; i++) { Ipivot[i] = i; }

    for (int l=0; l<L; l++) {
      I = l;
      for (int k=l+1; k<L; k++) {
        if (Math.abs(LU[Ipivot[k]][l]) > Math.abs(LU[Ipivot[I]][l])) { I = k; }
      }
      J=Ipivot[l]; Ipivot[l] = Ipivot[I]; Ipivot[I] = J;
      Pivot = LU[Ipivot[l]][l];
      if (Math.abs(Pivot) < Epsilon) {
      	throw new MatrixException("MatrixCalc.LUdecompose Warning! " + l + "th pivot is too small!" + Pivot + "(I=" + I + ")");
        //System.out.println("MatrixCalc.LUdecompose Warning! " + l + "th pivot is too small!" + Pivot + "(I=" + I + ")");
      }
      for (int m=l+1; m<L; m++) {
        T = LU[Ipivot[m]][l] / Pivot;
        LU[Ipivot[m]][l] = T;
        for (int n=l+1; n<L; n++) {
          LU[Ipivot[m]][n] -= T * LU[Ipivot[l]][n];
        }
      }
    }
  }

  public void SetMatrixA2B(int L, int M, double A[][], double B[][]) {
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        B[l][m] = A[l][m];
      }
    }
  }
  
  public double Det(MatrixObj A) {
	  double weight[] = new double[A.getL()];
	  int ip[] = new int[A.getL()];
	  double det=0;
	  double u=0, t;
	  int M=-1, j=-1;
	  for (int l=0; l<A.getL(); l++) {
		ip[l]=l; //交換情報の初期値
		u=0;
		for (int m=0; m<A.getM(); m++) {
			t=Math.abs(A.getData(l, m));
			if (t>u) u=t;
		}
		if (u==0D) return -1;
		weight[l] = 1 / u;
	  }
	  det = 1;
	  for (int l=0; l<A.getL(); l++) {
		  u=-1;
		  for (int m=l; m<A.getM(); m++) {
			M = ip[m];
			t = Math.abs(A.getData(M, m)) * weight[M];
			if (t > u) {
				u = t;
				j = m;
			}
		  }
		  int ik = ip[j];
		  if (j!=l) {
			  ip[j] = ip[l];  ip[l] = ik;
			  det = -det;
		  }
		  u = A.getData(ik, l);
		  det *= u;
		  if (u==0) return -1;
		  for (int i=l+1; i<A.getL(); i++) {
			  int ii = ip[i];
			  t = A.getData(ii, l) / u;
			  A.set_data(ii, l, t);
			  for (int m=l+1; m < A.getL(); m++) {
				  double data = A.getData(ii,m);
				  data -= t * A.getData(ik, m);
				  A.set_data(ii, m, data);
			  }
		  }
	  }
	  return det;	  
  }
  //detAを計算する
  /*public double Det(MatrixObj A) {
		double t;
		double det=1, u, data;
		MatrixObj B = new MatrixObj(A.getL(), A.getM(), A.getMatrix());
		for (int k=0; k<B.getL(); k++) {
			t = B.getData(k,k); det *= t;
			for (int i=0; i<B.getL(); i++) {
				data = B.getData(k,i);
				data /= t;
				B.set_data(k,i,data);
			}
			B.set_data(k,k,1/t);
			for (int j=0; j<B.getL(); j++) {//注意 B.getL()==B.getM()
				if (j!=k) {
					u = B.getData(j,k);
					for (int l=0; l<B.getL(); l++) {
						if (l != k) {
							data = B.getData(k,l);
							B.set_data(j,l, B.getData(j,l) - data * u);
						}else{
							B.set_data(j,l, -u/t);							
						}
					}
				}
			}
		}
		
		//this.MultiplyMatrix_M(A,B).display("AxB");
		return det;
//  }*/

  //サイズ指定付きdet A (行列Aの一部の要素だけを使ってdet Aを計算する時に使用する)
  public double Det(MatrixObj A, int sizeL, int sizeM) {
	double t;
	double det=1, u, data;
	MatrixObj B = new MatrixObj(sizeL, sizeM, A.getMatrix());
	for (int k=0; k<B.getL(); k++) {
		t = B.getData(k,k); det *= t;
		for (int i=0; i<B.getL(); i++) {
			data = B.getData(k,i);
			data /= t;
			B.set_data(k,i,data);
		}
		B.set_data(k,k,1/t);
		for (int j=0; j<B.getL(); j++) {
			if (j!=k) {
				u = B.getData(j,k);
				for (int l=0; l<B.getL(); l++) {
					if (l != k) {
						data = B.getData(k,l);
						B.set_data(j,l, B.getData(j, l) - data * u);
					}else{
						B.set_data(j,l, -u/t);							
					}
				}
			}
		}
	}
	return det;
  }
  
  boolean is_same(MatrixObj A, MatrixObj B) {
  	MatrixObj D = A.Diff(B);
  	for (int i=0; i<D.L; i++) {
  		for (int j=0; j<D.M; j++) {
  			if (D.getData(i,j) > 0.0000000000001D) {
  				return false;
  			}
  		}
  	}
  	return true;
  }

}