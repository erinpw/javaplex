package edu.stanford.math.plex4.homology.mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultiStartMultivariateRealOptimizer;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.random.GaussianRandomGenerator;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomVectorGenerator;
import org.apache.commons.math.random.UncorrelatedRandomVectorGenerator;

import edu.stanford.math.plex4.algebraic_structures.impl.DoubleFreeModule;
import edu.stanford.math.plex4.algebraic_structures.interfaces.GenericOrderedField;
import edu.stanford.math.plex4.datastructures.DoubleFormalSum;
import edu.stanford.math.plex4.datastructures.pairs.GenericPair;
import edu.stanford.math.plex4.free_module.AbstractGenericFormalSum;
import edu.stanford.math.plex4.free_module.AbstractGenericFreeModule;
import edu.stanford.math.plex4.free_module.UnorderedGenericFreeModule;
import edu.stanford.math.plex4.functional.GenericDoubleFunction;
import edu.stanford.math.plex4.homology.GenericAbsoluteHomology;
import edu.stanford.math.plex4.homology.barcodes.AugmentedBarcodeCollection;
import edu.stanford.math.plex4.homology.streams.derived.HomStream;
import edu.stanford.math.plex4.homology.streams.interfaces.AbstractFilteredStream;
import edu.stanford.math.plex4.utility.ArrayUtility;


public class HomComplexComputation<F extends Number, M, N> {
	private final GenericOrderedField<F> field;
	private final AbstractGenericFreeModule<F, GenericPair<M, N>> genericChainModule;
	private final DoubleFreeModule<GenericPair<M, N>> doubleChainModule = new DoubleFreeModule<GenericPair<M, N>>();
	
	private final AbstractFilteredStream<M> domainStream;
	private final AbstractFilteredStream<N> codomainStream;
	private final Comparator<M> domainComparator;
	private final Comparator<N> codomainComparator;

	private final HomStream<M, N> homStream;

	public HomComplexComputation(AbstractFilteredStream<M> domainStream, 
			AbstractFilteredStream<N> codomainStream, 
			Comparator<M> domainComparator, 
			Comparator<N> codomainComparator, 
			GenericOrderedField<F> field) {

		this.domainStream = domainStream;
		this.codomainStream = codomainStream;
		this.domainComparator = domainComparator;
		this.codomainComparator = codomainComparator;

		this.field = field;
		genericChainModule = new UnorderedGenericFreeModule<F, GenericPair<M, N>>(this.field);

		this.homStream = new HomStream<M, N>(this.domainStream, this.codomainStream, this.domainComparator, this.codomainComparator);
	}

	public List<AbstractGenericFormalSum<F, GenericPair<M, N>>> computeGeneratingCycles() {
		homStream.finalizeStream();

		//GenericPersistentHomologyOld<F, GenericPair<M, N>> homology = new GenericPersistentHomologyOld<F, GenericPair<M, N>>(this.field, homStream.getDerivedComparator(), 1);
		GenericAbsoluteHomology<F, GenericPair<M, N>> homology = new GenericAbsoluteHomology<F, GenericPair<M, N>>(this.field, homStream.getDerivedComparator(), 1);
		AugmentedBarcodeCollection<AbstractGenericFormalSum<F, GenericPair<M, N>>> barcodes = homology.computeAugmentedIntervals(homStream);

		List<AbstractGenericFormalSum<F, GenericPair<M, N>>> generatingCycles = new ArrayList<AbstractGenericFormalSum<F, GenericPair<M, N>>>();

		int numCycles = barcodes.getBarcode(0).getSize();
		for (int i = 0; i < numCycles; i++) {
			generatingCycles.add(barcodes.getBarcode(0).getGeneratingCycle(i));
		}

		return generatingCycles;
	}

	public AbstractGenericFormalSum<F, GenericPair<M, N>> sumGeneratingCycles(List<AbstractGenericFormalSum<F, GenericPair<M, N>>> generatingCycles) {
		AbstractGenericFormalSum<F, GenericPair<M, N>> sum = this.genericChainModule.createNewSum();
		int numCycles = generatingCycles.size();

		for (int i = 0; i < numCycles; i++) {
			sum = this.genericChainModule.add(sum, generatingCycles.get(i));
		}

		return sum;
	}

	public List<AbstractGenericFormalSum<F, GenericPair<M, N>>> getChainHomotopies() {
		GenericAbsoluteHomology<F, GenericPair<M, N>> homology = new GenericAbsoluteHomology<F, GenericPair<M, N>>(this.field, homStream.getDerivedComparator(), 1);
		
		return homology.getBoundaryColumns(this.homStream, 1);
	}

