//  GWASFGA_Settings.java 
//
//  Authors:
//       Rubén Saborido Infantes <rsain@uma.es>
//
//  Copyright (c) 2013 Rubén Saborido Infantes
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.experiments.settings;

import java.util.HashMap;

import jmetal.metaheuristics.gwasfga.*;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.core.*;
import jmetal.experiments.Settings;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.JMException;

/**
 * Settings class of algorithm GWASFGA (real encoding)
 */
public class GWASFGA_Settings extends Settings {
    public int populationSize_;
    public int maxEvaluations_;
    public double mutationProbability_;
    public double crossoverProbability_;
    public double mutationDistributionIndex_;
    public double crossoverDistributionIndex_;
    public double utopianValueInPercent_;
    QualityIndicator convergenceIndicator_;
    public boolean normalization_;
    public String weightsDirectory_;
    public AchievementScalarizingFunction asfInUtopian_;
    public AchievementScalarizingFunction asfInNadir_;
    public boolean estimatePoints_;

    /**
     * Constructor
     *
     * @throws JMException
     */
    public GWASFGA_Settings(String problem) throws JMException {
        super(problem);

        defaultSetting(problem);

    } // GWASFGA_Settings


    /**
     * Configure GWASFGA with user-defined parameter settings
     *
     * @return A GWASFGA algorithm object
     * @throws jmetal.util.JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Selection selection;
        Crossover crossover;
        Mutation mutation;

        HashMap parameters; // Operator parameters

        QualityIndicator indicators;

        // Creating the algorithm.
        algorithm = new GWASFGA(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("weightsDirectory", weightsDirectory_);
        algorithm.setInputParameter("indicators", convergenceIndicator_);
        algorithm.setInputParameter("normalization", normalization_);
        algorithm.setInputParameter("asfInUtopian", asfInUtopian_);
        algorithm.setInputParameter("asfInNadir", asfInNadir_);
        algorithm.setInputParameter("estimatePoints", estimatePoints_);
        algorithm.setInputParameter("utopianValueInPercent", utopianValueInPercent_);

        // Mutation and Crossover for Real codification
        parameters = new HashMap();
        parameters.put("probability", crossoverProbability_);
        parameters.put("distributionIndex", crossoverDistributionIndex_);
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

        parameters = new HashMap();
        parameters.put("probability", mutationProbability_);
        parameters.put("distributionIndex", mutationDistributionIndex_);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

        // Selection Operator
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        return algorithm;
    } // configure

    private void defaultSetting(String problem) {
        try {
            if (problem.startsWith("WFG")) {
                int numberOfObjectives = Integer.parseInt(problem.substring(problem.indexOf('_',0) + 1));
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
                problemName_ = problem.substring(0, problem.indexOf('_',0));
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


        // Default settings
        mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
        crossoverProbability_ = 0.9;
        mutationDistributionIndex_ = 20.0;
        crossoverDistributionIndex_ = 20.0;
        weightsDirectory_ = "../weight/";
        normalization_ = true;
        estimatePoints_ = true;
        utopianValueInPercent_ = 1.0;

//        String paretoFrontDirectory = new String("/Users/ajnebro/Softw/jMetal/jMetalWeb/jMetalWeb/paretoFronts/");
//        if (problem_.getName().substring(0, problem_.getName().length() - 1).equals("ZDT") || problem_.getName().substring(0, problem_.getName().length() - 1).equals("LZ09_F"))
//            convergenceIndicator_ = new QualityIndicator(problem_, paretoFrontDirectory + problem_.getName() + ".pf");
//        else
//            convergenceIndicator_ = new QualityIndicator(problem_, paretoFrontDirectory + problem_.getName() + "." + problem_.getNumberOfObjectives() + "D.pf");

    }
} // GWASFGA_Settings