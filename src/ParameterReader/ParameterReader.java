package ParameterReader;

/**
 * <p>タイトル: ILS</p>
 * <p>説明: Incremental Learning with Sleep
 * <p>著作権: Copyright (c) 2002</p>
 * <p>会社名: Hokkaido University</p>
 * @author 未入力
 * @version 1.0
 */
//import java.util.Hashtable;

//import java.io.FileInputStream;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ParameterReader {
  String data = null;
  String filename;
  
  public ParameterReader(String filename) {
  	this.filename = filename;
  }
  
  //nameで指定されたタグのノードを返す。
  //fileのclose/openはその都度行われるため、nameを変えて何度でも呼び出すことができる。
  public Node Reader(String name) {
  	Node child_node;
    try {
        //FileInputStream is = new FileInputStream(this.filename);
        DOMParser parser = new DOMParser();
        parser.parse(this.filename);
        Document doc = parser.getDocument();        
        for (Node nd = doc.getFirstChild(); nd!=null;
    		nd = nd.getNextSibling()) {
        	for (child_node = nd.getFirstChild(); child_node!=null;
        		child_node = child_node.getNextSibling()) {
        		if (child_node.getNodeName().equals(name)) {
        			return child_node;
        		}
        	}
        }
        //is.close();
        return null;
    }catch(Exception ex) {
    	ex.printStackTrace();
    	return null;
    }
  }
  
/* 一例
<?xml version="1.0"?>
<parameter> <--ここはなんでも良い名前のタグ  この名前を引数として Reader(名前)を実行
<RAN> <--ここは読み込みたいモジュールの名前タグ
     <NumberOfInputs>12</NumberOfInputs>
     <NumberOfOutputs>1</NumberOfOutputs> <--この辺りのタグは各々のモジュールのパラメータ部分で定義してね。RAN.ran_parameter.java参照
     <AllocThreshold>0.0</AllocThreshold>
     <MinDistance>0.0001</MinDistance>
     <MaxDistance>0.3</MaxDistance>
     <learningSpeed>0.01</learningSpeed>
     <RatioOfOverlap>1</RatioOfOverlap>
     <InitCovValue>0.1</InitCovValue>
     <DecayRatioAllocDistance>0.001</DecayRatioAllocDistance>
     <DecayRatioMSE>0</DecayRatioMSE>
</RAN>
<MRAN> <--ここも読み込みたいモジュールの名前タグ
     <NumberOfInputs>12</NumberOfInputs>
     <NumberOfOutputs>1</NumberOfOutputs>
     <AllocThreshold>0.0</AllocThreshold>
     <MinDistance>0.0001</MinDistance>
     <MaxDistance>0.3</MaxDistance>
     <learningSpeed>0.01</learningSpeed>
     <RatioOfOverlap>1</RatioOfOverlap>
     <InitCovValue>0.1</InitCovValue>
     <DecayRatioAllocDistance>0.001</DecayRatioAllocDistance>
     <DecayRatioMSE>0</DecayRatioMSE>

     <FIFOLENGTH>20</FIFOLENGTH>
     <MSEAllocThreshold>0.01</MSEAllocThreshold>
     <PerformanceThreshold>0.5</PerformanceThreshold>
     <ConstructiveObservation>20</ConstructiveObservation>
</MRAN>
</parameter>

 */
}
