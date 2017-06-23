	package matrix;

/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2002</p>
 * <p>会社名: </p>
 * @author 未入力
 * @version 1.0
 * 
 */

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import VectorFunctions.VectorFunctions;

public class MatrixObj implements Cloneable {
  final boolean DEBUG = true;
  int L, M; // L: number of vartical lines, M: number of horizontal lines 
  public double matrix[][];
  public final int VarticalVector=1;
  public final int HorizontalVector=2;
  MatrixCalc mc;
  
  //Following subclass checks whether the specified value elements exists in the matrix except for its diagonal elements or not.
  //This method is used for checking whether the (pseudo) inverse matrix can be derived or not.
  public class CheckSpecifiedValueElement {
	  private int e_i=-1,e_j=-1; 
	  private boolean found=false;
	  
	  public CheckSpecifiedValueElement(MatrixObj m, double target_value) {
		  for (int i=0; i<m.getL(); i++) {
			  for (int j=0; j<m.getM(); j++) {
				  if (target_value == m.getData(i, j) && i != j) {
					  e_i = i; e_j = j;
					  found = true;
					  break;
				  }
			  }
		  }
	  }
	/**
	 * @return the e_i
	 */
	  public int getE_i() {
		return e_i;
	  }
	/**
	 * @return the e_j
	 */
	public int getE_j() {
		return e_j;
	}
	/**
	 * @return the found
	 */
	public boolean isFound() {
		return found;
	}
  }//CheckSpecifiedValueElement
  
  CheckSpecifiedValueElement check_s_value = null;
  
  public MatrixObj(int L, int M) {
    this.L = L;
    this.M = M;
    this.mc = new MatrixCalc();  	
    allocate_data(L,M);
  }

  //バイアス項として横の最後の要素に1が入る。引数WithBiasはコンストラクターの区別をつけるだけのものでTrue Faulseいずれを入れてもOK
  public MatrixObj(int L, int M, boolean WithBias) {
    this.mc = new MatrixCalc();  	
    this.L = L;
    this.M = M+1;
    allocate_data(L,M+1);
    for (int i=0; i<L; i++) {
    	this.set_data(i,this.M-1,1D); //set bias
    }
  }
  
  //横ベクトルを作るに等しい
  public MatrixObj(int M, double data[], int type) {
    this.mc = new MatrixCalc();  	
  	if (type == this.HorizontalVector) {
  		this.L = 1;
  		this.M = M;
  		allocate_data(1,M);
  		for (int m=0; m<M; m++) {
  			this.set_data(0,m,data[m]);
  		}
  	}else{
  		if (type == this.VarticalVector) {
  	  		this.L = M;
  	  		this.M = 1;
  	  		allocate_data(M, 1);
  	  		for (int m=0; m<M; m++) {
  	  			this.set_data(m,0,data[m]);
  	  		}
  		}else{
  			System.err.print("Invarid Vector Type! VectorType 1: Vartical, 2: Horizontal");
  			System.exit(1);
  		}
  	}
  }

  //バイアス項として横ベクトルの最後の要素に1が入る。引数WithBiasはコンストラクターの区別をつけるだけのものでTrue Faulseいずれを入れてもOK
  public MatrixObj(int M, double data[], boolean WithBias) {
    this.mc = new MatrixCalc();  	
    this.L = 1;
    this.M = M+1;
    allocate_data(1,this.M);
    for (int m=0; m<this.M-1; m++) {
      this.set_data(0,m,data[m]);
    }
    this.set_data(0,this.M-1,1D);
  }
  
  //lに1を指定すると横ベクトルを作って要素にdata[]を代入。同様にMを1とすると縦ベクトルを作って要素にdata[]を代入
  public MatrixObj(int l, int M, double data[]) {
    this.L = l;
    this.M = M;
    this.mc = new MatrixCalc();  	
    allocate_data(1,M);
    if (l==1) {
    	for (int m=0; m<M; m++) {
    		this.set_data(0,m,(double)data[m]);
    	}
    }else if (M==1) {
    	for (int j=0; j<L; j++) {
    		this.set_data(j,0, (double)data[j]);
    	}
    }else{
    	System.err.println("MatrixObj: l or m must be 1!");
    	System.exit(1);
    }
  }

