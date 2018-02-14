package jmetal.metaheuristics.gwasfga;

import java.util.Vector;

import jmetal.core.*;
import jmetal.util.*;

/**
 * Implementation of the Global WASF-GA algorithm. It is a generalized version
 * of the preference-based algorithm WASF-GA.
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
public class GWASFGA extends Algorithm {
    double[][] oddWeights_;
    double[][] pairWeights_;
    AchievementScalarizingFunction asfInUtopian, asfInNadir;
    boolean estimatePoints, normalization;
    double utopianValueInPercent;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public GWASFGA(Problem problem) {
        super(problem);
    } // GWASFGA

    /**
     * Runs the GWASFGA algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     * @throws ClassNotFoundException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;
        String weightsDirectory, weightFileName;
        Vector<double[][]> weights;

        SolutionSet population;
        SolutionSet offspringPopulation;
        SolutionSet union;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;

        evaluations = 0;

        // --- INITIALIZING DATA --- \\

        // Read the population size
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        population = new SolutionSet(populationSize);

        // Read the number of evaluations
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        // Read the normalization parameter.
        // It indicates if the Achievement Scalarizing Functions are normalized or not
        normalization = ((Boolean) getInputParameter("normalization")).booleanValue();

        // Read the estimatePoints parameter.
        estimatePoints = ((Boolean) getInputParameter("estimatePoints")).booleanValue();
        // If true, the nadir and ideal point of the Achievement Scalarizing Functions will be estimated using
        // the solutions in the population
        if (estimatePoints) {
            asfInUtopian = new AchievementScalarizingFunction(problem_.getNumberOfObjectives());
            asfInNadir = new AchievementScalarizingFunction(problem_.getNumberOfObjectives());
        } else {
            asfInUtopian = (AchievementScalarizingFunction) getInputParameter("asfInUtopian");
            asfInNadir = (AchievementScalarizingFunction) getInputParameter("asfInNadir");
        }
        utopianValueInPercent = ((Double) getInputParameter("utopianValueInPercent")).doubleValue();

        // Read the weights directory parameter
        weightsDirectory = this.getInputParameter("weightsDirectory").toString();
        // the name of the weight file must be "WND-P.dat", where N is the
        // dimension of the problem and P the population size
        weightFileName = weightsDirectory + "/W" + problem_.getNumberOfObjectives() + "D_" + populationSize + ".dat";

        // Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");

        // If the dimension of the problem is equal to 2, the weight vectors are
        // calculated
        if (problem_.getNumberOfObjectives() == 2) {
            weights = Weights.initUniformPairAndOddWeights2D(0.005, populationSize);
        }
        // If the dimension of the problem is greater than 2, the weight vectors
        // are read from a file
        else {
            weights = Weights.getPairAndOddWeightsFromFile(weightFileName);
        }

        // The weight vectors are inverted
        pairWeights_ = Weights.invertWeights(weights.get(0), true);
        oddWeights_ = Weights.invertWeights(weights.get(1), true);

        // --- ALGORITHM --- \\

        // Create the initial solutionSet
        for (int i = 0; i < populationSize; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            population.add(newSolution);

            // If the ideal point of the problem is not known, it is estimated
            // for the Achievement Scalarizing Functions
            if (estimatePoints)
                updateIdealPoint(newSolution);
        } // for

        // If the nadir point of the problem is not known, it is estimated
        // for the Achievement Scalarizing Functions
        if (estimatePoints)
            updateNadirPoint(population);

        // Evolutionary process
        while (evaluations < maxEvaluations) {
            // Create the offSpring solutionSet
            offspringPopulation = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];

            for (int i = 0; i < (populationSize / 2); i++) {
                // obtain parents
                parents[0] = (Solution) selectionOperator.execute(population);
                parents[1] = (Solution) selectionOperator.execute(population);
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                mutationOperator.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                offspringPopulation.add(offSpring[0]);
                offspringPopulation.add(offSpring[1]);
                evaluations += 2;

                // If the ideal point of the problem is not known, it is
                // estimated for the Achievement Scalarizing Functions
                if (estimatePoints) {
                    updateIdealPoint(offSpring[0]);
                    updateIdealPoint(offSpring[1]);
                } // end if (evaluations < maxEvaluations)
            } // end for (int i = 0; i < (populationSize / 2); i++)
            if (offspringPopulation.size() < populationSize) {
                // obtain parents
                parents[0] = (Solution) selectionOperator.execute(population);
                parents[1] = (Solution) selectionOperator.execute(population);
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                offspringPopulation.add(offSpring[0]);
                evaluations += 1;

                // If the ideal point of the problem is not known, it is
                // estimated for the Achievement Scalarizing Functions
                if (estimatePoints) {
                    updateIdealPoint(offSpring[0]);
                } // end if (evaluations < maxEvaluations)
            }

            // Create the solutionSet union of solutionSet and offSpring
            union = ((SolutionSet) population).union(offspringPopulation);

            // Ranking the union considering the values of the Achievement Scalarizing Functions
            RankingASFs ranking = new RankingASFs(union, asfInUtopian, this.pairWeights_, asfInNadir, this.oddWeights_, normalization);

            // Obtain the next front
            int remain = populationSize;
            int index = 0;
            SolutionSet front;
            population.clear();
            front = ranking.getSubfront(index);

            while ((remain > 0) && (remain >= front.size())) {
                // Add the individuals of this front
                for (int k = 0; k < front.size(); k++) {
                    population.add(front.get(k));
                }

                // Decrement remain
                remain = remain - front.size();

                // Obtain the next front
                index++;
                if (remain > 0) {
                    front = ranking.getSubfront(index);
                }
            } // while

            // Remain is less than front(index).size, insert only the best one
            if (remain > 0) {
                // front contains individuals to insert
                for (int k = 0; k < remain; k++) {
                    population.add(front.get(k));
                }

                remain = 0;
            }

            // If the nadir point of the problem is not known, it is estimated
            // for the Achievement Scalarizing Functions
            if (estimatePoints)
                updateNadirPoint(ranking.getSubfront(0));
        } // end while (evaluations < maxEvaluations)

        // Return the first non-dominated front
        RankingASFs ranking = new RankingASFs(population, asfInUtopian, this.pairWeights_, asfInNadir, this.oddWeights_, normalization);

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", evaluations);

        return ranking.getSubfront(0);
    } // execute

    /**
     * Update the ideal point in the two Achievement Scalarizing Functions (one
     * for the ideal point and the other one for the nadir point) considering
     * the values of the given <code>Solution</code> In fact, we use the utopian
     * value (a value better than the ideal). So, the best values are improved
     *
     * @param individual
     */
    private void updateIdealPoint(Solution individual) {
        double improvementValue;

        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) <= this.asfInUtopian.getIdeal()[n]) {
                improvementValue = (this.utopianValueInPercent * (this.asfInUtopian.getNadir()[n] - individual.getObjective(n))) / 100;
                improvementValue = Math.abs(improvementValue);
                this.asfInUtopian.setReferencePoint(n, individual.getObjective(n) - improvementValue);

                this.asfInUtopian.setIdeal(n, individual.getObjective(n));
                this.asfInNadir.setIdeal(n, individual.getObjective(n));
            }
        }
    }

    /**
     * Update the nadir point in the two Achievement Scalarizing Functions (one
     * for the ideal point and the other one for the nadir point) considering
     * the values of the given <code>Solution</code> In fact, we use a worse
     * nadir value (a value worse than the ideal). So, the worst values are
     * worsen
     *
     * @param individual
     */
    private void updateNadirPoint(Solution individual) {
        double deteriorationValue;

        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) >= this.asfInNadir.getNadir()[n]) {
                deteriorationValue = (this.utopianValueInPercent * (this.asfInUtopian.getIdeal()[n] - individual.getObjective(n))) / 100;
                deteriorationValue = Math.abs(deteriorationValue);
                this.asfInNadir.setReferencePoint(n, individual.getObjective(n) + deteriorationValue);

                this.asfInNadir.setNadir(n, individual.getObjective(n));
                this.asfInUtopian.setNadir(n, individual.getObjective(n));
            }
        }
    }

    /**
     * Update the nadir point in the two Achievement Scalarizing Functions (one
     * for the ideal point and the other one for the nadir point) considering
     * the values of the solutions in the given <code>SolutionSet</code> In
     * fact, we use a worse nadir value (a value worse than the ideal). So, the
     * worst values are worsen
     *
     * @param population
     */
    private void updateNadirPoint(SolutionSet population) {
        double deteriorationValue;

        // The nadir point is initialized considering the first solution in the
        // population
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asfInNadir.setNadir(n, population.get(0).getObjective(n));
            this.asfInUtopian.setNadir(n, population.get(0).getObjective(n));

            deteriorationValue = (this.utopianValueInPercent * (this.asfInUtopian.getIdeal()[n] - population.get(0).getObjective(n))) / 100;
            deteriorationValue = Math.abs(deteriorationValue);
            this.asfInNadir.setReferencePoint(n, population.get(0).getObjective(n) + deteriorationValue);
        }

        // The nadir point is updated considering the other solutions in the
        // population
        for (int i = 1; i < population.size(); i++) {
            updateNadirPoint(population.get(i));
        }
    }
}
