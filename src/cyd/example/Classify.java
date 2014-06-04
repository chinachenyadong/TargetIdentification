package cyd.example;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cyd.example.ImportExample.TxtFilter;

import cc.mallet.classify.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;
import cc.mallet.util.Randoms;

public class Classify
{
	Pipe pipe;

	public Classify()
	{
		pipe = buildPipe();
	}

	public Pipe buildPipe()
	{
		ArrayList pipeList = new ArrayList();

		// Read data from File objects
		pipeList.add(new Input2CharSequence("UTF-8"));

		// Regular expression for what constitutes a token.
		//  This pattern includes Unicode letters, Unicode numbers, 
		//   and the underscore character. Alternatives:
		//    "\\S+"   (anything not whitespace)
		//    "\\w+"    ( A-Z, a-z, 0-9, _ )
		//    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
		//                                    a group of only punctuation marks)
		Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

		// Tokenize raw strings
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// Normalize all tokens to all lowercase
		pipeList.add(new TokenSequenceLowercase());

		// Remove stopwords from a standard English stoplist.
		//  options: [case sensitive] [mark deletions]
		pipeList.add(new TokenSequenceRemoveStopwords(false, false));

		// Rather than storing tokens as strings, convert 
		//  them to integers by looking them up in an alphabet.
		pipeList.add(new TokenSequence2FeatureSequence());

		// Do the same thing for the "target" field: 
		//  convert a class label string to a Label object,
		//  which has an index in a Label alphabet.
		pipeList.add(new Target2Label());

		// Now convert the sequence of features to a sparse vector,
		//  mapping feature IDs to counts.
		pipeList.add(new FeatureSequence2FeatureVector());

		// Print out the features and the label
		pipeList.add(new PrintInputAndTarget());

		return new SerialPipes(pipeList);
	}

	public InstanceList readDirectory(File directory)
	{
		return readDirectories(new File[] { directory });
	}

	public InstanceList readDirectories(File[] directories)
	{

		// Construct a file iterator, starting with the 
		//  specified directories, and recursing through subdirectories.
		// The second argument specifies a FileFilter to use to select
		//  files within a directory.
		// The third argument is a Pattern that is applied to the 
		//   filename to produce a class label. In this case, I've 
		//   asked it to use the last directory name in the path.
		FileIterator iterator = new FileIterator(directories, new TxtFilter(), FileIterator.LAST_DIRECTORY);

		// Construct a new instance list, passing it the pipe
		//  we want to use to process instances.
		InstanceList instances = new InstanceList(pipe);

		// Now process each instance provided by the iterator.
		instances.addThruPipe(iterator);

		return instances;
	}

	public Classifier trainClassifier(InstanceList trainingInstances)
	{

		// Here we use a maximum entropy (ie polytomous logistic regression)                               
		//  classifier. Mallet includes a wide variety of classification                                   
		//  algorithms, see the JavaDoc API for details.                                                   

		ClassifierTrainer trainer = new MaxEntTrainer();
		return trainer.train(trainingInstances);
	}

