package Delta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;


public class LgrnnMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Scanner keyBoardScanner = new Scanner(System.in);
		int count=1;

		/*//データの入力数の入力
		System.out.println("input");
		int kernels = keyBoardScanner.nextInt();

		//データの入力次元数の入力
		System.out.println("dimensional-input");
		int dimensional_input = keyBoardScanner.nextInt() ;
		*/

		long start = System.currentTimeMillis();

		int kernels = 50;				//入力数
		int dimensional_input = 2;		//入力次元数
		double sigma = 0.4;				//σ値

		Learner ln = new Learner(kernels,dimensional_input,sigma);

		int kernelcount = kernels * (dimensional_input + 1);	//データセットのデータを格納する配列の個数
        double data[] = new double[kernelcount];				//データセットを格納する配列
        int countdata=0;										//データセットの読み込み時の配列の添え字

        //データセットの読み込み
        try{
            File file = new File("C:\\Users\\kazu123\\log\\learningsample\\ActorSample0.dat"); //サーバーでの実行:/home/ylabo/dataset/ConcreteData/データセットファイル名
            //C:\Users\kazu123\log\learningsample
            if (checkBeforeReadfile(file)){
              BufferedReader br = new BufferedReader(new FileReader(file));

              String str;
              while((str = br.readLine()) != null){


              	 StringTokenizer str1 = new StringTokenizer(str," ");
              	 while(str1.hasMoreTokens()) {

              			data[countdata] = Double.parseDouble(str1.nextToken());

              		 countdata++;
              	 }
              }

              br.close();
            }else{
              System.out.println("ファイルが見つからないか開けません");
            }
          }catch(FileNotFoundException e){
            System.out.println(e);
          }catch(IOException e){
            System.out.println(e);
          }

        //データの中身の確認用
        /*for(int c=0;c<kernelcount;c++){
        	System.out.println("data[" + c + "]=" + data[c]);
        }*/


		int datacount=0;		//入力配列の添え字

		while(count <= (kernels)){ //性能テストの場合: (count <= (kernels * 2))
			System.out.println("count:" + count);
			double input1 = (data[datacount]);
			double input2 = (data[datacount+1]);
//			double input3 = (data[datacount+2]);
//			double input4 = (data[datacount+3]);
//			double input5 = (data[datacount+4]);
//			double input6 = (data[datacount+5]);
//			double input7 = (data[datacount+6]);
//			double input8 = (data[datacount+7]);
//			double input9 = (data[datacount+8]);
//			double input10 = (data[datacount+9]);
//			double input11 = (data[datacount+10]);
//			double input12 = (data[datacount+11]);
			//double input13 = (data[datacount+12]);
			/*double input14 = (data[datacount+13]);
			double input15 = (data[datacount+14]);
			double input16 = (data[datacount+15]);
			double input17 = (data[datacount+16]);
			double input18 = (data[datacount+17]);
			double input19 = (data[datacount+18]);
			double input20 = (data[datacount+19]);
			double input21 = (data[datacount+20]);
			double input22 = (data[datacount+21]);
			double input23 = (data[datacount+22]);
			double input24 = (data[datacount+23]);
			double input25 = (data[datacount+24]);
			double input26 = (data[datacount+25]);
			double input27 = (data[datacount+26]);
			double input28 = (data[datacount+27]);
			double input29 = (data[datacount+28]);
			double input30 = (data[datacount+29]);
			double input31 = (data[datacount+30]);
			double input32 = (data[datacount+31]);
			double input33 = (data[datacount+32]);
			double input34 = (data[datacount+33]);
			double input35 = (data[datacount+34]);*/

			/*double[] x = {input1,input2,input3,input4,input5,input6,input7,input8,input9,input10,
							input11,input12,input13,input14,input15,input16,input17,input18,input19,input20,
							input21,input22,input23,input24,input25,input26,input27,input28,input29,input30,
							input31,input32,input33,input34,input35};//harta1
			double answer = data[datacount+35];	 */ //harta1


			//double[] x = {input1,input2,input3,input4,input5,input6,input7,input8,input9,input10,input11,input12,input13};//housing
			//double answer = data[datacount+13];

			double[] x = {input1,input2};//servo
			double answer = data[datacount+2];

			//double[] x = {input1,input2,input3,input4,input5,input6,input7,input8,input9};//cpu-performance
			//double answer = data[datacount+9];

			//double[] x= {input1,input2,input3,input4,input5,input6,input7,input8};  //Concrete
			//double answer = data[datacount+8];

			//double[] x= {input1,input2,input3,input4,input5,input6,input7};  //mpg
			//double answer = data[datacount+7];

			ln.output(x, answer);

			count++;
			datacount += (dimensional_input + 1);	//入力数+出力の数プラス

			/*if(datacount == kernelcount){	//性能テストの場合に使用
				datacount = 0;
			}*/
		}

		long end = System.currentTimeMillis();
		System.out.println("実行時間：" + (end - start)  + "ms");

		}

	//データセットの読み込みに必要
	private static boolean checkBeforeReadfile(File file){
	    if (file.exists()){
	      if (file.isFile() && file.canRead()){
	        return true;
	      }
	    }

	    return false;
	  }

}
