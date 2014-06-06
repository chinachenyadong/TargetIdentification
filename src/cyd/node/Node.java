package cyd.node;

import java.util.*;

/**
 * This class represent raw one line
 * @author chenyadong
 */
public class Node
{
	boolean isTarget; 
	
	String lemma;
	String pos;
	String ner;
	
	int index;
//	HashMap<>
	ArrayList<String> childDep = new ArrayList<String>();
	ArrayList<String> parenetDep = new ArrayList<String>();
	ArrayList<String> dep = new ArrayList<String>();
	
	
	public String getNer()
	{
		return ner;
	}

	public void setNer(String ner)
	{
		this.ner = ner;
	}

	public boolean isTarget()
	{
		return isTarget;
	}

	public void setTarget(boolean isTarget)
	{
		this.isTarget = isTarget;
	}

	public String getLemma()
	{
		return lemma;
	}

	public void setLemma(String lemma)
	{
		this.lemma = lemma;
	}

	public String getPos()
	{
		return pos;
	}

	public void setPos(String pos)
	{
		this.pos = pos;
	}

}
