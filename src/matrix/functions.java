package matrix;

/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2002</p>
 * <p>会社名: </p>
 * @author 未入力
 * @version 1.0
 */

public class functions {

  public functions() {
  }

  // C<-A*B
  // A:L*M, B:M*N, C: L*N
  public MatrixObj MultiplyMatrix(MatrixObj A, MatrixObj B) {
    double sum, data1, data2;
    boolean AisDiagonal = false;
    boolean BisDiagonal = false;

    AisDiagonal = A.getClass().getName().equals("matrix.DiagonalMatrixObj");
    BisDiagonal = B.getClass().getName().equals("matrix.DiagonalMatrixObj");

    if (!AisDiagonal && !BisDiagonal) {
      MatrixObj C = new MatrixObj(A.getL(),B.getM());
      for (int n=0; n<B.getM(); n++) {
        for (int l=0; l<A.getL(); l++) {
          sum = 0D;
          for (int m=0; m<A.getM(); m++) {
            if (Math.abs(data1=A.getData(l,m))>0D) {
              if (Math.abs(data2=B.getData(m,n))>0D) {
                sum += data1 * data2;
              }
            }
          }/* for m */
          C.set_data(l,n,sum);
        }/*for l */
      }/* for n */
      return C;
    }else if (AisDiagonal && !BisDiagonal) {
      MatrixObj C = new MatrixObj(A.getL(),B.getM());
      for (int n=0; n<A.getM(); n++) {
        if (Math.abs(data1=A.getData(n,n))>0D) {
          for (int m=0; m<B.getM(); m++) {
            sum = 0D;
            if (Math.abs(data2=B.getData(n,m))>0D) {
              sum = data1 * data2;
            }
            C.set_data(n,m,sum);
          }
        }
      }
      return C;
    } else if (!AisDiagonal && BisDiagonal) {
      MatrixObj C = new MatrixObj(A.getL(),B.getM());
      for (int n=0; n<B.getM(); n++) {
        if ((data1=B.getData(n,n))>0D) {
          for (int l=0; l<A.getL(); l++) {
            if ((data2=A.getData(l,n)) > 0D) {
              sum = data1 * data2;
              C.set_data(l,n,sum);
            }
          }
        }
      }
      return C;
    } else if (AisDiagonal && BisDiagonal) {
      DiagonalMatrixObj C = new DiagonalMatrixObj(A.getL(),B.getM());
      for (int n=0; n<A.getL(); n++) {
        if (Math.abs(data1=A.getData(n,n))>0D && Math.abs(data2=B.getData(n,n))>0D) {
          C.set_data(n,n,data1*data2);
        }
      }
      return C;
    }
    return null;
  }/* MultiplyMatrix */
  
  //行列にスカラーを掛ける
  public MatrixObj MultiplyMatrix_gain(MatrixObj A, double b) {
  	DiagonalMatrixObj B = new DiagonalMatrixObj(A.L, b);
  	return this.MultiplyMatrix(B,A);
  }

  // C<-A*Bとするが、対角行列で出力する。それ以外の要素は強制的にゼロにする。
  // A:L*M, B:M*L, C: L*L
  public DiagonalMatrixObj MultiplyDiagonalMatrix(MatrixObj A, MatrixObj B) {
    double sum, data1, data2;
    boolean AisDiagonal, BisDiagonal;

    DiagonalMatrixObj C = new DiagonalMatrixObj(A.getL());
    AisDiagonal = A.getClass().getName().equals("matrix.DiagonalMatrixObj");
    BisDiagonal = B.getClass().getName().equals("matrix.DiagonalMatrixObj");

    if (AisDiagonal || BisDiagonal) {
      for (int n=0; n<A.getL(); n++) {
        if (Math.abs(data1=A.getData(n,n))>0D && Math.abs(data2=B.getData(n,n))>0D) {
           sum = data1 * data2;
        }else{
          sum = 0D;
        }
        C.set_data(n,sum);
      }
    }else{
      for (int n=0; n<A.getL(); n++) {
          sum = 0D;
          for (int m=0; m<A.getM(); m++) {
            if (Math.abs(data1=A.getData(n,m))>0D) {
              if (Math.abs(data2=B.getData(m,n))>0D) {
                sum += data1 * data2;
              }
            }
          }/* for m */
          C.set_data(n,sum);
      }/* for n */
    }
    return C;
  }/* MultiplyDiagonalMatrix */

  //C<-A+B
  //A,B,C:L*M
  public MatrixObj AddMatirx(MatrixObj A, MatrixObj B) {
      MatrixObj C = new MatrixObj(A.getL(),A.getM());
      for (int l=0; l<A.getL(); l++) {
        for (int m=0; m<A.getM(); m++) {
          C.set_data(l,m,A.getData(l,m) + B.getData(l,m));
        }/* for m */
      }/*for l */
      return C;
  }/* AddMatrix */

  //C<-A+B
  //A,B,C:L*M
  public DiagonalMatrixObj AddDiagonalMatirx(MatrixObj A, MatrixObj B) {
      DiagonalMatrixObj C = new DiagonalMatrixObj(A.getL());
      if ((A.getL() != A.getM())||(B.getL() != B.getM())) {
        System.err.println("AddDiagonalMatrix: Error! : A and B are both NxN matrix!");
        System.exit(1);
      }
      for (int l=0; l<A.getL(); l++) {
          C.set_data(l,l,A.getData(l,l) + B.getData(l,l));
      }/*for l */
      return C;
  }/* AddDiagonalMatrix */

