package FIFO;

/**
 * <p>タイトル: Extra Extended  Kalman Filter</p>
 * <p>説明: non-iidで現れるパターンを学習するEKF</p>
 * <p>著作権: Copyright (c) 2004</p>
 * <p>会社名: Graduate School of Information Science and Technology</p>
 * @author Koichiro Yamauchi
 * @version 1.0
 */


public class FIFO2D {
  double input_buffer[][];
  double output_buffer[][];
  double error[][], square_error[];
  boolean flag[];

  int BufferSize;
  int RecordNumber;
  int NumberOfInputs;
  int NumberOfOutputs;
  int current_index, max_index;
  int remained_instances;

  public FIFO2D(int buffersize, int NumberOfInputs, int NumberOfOutputs) {
    this.BufferSize = buffersize;
    this.NumberOfInputs = NumberOfInputs;
    this.NumberOfOutputs = NumberOfOutputs;
    this.max_index =0;
    this.RecordNumber = 0;

    input_buffer = new double[this.BufferSize][this.NumberOfInputs];
    output_buffer = new double[this.BufferSize][this.NumberOfOutputs];
    error = new double[this.BufferSize][this.NumberOfOutputs];
    square_error = new double[this.BufferSize];
    flag = new boolean[this.BufferSize];
    this.remained_instances = this.BufferSize;
    this.reset_shuffle_flag();
  }


  public void reset_fifo() {
    this.current_index = 0;
    for (int i=0; i<this.BufferSize; i++) {
      for (int j=0; j<this.NumberOfInputs; i++) {
        this.input_buffer[i][j] = 0D;
      }
      for (int j=0; j<this.NumberOfOutputs; j++) {
        this.output_buffer[i][j] = 0D;
      }
      this.flag[i] = false;
    }
  }
  
  public void reset_shuffle_flag() {
    for (int i=0; i<this.BufferSize; i++) {
    	this.flag[i] = false;
    }
    this.remained_instances = this.get_max_index();
    //System.out.println("FIFO2D reset flag!");
  }
  
  public int shuffled_index() {
  	int target;
  	int j=0;
  	
  	target = (int)(Math.random() * this.remained_instances);
  	//System.out.println(" shuffled index target is " + target);
  	j= target;
  	while (this.flag[j] && j<this.remained_instances) j++;
  	if (this.flag[j]==true && j==this.remained_instances) {
  		j=0;
  	  	while (this.flag[j] && j<this.remained_instances) j++;  		
  	}
  	this.flag[j] = true;
  	//System.out.println(" shuffled index is " + j + " max index is " + this.get_max_index());
	return j;
  }

  public int get_max_index() {
    return this.max_index;
  }

  public void push_data(double input_data[], double output_data[], double err[]) {
    this.RecordNumber ++;
    if (this.RecordNumber > this.BufferSize) this.RecordNumber = this.BufferSize;

    for (int i=0; i<this.NumberOfInputs; i++) {
      this.input_buffer[this.current_index][i] = input_data[i];
    }
    for (int i=0; i<this.NumberOfOutputs; i++) {
      this.output_buffer[this.current_index][i] = output_data[i];
    }
    this.error[this.current_index] = err;
    this.square_error[this.current_index] = this.get_square(err);
    this.current_index = this.get_min_error_index();

    if (this.max_index < this.current_index) this.max_index = this.current_index;
    //保護
    if (this.current_index >= this.BufferSize || this.current_index < 0) {
      this.current_index = 0;
      this.max_index = this.BufferSize;
    }
  }

  //エラーをぷっしゅしないもの
  public void push_data(double input_data[], double output_data[]) {
    this.RecordNumber ++;
    if (this.RecordNumber > this.BufferSize) this.RecordNumber = this.BufferSize;

    for (int i=0; i<this.NumberOfInputs; i++) {
      this.input_buffer[this.current_index][i] = input_data[i];
    }
    for (int i=0; i<this.NumberOfOutputs; i++) {
      this.output_buffer[this.current_index][i] = output_data[i];
    }
    this.current_index++;

    if (this.max_index < this.current_index) this.max_index = this.current_index;
    //保護
    if (this.current_index >= this.BufferSize || this.current_index < 0) {
      this.current_index = 0;
      this.max_index = this.BufferSize;
    }
  }
  
  public void reset_err(int index, double err[]) {
    for (int i=0; i<this.NumberOfOutputs; i++) {
      this.error[index][i] = err[i];
    }
  }

  public double getMeanSquareError() {
  	double sum=0D;
  	
  	for (int i=0; i<this.get_max_index(); i++) {
  		//sum += this.get_square(this.getErrors(i));
  		sum += Math.pow(this.getSqrtError(i),2D);
  	}
  	sum /= (double)this.get_max_index();
  	return sum;
  }
  
  public void estimated_push_data(double input_data[], double output_data[], double err[], double square_error) {
    int min_index;

    if (this.max_index == this.BufferSize-1) {
      min_index = this.get_min_error_index();
      if (min_index < 0) {
        min_index = 0;
      }
      this.current_index = min_index;
      //System.out.println("min index is  " + min_index);
      //if (this.square_error[min_index] < square_error) {
        for (int i = 0; i < this.NumberOfInputs; i++) {
          this.input_buffer[min_index][i] = input_data[i];
        }
        for (int i = 0; i < this.NumberOfOutputs; i++) {
          this.output_buffer[min_index][i] = output_data[i];
        }
        this.reset_err(min_index, err);

        this.square_error[min_index] = this.get_square(err);
        this.current_index = this.get_min_error_index();
        this.max_index++;
        //保護
        if (this.max_index >= this.BufferSize) {
          this.max_index = this.BufferSize - 1;
        }
      //}
    }else{

      for (int i=0; i<this.NumberOfInputs; i++) {
        this.input_buffer[this.current_index][i] = input_data[i];
      }
      for (int j=0; j<this.NumberOfOutputs; j++) {
        this.output_buffer[this.current_index][j] = output_data[j];
      }
      this.reset_err(current_index, err);

      this.current_index ++;
      if (this.max_index < this.current_index) this.max_index = this.current_index;
      if (this.current_index >= this.BufferSize) {
        this.current_index = this.BufferSize-1;
        this.max_index = this.BufferSize-1;
      }

    }
  }


  public void set_error(int index, double err[], double square_error) {
    this.error[index] = err;
    this.square_error[index] = square_error;
  }

  public double[] getInputData(int index) {
    return this.input_buffer[index];
  }

  public double[] getOutputData(int index) {
    return this.output_buffer[index];
  }

  public double[] getErrors(int index) {
    return this.error[index];
  }

  public double getSqrtError(int index) {
  	return this.square_error[index];
  }
  
  public int getBufferSize() {
    return this.RecordNumber;
  }

/*
  public int getBufferSize() {
    return this.BufferSize;
  }
*/



  int get_min_error_index() {
    double min_error=100000000D;
    int min_index=-1;
    for (int i=0; i<this.BufferSize; i++) {
      if (this.square_error[i] < min_error) {
        min_index = i;
        min_error = this.square_error[i];
      }
    }
    return min_index;
  }

  double get_square(double data[]) {
    double sum = 0D;
    for (int i=0; i<this.NumberOfOutputs; i++) {
      sum += Math.pow(data[i],2D);
    }
    return sum;
  }
}