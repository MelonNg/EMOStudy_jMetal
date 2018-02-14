package jmetal.experiments.settings;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.moead.MOEAD_DRA_STM;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Mengyuan Wu on 09-Jun-15.
 */
public class MOEAD_DRA_STM_Settings extends Settings {
    public double CR_ ;
    public double F_  ;
    public int populationSize_ ;
    public int maxEvaluations_ ;
    public int finalSize_      ;

    public double mutationProbability_          ;
    public double mutationDistributionIndex_ ;

    public String crossoverName_  ;
    public String mutationName_  ;
    public int numPoints_;

    public int T_        ;
    public double delta_ ;
    public int nr_    ;

    public String dataDirectory_  ;

    /**
     * Constructor
     */
    public MOEAD_DRA_STM_Settings(String problem) {
        this(problem, "Real");
    }

    public MOEAD_DRA_STM_Settings(String problem, String solutionType) {
        super(problem);

        Object [] problemParams = {solutionType};
        try {
            problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
        } catch (JMException e) {
            e.printStackTrace();
        }

        // Default experiments.settings
        CR_ = 1.0 ;
        F_  = 0.5 ;
        populationSize_ = 600;
        maxEvaluations_ = 300000;

        finalSize_ = 300 ;

        mutationProbability_ = 1.0/problem_.getNumberOfVariables() ;
        mutationDistributionIndex_ = 20;

        crossoverName_ = "DifferentialEvolutionCrossover" ;
        mutationName_ = "PolynomialMutation" ;
        numPoints_ = 1;

        T_ = 20;
        delta_ = 0.9;

        dataDirectory_ =  "./data/MOEAD_parameters/Weight" ;

    }

    /**
     * Configure the algorithm with the specified parameter experiments.settings
     * @return an algorithm object
     * @throws JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Operator crossover;
        Operator mutation;

        HashMap parameters ; // Operator parameters

        // Creating the problem
        algorithm = new MOEAD_DRA_STM(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_) ;
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_) ;
        algorithm.setInputParameter("dataDirectory", dataDirectory_)   ;
        algorithm.setInputParameter("finalSize", finalSize_)           ;

        algorithm.setInputParameter("T", T_) ;
        algorithm.setInputParameter("delta", delta_) ;

        // Crossover operator
        parameters = new HashMap() ;
        parameters.put("CR", CR_) ;
        parameters.put("F", F_) ;
        parameters.put("numPoints", numPoints_) ;
        crossover = CrossoverFactory.getCrossoverOperator(crossoverName_, parameters);

        // Mutation operator
        parameters = new HashMap() ;
        parameters.put("probability", mutationProbability_) ;
        parameters.put("distributionIndex", mutationDistributionIndex_) ;
        parameters.put("numPoints", numPoints_) ;
        mutation = MutationFactory.getMutationOperator(mutationName_, parameters);

        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);

        return algorithm;
    } // configure

    /**
     * Configure MOEAD_DRA with user-defined parameter experiments.settings
     * @return A MOEAD_DRA algorithm object
     */
    @Override
    public Algorithm configure(Properties configuration) throws JMException {
        Algorithm algorithm ;
        Selection selection ;
        Crossover crossover ;
        Mutation mutation  ;

        HashMap  parameters ; // Operator parameters

        // Creating the algorithm.
        algorithm = new MOEAD_DRA_STM(problem_) ;

        // Algorithm parameters
        populationSize_ = Integer.parseInt(configuration.getProperty("populationSize",String.valueOf(populationSize_)));
        maxEvaluations_  = Integer.parseInt(configuration.getProperty("maxEvaluations",String.valueOf(maxEvaluations_)));
        finalSize_  = Integer.parseInt(configuration.getProperty("finalSize",String.valueOf(finalSize_)));
        dataDirectory_  = configuration.getProperty("dataDirectory", dataDirectory_);
        delta_ = Double.parseDouble(configuration.getProperty("delta", String.valueOf(delta_)));
        T_ = Integer.parseInt(configuration.getProperty("T", String.valueOf(T_)));
        nr_ = Integer.parseInt(configuration.getProperty("nr", String.valueOf(nr_)));

        algorithm.setInputParameter("populationSize",populationSize_);
        algorithm.setInputParameter("maxEvaluations",maxEvaluations_);
        algorithm.setInputParameter("dataDirectory",dataDirectory_)  ;
        algorithm.setInputParameter("finalSize", finalSize_)         ;

        algorithm.setInputParameter("T", T_) ;
        algorithm.setInputParameter("delta", delta_) ;

        // Crossover operator
        CR_ = Double.parseDouble(configuration.getProperty("CR",String.valueOf(CR_)));
        F_ = Double.parseDouble(configuration.getProperty("F",String.valueOf(F_)));
        parameters = new HashMap() ;
        parameters.put("CR", CR_) ;
        parameters.put("F", F_) ;
        crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover", parameters);

        // Mutation parameters
        mutationProbability_ = Double.parseDouble(configuration.getProperty("mutationProbability",String.valueOf(mutationProbability_)));
        mutationDistributionIndex_ = Double.parseDouble(configuration.getProperty("mutationDistributionIndex",String.valueOf(mutationDistributionIndex_)));
        parameters = new HashMap() ;
        parameters.put("probability", mutationProbability_) ;
        parameters.put("distributionIndex", mutationDistributionIndex_) ;
        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover",crossover);
        algorithm.addOperator("mutation",mutation);

        return algorithm ;
    }
}