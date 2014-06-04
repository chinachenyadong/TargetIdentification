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
		int numfeatures = (int) (instances.getDataAlphabet().size() * 0.8);
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
                                                                                   
	/** in the training feature table, Lines should be formatted as:
	 *     [name] [label] [data ...]
	 * @param trainPath
	 * @param modelPath
	 */
	public static Classifier TrainMaxent(String trainPath, String modelPath) throws IOException
	{
		// build data input pipe
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		
		// define pipe
		// the features in [data ...] should like: feature:value
		pipes.add(new Target2Label());
		pipes.add(new Csv2FeatureVector());

		Pipe pipe = new SerialPipes(pipes);
		pipe.setTargetProcessing(true);

		// read data
		InstanceList trainingInstances = new InstanceList(pipe);
		FileReader training_file_reader = new FileReader(trainPath);
		CsvIterator reader = new CsvIterator(training_file_reader, "(\\w+)\\s+(\\S+)\\s+(.*)", 3, 2, 1); // (data, label, name) field indices    
		trainingInstances.addThruPipe(reader);
		training_file_reader.close();

		// prune by info gain
		pruneByInfoGain(trainingInstances);

		PrintStream temp = System.err;
		System.setErr(System.out);
		
		long startTime = System.currentTimeMillis();
		// train a Maxent classifier (could be other classifiers)
		ClassifierTrainer trainer = new MaxEntTrainer(1.0);
		Classifier classifier = trainer.train(trainingInstances);
		// calculate running time
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total training time: " + totalTime);

		System.setErr(temp);
		
		// write model
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelPath));
		oos.writeObject(classifier);
		oos.close();

		return classifier;
	}

	public static void main(String[] args) throws IOException
	{
		String trainPath = "./tmp/train.input";
		String modelPath = "./tmp/model.txt";

		// train the model
		TrainMaxent(trainPath, modelPath);
	}

}
