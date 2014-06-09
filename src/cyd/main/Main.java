package cyd.main;

import java.io.PrintStream;

import cyd.maxent.MeDecoder;
import cyd.maxent.MeTrainer;
import cyd.preprocess.Preprocess;

import cyd.feature.gain.*;;

public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{

		// topic num select
//		String topicIn = "./tmp/topic.input";
//		String topicIn = "./data/frameAce.input";
//		String topicOut= "./tmp/topic.output";
//		String trainRaw = "./data/train.raw";
//		String trainPath = "./tmp/train.topic";
//		String testRaw = "./data/test.raw";
//		String testPath = "./tmp/test.topic";
//		Topic.gainTopics(topicIn, topicOut, 200, 1000);	
//		Topic.topicsToRaw(topicOut, trainRaw, trainPath);
//		Topic.topicsToRaw(topicOut, testRaw, testPath);
		
		// TODO Auto-generated method stub
		String trainPath = "./data/train.raw";
		String testPath = "./data/test.raw";
		
		String trainInput = "./tmp/train.input";
		String modelPath = "./tmp/model.txt";
		String testInput = "./tmp/test.input";

		Preprocess.load();
		Preprocess.preprocess(trainPath, trainInput);
		Preprocess.preprocess(testPath, testInput);
		
		// train the model
		MeTrainer.TrainMaxent(trainInput, modelPath);

		MeDecoder decoder = new MeDecoder(modelPath);
		decoder.decodeOnFeatureTable(testInput);

		System.exit(0);
	}

}
