package cyd.main;

import java.io.PrintStream;

import cyd.maxent.MeDecoder;
import cyd.maxent.MeTrainer;
import cyd.preprocess.Preprocess;

public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		String trainRaw = "./data/train.raw";
		String testRaw = "./data/test.raw";
		
		String trainPath = "./tmp/train.input";
		String modelPath = "./tmp/model.txt";
		String testPath = "./tmp/test.input";

		PrintStream out = new PrintStream(System.out);
		Preprocess.init();
		Preprocess.preprocess(trainRaw, trainPath, out);
		Preprocess.preprocess(testRaw, testPath, out);
		
		// train the model
		MeTrainer.TrainMaxent(trainPath, modelPath);

		// decode
		MeDecoder decoder = new MeDecoder(modelPath);
		decoder.decodeOnFeatureTable(testPath);
		
		// exist a problem : can't print weights following with decode 
//		String weightPath = "./tmp/weight.txt";
//		PrintFeatureWeights.print(modelPath, weightPath);
		System.exit(0);
	}

}
