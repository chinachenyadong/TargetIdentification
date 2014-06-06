package cyd.topic;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

class LDA
{
	public final static String STOP_WORDS = "data/stopwords.txt";

	/** 
	 * Input file Format: (document Id) \t X \t (document text)  
	 */
	public final static String INPUT_FILE = "tmp/topic.example";
	/* in this file topics will be stored */
	public final static String TOPIC_LIST = "tmp/topics_representation.txt";
	/* in this file we will store data on which document belongs to which topic */
	public final static String TOPICS_PER_DOC = "tmp/topics_per_doc.txt";
	/* number of interactions */
	public final static int NUM_OF_ITER = 1000; // real app 1000 - 2000
	/* the expected number of topics */
	public final static int NUM_OF_TOPICS = 30;

	public static void main(String[] args) throws Exception
	{
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(STOP_WORDS), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(INPUT_FILE)), "UTF-8");
		instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data, label, name fields

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is the parameter for a single dimension of the Dirichlet prior.
		int numTopics = NUM_OF_TOPICS;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only,
		//  for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(NUM_OF_ITER);
		model.estimate();
		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		/**
		 * get each document' words : topics
		 */
		FileWriter fw = new FileWriter("./tmp/doc_word_topic.txt");
		for (int i = 0; i < instances.size(); ++i)
		{
			FeatureSequence tokens = (FeatureSequence) model.getData().get(i).instance.getData();
			LabelSequence topics = model.getData().get(i).topicSequence;
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			for (int position = 0; position < tokens.getLength(); position++)
			{
				out.format("%s:%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
			}
			fw.write(out + "\n");
		}
		fw.close();

		/**
		 * Get topics. Store each topic with a list of words and their weight:
		 * (topic name)  - (word): (weight), ...
		 */
		FileWriter topicList = new FileWriter(TOPIC_LIST);
		int i = 0;
		StringBuilder sb;
		for (TreeSet<IDSorter> set : model.getSortedWords())
		{
			sb = new StringBuilder().append(i);
			sb.append(" - ");
			for (IDSorter s : set)
			{
				sb.append(dataAlphabet.lookupObject(s.getID())).append(":").append(s.getWeight()).append(", ");
			}
			topicList.write(sb.append("\n").toString());
			i++;
		}
		topicList.close();

		/**
		 * Get topic per document
		 */
		TopicInferencer inferencer = model.getInferencer();
		FileWriter topicsPerDoc = new FileWriter(TOPICS_PER_DOC);

		for (int numOfInst = 0; numOfInst < instances.size(); numOfInst++)
		{
			StringBuilder sb1 = new StringBuilder();
			Instance inst = instances.get(numOfInst);
			double[] testProbabilities = inferencer.getSampledDistribution(inst, 10, 1, 5);
			sb1.append(inst.getName());

			for (int j = 0; j < testProbabilities.length; j++)
			{
				sb1.append('\t').append(testProbabilities[j]);
			}
			topicsPerDoc.write(sb1.append('\n').toString());
		}
		topicsPerDoc.close();
	}
}