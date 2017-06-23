package matrix;

/**
 * ����θ�ͭ�٥��ȥ�ȸ�ͭ�ͤ����
 * @author Koichiro Yamauchi
 * @version 1.0
 */

public class Jacobi {
  final double EPS = 0.0000001D;
  final int MAX_ITER = 100;
  final double TINY = 0.00000000001D;

  double a[][];//���ι�������(�ǽ�Ū�ˤ�a[k][k]�˸�ͭ�ͤ�����)
  double w[][];//��ͭ�٥��ȥ뤬���� w[k][0..n]��k���ܤθ�ͭ�٥��ȥ�
  //â���ܥץ����Ǥ��¤��Ѥ��ϹԤ�ʤ����� MaxEigenVectorIndex�����Ǥ������ͭ�٥��ȥ�Ȥʤ�
  int N;
  int DecendOrderIndex[];//�����ͭ�ͤ���Ǿ��ޤǤ�0��N-1�����Ǥǥ���ǥå�����ɽ����(2008ǯ11��18���ɲ�)
  //���ʤ���������ͭ�٥��ȥ��w[DecendOrderIndex[0]], �����礭����ͭ�ͤ�w[DecendOrderIndex[1]]�Ȥʤ롣

  public Jacobi(int n) {
    this.N = n;
    this.a = new double[this.N][this.N];
    this.w = new double[this.N][this.N];
    this.DecendOrderIndex = new int[this.N];
  }

  public Jacobi(int n, double import_matrix[][]) {
    this.N = n;
    this.w = new double[this.N][this.N];
    this.a = new double[this.N][this.N];
    this.DecendOrderIndex = new int[this.N];    
    //this.a = import_matrix;
    for (int i=0; i<this.N; i++) {
        for (int j=0; j<this.N; j++) {
        	this.a[i][j] = import_matrix[i][j];
        	//System.out.println("jaco A(" + i + "," + j + ")=" + this.a[i][j]);
        }
    }
  }

  public void set_matrix_a(int index_p, int index_q, double data) {
    if (index_p>this.N || index_q > this.N) this.error_and_stop("Jacobi: matrix index is out of bound!!");
    this.a[index_p][index_q] = data;
  }


  //��ͭ�٥��ȥ�����ؿ����Ρ�
  //�����¹Ԥ�������set_matrix_a��¹ԤΤ���
  public void jacobi() throws MatrixException {
    double tolerance, offdiag, s, c, t, upper_limit;
    int iter;
    //�����
    s = offdiag = 0D;
    for (int j=0; j<this.N; j++) {
      for (int k=0; k<this.N; k++) {
        w[j][k] = 0D;
      }
      w[j][j] = 1D; //�г����Ǥ�1�˽����
      for (int k=j+1; k<this.N; k++) {//���г����Ǥ��ͤ��礭����offdiag������
        offdiag += Math.pow(a[j][k], 2D);
      }
    }
    tolerance = EPS * EPS * (s/2 + offdiag);//��

    iter = 0;
    do {
      offdiag = 0D;
      for (int j=0; j<this.N-1; j++) {
        for (int k=0; k<this.N; k++) {
          offdiag += Math.pow(a[j][k],2D);
        }
      }
      iter++;

      for (int j=0; j<this.N-1; j++) {
        for (int k=j+1; k<this.N; k++) {
          if (Math.abs(a[j][k])>this.TINY) {
            t = (a[k][k] - a[j][j]) / (2*a[j][k]);
            if (t>=0) {
              t = 1 / (t + Math.sqrt(t*t+1));
            }else{
              t = 1 / (t - Math.sqrt(t*t+1));
            }
            c = 1 / Math.sqrt(t * t + 1);
            s = t * c;
            t *= a[j][k];
            a[j][j] -= t; a[k][k] += t; a[j][k] = 0D;
            for (int i=0; i<j; i++)   this.rotate(a, i, j, i, k, c, s);
            for (int i=j+1; i<k; i++) this.rotate(a, j, i, i, k, c, s);
            for (int i=k+1; i<this.N; i++) this.rotate(a, j, i, k, i, c, s);
            for (int i=0; i<this.N; i++) this.rotate(w, j, i, k, i, c, s);
          }//fi
        }// for k
      }// for j
    }while(offdiag >= tolerance && iter < this.MAX_ITER);


    t = 0D;
    upper_limit = 999999999999999999D;
    s = 100000000D;
    for (int k=0; k<this.N; k++) {
    	t = 0D;
    	for (int j=0; j<this.N; j++) {

    		if (Math.abs(a[j][j]) > t && Math.abs(a[j][j]) < upper_limit) {
    			this.DecendOrderIndex[k] = j;//��k���ܤ��礭����ͭ�٥��ȥ�Υ���ǥå���������(2008ǯ11��18���ɲ�)
    			t = Math.abs(a[j][j]);
    		}
    	}
    	upper_limit = Math.abs(a[this.DecendOrderIndex[k]][this.DecendOrderIndex[k]]);
    }
    //System.out.println("min eigen value = " + s + " max = " + t);
    /*if (s > t) {
    	for (int k=0; k<this.N; k++) {
    		System.out.println("abs(a[" +k+"][" + k + "])=" + Math.abs(a[k][k]));
    	}
    	throw new MatrixException("Jacobi.jacobi(): abnormal result!");
    }*/

  }

