package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

import config.Constants;

public class NeuralNet implements LearningEventListener{

	private int epoch= 0;
	
	public void train(Map<double[], double[]> io) {
		MultiLayerPerceptron mp= new MultiLayerPerceptron(TransferFunctionType.SIGMOID, (Constants.vectorSize- 1), (int)Math.sqrt(Constants.vectorSize/ 2), (Constants.vectorSize- 1));
//		mp.getLearningRule().addListener(this);
		
		//	Set the training data
		DataSet trainingSet= new DataSet(mp.getInputsCount(), mp.getOutputsCount());
		for(Map.Entry<double[], double[]> entry: io.entrySet()) {
			trainingSet.addRow(entry.getValue(), entry.getKey());
		}
//		System.out.println(trainingSet.size());
		BackPropagation bp= new BackPropagation();
		bp.setLearningRate(0.03);
		bp.setMaxError(0.001);
		bp.setMaxIterations(1000);
		bp.addListener(this);
		mp.setLearningRule(bp);
		
		System.out.println("Started training");
		mp.learn(trainingSet);
		System.out.println("Weight is "+ mp.getWeights()[0]);
		save(mp.getWeights());
		mp.save(config.Constants.nn_path);
	}
	
	public static double[] prediction(double[] input) {
//		NeuralNetwork  mp= new MultiLayerPerceptron(37, 20, 20, 37);
		NeuralNetwork mp= MultiLayerPerceptron.createFromFile(config.Constants.nn_path);
//		mp.setWeights(load());
		mp.setInput(input);
		mp.calculate();
//		System.out.println("Weights wil be printed after this");
//		System.out.println("Weights are "+ Arrays.toString(mp.getWeights()));
//		System.out.println("Output is "+ Arrays.toString(mp.getOutput()));
		return mp.getOutput();
	}
	
	private static void save(Double[] weights) {
		try {
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(config.Constants.nn_path+ "1"));
//			System.out.println(Arrays.toString(weights));
			for(Double weight: weights)	myWriter.write(weight+ ",");
			
			myWriter.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private static double[] load() {
		List<Double> list= new ArrayList<>();
		
		try {
			BufferedReader myReader= new BufferedReader(new java.io.FileReader(config.Constants.nn_path));
			
			String[] myRead= myReader.readLine().split(",");
//			System.out.println(Arrays.toString(myRead));
			for(int i= 0; i< myRead.length; i++)	list.add(Double.parseDouble(myRead[i]));
			
			myReader.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
//		System.out.println(list);
		return list.stream().mapToDouble(Double::doubleValue).toArray();
	}

	@Override
	public void handleLearningEvent(LearningEvent event){
		BackPropagation bp= (BackPropagation) event.getSource();
		
		this.epoch=  bp.getCurrentIteration();
		
		if(this.epoch%2== 0) {
			System.out.println(Instant.now()+ " Iteration: "+ this.epoch+ " Error: "+ bp.getTotalNetworkError());
		}
		
	}
}