	public Classifier loadClassifier(File serializedFile) throws FileNotFoundException, IOException, ClassNotFoundException
	{

		// The standard way to save classifiers and Mallet data                                            
		//  for repeated use is through Java serialization.                                                
		// Here we load a serialized classifier from a file.                                               

		Classifier classifier;

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedFile));
		classifier = (Classifier) ois.readObject();
		ois.close();

		return classifier;
	}

	public void saveClassifier(Classifier classifier, File serializedFile) throws IOException
	{

		// The standard method for saving classifiers in                                                   
		//  Mallet is through Java serialization. Here we                                                  
		//  write the classifier object to the specified file.                                             
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializedFile));
		oos.writeObject(classifier);
		oos.close();
	}

	public void printLabelings(Classifier classifier, File file) throws IOException
	{

		// Create a new iterator that will read raw instance data from                                     
		//  the lines of a file.                                                                           
		// Lines should be formatted as:                                                                   
		//                                                                                                 
		//   [name] [label] [data ... ]                                                                    
		//                                                                                                 
		//  in this case, "label" is ignored.                                                              

		CsvIterator reader = new CsvIterator(new FileReader(file), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1); // (data, label, name) field indices               

		// Create an iterator that will pass each instance through                                         
		//  the same pipe that was used to create the training data                                        
		//  for the classifier.                                                                            
		Iterator instances = classifier.getInstancePipe().newIteratorFrom(reader);

		// Classifier.classify() returns a Classification object                                           
		//  that includes the instance, the classifier, and the                                            
		//  classification results (the labeling). Here we only                                            
		//  care about the Labeling.                                                                       
		while (instances.hasNext())
		{
			Labeling labeling = classifier.classify(instances.next()).getLabeling();

			// print the labels with their weights in descending order (ie best first)                     

			for (int rank = 0; rank < labeling.numLocations(); rank++)
			{
				System.out.print(labeling.getLabelAtRank(rank) + ":" + labeling.getValueAtRank(rank) + " ");
			}
			System.out.println();

		}
	}

	public void evaluate(Classifier classifier, File file) throws IOException
	{

		// Create an InstanceList that will contain the test data.                                         
		// In order to ensure compatibility, process instances                                             
		//  with the pipe used to process the original training                                            
		//  instances.                                                                                     

		InstanceList testInstances = new InstanceList(classifier.getInstancePipe());

		// Create a new iterator that will read raw instance data from                                     
		//  the lines of a file.                                                                           
		// Lines should be formatted as:                                                                   
		//                                                                                                 
		//   [name] [label] [data ... ]                                                                    

		CsvIterator reader = new CsvIterator(new FileReader(file), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1); // (data, label, name) field indices               

		// Add all instances loaded by the iterator to                                                     
		//  our instance list, passing the raw input data                                                  
		//  through the classifier's original input pipe.                                                  

		testInstances.addThruPipe(reader);

		Trial trial = new Trial(classifier, testInstances);

		// The Trial class implements many standard evaluation                                             
		//  metrics. See the JavaDoc API for more details.                                                 

		System.out.println("Accuracy: " + trial.getAccuracy());

		// precision, recall, and F1 are calcuated for a specific                                          
		//  class, which can be identified by an object (usually                                           
		//  a String) or the integer ID of the class                                                       

		System.out.println("F1 for class 'good': " + trial.getF1("good"));

		System.out.println("Precision for class '" + classifier.getLabelAlphabet().lookupLabel(1) + "': " + trial.getPrecision(1));
	}

	public Trial testTrainSplit(InstanceList instances)
	{

		int TRAINING = 0;
		int TESTING = 1;
		int VALIDATION = 2;

		// Split the input list into training (90%) and testing (10%) lists.                               
		// The division takes place by creating a copy of the list,                                        
		//  randomly shuffling the copy, and then allocating                                               
		//  instances to each sub-list based on the provided proportions.                                  

		InstanceList[] instanceLists = instances.split(new Randoms(), new double[] { 0.9, 0.1, 0.0 });

		// The third position is for the "validation" set,                                                 
		//  which is a set of instances not used directly                                                  
		//  for training, but available for determining                                                    
		//  when to stop training and for estimating optimal                                               
		//  settings of nuisance parameters.                                                               
		// Most Mallet ClassifierTrainers can not currently take advantage                                 
		//  of validation sets.                                                                            

		Classifier classifier = trainClassifier(instanceLists[TRAINING]);
		return new Trial(classifier, instanceLists[TESTING]);
	}

	public static void main(String[] args) throws IOException
	{

		Classify importer = new Classify();
		InstanceList instances = importer.readDirectory(new File("./data/sample-data/web"));
		instances.save(new File("./data/result.txt"));

	}

	/** This class illustrates how to build a simple file filter */
	class TxtFilter implements FileFilter
	{

		/** Test whether the string representation of the file 
		 *   ends with the correct extension. Note that {@ref FileIterator}
		 *   will only call this filter if the file is not a directory,
		 *   so we do not need to test that it is a file.
		 */
		public boolean accept(File file)
		{
			return file.toString().endsWith(".txt");
		}
	}
}
