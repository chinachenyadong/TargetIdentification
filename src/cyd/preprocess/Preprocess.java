package cyd.preprocess;

import java.io.*;
import java.util.*;

import cyd.feature.AddFeature;
import cyd.node.*;

/**
 * important: select features
 * @author chenyadong
 */
public class Preprocess
{
	static int lineIndex = 0;
	static HashMap<String, Double> targetRateMap = new HashMap<String, Double>();
	static HashMap<String, Double> frameNetLFMap = new HashMap<String, Double>();
	static HashMap<String, Double> gutenbergLFMap = new HashMap<String, Double>();
	
	/**
	 * load 1. target rate; 2. FrameNet train lemma frequency; 3. Gutenberg lemma frequency
	 */
	public static void init() throws IOException
	{
		// load target rate
		BufferedReader br = new BufferedReader(new FileReader("./data/target_rate.txt"));
		String line = null;
		while ( (line = br.readLine()) != null ) 
		{
			String[] strs = line.split(" ");
			targetRateMap.put(strs[0], Double.parseDouble(strs[1]));
		}
		br.close();
		
		// load framenet Train lemma frequency file
		br = new BufferedReader(new FileReader("./data/FrameNet_Train_LemmaFrequency.txt"));
		while ( (line = br.readLine()) != null ) 
		{
			String[] strs = line.split(" ");
			frameNetLFMap.put(strs[0], Double.parseDouble(strs[1]));
		}
		br.close();
				
		// load Gutenberg lemma frequency file
		br = new BufferedReader(new FileReader("./data/Gutenberg_LemmaFrequency.txt"));
		while ( (line = br.readLine()) != null ) 
		{
			String[] strs = line.split("\t");
			gutenbergLFMap.put(strs[1], Double.parseDouble(strs[2]));
		}
		br.close();
	}
	
	/**
	 * convert sentence of nodes to feture representation 
	 * @param nodeList
	 * @return feature representation
	 */
	public static String maxEntSelectFeature(ArrayList<Node> nodeList) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nodeList.size(); ++i)
		{
			Node node = nodeList.get(i);
			sb.append(++lineIndex + " ");
			if (node.isTarget() == true)
			{
				sb.append("1");
			}
			else
			{
				sb.append("-1");
			}
			// add feature
			AddFeature.addFeatures(sb, node, i, nodeList, targetRateMap, frameNetLFMap, gutenbergLFMap);

		}
		return sb.toString();
	}

	/**
	 * change raw file to node of feature represent
	 * @param inputFile
	 * @param outputFile
	 * @param out
	 */
	public static void preprocess(String inputFile, String outputFile, PrintStream out) throws IOException
	{
		lineIndex = 0;
		out.println("change raw file to node of feature represent...");
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		FileWriter fw = new FileWriter(outputFile);
		String line = null;
		ArrayList<Node> nodeList = new ArrayList<Node>();
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No."))
			{
				nodeList = new ArrayList<Node>();
				continue;
			}
			else if (line.equals(""))
			{
				// sentence to node feature representation
				String res = maxEntSelectFeature(nodeList);
				fw.write(res);
				continue;
			}

			String[] strs = line.split(" \\|\\| ");
			String isPunt = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (isPunt.equals(""))
			{
				continue;
			}

			// add label
			Node node = new Node();
			if (strs[strs.length - 1].equals("NULL"))
			{
				node.setTarget(false);
			}
			else
			{
				node.setTarget(true);
			}
			//add feature: pos, lemma, ner
			String pos = strs[2];
			String lemma = strs[3];
			String ner = strs[4];
			node.setLemma(lemma);
			node.setPos(pos);
			node.setNer(ner);
			
			// add dependency
			
			
			nodeList.add(node);
		}
		br.close();
		fw.close();
	}

	public static void main(String[] args) throws IOException
	{

	}

}
