package matrix;

/**
 * タイトル:
 * 説明:
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author
 * @version 1.0
 */

public class DiagonalMatrixObj extends MatrixObj {
  public double matrix[];

  //constructor
  public DiagonalMatrixObj(int L) {
    super(L,L);
  }

  public DiagonalMatrixObj(int L, double diagonalValue) {
    super(L,L);
    for (int i=0; i<L; i++) {
      matrix[i] = diagonalValue;
    }
  }

  public DiagonalMatrixObj(int L, double data[]) {
    super(L,L);
    for (int i=0; i<L; i++) {
      matrix[i] = data[i];
    }
  }

  //初期値としてMOをせっとし、余った次元分の対格要素をdiagonal_valueにセットする
  public DiagonalMatrixObj(int L, MatrixObj MO, double diagonal_value) {
    super(L,L);
    for (int l=0; l<L; l++) {
        if (l<MO.getL()) {
         this.set_data(l,MO.getData(l,l));
        }else this.set_data(l,diagonal_value);
    }
  }

  void allocate_data(int L, int M) {
    matrix = new double[L];
  }

  //orthogonal matrixかどうかを返す
  public boolean isDiagonal() {
    return true;
  }

  public MatrixObj multiply(MatrixObj A) {
	  	return mc.MultiplyMatrix_M(this, A);
  }
  
  public void set_data(int l, double data) {
    this.matrix[l] = data;
  }
  public void set_data(int l, int m, double data) {
    this.matrix[l] = data;
  }

  public double getData(int l, int m) {
    if (l==m) {
      return this.matrix[l];
    }else{
      return 0D;
    }
  }

  public double[] getArrayData() {
    return matrix;
  }

  public int getL() {
    return this.L;
  }

  public int getM() {
    return this.M;
  }

  public MatrixObj Transport() {//same as this
	  	return this;
  }
  
  public void display() {
    System.out.println("-------------\n");
    for (int l=0; l<this.getL(); l++) {
      for (int m=0; m<this.getM(); m++) {
        if (l==m) {
          System.out.print(this.getData(l,l) + ", ");
        }else{
          System.out.print("0, ");
        }
      }
      System.out.println("\n");
    }
  }

  public double det() {
    double sum=1D;
    for (int i=0; i<this.getL(); i++) {
      sum *= this.getData(i,i);
    }
    return sum;
  }

  public double getSum() {
    double sum=0D;
    for (int i=0; i<this.getL(); i++) {
      sum += this.getData(i,i);
    }
    sum /= this.getL();
    return sum;
  }
  
  public DiagonalMatrixObj add(MatrixObj A) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(this.L, this.L);
  	for (int i=0; i<this.L; i++) {
  		result.set_data(i, this.getData(i,i) + A.getData(i,i));
  	}
  	return result;
  }
  public DiagonalMatrixObj add(DiagonalMatrixObj A) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(this.L, this.L);
  	for (int i=0; i<this.L; i++) {
  		result.set_data(i, this.getData(i,i) + A.getData(i,i));
  	}
  	return result;
  }
  
  public DiagonalMatrixObj diff(MatrixObj A) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(this.L, this.L);
  	for (int i=0; i<this.L; i++) {
  		result.set_data(i, this.getData(i,i) - A.getData(i,i));
  	}
  	return result;
  }
  public DiagonalMatrixObj diff(DiagonalMatrixObj A) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(this.L, this.L);
  	for (int i=0; i<this.L; i++) {
  		result.set_data(i, this.getData(i,i) - A.getData(i,i));
  	}
  	return result;
  }
  
  
  public MatrixObj MultiplyMatrix(MatrixObj A) {
  	return this.mc.MultiplyMatrix_M(this, A);  	
  }
  public DiagonalMatrixObj MultiplyMatrix(DiagonalMatrixObj A) {
  	return this.mc.MultiplyMatrix_M(this, A);  	
  }
  public DiagonalMatrixObj MultiplyMatrix(double a) {
  	DiagonalMatrixObj result = new DiagonalMatrixObj(this.L, this.L);
  	for (int i=0; i<this.L; i++) {
  		result.set_data(i, this.getData(i,i) * a);
  	}
  	return result;
  }

  

  
  public DiagonalMatrixObj inverse_d() {
  	return mc.InverseMatrix_M(this);
  	/*DiagonalMatrixObj result = new DiagonalMatrixObj(this.getL());
  	for (int i=0; i<this.getL(); i++) {
  		result.set_data(i, 1/(double)this.getData(i,i));
  	}
  	return result;*/
  }

	public DiagonalMatrixObj clone() {  
	    return (DiagonalMatrixObj)super.clone();  
	}     
}
