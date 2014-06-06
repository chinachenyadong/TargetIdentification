package cyd.feature.gain;

import java.io.*;
import java.util.*;

/**
 * construct lemma frequency
 * @author chenyadong
 */
public class LemmaFrequency
{
	public static void lemmaFrequency() throws IOException
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader("./data/train.raw"));
		String line = null;
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No.") || line.equals(""))
			{
				continue;
			}

			String[] strs = line.split(" \\|\\| ");
			String isPunt = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (isPunt.equals(""))
			{
				continue;
			}
			
			String lemma = strs[3];
			if (map.containsKey(lemma) == false)
			{
				map.put(lemma, 1);
			}
			else
			{
				map.put(lemma, map.get(lemma)+1);
			}
		}
		
		FileWriter fw = new FileWriter("./data/FrameNet_LemmaFrequency.txt");
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			int val = (Integer) entry.getValue();
			fw.write(key + " " + val + "\n");
		}
		fw.close();
	}
	
	public static void main(String[] args) throws IOException 
	{
		lemmaFrequency();
	}

}
