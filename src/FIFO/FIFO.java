package FIFO;

/**
 * <p>タイトル: ILS without GUI</p>
 * <p>説明: ILS witout X window</p>
 * <p>著作権: Copyright (c) 2002</p>
 * <p>会社名: Hokkaido University</p>
 * @author Koichiro Yamauchi
 * @version 1.0
 */

public class FIFO {
  double buffer[];
  int buffer_size;
  int actual_buffer_size;
  int current_index;
  public FIFO(int buffer_size) {
    this.buffer_size = buffer_size;
    buffer = new double[buffer_size];
  }

  public void reset_fifo() {
    this.current_index = 0;
    this.actual_buffer_size = 0;
    for (int i=0; i<this.buffer_size; i++) {
      buffer[i] = 0D;
    }
  }

  public void push_data(double data) {
    buffer[this.current_index] = data;
    this.current_index ++;
    if (this.current_index >= this.buffer_size) {
      this.current_index = 0;
    }
    this.actual_buffer_size ++;
    if (this.actual_buffer_size >= this.buffer_size) {
    	this.actual_buffer_size = this.buffer_size;
    }
  }

  public double getData(int index) {
	  return buffer[index];
  }
  
  public double get_mean_fifo() {
    double sum=0D;
    for (int i=0; i<this.buffer_size; i++) {
      sum += buffer[i];
    }
    //sum /= (double)this.buffer_size;
    sum /= (double)this.actual_buffer_size;
    return sum;
  }
  
  public double get_std_fifo() {
    double sum=0D;
    double mean = get_mean_fifo();
    for (int i=0; i<this.buffer_size; i++) {
      sum += Math.pow((buffer[i]-mean),2D);
    }
    sum /= (double)this.buffer_size;
    sum = Math.sqrt(sum);
    return sum;
  }
  
}