  private void rotate(double a[][], int i, int j, int k, int l, double c, double s) {
    double x = a[i][j];
    double y = a[k][l];
    a[i][j] = x*c - y*s;
    a[k][l] = x*s + y*c;
  }

  //�����ͭ�ͤθ�ͭ�٥��ȥ뤬���Ϥ����
  public double[] get_max_eigen_vector() {
    return this.get_eigen_vector(this.DecendOrderIndex[0]);
  }
  //�����ͭ�ͤ��֤�
  public double get_max_eigen_value() {
  	return this.a[this.DecendOrderIndex[0]][this.DecendOrderIndex[0]];
  }
  
  //�Ǿ���ͭ�ͤθ�ͭ�٥��ȥ뤬���Ϥ����  
  public double[] get_min_eigen_vector() {
    //System.out.println("get_min_eigen_Vector: mineigenvectorindex is " + this.MinEigenVectorIndex);
    return this.get_eigen_vector(this.DecendOrderIndex[this.N-1]);
  }
  //�Ǿ���ͭ�ͤ��֤�
  public double get_min_eigen_value() {
  	return this.a[this.DecendOrderIndex[this.N-1]][this.DecendOrderIndex[this.N-1]];
  }

  //k�֤�θ�ͭ�ͤ��֤���â������ͭ�ͤ��羮�Ȥ�̵�ط������Τޤޤν����
  public double get_eigen_value(int k) {
    return this.a[k][k];
  }

  //����k���ܤ��礭�ʡ׸�ͭ�ͤ��֤���k=0��N-1����� (2008ǯ11��18���ɲ�)
  public double get_DescentOrderEigenValue(int k) {
  	int i = this.DecendOrderIndex[k];
  	return this.a[i][i];
  }
  
  //k�֤�θ�ͭ�٥��ȥ���֤���â������ͭ�ͤ��羮�Ȥ�̵�ط������Τޤޤν����
  public double[] get_eigen_vector(int k) {
    return this.w[k];
  }
  
  //����k���ܤ��礭�ʡ׸�ͭ�٥��ȥ���֤���k=0��N-1�����  (2008ǯ11��18���ɲ�)
  public double[] get_DescentOrderEigenVector(int k) {
  	return this.w[this.DecendOrderIndex[k]];
  }

  //�����ͭ�ͤ��֤�
  public int getMaxEigenVectorIndex() {
  	return this.DecendOrderIndex[0];
  }
  
  //�Ǿ���ͭ�ͤθ�ͭ�٥��ȥ���֤�
  public int getMinEigenVectorIndex() {
  	return this.DecendOrderIndex[this.N-1];
  }
  
  void error_and_stop(String message) {
    System.err.println(message);
    System.exit(1);
  }
}