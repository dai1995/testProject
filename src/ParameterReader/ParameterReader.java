package ParameterReader;

/**
 * <p>�����ȥ�: ILS</p>
 * <p>����: Incremental Learning with Sleep
 * <p>���: Copyright (c) 2002</p>
 * <p>���̾: Hokkaido University</p>
 * @author ̤����
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
  
  //name�ǻ��ꤵ�줿�����ΥΡ��ɤ��֤���
  //file��close/open�Ϥ������ٹԤ��뤿�ᡢname���Ѥ��Ʋ��٤Ǥ�ƤӽФ����Ȥ��Ǥ��롣
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
  
/* ����
<?xml version="1.0"?>
<parameter> <--�����Ϥʤ�Ǥ��ɤ�̾���Υ���  ����̾��������Ȥ��� Reader(̾��)��¹�
<RAN> <--�������ɤ߹��ߤ����⥸�塼���̾������
     <NumberOfInputs>12</NumberOfInputs>
     <NumberOfOutputs>1</NumberOfOutputs> <--�����դ�Υ����ϳơ��Υ⥸�塼��Υѥ�᡼����ʬ��������Ƥ͡�RAN.ran_parameter.java����
     <AllocThreshold>0.0</AllocThreshold>
     <MinDistance>0.0001</MinDistance>
     <MaxDistance>0.3</MaxDistance>
     <learningSpeed>0.01</learningSpeed>
     <RatioOfOverlap>1</RatioOfOverlap>
     <InitCovValue>0.1</InitCovValue>
     <DecayRatioAllocDistance>0.001</DecayRatioAllocDistance>
     <DecayRatioMSE>0</DecayRatioMSE>
</RAN>
<MRAN> <--�������ɤ߹��ߤ����⥸�塼���̾������
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
