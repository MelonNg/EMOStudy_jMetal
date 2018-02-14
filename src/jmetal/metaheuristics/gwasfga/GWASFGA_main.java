package jmetal.metaheuristics.gwasfga;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.gwasfga.GWASFGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.problems.ZDT.ZDT1;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Class to configure and execute the GWASFGA algorithm.
 *
 * @author Rubén Saborido Infantes
 *         <p/>
 *         R. Saborido, A.B. Ruiz, M. Luque "The Global WASF-GA: An Evolutionary
 *         Algorithm in Multiobjective Optimization to Approximate the Whole
 *         Pareto Optimal Front"
 *         <p/>
 *         It was presented in March 2015 in the following conference: X
 *         Congreso Español sobre Metaheurísticas, Algoritmos Evolutivos y
 *         Bioinspirados (MAEB 2015), At Mérida-Almendralejo (España)
 */

public class GWASFGA_main {
    public static Logger logger_;      // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage if objectives number is equal to two:
     *                           - jmetal.metaheuristics.gwasfga.GWASFGA_main problemName
     *                           - jmetal.metaheuristics.gwasfga.GWASFGA_main problemName paretoFrontFile
     *                           <p/>
     *                           Usage if objectives number is greater than two:
     *                           - jmetal.metaheuristics.gwasfga.GWASFGA_main problemName weightsDirectory
     *                           - jmetal.metaheuristics.gwasfga.GWASFGA_main problemName paretoFrontFile weightsDirectory
     *                           <p/>
     *                           The weightsDirectory is a folder containing the weights vector files.
     *                           The name of these files is WND_P.txt, where N is the number of objectives and P is the population size (one vector for each solution)
     */
    public static void main(String[] args) throws
            JMException,
            SecurityException,
            IOException,
            ClassNotFoundException {
        Problem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use
        Operator crossover; // Crossover operator
        Operator mutation; // Mutation operator
        Operator selection; // Selection operator

        HashMap parameters; // Operator parameters

        QualityIndicator indicators; // Object to get quality indicators
        String weightsDirectory;

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("GWASFGA_main.log");
        logger_.addHandler(fileHandler_);

        indicators = null;
        if (args.length == 1) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
            weightsDirectory = "";

            if (problem.getNumberOfObjectives() > 2) {
                logger_.severe("If the number of objectives is greater than two, a weights vector folder must be specified. ");
                System.exit(-1);
            }
        } // if
        if (args.length == 2) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);

            if (problem.getNumberOfObjectives() > 2)
                weightsDirectory = args[1];
            else {
                indicators = new QualityIndicator(problem, args[1]);
                weightsDirectory = "";
            }
        } // if
        else if (args.length == 3) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
            indicators = new QualityIndicator(problem, args[1]);
            weightsDirectory = args[2];
        } // if
        else { // Default problem
            problem = new ZDT1("ArrayReal", 30);
            double[] rp = new double[2];
            rp[0] = 0;
            rp[1] = 0;
            weightsDirectory = "";
        }

        algorithm = new GWASFGA(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 100);
        algorithm.setInputParameter("maxEvaluations", 30000);
        algorithm.setInputParameter("weightsDirectory", weightsDirectory);
        algorithm.setInputParameter("normalization", true);
        algorithm.setInputParameter("estimatePoints", true);
        algorithm.setInputParameter("asfInUtopian", new AchievementScalarizingFunction(problem.getNumberOfObjectives()));
        algorithm.setInputParameter("asfInNadir", new AchievementScalarizingFunction(problem.getNumberOfObjectives()));
        algorithm.setInputParameter("utopianValueInPercent", 1.0);

        // Mutation and Crossover for Real codification
        parameters = new HashMap();
        parameters.put("probability", 0.9);
        parameters.put("distributionIndex", 20.0);
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

        parameters = new HashMap();
        parameters.put("probability", 1.0 / problem.getNumberOfVariables());
        parameters.put("distributionIndex", 20.0);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

        // Selection Operator
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Add the indicator object to the algorithm
        algorithm.setInputParameter("indicators", indicators);

        // Execute the Algorithm
        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages
        logger_.info("Total execution time: " + estimatedTime + "ms");
        logger_.info("Variables values have been writen to file VAR_GWASFGA");
        population.printVariablesToFile("VAR_GWASFGA");
        logger_.info("Objectives values have been writen to file FUN_GWASFGA");
        population.printObjectivesToFile("FUN_GWASFGA");

        if (indicators != null) {
            logger_.info("Quality indicators");
            logger_.info("Hypervolume: " + indicators.getHypervolume(population));
            logger_.info("GD         : " + indicators.getGD(population));
            logger_.info("IGD        : " + indicators.getIGD(population));
            logger_.info("Spread     : " + indicators.getSpread(population));
            logger_.info("Epsilon    : " + indicators.getEpsilon(population));

            int evaluations = ((Integer) algorithm.getOutputParameter("evaluations")).intValue();
            logger_.info("Speed      : " + evaluations + " evaluations");
        } // if
    } //main
} // GWASFGA_main
