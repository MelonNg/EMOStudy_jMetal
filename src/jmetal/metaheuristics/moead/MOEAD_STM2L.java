package jmetal.metaheuristics.moead;

import jmetal.core.*;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Mengyuan Wu on 25-Jun-15.
 */
public class MOEAD_STM2L extends Algorithm {
    private int populationSize_;
    /**
     * Stores the population
     */
    private SolutionSet population_;
    private SolutionSet currentOffspring_;
    private SolutionSet union_;
    /**
     * Stores the values of the individuals
     */
    private Solution[] savedValues_;

    /**
     * Z vector (ideal point)
     */
    double[] z_;

    /**
     * NZ vector (nadir point)
     */
    double[] nz_;

    /**
     * Weight vectors
     */
    double[][] lambda_;

    /**
     * T: neighborhood size
     */
    int T_;
    /**
     * Neighborhood
     */
    int[][] neighborhood_;
    /**
     * delta: probability that parent solutions are selected from neighborhood
     */
    double delta_;
    /**
     * r: reduced preference list size
     */
    int r_;

    String functionType_;
    int evaluations_;
    /**
     * Operators
     */
    Operator crossover_;
    Operator mutation_;

    String dataDirectory_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public MOEAD_STM2L(Problem problem) {
        super(problem);

//		functionType_ = "_TCHE1";
        functionType_ = "_TCHE2";
//		functionType_ = "_PBI";

    } // MOEA/D-DRA-STM

    public SolutionSet execute() throws JMException, ClassNotFoundException {

        int type;
        int maxEvaluations;

        String str1 = "EVA";
        String str2;

        evaluations_    = 0;
        dataDirectory_  = this.getInputParameter("dataDirectory").toString();
        maxEvaluations  = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
        populationSize_ = ((Integer) this.getInputParameter("populationSize")).intValue();

        population_  = new SolutionSet(populationSize_);
        savedValues_ = new Solution[populationSize_];

        T_ = ((Integer) this.getInputParameter("T")).intValue();
        delta_ = ((Double) this.getInputParameter("delta")).doubleValue();
        r_ = ((Integer) this.getInputParameter("r")).intValue();

        z_ 			  = new double[problem_.getNumberOfObjectives()];
        nz_ 		  = new double[problem_.getNumberOfObjectives()];
        lambda_ 	  = new double[populationSize_][problem_.getNumberOfObjectives()];
        neighborhood_ = new int[populationSize_][T_];

        crossover_ = operators_.get("crossover");
        mutation_  = operators_.get("mutation");

        // STEP 1. Initialization
        // STEP 1.1. Compute Euclidean distances between weight vectors and find T
        initUniformWeight();
        initNeighborhood();

        // STEP 1.2. Initialize population
        initPopulation();

        // Save the medium results
        str2 = str1 + Integer.toString(0);
        population_.printObjectivesToFile(str2);

        // STEP 1.3. Initialize the ideal point 'z_' and the nadir point 'nz_'
        initIdealPoint();
        initNadirPoint();

        int iteration = 0;
        // STEP 2. Update
        do {
            int[] permutation = new int[populationSize_];
            Utils.randomPermutation(permutation, populationSize_);
            currentOffspring_   = new SolutionSet(populationSize_);

            for (int i = 0; i < populationSize_; i++) {
                int n = permutation[i];

                double rnd = PseudoRandom.randDouble();

                // STEP 2.1. Mating selection based on probability
                if (rnd < delta_)
                {
                    type = 1; // neighborhood
                } else {
                    type = 2; // whole population
                }
                Solution child;
                Solution[] parents = new Solution[3];
                Vector<Integer> p = new Vector<Integer>();

                parents = matingSelection(p, n, 2, type);

                // Apply DE crossover and polynomial mutation
                child = (Solution) crossover_.execute(new Object[] {population_.get(n), parents});
                mutation_.execute(child);

                // Evaluation
                problem_.evaluate(child);
                evaluations_++;

				/* STEP 2.3. Update the ideal point 'z_' and nadir point 'nz_' */
                updateReference(child);
                updateNadirPoint(child);

                // Add into the offspring population
                currentOffspring_.add(child);
            } // for

            // Combine the parent and the current offspring populations
            union_ = ((SolutionSet) population_).union(currentOffspring_);

            // Selection Procedure
//			if (evaluations_  < (int)(evaluations_ * 0.3))
//				eliteSelection();
//			else
//				selection();
            selection();
//			randomSelection();
//			eliteSelection();

            iteration++;

            // Save the medium results
//			if (evaluations_ % 6000 == 0) {
//				str2 = str1 + Integer.toString(evaluations_);
//				population_.printObjectivesToFile(str2);
//			}
//			System.out.println(evaluations_);

        } while (evaluations_ <= maxEvaluations);

        return population_;

//		int final_size = populationSize_;
//		;
//		try {
//			final_size = (Integer) (getInputParameter("finalSize"));
//			System.out.println("FINAL SOZE: " + final_size);
//		} catch (Exception e) { // if there is an exception indicate it!
//			System.err.println("The final size paramater has been ignored");
//			System.err.println("The number of solutions is "
//					+ population_.size());
//			return population_;
//
//		}
//		return finalSelection(final_size);
    }

