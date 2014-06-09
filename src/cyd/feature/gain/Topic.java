package cyd.feature.gain;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cyd.node.Node;

public class Topic
{
	public final static String STOP_WORDS = "data/stopwords.txt";
	public final static String TOPIC_WORDS = "tmp/topic.words";
	public final static String TOPICS_PER_DOC = "tmp/topic.docs";

	/**
	 * train.raw change to words_per_doc
	 *     format: [no.] [file name] [words]
	 */
	public static void wordsPerDoc(String inputPath, String outputPath) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(inputPath));
		FileWriter fw = new FileWriter(outputPath);
		String line = null;
		String fileName = null;
		boolean start = true;

		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No."))
			{
				String[] strs = line.split(" \\|\\| ");
				if (start)
				{
					fileName = strs[1];
					fw.write(fileName + "\tX\t");
					start = false;
				}
				else if (strs[1].equals(fileName) == false)
				{
					fileName = strs[1];
					fw.write("\n" + fileName + "\tX\t");
				}
				continue;
			}
			else if (line.equals(""))
			{
				continue;
			}

			String[] strs = line.split(" \\|\\| ");
			String isPunt = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (isPunt.equals(""))
			{
				continue;
			}
			fw.write(strs[1] + " ");
		}
		fw.write("\n");
		br.close();
		fw.close();
	}

	/**
	 * combine topic txt of train and text
	 */
	public static void combineTrainTest(String trainPath, String testPath, String outputPath) throws Exception
	{
		FileWriter fw = new FileWriter(outputPath);
		BufferedReader br = new BufferedReader(new FileReader(trainPath));
		String line = null;
		while ((line = br.readLine()) != null)
		{
			fw.write(line + "\n");
		}
		br.close();

		br = new BufferedReader(new FileReader(testPath));
		while ((line = br.readLine()) != null)
		{

			fw.write(line + "\n");
		}
		br.close();

		fw.close();
	}

	public static void topicsToRaw(String topicPath, String rawPath, String outputPath) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(topicPath));
		String line = null;
		HashMap<String, String[]> fileWordsMap = new HashMap<String, String[]>();
		while ((line = br.readLine()) != null)
		{
			String[] strs = line.split("\t");
			String[] words = strs[1].split(" ");

			fileWordsMap.put(strs[0], words);
		}
		br.close();

		br = new BufferedReader(new FileReader(rawPath));
		FileWriter fw = new FileWriter(outputPath);
		boolean start = true;
		String[] words = null;
		String fileName = null;
		int wordIndex = 0;
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("Sen.No."))
			{
				String[] strs = line.split(" \\|\\| ");
				if (start)
				{
					fileName = strs[1];
					words = fileWordsMap.get(fileName);
					wordIndex = 0;
					start = false;
				}
				else if (strs[1].equals(fileName) == false)
				{
					fileName = strs[1];
					words = fileWordsMap.get(fileName);
					wordIndex = 0;
				}
				fw.write(line + "\n");
				continue;
			}
			else if (line.equals(""))
			{
				fw.write(line + "\n");
				continue;
			}

			String[] strs = line.split(" \\|\\| ");
			String isPunt = strs[1].replaceAll("[\\pP‘’“”]", "");
			if (isPunt.equals(""))
			{
				fw.write(line + "\n");
				continue;
			}
			String word = strs[1].toLowerCase();

			String topicWord = words[wordIndex].substring(0, words[wordIndex].lastIndexOf(":"));
			String topic = "-1";

			// lda:  u.s. => u.s
			if (word.endsWith("."))
			{
				word = word.substring(0, word.lastIndexOf("."));
			}

			if (word.equals(topicWord))
			{
				topic = words[wordIndex].substring(words[wordIndex].lastIndexOf(":") + 1);
				++wordIndex;
				if (wordIndex >= words.length) // last word is stopword
				{
					--wordIndex;
				}
			}

			fw.write(strs[0]);
			for (int i = 1; i <= 6; ++i)
			{
				fw.write(" || " + strs[i]);
			}
			fw.write(" || " + topic + " || " + strs[7] + " || " + strs[8] + "\n");
		}
		br.close();
		fw.close();
	}

	public static void gainTopics(String inputPath, String outputPath, int NUM_OF_TOPICS, int NUM_OF_ITER) throws Exception
	{
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(STOP_WORDS), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(inputPath)), "UTF-8");
		instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data, label, name fields

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
		 * get each document's words : topics
		 */
		FileWriter fw = new FileWriter(outputPath);
		for (int i = 0; i < instances.size(); ++i)
		{
			FeatureSequence tokens = (FeatureSequence) model.getData().get(i).instance.getData();
			LabelSequence topics = model.getData().get(i).topicSequence;
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			fw.write(instances.get(i).getName().toString() + "\t");

			for (int position = 0; position < tokens.getLength(); position++)
			{
				out.format("%s:%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
			}
			fw.write(out + "\n");
		}
		fw.close();
	}

	/**
	 * 只保留sgm文件
	 */
	public static void sgm(String rootDirPath) throws Exception
	{
		File rootDir = new File(rootDirPath);
		File[] dirs = rootDir.listFiles();
		for (File dir : dirs)
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				if (file.getName().endsWith(".sgm") == false)
				{
					file.delete();
				}
			}

		}
	}

	/**
	 * add ACE corpus to balance topics
	 */
	public static void combineFramenetACE(String framePath, String acePath, String frameAcePath) throws Exception
	{
		FileWriter fw = new FileWriter(frameAcePath);
		BufferedReader br = new BufferedReader(new FileReader(framePath));
		String line = null;
		while ((line = br.readLine()) != null)
		{
			fw.write(line + "\n");
		}
		br.close();

		SAXReader reader = new SAXReader();
		reader.setValidation(false);
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		File rootDir = new File(acePath);
		File[] dirs = rootDir.listFiles();
		for (File dir : dirs)
		{

			String dirName = dir.getName();
			String pattern = null;
			if (dirName.equals("bc") || dirName.equals("bn") || dirName.equals("cts"))
			{
				pattern = "/DOC/BODY/TEXT/TURN";
			}
			else if (dirName.equals("nw"))
			{
				pattern = "/DOC/BODY/TEXT";
			}
			else if (dirName.equals("un") || dirName.equals("wl"))
			{
				pattern = "/DOC/BODY/TEXT/POST";
			}

			File[] files = dir.listFiles();
			for (File file : files)
			{
				try
				{
					Document document = reader.read(file);
					List list = document.selectNodes(pattern);
					Iterator iter = list.iterator();

					fw.write(file.getName() + "\tX\t");
					while (iter.hasNext())
					{
						Element textElement = (Element) iter.next();
						String text = textElement.getText().replaceAll("\n", " ");
						fw.write(text + " ");
					}
					fw.write("\n");
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
		fw.close();
	}

	public static void main(String[] args) throws Exception
	{
		//		String inputPath = "./tmp/topic.input";
		//		String outputPath = "./tmp/topic.output";
		//		gainTopics(inputPath, outputPath, 100, 1000);

		//		String topicPath = "./tmp/topic.output";	
		//		String rawPath = "./data/test.raw";
		//		String outputPath = "./tmp/test.topic";
		//		topicsToRaw(topicPath, rawPath, outputPath);

		//		sgm("F:/研究/事件相关/data/sgm");

		String framePath = "./tmp/topic.input";
		String acePath = "F:/研究/事件相关/data/sgm";
		String frameAcePath = "./tmp/frameAce.input";
		combineFramenetACE(framePath, acePath, frameAcePath);

	}
}