  //lに1を指定すると横ベクトルを作る。Mを1とすると縦ベクトルを作るが、WithBias=trueの場合には最後の要素にバイアス項(要素は1)を作る。
  public MatrixObj(int L, double data[], boolean vartical, boolean WithBias) {
    this.mc = new MatrixCalc();  	
    if (!vartical) { //横ベクトル
        this.L = 1;
        if (WithBias) this.M = L+1;
        else this.M = L;
        allocate_data(1,this.M);
        if (WithBias) {//バイアスあり
        	for (int m=0; m<this.M-1; m++) {
        		this.set_data(0,m,(double)data[m]);
        	}
        	this.set_data(0,this.M-1,(double)1);
        }else{//バイアス無し
        	for (int m=0; m<this.M; m++) {
        		this.set_data(0,m,(double)data[m]);
        	}
        }
    }else{ //縦ベクトル
    	this.M=1;
    	if (WithBias) this.L=L+1;
    	else this.L = L;
        allocate_data(this.L, 1);
    	if (WithBias) {
    		for (int j=0; j<this.L-1; j++) {
    			this.set_data(j,0, (double)data[j]);
    		}
    		this.set_data(this.L-1, 0, 1D);
    	}else{
    		for (int j=0; j<this.L; j++) {
    			this.set_data(j,0, (double)data[j]);
    		}
    	}
    }
  }
  
  //縦L行、横M列のベクトルにdata[][]を代入
  public MatrixObj(int L, int M, float data[][]) {
    this.mc = new MatrixCalc();  	
    this.L = L;
    this.M = M;
    allocate_data(L,M);
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        this.set_data(l,m,(double)data[l][m]);
      }
    }
  }

  //sourceと同じ行列をコピー
  public MatrixObj(MatrixObj source) {
    this.mc = new MatrixCalc();  	
    this.L = source.getL();
    this.M = source.getM();
    allocate_data(L,M);
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        this.set_data(l,m,(double)source.getData(l, m));
      }
    }
  }  
  
  /*
  public MatrixObj(int L, int M, double init_value) {
    this.L = L;
    this.M = M;
    allocate_data(L,M);
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        this.set_data(l,m,init_value);
      }
    }
  }*/


  public MatrixObj(int L, int M, double data[][]) {
    this.mc = new MatrixCalc();  	
    this.L = L;
    this.M = M;
    allocate_data(L,M);
    for (int l=0; l<L; l++) {
      for (int m=0; m<M; m++) {
        this.set_data(l,m,data[l][m]);
      }
    }
  }

  //vectorに入ったdouble型の値の列から縦ベクトル行列を作る.
  //vectorに縦ベクトルのMatrixが入っている場合にも対応
  //@SuppressWarnings("unchecked")
public MatrixObj(Vector<Object> v) {
  	int l,m;
    this.mc = new MatrixCalc();  	
    //System.out.println(v.get(0).toString());
  	if (v.get(0).getClass().toString().equals("class java.lang.Double")) {
  		//System.out.println("MatrixObj(vector):double verticalx1");
  		this.L = v.size();
  		this.M = 1;
  		allocate_data(L, M);
  		Enumeration<Object> e = v.elements();
  		l=0;
  		while (e.hasMoreElements()) {
  			Double data = (Double)(e.nextElement());
  			this.set_data(l,0,data.doubleValue());
  			l++;
  		}
  	}else if (v.get(0).getClass().toString().indexOf("MatrixObj")>1) {//matrixがベクターに格納されている場合
  		//System.out.println("pass 2");
  		MatrixObj each_vector = (MatrixObj)v.get(0);
  		if (each_vector.getL()>1 && each_vector.getM()==1) {//縦ベクトルmatrixの場合
  			this.L = each_vector.getL();
  			this.M = v.size();
  		    allocate_data(L,M);  			
  	  		//System.out.println("MatrixObj(vector):double vertical (" + this.L + "," + this.M +")");
  			m=0;
  			Enumeration<Object> e = v.elements();
  			while (e.hasMoreElements()) {
  				each_vector = (MatrixObj)e.nextElement();
  				//each_vector.display("each_vector");
  				for (l=0; l<each_vector.getL(); l++) {
  					this.set_data(l,m,each_vector.getData(l,0));
  				}
  				m++;
  			}
  		}else if (each_vector.getL()==1 && each_vector.getM()>1) { //横ベクトルmatrixの場合
  			this.L = v.size();
  			this.M = each_vector.getM();
  		    allocate_data(L,M);
  	  		//System.out.println("MatrixObj(vector):double horizontal (" + this.L + "," + this.M +")");  			
  			l = 0;
  			Enumeration<Object> e = v.elements();
  			while (e.hasMoreElements()) {
  				each_vector = (MatrixObj)e.nextElement();
  				for (m=0; m<each_vector.getM(); m++) {
  					this.set_data(l,m,each_vector.getData(0,m));
  				}
  				l++;
  			}
  		}else{
  			System.err.println("MatrixObj(Vector v): invalid objects are saved in the vector!");
  			System.exit(1);
  		}
  	}
  	
  	
  }
  