    /**
     * Select the next parent population, based on the stable matching criteria
     */
    public void selection() {

        int[] idx;

        int[][]    solPref   = new int[union_.size()][];
        double[][] solMatrix = new double[union_.size()][];
        for (int i = 0; i < union_.size(); i++) {
            solPref[i]   = new int[populationSize_];
            solMatrix[i] = new double[populationSize_];
        }
        int[][]    subpPref   = new int[populationSize_][];
        double[][] subpMatrix = new double[populationSize_][];
        for (int i = 0; i < populationSize_; i++) {
            subpPref[i]   = new int[union_.size()];
            subpMatrix[i] = new double[union_.size()];
        }

        // Calculate the preference values of subproblem matrix and solution matrix
        for (int i = 0; i < union_.size(); i++) {
            for (int j = 0; j < populationSize_; j++) {
                subpMatrix[j][i] = fitnessFunction(union_.get(i), lambda_[j]);
                solMatrix[i][j]  = calculateDistance(union_.get(i), lambda_[j]);
            }
        }

        // Sort the preference value matrix to get the preference rank matrix
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < union_.size(); j++)
                subpPref[i][j] = j;
            Utils.minFastSort(subpMatrix[i], subpPref[i], union_.size(), union_.size());
        }
        for (int i = 0; i < union_.size(); i++) {
            for (int j = 0; j < populationSize_; j++)
                solPref[i][j] = j;
            Utils.minFastSort(solMatrix[i], solPref[i], populationSize_, r_);
        }

        int[] subpStatus = new int[populationSize_];
        int[] solStatus = new int[union_.size()];
        int remainedSubpSize = stableMatchingIncompletePreference(subpPref, solPref, populationSize_, union_.size(), subpStatus, solStatus);
        int remainedSolSize = union_.size() - (populationSize_ - remainedSubpSize);

        // second level
        int[][]    remainedSolPref   = new int[remainedSolSize][];
        double[][] remainedSolMatrix = new double[remainedSolSize][];
        for (int i = 0; i < remainedSolSize; i++) {
            remainedSolPref[i]   = new int[remainedSubpSize];
            remainedSolMatrix[i] = new double[remainedSubpSize];
        }
        int[][]    remainedSubpPref   = new int[remainedSubpSize][];
        double[][] remainedSubpMatrix = new double[remainedSubpSize][];
        for (int i = 0; i < remainedSubpSize; i++) {
            remainedSubpPref[i]   = new int[remainedSolSize];
            remainedSubpMatrix[i] = new double[remainedSolSize];
        }

        int[] remainedSubp = new int[remainedSubpSize];
        for (int i = 0, j = 0; i < populationSize_; i++) {
            if(subpStatus[i] == -1)
                remainedSubp[j++] = i;
        }
        int[] remainedSol = new int[remainedSolSize];
        for (int i = 0, j = 0; i < union_.size(); i++) {
            if(solStatus[i] == -1)
                remainedSol[j++] = i;
        }

        for (int i = 0; i < remainedSolSize; i++) {
            for (int j = 0; j < remainedSubpSize; j++) {
                remainedSubpMatrix[j][i] = subpMatrix[remainedSubp[j]][remainedSol[i]];
                remainedSolMatrix[i][j]  = solMatrix[remainedSol[i]][remainedSubp[j]];
            }
        }

        // Sort the preference value matrix to get the preference rank matrix
        for (int i = 0; i < remainedSubpSize; i++) {
            for (int j = 0; j < remainedSolSize; j++)
                remainedSubpPref[i][j] = j;
            Utils.minFastSort(remainedSubpMatrix[i], remainedSubpPref[i], remainedSolSize, remainedSolSize);
        }
        for (int i = 0; i < remainedSolSize; i++) {
            for (int j = 0; j < remainedSubpSize; j++)
                remainedSolPref[i][j] = j;
            Utils.minFastSort(remainedSolMatrix[i], remainedSolPref[i], remainedSubpSize, remainedSubpSize);
        }

        idx = stableMatching(remainedSubpPref, remainedSolPref, remainedSubpSize, remainedSolSize);

        for (int i = 0, j = 0; i < populationSize_; i++) {
            if(subpStatus[i] == -1)
                subpStatus[i] = remainedSol[idx[j++]];
        }

        for (int i = 0; i < populationSize_; i++)
            population_.replace(i, new Solution(union_.get(subpStatus[i])));
    }

    /**
     * Return the stable matching between 'subproblems' and 'solutions'
     * ('subproblems' propose first). It is worth noting that the number of
     * solutions is larger than that of the subproblems.
     *
     * @param manPref
     * @param womanPref
     * @param menSize
     * @param womenSize
     * @return
     */
    public int[] stableMatching(int[][] manPref, int[][] womanPref, int menSize, int womenSize) {

        // Indicates the mating status
        int[] statusMan   = new int[menSize];
        int[] statusWoman = new int[womenSize];

        final int NOT_ENGAGED = -1;
        for (int i = 0; i < womenSize; i++)
            statusWoman[i] = NOT_ENGAGED;

        // List of men that are not currently engaged.
        LinkedList<Integer> freeMen = new LinkedList<Integer>();
        for (int i = 0; i < menSize; i++)
            freeMen.add(i);

        // next[i] is the next woman to whom i has not yet proposed.
        int[] next = new int[womenSize];

        while (!freeMen.isEmpty()) {
            int m = freeMen.remove();
            int w = manPref[m][next[m]];
            next[m]++;
            if (statusWoman[w] == NOT_ENGAGED) {
                statusMan[m]   = w;
                statusWoman[w] = m;
            } else {
                int m1 = statusWoman[w];
                if (prefers(m, m1, womanPref[w], menSize)) {
                    statusMan[m]   = w;
                    statusWoman[w] = m;
                    freeMen.add(m1);
                } else {
                    freeMen.add(m);
                }
            }
        }

        return statusMan;
    }

    /**
     * Return the stable matching with incomplete preference list between 'subproblems' and 'solutions'
     * ('subproblems' propose first). It is worth noting that the number of
     * solutions is larger than that of the subproblems.
     *
     * @param manPref
     * @param womanPref
     * @param menSize
     * @param womenSize
     * @return
     */
    public int stableMatchingIncompletePreference(int[][] manPref, int[][] womanPref, int menSize, int womenSize, int[] statusMan, int[] statusWoman) {

        final int NOT_ENGAGED = -1;
        for (int i = 0; i < womenSize; i++)
            statusWoman[i] = NOT_ENGAGED;

        for (int i = 0; i < menSize; i++)
            statusMan[i] = NOT_ENGAGED;

        // List of men that are not currently engaged.
        LinkedList<Integer> candidateMen = new LinkedList<Integer>();
        LinkedList<Integer> eliminatedMen = new LinkedList<Integer>();
        for (int i = 0; i < menSize; i++)
            candidateMen.add(i);

        // next[i] is the next woman to whom i has not yet proposed.
        int[] next = new int[womenSize];

        while (!candidateMen.isEmpty()) {
            int m = candidateMen.remove();
            int w = manPref[m][next[m]];
            next[m]++;
            if (containedInList(m, womanPref[w])) {
                if (statusWoman[w] == NOT_ENGAGED) {
                    statusMan[m] = w;
                    statusWoman[w] = m;
                } else {
                    int m1 = statusWoman[w];
                    if (prefers(m, m1, womanPref[w], r_)) {
                        statusMan[m] = w;
                        statusWoman[w] = m;
                        if (next[m1] != r_)
                            candidateMen.add(m1);
                        else
                            eliminatedMen.add(m1);
                    } else {
                        if (next[m] != r_)
                            candidateMen.add(m);
                        else
                            eliminatedMen.add(m);
                    }
                }
            }
            else {
                if (next[m] != r_)
                    candidateMen.add(m);
                else
                    eliminatedMen.add(m);
            }
        }

        return eliminatedMen.size();
    }

    public boolean containedInList(int x, int[] womanPref) {
        for (int i = 0; i < r_; i++) {
            if (womanPref[i] == x)
                return true;
        }
        return false;
    }

    /**
     * Returns true in case that a given woman prefers x to y.
     * @param x
     * @param y
     * @param womanPref
     * @param menSize
     * @return
     */
    public boolean prefers(int x, int y, int[] womanPref, int menSize) {

        for (int i = 0; i < menSize; i++) {
            int pref = womanPref[i];
            if (pref == x)
                return true;
            if (pref == y)
                return false;
        }
        // This should never happen.
        System.out.println("Error in womanPref list!");
        return false;
    }

    /**
     * This selection procedure creates the random matching between subproblems
     * and solutions
     */
    public void randomSelection() {

        int[] idx = new int[union_.size()];

        Utils.randomPermutation(idx, union_.size());

        for (int i = 0; i < populationSize_; i++)
            population_.replace(i, new Solution(union_.get(idx[i])));
    }

    /**
     * This selection procedure tends to match each subproblem with its best solution. In this case,
     */
    public void eliteSelection() {

        int[] idx = new int[populationSize_];

        int[][] subpPref      = new int[populationSize_][];
        double[][] subpMatrix = new double[populationSize_][];
        for (int i = 0; i < populationSize_; i++) {
            subpPref[i]   = new int[union_.size()];
            subpMatrix[i] = new double[union_.size()];
        }

        // Calculate the preference values of subproblem matrix and solution matrix
        for (int i = 0; i < union_.size(); i++) {
            for (int j = 0; j < populationSize_; j++) {
                subpMatrix[j][i] = fitnessFunction(union_.get(i), lambda_[j]);
            }
        }

        // Sort the preference value matrix to get the preference rank matrix
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < union_.size(); j++)
                subpPref[i][j] = j;
            Utils.minFastSort(subpMatrix[i], subpPref[i], union_.size(), union_.size());
        }

        for (int i = 0; i < populationSize_; i++) {
            idx[i] = subpPref[i][0];
        }

        for (int i = 0; i < populationSize_; i++)
            population_.replace(i, new Solution(union_.get(idx[i])));
    }

    /**
     * Calculate the perpendicular distance between the solution and reference
     * line
     *
     * @param individual
     * @param lambda
     * @return
     */
    public double calculateDistance(Solution individual, double[] lambda) {
        double scale;
        double distance;

        double[] vecInd  = new double[problem_.getNumberOfObjectives()];
        double[] vecProj = new double[problem_.getNumberOfObjectives()];

        // vecInd has been normalized to the range [0,1]
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            vecInd[i] = (individual.getObjective(i) - z_[i]) / (nz_[i] - z_[i]);

        scale = innerproduct(vecInd, lambda) / innerproduct(lambda, lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            vecProj[i] = vecInd[i] - scale * lambda[i];

        distance = norm_vector(vecProj);

        return distance;
    }

    public double calculateDistance2(Solution indiv, double[] lambda) {

        // normalize the weight vector (line segment)
        double nd = norm_vector(lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            lambda[i] = lambda[i] / nd;

        double[] realA = new double[problem_.getNumberOfObjectives()];
        double[] realB = new double[problem_.getNumberOfObjectives()];

        // difference between current point and reference point
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            realA[i] = (indiv.getObjective(i) - z_[i]);

        // distance along the line segment
        double d1 = Math.abs(innerproduct(realA, lambda));

        // distance to the line segment
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            realB[i] = (indiv.getObjective(i) - (z_[i] + d1 * lambda[i]));

        double distance = norm_vector(realB);

        return distance;
    }

    /**
     * Calculate the aggregated distance between the solution and reference
     * line, by considering both of the perpendicular distance and projection
     * length
     *
     * @param individual
     * @param lambda
     * @return
     */
    public double calculateDistanceAggregate(Solution individual, double[] lambda) {
        double scale;
        double theta;
        double utility;
        double distance;

        double[] vecInd  = new double[problem_.getNumberOfObjectives()];
        double[] vecProj = new double[problem_.getNumberOfObjectives()];

        double nd = norm_vector(lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            lambda[i] = lambda[i] / nd;

        // vecInd has been normalized to the range [0,1]
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            vecInd[i] = (individual.getObjective(i) - z_[i]) / (nz_[i] - z_[i]);
        //vecInd[i] = individual.getObjective(i) - z_[i];
        //vecInd[i] = individual.getObjective(i);

        scale = innerproduct(vecInd, lambda) / innerproduct(lambda, lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            vecProj[i] = vecInd[i] - scale * lambda[i];

        distance = norm_vector(vecProj);

        theta = 0.5;

//		utility = theta * distance + (1 - theta) * scale;
        utility = distance + theta * scale;

        return utility;
    }

    /**
     * Initialize the weight vectors for subproblems (We only use the data that are already available)
     */
    public void initUniformWeight() {
        if ((problem_.getNumberOfObjectives() == 2) && (populationSize_ < 100)) {
            lambda_[0][0] = 1.0;
            lambda_[0][1] = 0.0;
            lambda_[1][0] = 0.0;
            lambda_[1][1] = 1.0;
            for (int n = 2; n < populationSize_; n++) {
                double a = 1.0 * (n - 1) / (populationSize_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            } // for
        } // if
        else {
            String dataFileName;
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_"
                    + populationSize_ + ".dat";

            try {
                // Open the file
                FileInputStream fis = new FileInputStream(dataDirectory_ + "/"
                        + dataFileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                int i = 0;
                int j = 0;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = (new Double(st.nextToken())).doubleValue();
                        lambda_[i][j] = value;
                        // System.out.println("lambda["+i+","+j+"] = " + value)
                        // ;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                System.out
                        .println("initUniformWeight: failed when reading for file: "
                                + dataDirectory_ + "/" + dataFileName);
                e.printStackTrace();
            }
        }
    } // initUniformWeight


    /**
     * Initialize the neighborhood of subproblems
     */
    public void initNeighborhood() {
        double[] x = new double[populationSize_];
        int[] idx = new int[populationSize_];

        for (int i = 0; i < populationSize_; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < populationSize_; j++) {
                x[j] = Utils.distVector(lambda_[i], lambda_[j]);
                // x[j] = dist_vector(population[i].namda,population[j].namda);
                idx[j] = j;
                // System.out.println("x["+j+"]: "+x[j]+
                // ". idx["+j+"]: "+idx[j]) ;
            } // for

            // find 'niche' nearest neighboring subproblems
            Utils.minFastSort(x, idx, populationSize_, T_);
            // minfastsort(x,idx,population.size(),niche);

            for (int k = 0; k < T_; k++) {
                neighborhood_[i][k] = idx[k];
                // System.out.println("neg["+i+","+k+"]: "+ neighborhood_[i][k])
                // ;
            }
        } // for
    } // initNeighborhood

    /**
     *
     */
    public void initPopulation() throws JMException, ClassNotFoundException {
        Solution newSolution;

        for (int i = 0; i < populationSize_; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            evaluations_++;
            population_.add(newSolution) ;
            savedValues_[i] = new Solution(newSolution);
        } // for
    } // initPopulation

    /**
     * Initialize the ideal point
     * @throws JMException
     * @throws ClassNotFoundException
     */
    void initIdealPoint() throws JMException, ClassNotFoundException {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            z_[i] = 1.0e+30;

        for (int i = 0; i < populationSize_; i++)
            updateReference(population_.get(i));
    } // initIdealPoint

    /**
     * Initialize the nadir point
     * @throws JMException
     * @throws ClassNotFoundException
     */
    void initNadirPoint() throws JMException, ClassNotFoundException {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            nz_[i] = -1.0e+30;

        for (int i = 0; i < populationSize_; i++)
            updateNadirPoint(population_.get(i));
    } // initNadirPoint

    /**
     * Mating selection is used to select the mating parents for offspring generation
     * @param list : the set of the indexes of selected mating parents
     * @param cid  : the id of current subproblem
     * @param size : the number of selected mating parents
     * @param type : 1 - neighborhood; otherwise - whole population
     */
    public Solution[] matingSelection(Vector<Integer> list, int cid, int size, int type) {

        int ss, r, p;

        Solution[] parents = new Solution[3];

        ss = neighborhood_[cid].length;
        while (list.size() < size) {
            if (type == 1) {
                r = PseudoRandom.randInt(0, ss - 1);
                p = neighborhood_[cid][r];
            } else {
                p = PseudoRandom.randInt(0, populationSize_ - 1);
            }
            boolean flag = true;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == p) // p is in the list
                {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                list.addElement(p);
            }
        }

        parents[0] = population_.get(list.get(0));
        parents[1] = population_.get(list.get(1));
        parents[2] = population_.get(cid);

//		switch (Check_Dominance(parents[0], parents[1])) {
//			case 1: {
//				break;
//			}
//			case -1: {
//				Solution temp = new Solution();
//				temp 		  = parents[0];
//				parents[0]    = parents[1];
//				parents[1]    = temp;
//			}
//			case 0: {
//				double rnd = PseudoRandom.randDouble();
//				if (rnd < 0.5) {
//					Solution temp = new Solution();
//					temp 		  = parents[0];
//					parents[0] 	  = parents[1];
//					parents[1] 	  = temp;
//				}
//				break;
//			}
//		}

        return parents;
    } // matingSelection

    /**
     * Check the Pareto dominance relationship between two solutions
     * @param a
     * @param b
     * @return
     */
    public int Check_Dominance(Solution a, Solution b) {
        int[] flag1 = new int[1];
        int[] flag2 = new int[1];

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (a.getObjective(i) < b.getObjective(i)) {
                flag1[0] = 1;
            } else {
                if (a.getObjective(i) > b.getObjective(i)) {
                    flag2[0] = 1;
                }
            }
        }
        if (flag1[0] == 1 && flag2[0] == 0) {
            return 1;
        } else {
            if (flag1[0] == 0 && flag2[0] == 1) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Update the ideal point, it is just an approximation with the best value for each objective
     *
     * @param individual
     */
    void updateReference(Solution individual) {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (individual.getObjective(i) < z_[i])
                z_[i] = individual.getObjective(i);
        }
    } // updateReference

    /**
     * Update the nadir point, it is just an approximation with worst value for each objective
     *
     * @param individual
     */
    void updateNadirPoint(Solution individual) {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (individual.getObjective(i) > nz_[i])
                nz_[i] = individual.getObjective(i);
        }
    } // updateNadirPoint

    /**
     * Calculate the dot product of two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++)
            sum += vec1[i] * vec2[i];

        return sum;
    }

    /**
     * Calculate the norm of the vector
     * @param z
     * @return
     */
    public double norm_vector(double[] z) {
        double sum = 0;

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            sum += z[i] * z[i];

        return Math.sqrt(sum);
    }

    /**
     * Calculate the fitness value of a given individual, based on the specific scalarizing function
     * @param individual
     * @param lambda
     * @return
     */
    double fitnessFunction(Solution individual, double[] lambda) {
        double fitness;
        fitness = 0.0;

        if (functionType_.equals("_TCHE1")) {
            double maxFun = -1.0e+30;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = Math.abs(individual.getObjective(i) - z_[i]);

                double feval;
                if (lambda[i] == 0) {
                    feval = 0.000001 * diff;
                } else {
                    feval = diff * lambda[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for
            fitness = maxFun;
        } else if (functionType_.equals("_TCHE2")) {
            double maxFun = -1.0e+30;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = Math.abs(individual.getObjective(i) - z_[i]);

                double feval;
                if (lambda[i] == 0) {
                    feval = diff / 0.000001;
                } else {
                    feval = diff / lambda[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for
            fitness = maxFun;
        } else if (functionType_.equals("_PBI")) {
            double theta; // penalty parameter
            theta = 5.0;

            // normalize the weight vector (line segment)
            double nd = norm_vector(lambda);
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                lambda[i] = lambda[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realA[n] = (individual.getObjective(n) - z_[n]);

            // distance along the line segment
            double d1 = Math.abs(innerproduct(realA, lambda));

            // distance to the line segment
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realB[n] = (individual.getObjective(n) - (z_[n] + d1
                        * lambda[n]));
            double d2 = norm_vector(realB);

            fitness = d1 + theta * d2;
        } else {
            System.out.println("MOEAD.fitnessFunction: unknown type "
                    + functionType_);
            System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation


    /** @author Juanjo
     * This method selects N solutions from a set M, where N <= M
     * using the same method proposed by Qingfu Zhang, W. Liu, and Hui Li in
     * the paper describing MOEA/D-DRA (CEC 09 COMPTETITION)
     * An example is giving in that paper for two objectives.
     * If N = 100, then the best solutions  attenting to the weights (0,1),
     * (1/99,98/99), ...,(98/99,1/99), (1,0) are selected.
     *
     * Using this method result in 101 solutions instead of 100. We will just
     * compute 100 even distributed weights and used them. The result is the same
     *
     * In case of more than two objectives the procedure is:
     * 1- Select a solution at random
     * 2- Select the solution from the population which have maximum distance to
     * it (whithout considering the already included)
     *
     *
     *
     * @param n: The number of solutions to return
     * @return A solution set containing those elements
     *
     */
    SolutionSet finalSelection(int n) throws JMException {
        SolutionSet res = new SolutionSet(n);
        if (problem_.getNumberOfObjectives() == 2) { // subcase 1
            double[][] intern_lambda = new double[n][2];
            for (int i = 0; i < n; i++) {
                double a = 1.0 * i / (n - 1);
                intern_lambda[i][0] = a;
                intern_lambda[i][1] = 1 - a;
            } // for

            // we have now the weights, now select the best solution for each of
            // them
            for (int i = 0; i < n; i++) {
                Solution current_best = population_.get(0);
                int index = 0;
                double value = fitnessFunction(current_best, intern_lambda[i]);
                for (int j = 1; j < n; j++) {
                    double aux = fitnessFunction(population_.get(j),
                            intern_lambda[i]); // we are looking the best for
                    // the weight i
                    if (aux < value) { // solution in position j is better!
                        value = aux;
                        current_best = population_.get(j);
                    }
                }
                res.add(new Solution(current_best));
            }

        } else { // general case (more than two objectives)

            Distance distance_utility = new Distance();
            int random_index = PseudoRandom.randInt(0, population_.size() - 1);

            // create a list containing all the solutions but the selected one
            // (only references to them)
            List<Solution> candidate = new LinkedList<Solution>();
            candidate.add(population_.get(random_index));

            for (int i = 0; i < population_.size(); i++) {
                if (i != random_index)
                    candidate.add(population_.get(i));
            } // for

            while (res.size() < n) {
                int index = 0;
                Solution selected = candidate.get(0); // it should be a next! (n
                // <= population size!)
                double distance_value = distance_utility
                        .distanceToSolutionSetInObjectiveSpace(selected, res);
                int i = 1;
                while (i < candidate.size()) {
                    Solution next_candidate = candidate.get(i);
                    double aux = distance_value = distance_utility
                            .distanceToSolutionSetInObjectiveSpace(
                                    next_candidate, res);
                    if (aux > distance_value) {
                        distance_value = aux;
                        index = i;
                    }
                    i++;
                }

                // add the selected to res and remove from candidate list
                res.add(new Solution(candidate.remove(index)));
            } //
        }
        return res;
    }
}