  //C<-A-B
  //A,B,C:L*M
  public MatrixObj DiffMatrix(MatrixObj A, MatrixObj B) {
      MatrixObj C = new MatrixObj(A.getL(),A.getM());

      if ((A.getL() != B.getL()) || (A.getM() != B.getM())) {
        System.err.println("DffMatrixObj: Error! : The size of A and B must be the same!");
        System.exit(1);
      }

      for (int l=0; l<A.getL(); l++) {
        for (int m=0; m<A.getM(); m++) {
          C.set_data(l,m,A.getData(l,m) - B.getData(l,m));
        }
      }/*for l */
      return C;
  }/* DiffMatrix */

  //C<-A+B
  //A,B,C:L*M
  public DiagonalMatrixObj DiffDiagonalMatirx(MatrixObj A, MatrixObj B) {
      DiagonalMatrixObj C = new DiagonalMatrixObj(A.getL());
      if ((A.getL() != A.getM())||(B.getL() != B.getM())) {
        System.err.println("DiffDiagonalMatrix: Error! : A and B are both NxN matrix!");
        System.exit(1);
      }
      for (int l=0; l<A.getL(); l++) {
          C.set_data(l,A.getData(l,l) - B.getData(l,l));
      }/*for l */
      return C;
  }/* AddDiagonalMatrix */


  //転置
  public MatrixObj TransportMatrix(MatrixObj A) {
    MatrixObj C = new MatrixObj(A.getM(), A.getL());
    for (int l=0; l<A.getL(); l++) {
      for (int m=0; m<A.getM(); m++) {
        C.set_data(m,l,A.getData(l,m));
      }
    }
    return C;
  }


  //LxL行列の逆行列を求める(正方行列でないと動作は保証されん!)
  public MatrixObj InverseMatrix(MatrixObj A) {
    int Ipivot[] = new int[A.getL()];
    double T;
    MatrixObj LU;
    MatrixObj Ainv = new MatrixObj(A.getL(),A.getL());
    int j,k;

    LU = LUdecompose(A,Ipivot);
    for (int i=0; i<A.getL(); i++) {
      for (j=0; j<A.getL(); j++) {
        if (Ipivot[j] == i) {
          T=1D;
        }else{
          T=0D;
        }
        for (k=0; k<=j-1; k++) {
          T -= LU.getData(Ipivot[j],k) * Ainv.getData(k,i);
        }
        Ainv.set_data(j,i,T);
      }
      for (j=A.getL()-1; j>=0; j--) {
        T = Ainv.getData(j,i);
        for (k=j+1; k<A.getL(); k++) {
          T -= LU.getData(Ipivot[j],k) * Ainv.getData(k,i);
        }
        Ainv.set_data(j,i, T / LU.getData(Ipivot[j],j));
      }
    }
    return Ainv;
  }/* InverseMatrix() */

  //LU分解
  MatrixObj LUdecompose(MatrixObj A, int Ipivot[]) {
    final double Epsilon=0.0000001;
    int I,J;
    double Pivot, T;

    MatrixObj LU = new MatrixObj(A.getL(), A.getM());
    this.SetMatrixA2B(A,LU);
    for (int i=0; i<A.getL(); i++) { Ipivot[i] = i; }

    for (int l=0; l<A.getL(); l++) {
      I = l;
      for (int k=l+1; k<A.getL(); k++) {
        if (Math.abs(LU.getData(Ipivot[k],l)) > Math.abs(LU.getData(Ipivot[I],l))) { I = k; }
      }
      J=Ipivot[l]; Ipivot[l] = Ipivot[I]; Ipivot[I] = J;
      Pivot = LU.getData(Ipivot[l],l);
      if (Math.abs(Pivot) < Epsilon) {
        System.err.println("MatrixCalc.LUdecompose Warning! " + l + "th pivot is too small!" + Pivot + "(I=" + I + ")");
        /*if (Pivot > 0) Pivot = Epsilon;
        else Pivot = -Epsilon;*/
      }
      for (int m=l+1; m<A.getL(); m++) {
        T = LU.getData(Ipivot[m],l) / Pivot;
        LU.set_data(Ipivot[m],l,T);
        for (int n=l+1; n<A.getL(); n++) {
          double data = LU.getData(Ipivot[m],n);
          data -= T * LU.getData(Ipivot[l],n);
          LU.set_data(Ipivot[m], n, data);
        }
      }
    }
    return LU;
  }

  public void SetMatrixA2B(MatrixObj A, MatrixObj B) {
    for (int l=0; l<A.getL(); l++) {
      for (int m=0; m<A.getM(); m++) {
        B.set_data(l,m,A.getData(l,m));
      }
    }
  }

  public double ElementWiseMultiplex(MatrixObj A, MatrixObj B) {
	double result=0D;
	for (int i=0; i<A.getL(); i++) {
		for (int j=0; j<A.getM(); j++) {
			result += A.getData(i,j) * B.getData(i,j);
		}
	}
	return result;
  }

	public double Det(MatrixObj A) {
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
			for (int j=0; j<B.getL(); j++) {
				if (j!=k) {
					u = B.getData(j,k);
					for (int l=0; l<B.getL(); l++) {
						if (l != k) {
							data = B.getData(k,l);
							B.set_data(j,l, data - data * u);
						}else{
							B.set_data(j,l, -u/t);							
						}
					}
				}
			}
		}
		return det;
	}
	
  
  	public double paseudo_Det(MatrixObj A) {
  		double sum = 0D;
  		for (int i=0; i<A.getL(); i++) {
  			for (int j=0; j<A.getM(); j++) {
  				sum += Math.pow(A.getData(i,j),2D);
  			}
  		}
  		return sum;
  	}
}