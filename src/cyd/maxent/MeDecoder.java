package cyd.maxent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SvmLight2FeatureVectorAndLabel;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.SelectiveFileLineIterator;
import cc.mallet.pipe.iterator.SimpleFileLineIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;

public class MeDecoder
{
	// the model
	Classifier classifier;

	public MeDecoder(String modelFile) throws IOException
	{
		// read model
		ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File(modelFile)));
		try
		{
			classifier = (Classifier) oos.readObject();
			oos.close();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

//	public void decodeOnFeatureTable(String testPath) throws FileNotFoundException
//	{
//		SimpleFileLineIterator it = new SimpleFileLineIterator(testPath); // this iterator will feed each line in the file to the pipe
//		Pipe pipe = new SvmLight2FeatureVectorAndLabel(); // the pipe that will do the conversion
//		InstanceList testingInstances = new InstanceList(pipe); // create an empty list
//		testingInstances.addThruPipe(it); // feeding the file to the pipe and to the data list
//
//		ArrayList<Classification> list = classifier.classify(testingInstances);
//		int both = 0, p = 0, r = 0;
//		for (int i = 0; i < list.size(); i++)
//		{
//			Classification c = list.get(i);
//			Instance instance = c.getInstance();
//			Labeling labeling = c.getLabeling();
//
//			// P R F
//			String label = instance.getTarget().toString();
//			String test = labeling.getBestLabel().toString();
//		
//			
//			if (test.equals(label) && label.equals("1"))
//			{
//				++both;
//			}
//			if (test.equals("1"))
//			{
//				++p;
//			}
//			if (label.equals("1"))
//			{
//				++r;
//			}		
//			
//			System.out.print(label + " " + test + " ");
//			for (int rank = 0; rank < labeling.numLocations(); rank++)
//            {
//                System.out.print(labeling.getLabelAtRank(rank) + ":" +
//                                 labeling.getValueAtRank(rank) + " ");
//            }
//            System.out.println();
//		}
//		System.out.println(testingInstances.size());
//		double P = (double)both/p;
//		double R = (double)both/r;
//		double F = 2*P*R/(P+R);
//		System.out.println("P: "+ P);
//		System.out.println("R: "+ R);
//		System.out.println("F: "+ F);
		
		// get the predicted labeling    
//		int both = 0, p = 0, r = 0;
//		for (int i = 0; i < testingInstances.size(); i++)
//		{
//			Instance instance = testingInstances.get(i);
//			Labeling labeling = classifier.classify(instance).getLabeling();
//
//			// P R F
//			String label = instance.getTarget().toString();
//			String test = labeling.getBestLabel().toString();
//		
//			
//			if (test.equals(label) && label.equals("1"))
//			{
//				++both;
//			}
//			if (test.equals("1"))
//			{
//				++p;
//			}
//			if (label.equals("1"))
//			{
//				++r;
//			}		
//			
//			System.out.print(label + " " + test + " ");
//			for (int rank = 0; rank < labeling.numLocations(); rank++)
//            {
//                System.out.print(labeling.getLabelAtRank(rank) + ":" +
//                                 labeling.getValueAtRank(rank) + " ");
//            }
//            System.out.println();
//		}
//		System.out.println(testingInstances.size());
//		double P = (double)both/p;
//		double R = (double)both/r;
//		double F = 2*P*R/(P+R);
//		System.out.println("P: "+ P);
//		System.out.println("R: "+ R);
//		System.out.println("F: "+ F);	
//	}
	
	public void decodeOnFeatureTable(String testPath) throws FileNotFoundException
	{
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        pipeList.add(new SvmLight2FeatureVectorAndLabel());
        SerialPipes instancePipe = new SerialPipes(pipeList);
        InstanceList testingInstances = new InstanceList(instancePipe);
        Reader fileReader = new InputStreamReader(new FileInputStream(testPath));
        // Read instances from the file
        testingInstances.addThruPipe (new SelectiveFileLineIterator (fileReader, "^\\s*#.+"));
		
//		SimpleFileLineIterator it = new SimpleFileLineIterator(testPath); // this iterator will feed each line in the file to the pipe
//		Pipe pipe = new SvmLight2FeatureVectorAndLabel(); // the pipe that will do the conversion
//		InstanceList testingInstances = new InstanceList(pipe); // create an empty list
//		testingInstances.addThruPipe(it); // feeding the file to the pipe and to the data list

		ArrayList<Classification> list = classifier.classify(testingInstances);
		int both = 0, p = 0, r = 0;
		for (int i = 0; i < list.size(); i++)
		{
			Classification c = list.get(i);
			Instance instance = c.getInstance();
			Labeling labeling = c.getLabeling();

			// P R F
			String label = instance.getTarget().toString();
			String test = labeling.getBestLabel().toString();
		
			
			if (test.equals(label) && label.equals("1"))
			{
				++both;
			}
			if (test.equals("1"))
			{
				++p;
			}
			if (label.equals("1"))
			{
				++r;
			}		
			
			System.out.print(label + " " + test + " ");
			for (int rank = 0; rank < labeling.numLocations(); rank++)
            {
                System.out.print(labeling.getLabelAtRank(rank) + ":" +
                                 labeling.getValueAtRank(rank) + " ");
            }
            System.out.println();
		}
		System.out.println(testingInstances.size());
		double P = (double)both/p;
		double R = (double)both/r;
		double F = 2*P*R/(P+R);
		System.out.println("P: "+ P);
		System.out.println("R: "+ R);
		System.out.println("F: "+ F);
	}
	
	public static void tmp() throws IOException 
	{
		 ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
         pipeList.add(new SvmLight2FeatureVectorAndLabel());
         SerialPipes instancePipe = new SerialPipes(pipeList);

         InstanceList instances = new InstanceList (instancePipe);
         Reader fileReader = new InputStreamReader(new FileInputStream("./tmp/test.txt"));
         // Read instances from the file
         instances.addThruPipe (new SelectiveFileLineIterator (fileReader, "^\\s*#.+"));
	}

	static public void main(String[] args) throws IOException, ClassNotFoundException
	{
		String modelPath = "./tmp/model.txt";	
		String testPath = "./tmp/test.txt";
		// test
		MeDecoder decoder = new MeDecoder(modelPath);
		decoder.decodeOnFeatureTable(testPath);
	}
}
