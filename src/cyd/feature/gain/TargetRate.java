package cyd.feature.gain;

import java.io.*;
import java.util.*;

/**
 * target rate: only save not 0.0 target
 * @author chenyadong
 */
public class TargetRate
{
	public static void targetRate() throws IOException
	{
		// train.input  format:
		// line: index label lemma
		HashMap<String, Integer> childMap = new HashMap<String, Integer>();
		HashMap<String, Integer> parentMap = new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader("./tmp/train.input"));
		String line = null;
		while ( (line = br.readLine()) != null ) 
		{
			String[] strs = line.split(" ");
			if (!childMap.containsKey(strs[2])) 
			{
				parentMap.put(strs[2], 1);
				if (strs[1].equals("1")) 
				{
					childMap.put(strs[2], 1);
				}
			}
			else 
			{
				parentMap.put(strs[2], parentMap.get(strs[2])+1);
				if (strs[1].equals("1")) 
				{
					childMap.put(strs[2], childMap.get(strs[2])+1);
				}
			}
		}
		br.close();
		
		FileWriter fw = new FileWriter("./data/target_rate.txt");
		Iterator iter = childMap.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			int childValue = (Integer) childMap.get(key);
			int parentValue = parentMap.get(key);
			fw.write(key + " " + (double) childValue/parentValue + "\n");
		}
		fw.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException 
	{
		targetRate();
	}

}
