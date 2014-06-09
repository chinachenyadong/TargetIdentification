package cyd.rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class Rule
{

	public static void ruleSupvise() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				"./data/rule/exampleLuFrame.txt"));
		String line = null;
		HashSet<String> targetSet = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			String[] strs = line.split(" \\|\\| ");
			targetSet.add(strs[0]);
		}
		br.close();

		br = new BufferedReader(new FileReader("./data/train.raw"));
		while ((line = br.readLine()) != null) {
			if (line.startsWith("Sen.No.") || line.equals("")) {
				continue;
			}
			String[] strs = line.split(" \\|\\| ");
			String tmp = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (tmp.equals("")) {
				continue;
			}
			if (strs[strs.length - 1].equals("NULL") == false) {
				targetSet.add(strs[strs.length - 2].toLowerCase());
			}
		}
		br.close();

		// maxentropy result
		ArrayList<String> resList = new ArrayList<String>();
		br = new BufferedReader(new FileReader("./tmp/result.txt"));
		while ((line = br.readLine()) != null) {
			String[] strs = line.split(" : ");
			resList.add(strs[1]);
		}

		br = new BufferedReader(new FileReader("./data/test.raw"));
		int cn = 0, pn = 0, rn = 0, index = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("Sen.No.") || line.equals("")) {
				continue;
			}
			String[] strs = line.split(" \\|\\| ");
			String tmp = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (tmp.equals("")) {
				continue;
			}
			String lu = strs[3].toLowerCase();
			String pos = strs[2];
			boolean isTarget = false;
			if (pos.startsWith("N")) {
				pos = "n";
			} else if (pos.startsWith("V")) {
				pos = "v";
			} else if (pos.startsWith("J")) {
				pos = "a";
			} else if (pos.startsWith("RB")) {
				pos = "adv";
			} else if (pos.startsWith("CD")) {
				pos = "num";
			}
			lu = lu + "." + pos;
			if (targetSet.contains(lu)) {
				isTarget = true;
			}
			
			if (lu.equals("have")) {
				isTarget = false;
				if (strs[5].startsWith("child :")) {
					String[] tmps = strs[5].split(" : ");
					for (int i = 1; i < tmps.length; i += 2) {
						if (tmps[i].equals("dobj")) {
							isTarget = true;
							break;
						}
					}
				}
			} else if (lu.equals("will") || lu.equals("be")) {
				isTarget = false;
			} else if (pos.equals("IN") || pos.equals("TO")) {
				isTarget = false;
			} else {
				if (strs[6].startsWith("parent :")) {
					String[] tmps = strs[6].split(" : ");
					for (int i = 1; i < tmps.length; i += 2) {
						if (tmps[i].equals("aux")) {
							isTarget = false;
							break;
						}
					}
				}
			}

			// max entropy result
			String result = resList.get(index++);
			if (isTarget == false && result.equals("1")) {
				isTarget = true;
			}

			// p r f
			if (strs[strs.length - 1].equals("NULL") == false) {
				++rn;
			}
			if (isTarget == true) {
				++pn;
				if (strs[strs.length - 1].equals("NULL") == false) {
					++cn;
				}
			}
		}
		br.close();
		double p = (double) cn / pn;
		double r = (double) cn / rn;
		double f = 2 * p * r / (p + r);
		System.out.println("P: " + p);
		System.out.println("R: " + r);
		System.out.println("F: " + f);
	}
	
	
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
//		ruleSupvise();
		
//		double a = 85.04;
//		double b = 58.57;
//		double c = 2*a*b/(a+b);
//		System.out.println("F:" + String.format("%.2f", c));
		
		double a = 66.60 - 61.75;
		System.out.println(a);
	}

}
