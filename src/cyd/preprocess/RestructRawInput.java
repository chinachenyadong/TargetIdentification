package cyd.preprocess;

import java.io.*;
import java.util.*;

import cyd.feature.AddFeature;
import cyd.node.*;

/**
 * restruct raw input 
 * 1. add dependency word
 * 2. add topic
 * @author chenyadong
 */
public class RestructRawInput
{
	
	/**
	 * punctuation add childDep and parentDep
	 */
	public static void puncAddDep(String inputPath, String outputPath) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(inputPath));
		FileWriter fw = new FileWriter(outputPath);
		String line = null;
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No.") || line.equals(""))
			{
				fw.write(line + "\n");
				continue;
			}

			String[] strs = line.split(" \\|\\| ");
			
			if (strs.length == 7)
			{
				fw.write(strs[0]);
				for (int i = 1; i <= 4; ++i) 
				{
					fw.write(" || " + strs[i]);
				}
				fw.write(" || child :  || parent : ");
				fw.write(" || " + strs[5] + " || " + strs[6] + "\n");
			}
			else 
			{
				fw.write(line + "\n");
			}
		}
		br.close();
		fw.close();
	}
	
	/**
	 * add dependency word and punctuation add childDep and parentDep
	 */
	public static void addDependencyWord(String inputPath, String outputPath) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(inputPath));
		FileWriter fw = new FileWriter(outputPath);
		String line = null;
		
		ArrayList<String> list = new ArrayList<String>();
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No."))
			{
				list = new ArrayList<String>();
				map = new HashMap<Integer, String>();
				fw.write(line + "\n");
				continue;
			} 
			else if (line.equals(""))
			{
				for (int i = 0; i < list.size(); ++i)
				{
					line = list.get(i);
					String[] strs = line.split(" \\|\\| ");
					fw.write(strs[0]);
					for (int j = 1; j <= 4; ++j) 
					{
						fw.write(" || " + strs[j]);
					}
					
					// add child dep
					String[] tmps = strs[5].split(" : ");
					fw.write(" || child");
					int childDepNum = (tmps.length-1)/2;
					for (int j = 1; j <= childDepNum; ++j)
					{
						String dep = tmps[j*2-1];
						int index = Integer.parseInt(tmps[j*2]);
						String lemma = map.get(index);
						fw.write(" : " + dep + " : " + index + " : " + lemma);
					}
					if (childDepNum == 0)
					{
						fw.write(" : ");
					}
					
					// add parent dep
					tmps = strs[6].split(" : ");
					fw.write(" || parent");
					int parentDepNum = (tmps.length-1)/2;
					for (int j = 1; j <= parentDepNum; ++j)
					{
						String dep = tmps[j*2-1];
						int index = Integer.parseInt(tmps[j*2]);
						String lemma = map.get(index);
						fw.write(" : " + dep + " : " + index + " : " + lemma);
					}
					if (parentDepNum == 0)
					{
						fw.write(" : ");
					}
					fw.write(" || " + strs[7] + " || " + strs[8] + "\n");
				}
				fw.write("\n");
				continue;
			}

			// record each node information
			String[] strs = line.split(" \\|\\| ");
			int index = Integer.parseInt(strs[0]);
			String lemma = strs[3];
			map.put(index, lemma);
			list.add(line);
		}
		br.close();
		fw.close();
	}

	public static void addTopic(String inputPath, String outputPath) throws IOException
	{
		
	}
	
	public static void main(String[] args) throws IOException
	{
//		String inputPath = "./data/train.raw";
//		String outputPath = "./data/train.raw1";
//		addDependencyWord(inputPath, outputPath);
	}

}