//縦ベクトルを作る(2016.5.15)
public MatrixObj(ArrayList<Double> desired) {
	// TODO Auto-generated constructor stub
	this.mc = new MatrixCalc();
	  this.allocate_data(desired.size(), 1);
	  this.Log("MatrixObj size of desired is " + desired.size());
	  for (int l=0; l<desired.size(); l++) {
		  this.matrix[l][0] = desired.get(l);
	  }
 }

 
private void Log(String string) {
	// TODO Auto-generated method stub
	if (this.DEBUG) {
		System.out.println("MatrixObj." + string);
	}
	
}

void allocate_data(int L, int M) {
    this.matrix = new double[L][M];
    this.L = L;
    this.M = M;
  }
  
  public void init2BIdentity(double init_data) {
  	if (this.L == this.M) {
  		for (int i=0; i<this.L; i++) {
  			this.matrix[i][i] = init_data;
  		}
  	}else{
  		System.err.println("MatrixObj.toIdentity() error: only NxN matrix achieves this method!");
  		System.exit(1);
  	}
  }
  
  public boolean IsExistInverse() {
	  this.check_s_value = new CheckSpecifiedValueElement(this, 1.0D);
	  return this.check_s_value.isFound();
  }
  public int[] AbnormalElements() {
	  int[] result = new int[2];
	  if (this.check_s_value == null) return null;
	  result[0] = this.check_s_value.getE_i();
	  result[1] = this.check_s_value.getE_j();
	  return result;
  }
  
  //要素を増やす newL, newM は新しいサイズ
  public void increase_allocate_data(int newL, int newM) {
  	double new_matrix[][];
  	if (newL >= this.L && newM >= this.M) {//安全対策
  		new_matrix = new double[newL][newM];
  		for (int i=0; i<newL; i++) {
  			for (int j=0; j<newM; j++) {
  				if (i<this.L && j<this.M) {
  					new_matrix[i][j] = this.matrix[i][j];//古いデータをコピー
  				}else{
  					//新しい部分は単位行列
  					if (i==j) {
  						new_matrix[i][j] = 1;
  					}else{
  						new_matrix[i][j] = 0;
  					}
  				}
  			}
  		}
  		this.matrix = new_matrix;
  		this.L = newL;
  		this.M = newM;
  	}
  }
  
  //要素を減らす target* 最初のポイント size* 減らすサイズ 
  public void decrease_allocate_data(int targetL, int size_L, int targetM, int size_M) {
  	double new_matrix[][];
  	int newL, newM, old_index_l, old_index_m;
  	newL = this.L - size_L;
  	newM = this.M - size_M;
  	new_matrix = new double[newL][newM];
  	for (int i=0; i<newL; i++) {
  		old_index_l = i;
  		if (i>=targetL) old_index_l += size_L; //読み飛ばす
  		for (int j=0; j<newM; j++) {
  			old_index_m = j;
  			if (j>=targetM) old_index_m += size_M; //読み飛ばす
  			new_matrix[i][j] = this.matrix[old_index_l][old_index_m];
  		}
  	}
  	this.L = newL; this.M = newM;
  	this.matrix = new_matrix;
  }
  

  public int getL() {
    return this.L;
  }

  public int getM() {
    return this.M;
  }

  public void set_array_data(double data[][]) {
  	for (int l=0; l<this.L; l++) {
  		for (int m=0; m<this.M; m++) {
  			this.matrix[l][m] = data[l][m];
  		}
  	}
  }
  
  public void set_data(int l, int m, double data) {
    this.matrix[l][m] = data;
  }

  public void add_data(int l, int m, double data) {
    this.matrix[l][m] += data;
  }

  public double getData(int l, int m) {
    return this.matrix[l][m];
  }

  public double[][] getMatrix() {
    return this.matrix;
  }

  public void display(String name) {
    System.out.println("------< display matrix " + name + " begin (" + this.getL() + " x " + this.getM() + ") >-------\n");
    for (int l=0; l<this.getL(); l++) {
      for (int m=0; m<this.getM(); m++) {
        System.out.print(this.getData(l,m) + ", ");
      }
      System.out.println("\n");
    }
    System.out.println("------<          end   " + name + "       >-------\n");    
  }

  //orthogonal matrixかどうかを返す
  public boolean isOrghogonal() {
    return false;
  }

  public void reset() {
    for (int l=0; l<this.getL(); l++) {
      for (int m=0; m<this.getM(); m++) {
        this.set_data(l,m,0D);
      }
    }
  }
  
  public MatrixObj Transport() {
  	return this.mc.TransPortMatrix_M(this);
  }
  
  public double getTrace() throws MatrixException {
  	double sum=0;
  	if (this.L == this.M) {
  		for (int i=0; i<this.L; i++) {
  			sum += this.getData(i,i);
  		}
  		return sum;
  	}else{
  		throw new MatrixException("MatrixObj.getTrace(): matrix size should be NxN");  		
  	}
  }
  
  //|| M ||を計算
  public double getNorm() throws MatrixException {
  	double sum = 0;
  	
  	// if the matrix represents a vector.
  	if (this.L==1 || this.M == 1) {
  		if (this.L>=1) {
  			for (int l=0; l<this.L; l++) {
  				sum += Math.pow(this.getData(l,0),2D);
  			}
  		}else if (this.M>=1) {
  			for (int m=0; m<this.M; m++) {
  				sum += Math.pow(this.getData(0,m),2D);  				
  			}
  		}
  		//System.out.println("MatrixObj getNorm pass");
  		return Math.sqrt(sum);
  	}else{ // if the matrix represets nxm matrix.
  		if (this.L>1 && this.M>1 && (this.L == this.M)) {//行列のノルム： max AX where ||x||=1 これは λmaxとなる。

  		  	Jacobi jacobi = new Jacobi(this.L, this.matrix);
  		  	jacobi.jacobi();
  		  	double eigen_vector_norm = VectorFunctions.getNorm(jacobi.get_max_eigen_vector());
  		  	if (eigen_vector_norm < 0.000000001) {
  		  	throw new MatrixException("MatrixObj.getNorm(): eigen vector is zero vector !!");
  		  	}
  		  	//System.out.println("MatixObj:getNorm(): eigen_vector_norm is " + eigen_vector_norm);
  		  	double MatrixNorm = jacobi.get_max_eigen_value()/ eigen_vector_norm;
  		  	return MatrixNorm;
  		}else{
  			throw new MatrixException("MatrixObj.getNorm(): must be NxN matrix!");
  		}
  	}
  }
  
  public double det() {
  	return mc.Det(this);
  }
  
 /* public MatrixObj inverse() {
  	try {
  		return mc.InverseMatrix_M(this);
  	}catch(MatrixException me) {
  		me.printStackTrace();
  		return null;
  	}
  }*/
  public MatrixObj inverse() throws MatrixException {
	  		return mc.InverseMatrix_M(this);
  }  
  public MatrixObj PseudoInverse() throws MatrixException {
	  		return mc.PseudoInverse(this);
  }
  
  public MatrixObj multiply(MatrixObj A) {
	  //this.display("this");
	  //A.display("A");
  	return mc.MultiplyMatrix_M(this, A);
  }
  public MatrixObj multiply(DiagonalMatrixObj A) {
	  	return mc.MultiplyMatrix_M(this, A);
  }  
  public MatrixObj multiply(double gain) {
	  MatrixObj result = new MatrixObj(this.getL(), this.getM());
	  for (int l=0; l<this.L; l++) {
		  for (int m=0; m<this.M; m++) {
			  result.set_data(l, m, gain * this.getData(l, m));
		  }
	  }
	  return result;
  }
  
  public MatrixObj Add(MatrixObj A) {
  	return mc.AddMatrix_M(this, A);
  }
  public MatrixObj Add(DiagonalMatrixObj A) {
  	return mc.AddMatrix_M(this, A);
  }
  
  public MatrixObj Diff(MatrixObj A) {
  	return mc.DiffMatrix_M(this, A);
  }
  
  public boolean Same(MatrixObj A) {
  	return mc.is_same(this, A);
  }
  
	public MatrixObj clone() {  
	    try {  
	          return (MatrixObj)super.clone();  
	    } catch (CloneNotSupportedException e) {  
	         return null;  
	    }  
	}    
}