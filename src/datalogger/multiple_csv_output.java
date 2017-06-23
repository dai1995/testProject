package datalogger;

import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Node;

public class multiple_csv_output extends multiple_dataOutput {
	double data[][];
	String label_x[], label_y[];
	int NumberOfX, NumberOfY;
	int label_index_x=0, label_index_y=0;
	String kugiri = ",";
	
	public multiple_csv_output(Node nd) {
		super(nd);
	}
	
	public void init_data_area(int NumberOfX, int NumberOfY) {
		this.NumberOfX = NumberOfX;
		this.NumberOfY = NumberOfY;
		this.data = new double[this.NumberOfY][this.NumberOfX];
		this.label_x = new String[this.NumberOfX];
		this.label_y = new String[this.NumberOfY];
		this.label_index_x = 0;
		this.label_index_y = 0;
	}

	public void open() {
		if (this.isEnable) {
			String filename = this.head_filename + this.NumberOfSegment + ".csv";
			try {
				wfp = new FileWriter(filename);
				this.NumberOfSegment ++;
			}catch(IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
	
	public void putLabelX(int index_x, String str) {
		label_x[index_x] = str;
	}
	
	public void putLabelY(int index_y, String str) {
		label_y[index_y] = str;
	}
	
	public void putData(int x, int y, double data) {
		this.data[y][x] = data;
	}
	
	public void put_csv() {
	    	if (this.isEnable) {
	    		try {
	    			for (int y=0; y<=this.NumberOfY; y++) {
	    				
	    				for (int x=0; x<=this.NumberOfX; x++) {
	    					if (y==0) {
	    						if (x==0) {
	    							wfp.write(this.kugiri);
	    						}else{
	    							wfp.write(this.label_x[x-1] + this.kugiri);
	    						}
	    					}else{
	    					  if (x==0) {
	    						if (y==0) {
	    							wfp.write(this.kugiri);
	    						}else{
	    							wfp.write(this.label_y[y-1] + this.kugiri);
	    						}
	    					  }else{
	    						  wfp.write(String.valueOf(this.data[y-1][x-1]) + this.kugiri);
	    					  }
	    					}
	    				}
	    				wfp.write("\n");
	    			}
	    		}catch(IOException ioex) {
	    			ioex.printStackTrace();
	    		}
	    	}
	}		
}
