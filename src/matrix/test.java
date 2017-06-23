package matrix;

import VectorFunctions.VectorFunctions;
import java.util.Vector;
/**
 * タイトル:  Matrix
 * 説明:    行列演算ライブラリ
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author
 * @version 1.0
 */

public class test {
	
  functions func = new functions();
  final int L=4, N=2, M=4;
  MatrixObj A = new MatrixObj(L,M);
  MatrixObj B = new MatrixObj(M,N);
  MatrixObj C, D;
  DiagonalMatrixObj E = new DiagonalMatrixObj(L);
  double err[] = new double[L];
  

  	Vector<Object> data;


	public test() {
  	
  	//VectorにDoubleオブジェクトを格納した場合
    data = new Vector<Object>();
    for (int i=0; i<10; i++) {
    	data.addElement(new Double(i));
    }
    MatrixObj E = new MatrixObj(data);
    E.display("E");
    E.Transport().display("TrE");
    data.removeAllElements();
    
    //VectorにMatrixObjを格納した場合(縦)
    data = new Vector<Object>();
    for (int i=0; i<10; i++) {
    	data.addElement(new MatrixObj(5,1));
    }
    MatrixObj F = new MatrixObj(data);
    F.display("F");
    F.Transport().display("TrF");
    data.removeAllElements();
    
    //VectorにMatrixObjを格納した場合(横)
    data = new Vector<Object>();
    for (int i=0; i<2; i++) {
    	data.addElement(new MatrixObj(1,5));
    }
    MatrixObj G = new MatrixObj(data);
    G.display("G");
    G.Transport().display("TrG");
    
    setup();
    //C = func.MultiplyMatrix(A,B);
    //C = func.AddMatirx(A,A);
    //C = func.DiffMatrix(A,A);
    //C = func.TransportMatrix(B);
    System.out.println("classname is " + E.getClass().getName().equals("matrix.DiagonalMatrixObj"));
    //C = func.InverseMatrix(A);
    //MC.InverseMatrix(L,A,B);
    A.display("A");
    System.out.println("detA=" + A.det());
    B.display("B");
    //C.display("inverse of A");
    
    
    
    try {
		//B.inverse().display("B inverse");
    	//A.Transport().display("A^T");
		C = A.PseudoInverse();
		C.display("Pseudo Inverse A");
		A.multiply(C).display("AxC");
	} catch (MatrixException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    System.out.println("det B = " + B.det());
    System.out.println("det A = " + A.det());
    try {
		B.multiply(B.inverse()).display("どうかな");
	} catch (MatrixException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    D = func.MultiplyMatrix(A,C);
    D.display("A*A^-1");
    D.set_data(0,0,2); D.set_data(0,1, 1);
    D.set_data(1,0,1); D.set_data(1,1,2);
    D.display("D");
    Jacobi jaco = new Jacobi(D.getL(), D.getMatrix());
    try {
    	jaco.jacobi();
    }catch(MatrixException ex) {
    	ex.printStackTrace();
    }
    VectorFunctions.display_vector("min eigen vector", jaco.get_min_eigen_vector());
    System.out.println("min eigen value is " + jaco.get_eigen_value(jaco.getMinEigenVectorIndex()));
    VectorFunctions.display_vector("max eigen vector", jaco.get_max_eigen_vector());
    System.out.println("max eigen value is " + jaco.get_eigen_value(jaco.getMaxEigenVectorIndex()));    
    //MatrixObj s = new MatrixObj(1,jaco.get_min_eigen_vector());
    MatrixCalc mc = new MatrixCalc();
    double[] c = new double[2];
    D.display("D");
    
    mc.MultiplyMatrix(2,2,D.getMatrix(), jaco.get_min_eigen_vector(),c);    
    VectorFunctions.display_vector("multiply", c);
  }

  

  void setup() {
    for (int l=0; l<L; l++) {
      err[l] = 0.1 * l;
      for (int m=0; m<M; m++) {
        for (int n=0; n<N; n++) {
        	A.set_data(l,m,l+m);
          B.set_data(m,n,(double)(n+2*m));
        }
      }
    }
  }

  public static void main(String[] args) {
    new test();
  }
}