	private DoubleFormalSum<GenericPair<M, N>> computeHomCycle(double[] homotopyCoefficients,
			DoubleFormalSum<GenericPair<M, N>> generatingCycle,
			List<DoubleFormalSum<GenericPair<M, N>>> homotopies) {
		
		DoubleFormalSum<GenericPair<M, N>> homCycle = new DoubleFormalSum<GenericPair<M, N>>();
		
		homCycle = doubleChainModule.add(homCycle, generatingCycle);
		
		int i = 0;
		for (DoubleFormalSum<GenericPair<M, N>> homotopy: homotopies) {
			homCycle = doubleChainModule.add(homCycle, doubleChainModule.multiply(homotopyCoefficients[i], homotopy));
			i++;
		}
		
		return homCycle;		
	}
	
	private DoubleFormalSum<GenericPair<M, N>> computeHomCycle(double[] homotopyCoefficients,
			AbstractGenericFormalSum<F, GenericPair<M, N>> generatingCycle,
			List<AbstractGenericFormalSum<F, GenericPair<M, N>>> homotopies) {
		return this.computeHomCycle(homotopyCoefficients, MappingUtility.toDoubleFormalSum(generatingCycle), MappingUtility.toDoubleFormalSumList(homotopies));
	}
	
	public RealPointValuePair findOptimalCoefficients(final AbstractGenericFormalSum<F, GenericPair<M, N>> generatingCycle, 
			final List<AbstractGenericFormalSum<F, GenericPair<M, N>>> homotopies,
			final GenericDoubleFunction<DoubleFormalSum<GenericPair<M, N>>> mappingPenaltyFunction) throws OptimizationException, FunctionEvaluationException, IllegalArgumentException {
		
		MultivariateRealFunction objective = this.getObjectiveFunctionViaMappingPenalty(generatingCycle, homotopies, mappingPenaltyFunction);
		
		//MultivariateRealOptimizer optimizer = new NelderMead();
		RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(homotopies.size(), new GaussianRandomGenerator(new MersenneTwister()));
		MultivariateRealOptimizer optimizer = new MultiStartMultivariateRealOptimizer(new NelderMead(), 10, generator);
		
		DiscreteOptimization discreteOptimizer = new DiscreteOptimization();
		
		double initialValue = objective.value(new double[homotopies.size()]);
		System.out.println(initialValue);
		
		RealPointValuePair optimum = optimizer.optimize(objective, GoalType.MINIMIZE, new double[homotopies.size()]);
		
		//RealPointValuePair optimum = discreteOptimizer.multistartOptimize(objective, homotopies.size(), 20);
		
		System.out.println(optimum.getValue());
		System.out.println(ArrayUtility.toMatlabString(optimum.getPoint()));
		
		return optimum;
	}
	
	public DoubleFormalSum<GenericPair<M, N>> findOptimalChainMap(final AbstractGenericFormalSum<F, GenericPair<M, N>> generatingCycle, 
			final List<AbstractGenericFormalSum<F, GenericPair<M, N>>> homotopies,
			final GenericDoubleFunction<DoubleFormalSum<GenericPair<M, N>>> mappingPenaltyFunction) throws OptimizationException, FunctionEvaluationException, IllegalArgumentException {
		
		RealPointValuePair pair = this.findOptimalCoefficients(generatingCycle, homotopies, mappingPenaltyFunction);
		return this.computeHomCycle(MappingUtility.round(pair.getPoint()), generatingCycle, homotopies);
	}
	
	MultivariateRealFunction getObjectiveFunctionViaMappingPenalty(final AbstractGenericFormalSum<F, GenericPair<M, N>> generatingCycle, 
			final List<AbstractGenericFormalSum<F, GenericPair<M, N>>> homotopies,
			final GenericDoubleFunction<DoubleFormalSum<GenericPair<M, N>>> mappingPenaltyFunction) {
	
		return new MultivariateRealFunction() {

			public double value(double[] arg0) throws FunctionEvaluationException, IllegalArgumentException {
				DoubleFormalSum<GenericPair<M, N>> homCycle = computeHomCycle(arg0, generatingCycle, homotopies);
				double penalty = 0;
				for (int i = 0; i < arg0.length; i++) {
					//penalty += Math.abs(arg0[i] - (i % 2 ==1 ? 1 : 1));
					
					penalty += Math.abs(arg0[i]);
					
					/*
					if (arg0[i] > 1) {
						penalty += Math.abs(arg0[i] - 1);
					} else if (arg0[i] < -1) {
						penalty += Math.abs(arg0[i] + 1);
					}*/
				}
				
				return mappingPenaltyFunction.evaluate(homCycle) + 0 * penalty;
			}
		};
	}
	
	MultivariateRealFunction getObjectiveFunctionViaImagePenalty(final AbstractGenericFormalSum<F, GenericPair<M, N>> generatingCycle, 
			final List<AbstractGenericFormalSum<F, GenericPair<M, N>>> homotopies,
			final GenericDoubleFunction<DoubleFormalSum<N>> imagePenaltyFunction) {
		
		return getObjectiveFunctionViaMappingPenalty(generatingCycle, homotopies, MappingUtility.computeInducedFunction(imagePenaltyFunction, domainStream));
	}
}