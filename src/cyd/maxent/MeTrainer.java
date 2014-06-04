package cyd.maxent;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEnt;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;
import cc.mallet.util.FileUtils;

public class MeTrainer
{

	/**
	 * Use information gain to rank and select features
	 * @param instances
	 * @return 
	 */
	public static void pruneByInfoGain(InstanceList instances)
	{
		InfoGain ig = new InfoGain(instances);
		int numfeatures = (int) (instances.getDataAlphabet().size() * 0.9);
		FeatureSelection fs = new FeatureSelection(ig, numfeatures);
		for (int ii = 0; ii < instances.size(); ii++)
		{
			Instance instance = instances.get(ii);
			FeatureVector fv = (FeatureVector) instance.getData();
			FeatureVector fv2 = FeatureVector.newFeatureVector(fv, instances.getDataAlphabet(), fs);
			instance.unLock();
			instance.setData(fv2);
		}
	}

	// change this accordingly
	static double Gaussian_Variance = 1.0;

                                                                                                         
	public static Classifier TrainMaxent(String trainPath, String modelPath) throws IOException
	{
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        pipeList.add(new SvmLight2FeatureVectorAndLabel());
        SerialPipes instancePipe = new SerialPipes(pipeList);
        InstanceList trainingInstances = new InstanceList(instancePipe);
        Reader fileReader = new InputStreamReader(new FileInputStream(trainPath));
        // Read instances from the file
        trainingInstances.addThruPipe (new SelectiveFileLineIterator (fileReader, "^\\s*#.+"));
        
//		SimpleFileLineIterator it = new SimpleFileLineIterator(trainPath); // this iterator will feed each line in the file to the pipe
//		Pipe pipe = new SvmLight2FeatureVectorAndLabel(); // the pipe that will do the conversion
//		InstanceList trainingInstances = new InstanceList(pipe); // create an empty list
//		trainingInstances.addThruPipe(it); // feeding the file to the pipe and to the data list

		// prune by info gain
//		 pruneByInfoGain(trainingInstances);

		long startTime = System.currentTimeMillis();
		// train a Maxent classifier (could be other classifiers)
		ClassifierTrainer trainer = new MaxEntTrainer(Gaussian_Variance);
		Classifier classifier = trainer.train(trainingInstances);
		// calculate running time
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total training time: " + totalTime);

		// write model
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelPath));
		oos.writeObject(classifier);
		oos.close();

		return classifier;
	}
	
	
	static ArrayList<String> yeslist = new ArrayList<String>();
	static ArrayList<String> nolist = new ArrayList<String>();
	public static void split(String input) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = null;
		while ( (line = br.readLine()) != null ) 
		{
			String[] strs = line.split(" ");
			if (strs[0].equals("1"))
			{
				yeslist.add(strs[1] + " " + strs[2]);
			}
			else if (strs[0].equals("-1"))
			{
				nolist.add(strs[1] + " " + strs[2]);
			}
		}
		br.close();
	}

	
	
	public static void main(String[] args) throws IOException
	{
//		split("./tmp/train.input");
//		split("./tmp/test.input");
//		FileWriter fw1 = new FileWriter("./tmp/yes.txt");
//		FileWriter fw2 = new FileWriter("./tmp/no.txt");
//		for (int i = 0; i < yeslist.size(); ++i)
//		{
//			fw1.write(yeslist.get(i)+"\n");
//		}
//		for (int i = 0; i < nolist.size(); ++i)
//		{
//			fw2.write(nolist.get(i)+"\n");
//		}
//		fw1.close();
//		fw2.close();
		String trainPath = "./tmp/train.txt";
		String modelPath = "./tmp/model.txt";

//		// train the model
		TrainMaxent(trainPath, modelPath);
//		
		String testPath = "./tmp/test.txt";
//		// test
		MeDecoder decoder = new MeDecoder(modelPath);
		decoder.decodeOnFeatureTable(testPath);
	}

}
