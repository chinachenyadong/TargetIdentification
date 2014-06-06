package cyd.feature;

import java.io.*;
import java.util.*;

import cyd.node.Node;

/**
 * function of adding various features
 * @author chenyadong
 */
public class AddFeature
{
	public static void addFeatures(StringBuilder sb, Node node, int index, ArrayList<Node> nodeList, HashMap<String, Double> targetRateMap, HashMap<String, Double> smallTF, HashMap<String, Double> bigTF) throws IOException
	{
		// add lemma
		AddFeature.addLemma(sb, node);
//		AddFeature.addPos(sb, node);
		AddFeature.addTargetRate(sb, node, targetRateMap);
//		AddFeature.addContextPos(sb, index, nodeList, 1);
//		AddFeature.addContextLemmas(sb, index, nodeList, 1);
//		AddFeature.addNer(sb, node);
//		AddFeature.addLemmaPosJoint(sb, node);
		
//		AddFeature.addSmallTF(sb, node, smallTF);
//		AddFeature.addBigTF(sb, node, bigTF);
		
//		AddFeature.addDepNum(sb, node.getDep());
		AddFeature.addDep(sb, node.getChildDep());
//		AddFeature.addDepWord(sb, node.getChildDep());
		
		
		
		sb.append("\n");
	}

	public static void addDepWord(StringBuilder sb, ArrayList<String> list) throws IOException
	{
		int num = list.size();
		for (int i = 0; i < num; ++i) 
		{
			String[] strs = list.get(i).split(" : ");
			sb.append(" dep/" + strs[0] + ":1" + " DepWord/" + strs[2] + ":1");
		}
	}
	
	public static void addDep(StringBuilder sb, ArrayList<String> list) throws IOException
	{
		int num = list.size();
		for (int i = 0; i < num; ++i) 
		{
			String[] strs = list.get(i).split(" : ");
			sb.append(" dep/" + strs[0] + ":1");
		}
	}
	
	public static void addDepNum(StringBuilder sb, ArrayList<String> list) throws IOException
	{
		int num = list.size();
		sb.append(" DepNum:" + num);
	}
	
	public static void addSmallTF(StringBuilder sb, Node node, HashMap<String, Double> smallTF) throws IOException
	{
		String lemma = node.getLemma();
		double frequency = 0.0;
		if (smallTF.containsKey(lemma))
		{
			frequency = smallTF.get(lemma);
		}
		sb.append(" SmallTF:" + frequency);
	}
	
	public static void addBigTF(StringBuilder sb, Node node, HashMap<String, Double> bigTF) throws IOException
	{
		String lemma = node.getLemma();
		double frequency = 0.0;
		if (bigTF.containsKey(lemma))
		{
			frequency = bigTF.get(lemma);
		}
		sb.append(" BigTF:" + frequency);
	}
	
	public static void addContextPos(StringBuilder sb, int index, ArrayList<Node> nodeList, int len) throws IOException
	{
		for (int i = index-len; i <= index+len; ++i) 
		{
			if (i == index) 
			{
				continue;
			}
			int labelIndex = i - index;
			if (i < 0 || i >= nodeList.size()) 
			{
				sb.append(" pos(" + labelIndex + ")/0" + ":1");
			}
			else 
			{
				sb.append(" pos(" + labelIndex + ")/" + nodeList.get(i).getPos());
			}
		}
	}
	
	public static void addContextLemmas(StringBuilder sb, int index, ArrayList<Node> nodeList, int len) throws IOException
	{
		for (int i = index-len; i <= index+len; ++i) 
		{
			if (i == index) 
			{
				continue;
			}
			int labelIndex = i - index;
			if (i < 0 || i >= nodeList.size()) 
			{
				sb.append(" lemma(" + labelIndex + ")/0" + ":1");
			}
			else 
			{
				sb.append(" lemma(" + labelIndex + ")/" + nodeList.get(i).getLemma());
			}
		}
	}
	
	public static void addLemmaPosJoint(StringBuilder sb, Node node) throws IOException
	{
		// negative effect
		sb.append(" " + node.getLemma() + "/"+ node.getPos()+":1");
	}
	
	public static void addNer(StringBuilder sb, Node node) throws IOException
	{
		sb.append(" ner" + node.getNer()+":1");
	}
	
	public static void addLemma(StringBuilder sb, Node node) throws IOException
	{
		sb.append(" " + node.getLemma()+":1");
	}

	public static void addPos(StringBuilder sb, Node node) throws IOException
	{
		sb.append(" " + node.getPos()+":1");
	}

	public static void addTargetRate(StringBuilder sb, Node node, HashMap<String, Double> targetRateMap) throws IOException
	{
		String lemma = node.getLemma();
		double rate = 0.0;
		if (targetRateMap.containsKey(lemma))
		{
			rate = targetRateMap.get(lemma);
		}
		sb.append(" TargetRate:" + rate);
	}

	public static void main(String[] args) throws IOException
	{
		String tmp = "we are/hehe";
		String[] strs = tmp.split("\\s+");
		for (String str : strs) {
			System.out.println(str);
		}
	}

}
