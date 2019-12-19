package core;

import java.io.File;
import java.util.Map;

import org.apache.el.lang.ELArithmetic;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.pattern.JordanPattern;
import org.encog.persist.EncogDirectoryPersistence;

import config.Constants;

public class EncogNeuralNet{
	public static void train(Map<double[], double[]> ioTraining, Map<double[], double[]> ioTesting){
		BasicNetwork network= new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, Constants.vectorSize- 1));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, Constants.vectorSize* 2/ 3));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, Constants.vectorSize* 1/ 3));
		network.addLayer(new BasicLayer(new ActivationSoftMax(), false, Constants.vectorSize- 1));
		network.getStructure().finalizeStructure();
		network.reset();

		double[][] input= new double[ioTraining.size()][];
		double[][] output= new double[ioTraining.size()][];
		int n= 0;
		for (Map.Entry<double[], double[]> entry : ioTraining.entrySet()){
			input[n]= entry.getKey();
			output[n]= entry.getValue();

			n++;
		}

		MLDataSet trainingSet= new BasicMLDataSet(input, output);

		Backpropagation bp= new Backpropagation(network, trainingSet);
		bp.setLearningRate(0.01);
		bp.fixFlatSpot(false);
		int epoch= 0;
		System.out.println("Started training");
		do{
			bp.iteration();
			if (epoch% 5== 0)
				System.out.println("Epoch: "+ epoch+ " Error:"+ bp.getError());

			epoch++;
		} while (epoch< 30);
		bp.finishTraining();

		// Testing

		double[][] inputTesting= new double[ioTesting.size()][];
		double[][] outputTesting= new double[ioTesting.size()][];
		n= 0;
		for (Map.Entry<double[], double[]> entry : ioTesting.entrySet()){
			inputTesting[n]= entry.getKey();
			outputTesting[n]= entry.getValue();

			n++;
		}
		MLDataSet testingSet= new BasicMLDataSet(inputTesting, outputTesting);
		System.out.println("Testing error: "+ network.calculateError(testingSet));
		// EncogUtility.evaluate(network, testingSet);

		System.out.println("Error is "+ network.calculateError(trainingSet));
		EncogDirectoryPersistence.saveObject(new File(Constants.nn_path), network);
		Encog.getInstance().shutdown();
	}

	static BasicNetwork createJordanNetwork(){
		// construct an Elman type network
		JordanPattern  pattern= new JordanPattern ();
		pattern.setActivationFunction(new ActivationSigmoid());
		pattern.setInputNeurons(Constants.vectorSize- 1);
		pattern.addHiddenLayer(Constants.vectorSize* 3/ 3);
//		pattern.addHiddenLayer(Constants.vectorSize* 1/ 3);
		pattern.setOutputNeurons(Constants.vectorSize- 1);
		return (BasicNetwork) pattern.generate();
	}

	static BasicNetwork createFeedforwardNetwork(){
		// construct a feedforward type network
		FeedForwardPattern pattern= new FeedForwardPattern();
		pattern.setActivationFunction(new ActivationSigmoid());
		pattern.setInputNeurons(Constants.vectorSize- 1);
		pattern.addHiddenLayer(Constants.vectorSize* 3/ 3);
//		pattern.addHiddenLayer(Constants.vectorSize* 1/ 3);
		pattern.setOutputNeurons(Constants.vectorSize- 1);
		return (BasicNetwork) pattern.generate();
	}

	public static void trainRNN(Map<double[], double[]> ioTraining, Map<double[], double[]> ioTesting){
		final BasicNetwork elmanNetwork= EncogNeuralNet.createJordanNetwork();
//		final BasicNetwork feedforwardNetwork= EncogNeuralNet.createFeedforwardNetwork();

		double[][] input= new double[ioTraining.size()][];
		double[][] output= new double[ioTraining.size()][];
		int n= 0;
		for (Map.Entry<double[], double[]> entry : ioTraining.entrySet()){
			input[n]= entry.getKey();
			output[n]= entry.getValue();

			n++;
		}

		MLDataSet trainingSet= new BasicMLDataSet(input, output);

		final double elmanError= EncogNeuralNet.trainNetwork("Elman", elmanNetwork, trainingSet);
//		final double feedforwardError= EncogNeuralNet.trainNetwork("Feedforward", feedforwardNetwork, trainingSet);

		System.out.println("Best error rate with Elman Network: "+ elmanError);
//		System.out.println("Best error rate with Feedforward Network: "+ feedforwardError);
		System.out.println(
				"Elman should be able to get into the 10% range,\nfeedforward should not go below 25%.\nThe recurrent Elment net can learn better in this case.");
		System.out.println("If your results are not as good, try rerunning, or perhaps training longer.");

		// Testing

		double[][] inputTesting= new double[ioTesting.size()][];
		double[][] outputTesting= new double[ioTesting.size()][];
		n= 0;
		for (Map.Entry<double[], double[]> entry : ioTesting.entrySet()){
			inputTesting[n]= entry.getKey();
			outputTesting[n]= entry.getValue();

			n++;
		}
		MLDataSet testingSet= new BasicMLDataSet(inputTesting, outputTesting);
		
		System.out.println("Testing error elman: "+ elmanNetwork.calculateError(testingSet));
//		System.out.println("Testing error feedf: "+ feedforwardNetwork.calculateError(testingSet));
		// System.out.println("Testing error: "+ network.calculateError(testingSet));
		// // EncogUtility.evaluate(network, testingSet);
		//
		// System.out.println("Error is "+ network.calculateError(trainingSet));
		// EncogDirectoryPersistence.saveObject(new File(Constants.rnn_path), network);
		EncogDirectoryPersistence.saveObject(new File(Constants.rnn_path), elmanNetwork);
		Encog.getInstance().shutdown();
	}

	public static double trainNetwork(final String what, final BasicNetwork network, final MLDataSet trainingSet){
		// train the neural network
		CalculateScore score= new TrainingSetScore(trainingSet);
		final MLTrain trainAlt= new NeuralSimulatedAnnealing(network, score, 10, 2, 100);

		final MLTrain trainMain= new Backpropagation(network, trainingSet, 0.001, 0.0);

		final StopTrainingStrategy stop= new StopTrainingStrategy();
		trainMain.addStrategy(new Greedy());
		trainMain.addStrategy(new HybridStrategy(trainAlt));
		trainMain.addStrategy(stop);

		int epoch= 0;
		while (!stop.shouldStop()){
			trainMain.iteration();
			System.out.println("Training "+ what+ ", Epoch #"+ epoch+ " Error:"+ trainMain.getError());
			epoch++;
			if(epoch> 60)	break;
		}
		return trainMain.getError();
	}

	public static double[] prediction(double[] input){

		BasicNetwork network= (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(Constants.rnn_path));

		double[] output= new double[input.length];
		network.compute(input, output);
		return output;
	}
}
