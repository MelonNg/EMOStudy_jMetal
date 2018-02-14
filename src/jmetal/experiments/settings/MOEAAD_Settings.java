package jmetal.experiments.settings;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.moead.*;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

import java.util.HashMap;

/**
 * Created by mengyuawu3 on 23-Jun-16.
 */
public class MOEAAD_Settings extends Settings {
    public double crossoverProbability_;
    public double crossoverDistributionIndex_;
    public double mutationProbability_;
    public double mutationDistributionIndex_;

    public int populationSize_;
    public int maxEvaluations_;
    public int T_;
    public double delta_;
    public int nr1_;
    public int nr2_;
    public String functionType1_;
    public String functionType2_;

    public String dataDirectory_;

    /**
     * Constructor
     */
    public MOEAAD_Settings(String problem) {
        super(problem);

        defaultSetting(problem);
    }

    /**
     * Configure the algorithm with the specified parameter experiments.settings
     *
     * @return an algorithm object
     * @throws JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Operator crossover;
        Operator mutation;

        HashMap parameters; // Operator parameters

        // Creating the problem
        algorithm = new MOEAAD(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("T", T_);
        algorithm.setInputParameter("delta", delta_);
        algorithm.setInputParameter("nr1", nr1_);
        algorithm.setInputParameter("nr2", nr2_);
        algorithm.setInputParameter("functionType1", functionType1_);
        algorithm.setInputParameter("functionType2", functionType2_);
        algorithm.setInputParameter("dataDirectory", dataDirectory_);

        // Crossover operator
        parameters = new HashMap();
        parameters.put("probability", crossoverProbability_);
        parameters.put("distributionIndex", crossoverDistributionIndex_);
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

        // Mutation operator
        parameters = new HashMap();
        parameters.put("probability", mutationProbability_);
        parameters.put("distributionIndex", mutationDistributionIndex_);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);

        return algorithm;
    } // configure


    private void defaultSetting(String problem) {
        try {
            if (problem.startsWith("WFG")) {
                int numberOfObjectives = Integer.parseInt(problem.substring(5));
                switch (numberOfObjectives) {
                    case 2:
                        populationSize_ = 100;
                        maxEvaluations_ = populationSize_ * 250;
                        break;
                    case 3:
                        populationSize_ = 91;
                        maxEvaluations_ = populationSize_ * 400;
                        break;
                    case 5:
                        populationSize_ = 210;
                        maxEvaluations_ = populationSize_ * 750;
                        break;
                    case 8:
                        populationSize_ = 156;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 1500;
                        break;
                    case 10:
                        populationSize_ = 275;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 2000;
                        break;
                    case 12:
                        populationSize_ = 442;
                        maxEvaluations_ = populationSize_ * 2500;
                        break;
                    case 13:
                        populationSize_ = 104;
                        maxEvaluations_ = populationSize_ * 2500;
                        break;
                    case 15:
                        populationSize_ = 135;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 3000;
                        break;
                    default:
                        populationSize_ = 100;
                        maxEvaluations_ = 25000;
                }
                Object[] problemParams = {"Real", 2 * (numberOfObjectives - 1), 20, numberOfObjectives};
                problemName_ = problem.substring(0, 4);
                problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
            } else if (problem.startsWith("iWFG")) {
                int numberOfObjectives = Integer.parseInt(problem.substring(6));
                switch (numberOfObjectives) {
                    case 2:
                        populationSize_ = 100;
                        maxEvaluations_ = populationSize_ * 250;
                        break;
                    case 3:
                        populationSize_ = 91;
                        maxEvaluations_ = populationSize_ * 400;
                        break;
                    case 5:
                        populationSize_ = 210;
                        maxEvaluations_ = populationSize_ * 750;
                        break;
                    case 8:
                        populationSize_ = 156;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 1500;
                        break;
                    case 10:
                        populationSize_ = 275;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 2000;
                        break;
                    case 12:
                        populationSize_ = 442;
                        maxEvaluations_ = populationSize_ * 2500;
                        break;
                    case 13:
                        populationSize_ = 104;
                        maxEvaluations_ = populationSize_ * 2500;
                        break;
                    case 15:
                        populationSize_ = 135;
                        populationSize_ += 1;
                        maxEvaluations_ = populationSize_ * 3000;
                        break;
                    default:
                        populationSize_ = 100;
                        maxEvaluations_ = 25000;
                }
                Object[] problemParams = {"Real", 2 * (numberOfObjectives - 1), 20, numberOfObjectives};
                problemName_ = problem.substring(0, 5);
                problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
            } else if (problem.startsWith("DTLZ")) {
                int numberOfObjectives = Integer.parseInt(problem.substring(6));
                switch (numberOfObjectives) {
                    case 3:
                        populationSize_ = 91;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 400;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 250;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 600;
                                break;
                            case 5:
                                maxEvaluations_ = populationSize_ * 300;
                                break;
                            case 6:
                                maxEvaluations_ = populationSize_ * 500;
                                break;
                            case 7:
                                maxEvaluations_ = populationSize_ * 300;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 5:
                        populationSize_ = 210;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 600;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 350;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            default:
                                maxEvaluations_ = 54600;
                                break;
                        }
                        break;
                    case 8:
                        populationSize_ = 156;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 750;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 500;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 1250;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 10:
                        populationSize_ = 275;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 750;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1500;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 2000;
                                break;
                        }
                        break;
                    case 15:
                        populationSize_ = 135;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 1500;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 2000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 3000;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 25:
                        populationSize_ = 252;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 2:
                                maxEvaluations_ = 201600;
                                break;
                            default:
                                maxEvaluations_ = 201600;
                                break;
                        }
                        break;
                    case 50:
                        populationSize_ = 500;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 2:
                                maxEvaluations_ = 600000;
                                break;
                            default:
                                maxEvaluations_ = 600000;
                                break;
                        }
                        break;
                    case 75:
                        populationSize_ = 752;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 2:
                                maxEvaluations_ = 1128000;
                                break;
                            default:
                                maxEvaluations_ = 1128000;
                                break;
                        }
                        break;
                    case 100:
                        populationSize_ = 1000;
                        switch (Integer.parseInt(problem.substring(4, 5))) {
                            case 1:
                                maxEvaluations_ = 2500000;
                                break;
                            case 2:
                                maxEvaluations_ = 2000000;
                                break;
                            default:
                                maxEvaluations_ = 2000000;
                                break;
                        }
                        break;
                    default:
                        populationSize_ = 91;
                        maxEvaluations_ = 75000;
                }
                problemName_ = problem.substring(0, 5);
                if (Integer.parseInt(problem.substring(4, 5)) == 1) {
                    Object[] problemParams = {"Real", numberOfObjectives + 4, numberOfObjectives};
                    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
                } else {
                    Object[] problemParams = {"Real", numberOfObjectives + 9, numberOfObjectives};
                    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
                }
            } else if (problem.startsWith("iDTLZ")) {
                int numberOfObjectives = Integer.parseInt(problem.substring(7));
                switch (numberOfObjectives) {
                    case 3:
                        populationSize_ = 91;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = 36400;
                                break;
                            case 2:
                                maxEvaluations_ = 22750;
                                break;
                            case 3:
                                maxEvaluations_ = 91000;
                                break;
                            case 4:
                                maxEvaluations_ = 54600;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 5:
                        populationSize_ = 210;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = 126000;
                                break;
                            case 2:
                                maxEvaluations_ = 73500;
                                break;
                            case 3:
                                maxEvaluations_ = 210000;
                                break;
                            case 4:
                                maxEvaluations_ = 210000;
                                break;
                            default:
                                maxEvaluations_ = 54600;
                                break;
                        }
                        break;
                    case 8:
                        populationSize_ = 156;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 750;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 500;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 1250;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 10:
                        populationSize_ = 275;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 750;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 1500;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 2000;
                                break;
                        }
                        break;
                    case 15:
                        populationSize_ = 135;
                        populationSize_ += 1;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = populationSize_ * 1500;
                                break;
                            case 2:
                                maxEvaluations_ = populationSize_ * 1000;
                                break;
                            case 3:
                                maxEvaluations_ = populationSize_ * 2000;
                                break;
                            case 4:
                                maxEvaluations_ = populationSize_ * 3000;
                                break;
                            default:
                                maxEvaluations_ = 75000;
                                break;
                        }
                        break;
                    case 25:
                        populationSize_ = 252;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 2:
                                maxEvaluations_ = 201600;
                                break;
                            default:
                                maxEvaluations_ = 201600;
                                break;
                        }
                        break;
                    case 50:
                        populationSize_ = 500;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 2:
                                maxEvaluations_ = 600000;
                                break;
                            default:
                                maxEvaluations_ = 600000;
                                break;
                        }
                        break;
                    case 75:
                        populationSize_ = 752;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 2:
                                maxEvaluations_ = 1128000;
                                break;
                            default:
                                maxEvaluations_ = 1128000;
                                break;
                        }
                        break;
                    case 100:
                        populationSize_ = 1000;
                        switch (Integer.parseInt(problem.substring(5, 6))) {
                            case 1:
                                maxEvaluations_ = 2500000;
                                break;
                            case 2:
                                maxEvaluations_ = 2000000;
                                break;
                            default:
                                maxEvaluations_ = 2000000;
                                break;
                        }
                        break;
                    default:
                        populationSize_ = 91;
                        maxEvaluations_ = 75000;
                }
//                maxEvaluations_ = (int)(maxEvaluations_ * 1.5);
                problemName_ = problem.substring(0, 6);
                if (Integer.parseInt(problem.substring(5, 6)) == 1) {
                    Object[] problemParams = {"Real", numberOfObjectives + 4, numberOfObjectives};
                    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
                } else {
                    Object[] problemParams = {"Real", numberOfObjectives + 9, numberOfObjectives};
                    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
                }
            } else if (problem.startsWith("UF")) {
                if (Integer.parseInt(problem.substring(2)) <= 7) {
                    populationSize_ = 600;
                }
                else {
                    populationSize_ = 1000;
                }
                maxEvaluations_ = 300000;
                Object[] problemParams = {"Real"};
                problemName_ = problem.substring(0, 3);
                problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
            } else if (problem.startsWith("ZDT")) {
                populationSize_ = 100;
                populationSize_ = 25000;
                Object[] problemParams = {"Real"};
                problemName_ = problem.substring(0, 4);
                problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
            } else if (problem.startsWith("MOP")) {
                if (Integer.parseInt(problem.substring(3)) <= 5) {
                    populationSize_ = 100;
                }
                else {
                    populationSize_ = 300;
                }
                maxEvaluations_ = 300000;
                Object[] problemParams = {"Real"};
                problemName_ = problem.substring(0, 4);
                problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
            }
        } catch (JMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // Default experiments.settings

        crossoverProbability_ = 1;
        crossoverDistributionIndex_ = 30.0;
        mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
        mutationDistributionIndex_ = 20;

        T_ = 20;
        delta_ = 0.9;
//        nr1_ = problem_.getNumberOfObjectives();
        nr2_ = T_;
        nr1_ = 1;
        functionType1_ = "_PBI";
        functionType2_ = "_TCHA";
//        functionType2_ = "_IPBI";

        dataDirectory_ = "../weight";
    }
}