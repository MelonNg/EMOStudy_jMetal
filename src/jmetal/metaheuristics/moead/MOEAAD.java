package jmetal.metaheuristics.moead;

import jmetal.core.*;
import jmetal.util.JMException;
import jmetal.util.NonDominatedSolutionList;
import jmetal.util.PseudoRandom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by mengyuawu3 on 14-Jun-16.
 * v2+v3
 */
public class MOEAAD extends Algorithm {

    private int populationSize_;
    /**
     * Stores the population
     */
    private SolutionSet population1_;
    private SolutionSet population2_;
    private NonDominatedSolutionList nondominatedSet_;
    /**
     * Stores the old objective vectors
     */
    private double[][] oldObjs1_;
    private double[][] oldObjs2_;
    /**
     * population index
     */
    private int[] subpPopIndex_;
    /**
     * related subproblem
     */
    private int[] relatedSubp21_;
    private int[] relatedSubp12_;
    /**
     * the closeness of the subp to tje solution
     */
    private int[] subpCloseness_;
    /**
     * Z vector (ideal point)
     */
    private double[] z_;
    /**
     * Z vector (nadir point)
     */
    private double[] nz_;
    /**
     * weight vectors
     */
    //Vector<Vector<Double>> weights_ ;
    private double[][] weights_;
    /**
     * subp pair relation
     */
    private int[] subpPair12_;
    /**
     * T: neighbour size
     */
    private int T_;
    /**
     * Neighborhood
     */
    private int[][] neighborhood_;
    /**
     * delta: probability that parent solutions are selected from neighbourhood
     */
    private double delta_;
    /**
     * nr: maximal number of solutions replaced by each child solution
     */
    private int nr1_;
    private int nr2_;
    /**
     * indArray: points that form the ideal and nadir point
     */
    private Solution[] idealArray_;
    private Solution[] nadirArray_;
    /**
     * functionType: decomposition method
     */
    private String functionType1_;
    private String functionType2_;
    /**
     * evaluations: number of evaluations
     */
    private int evaluations_;
    private int maxEvaluations_;
    /**
     * Operators
     */
    private Operator crossover_;
    private Operator mutation_;
    /**
     * dataDirectory: store the weight vectors
     */
    private String dataDirectory_;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public MOEAAD(Problem problem) {
        super(problem);
    }

    public SolutionSet execute() throws JMException, ClassNotFoundException {

        crossover_ = operators_.get("crossover"); // default: DE crossover
        mutation_ = operators_.get("mutation");  // default: polynomial mutation
        
        functionType1_ = (String) this.getInputParameter("functionType1");
        functionType2_ = (String) this.getInputParameter("functionType2");
        T_ = (Integer) this.getInputParameter("T");
        nr1_ = (Integer) this.getInputParameter("nr1");
        nr2_ = (Integer) this.getInputParameter("nr2");
        delta_ = (Double) this.getInputParameter("delta");
        maxEvaluations_ = (Integer) this.getInputParameter("maxEvaluations");
        populationSize_ = (Integer) this.getInputParameter("populationSize");
        dataDirectory_ = (String) this.getInputParameter("dataDirectory");

        population1_ = new SolutionSet(populationSize_);
        population2_ = new SolutionSet(populationSize_);

        weights_ = new double[populationSize_][problem_.getNumberOfObjectives()];
        subpPair12_ = new int[populationSize_];
        neighborhood_ = new int[populationSize_][T_];
        idealArray_ = new Solution[problem_.getNumberOfObjectives()];
        nadirArray_ = new Solution[problem_.getNumberOfObjectives()];
        z_ = new double[problem_.getNumberOfObjectives()];
        nz_ = new double[problem_.getNumberOfObjectives()];
        subpPopIndex_ = new int[populationSize_];
        relatedSubp21_ = new int[populationSize_];
        relatedSubp12_ = new int[populationSize_];
        subpCloseness_ = new int[populationSize_];
        oldObjs1_ = new double[populationSize_][problem_.getNumberOfObjectives()];
        oldObjs2_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        evaluations_ = 0;

        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        initUniformWeight();

        initNeighborhood();

        // STEP 1.2. Initialize population
        initPopulation();

        // STEP 1.3. Initialize z_ nz_
        initIdealNadirPoint();

        // STEP 1.4. Initialize the second pop and their relationship
        for (int i = 0; i < populationSize_; i++) {
            subpPair12_[i] = i;
            population2_.add(population1_.get(i));
            population1_.get(i).setClosestSubp(i);
            subpPopIndex_[i] = 1;
            relatedSubp21_[i] = -1;
            relatedSubp12_[i] = -1;
            subpCloseness_[i] = 0;
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                oldObjs1_[i][j] = population1_.get(i).getObjective(j);
                oldObjs2_[i][j] = population2_.get(i).getObjective(j);
            }
        }

        // STEP 2. Update
        do {
//            int[] permutation = new int[populationSize_];
//            Utils.randomPermutation(permutation, populationSize_);

            for (int i = 0; i < populationSize_; i++) {
//                int n = permutation[i]; // or int n = i;
                int n = i ;

                subpSelect(n);

                if (subpPopIndex_[n] != 0) {
                    // STEP 2.1. Mating selection based on probability
                    ArrayList<Integer> matingIndexes2 = new ArrayList<>();

                    // Apply produce child solution
                    Solution child = produceOffspring(n, matingIndexes2);
                    evaluations_++;

                    // STEP 2.3. Repair. Not necessary

                    // STEP 2.4. Update z_ nz_
                    updateIdealPoint(child);
                    nondominatedSet_.add(child);
                    updateNadirPoint();

                    // STEP 2.5. Update of solutions
                    updateProblem(child, matingIndexes2);
                }

            } // for


//            if (PseudoRandom.randDouble() >= (double)evaluations_ / maxEvaluations_) {
//            if (Math.floorMod(evaluations_, 10 * populationSize_) == 0) {
                popMatch();
//                Utils.randomPermutation(subpPair12_, populationSize_);
//            }

        } while (evaluations_ < maxEvaluations_);

        return population1_.union(population2_);
    }


    public void initUniformWeight() {
        if ((problem_.getNumberOfObjectives() == 2) && (populationSize_ <= 99)) {
            for (int n = 0; n < populationSize_; n++) {
                double a = 1.0 * n / (populationSize_ - 1);
                weights_[n][0] = a;
                weights_[n][1] = 1 - a;
            } // for
        } // if
        else {
            String dataFileName;
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_" + populationSize_ + ".dat";

            try {
                // Open the file
                FileInputStream fis = new FileInputStream(dataDirectory_ + "/" + dataFileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

//                int numberOfObjectives = 0;
                int i = 0;
                int j = 0;
                String aux = br.readLine();
                StringTokenizer st;
                while (aux != null) {
                    st = new StringTokenizer(aux);
                    j = 0;
//                    numberOfObjectives = st.countTokens();
                    while (st.hasMoreTokens()) {
                        double value = new Double(st.nextToken());
                        weights_[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                System.out.println("initUniformWeight: failed when reading for file: " + dataDirectory_ + "/" + dataFileName);
                e.printStackTrace();
            }
        } // else

    } // initUniformWeight

    public void initNeighborhood() {
        double[] x = new double[populationSize_];
        int[] idx = new int[populationSize_];

        for (int i = 0; i < populationSize_; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < populationSize_; j++) {
                x[j] = Utils.distVector(weights_[i], weights_[j]);
                //x[j] = dist_vector(population[i].namda,population[j].namda);
                idx[j] = j;
                //System.out.println("x["+j+"]: "+x[j]+ ". idx["+j+"]: "+idx[j]) ;
            } // for

            // find 'niche' nearest neighboring subproblems
            Utils.minFastSort(x, idx, populationSize_, T_);
            //minfastsort(x,idx,population.size(),niche);

            System.arraycopy(idx, 0, neighborhood_[i], 0, T_);
        } // for
    } // initNeighborhood

    public void initPopulation() throws JMException, ClassNotFoundException {
        nondominatedSet_ = new NonDominatedSolutionList();
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);

            problem_.evaluate(newSolution);
            evaluations_++;
            population1_.add(newSolution);

            nondominatedSet_.add(newSolution);
        } // for
    } // initPopulation

    void initIdealNadirPoint() throws JMException, ClassNotFoundException {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            z_[i] = 1.0e+30;
            nz_[i] = -1.0e+30;
        } // for

        for (int i = 0; i < populationSize_; i++) {
            updateIdealPoint(population1_.get(i));
//            updateNadirPoint();
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                if (population1_.get(i).getObjective(n) > nz_[n]) {
                    nz_[n] = population1_.get(i).getObjective(n);

                    nadirArray_[n] = population1_.get(i);
                }
            }
        } // for


    } // initIdealNadirPoint

    public Solution produceOffspring(int cid, ArrayList<Integer> matingIndexes2) throws JMException {

        Solution childSolution;
        ArrayList<Integer> matingIndexesUnion;
        int baseIndex;


        double rnd = PseudoRandom.randDouble();
        if (rnd < delta_)
        {
            baseIndex = 0;
            if (subpPopIndex_[cid] == 1) {
                matingIndexes2.ensureCapacity(T_);
                matingIndexesUnion = new ArrayList<>(T_ * 2);
                for (int i = 0; i < T_; i++) {
                    matingIndexesUnion.add(neighborhood_[cid][i]);
                    if (relatedSubp12_[neighborhood_[cid][i]] != -1) {
                        matingIndexesUnion.add(populationSize_ + relatedSubp12_[neighborhood_[cid][i]]);
                        matingIndexes2.add(relatedSubp12_[neighborhood_[cid][i]]);
                    }
                }
            } else {
                matingIndexes2.ensureCapacity(T_);
                matingIndexesUnion = new ArrayList<>(T_);
                matingIndexesUnion.add(populationSize_ + subpPair12_[cid]);
                for (int i = 1; i < T_; i++) {
                    if (population2_.get(subpPair12_[cid]).getClosestSubp() != population2_.get(neighborhood_[subpPair12_[cid]][i]).getClosestSubp()) {
                        matingIndexesUnion.add(populationSize_ + neighborhood_[subpPair12_[cid]][i]);
                    }
                    matingIndexes2.add(neighborhood_[subpPair12_[cid]][i]);
                }
                if (matingIndexesUnion.size() < 2) {
                    matingIndexesUnion.clear();
                    matingIndexesUnion.ensureCapacity(T_);
                    for (int i = 0; i < T_; i++) {
                        matingIndexesUnion.add(populationSize_ + neighborhood_[subpPair12_[cid]][i]);
                    }
                }
            }


//            if (subpPopIndex_[cid] == 1) {
//                matingIndexes2.ensureCapacity(populationSize_);
//                matingIndexesUnion = new int[T_];
////                    matingIndexesUnion = new int[T_ + populationSize_];
//                for (int i = 0; i < T_; i++) {
//                    matingIndexesUnion[i] = neighborhood_[cid][i];
//                }
//                for (int i = 0; i < populationSize_; i++) {
////                        matingIndexesUnion[T_ + i] = populationSize_ + i;
//                    matingIndexes2.add(i);
//                }
//            } else {
//                if (relatedSubp21_[subpPair12_[cid]] == -1) {
//                    matingIndexes2.ensureCapacity(T_);
//                    matingIndexesUnion = new int[T_];
//                    for (int i = 0; i < T_; i++) {
//                        matingIndexesUnion[i] = populationSize_ + neighborhood_[subpPair12_[cid]][i];
//                        matingIndexes2.add(neighborhood_[subpPair12_[cid]][i]);
//                    }
//                } else {
//                    matingIndexes2.ensureCapacity(T_);
//                    matingIndexesUnion = new int[T_ * 2];
//                    for (int i = 0; i < T_; i++) {
//                        matingIndexesUnion[i] = neighborhood_[subpPair12_[relatedSubp21_[subpPair12_[cid]]]][i];
//                        matingIndexesUnion[T_ + i] = populationSize_ + neighborhood_[subpPair12_[cid]][i];
//                        matingIndexes2.add(neighborhood_[subpPair12_[cid]][i]);
//                    }
//                }
//            }

        } else {
            if (subpPopIndex_[cid] == 1) {
                baseIndex = cid;
            } else {
//                baseIndex = populationSize_;
                baseIndex = populationSize_ + subpPair12_[cid];
            }
            matingIndexes2.ensureCapacity(populationSize_);
            matingIndexesUnion = new ArrayList<>(populationSize_ * 2);
            for (int i = 0; i < populationSize_; i++) {
                matingIndexesUnion.add(i);
            }
//            matingIndexesUnion.add(populationSize_ + subpPair12_[cid]);
            for (int i = 0; i < populationSize_; i++) {
//                if (population2_.get(subpPair12_[cid]).getClosestSubp() != population2_.get(i).getClosestSubp()) {
                matingIndexesUnion.add(populationSize_ + i);
//                }
                matingIndexes2.add(i);
            }
        }


        // don't use baseSolution
        int numParents = 2;
        int rndIndex;
        Solution parents[] = new Solution[numParents];
        int parentsIndexes[] = new int[numParents];
//        parentsIndexes[0] = PseudoRandom.randInt(0, matingIndexesUnion.length - 1);
        parentsIndexes[0] = baseIndex;
        int numCurrentParents = 1;
        while (numCurrentParents < parentsIndexes.length) {
            rndIndex = PseudoRandom.randInt(0, matingIndexesUnion.size() - 1);
            boolean flag = true;
            for (int i = 0; i < numCurrentParents; i++) {
                if (rndIndex == parentsIndexes[i]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                parentsIndexes[numCurrentParents] = rndIndex;
                numCurrentParents++;
            }
        }
        for (int i = 0; i < 2; i++) {
            if (matingIndexesUnion.get(parentsIndexes[i]) < populationSize_) {
                parents[i] = population1_.get(matingIndexesUnion.get(parentsIndexes[i]));
            } else {
                parents[i] = population2_.get(matingIndexesUnion.get(parentsIndexes[i]) - populationSize_);
            }
        }

        childSolution = ((Solution[]) crossover_.execute(parents))[0];

        mutation_.execute(childSolution);
        problem_.evaluate(childSolution);

        return childSolution;
    }

    private void updateProblem(Solution individual, ArrayList<Integer> matingIndexes2) {
        // indiv: child solution
        // id:   the id of current subproblem
        // type: update solutions in - neighborhood (1) or whole population (otherwise)
        int time;

        double indNorm[] = new double[individual.getNumberOfObjectives()];
        double oldIndNorm[] = new double[individual.getNumberOfObjectives()];

        double distances[] = new double[populationSize_];
        int closestSubpsSequence[] = new int[populationSize_];
        for (int i = 0; i < individual.getNumberOfObjectives(); i++) {
            indNorm[i] = (individual.getObjective(i) - z_[i]) / (nz_[i] - z_[i]);
        }
        for (int i = 0; i < populationSize_; i++) {
            distances[i] = calculateDistance(indNorm, weights_[i]);
        }
        // find the m subproblems' indexes with min distances
        for (int i = 0; i < populationSize_; i++)
            closestSubpsSequence[i] = i;
        Utils.minFastSort(distances, closestSubpsSequence, populationSize_, 1);

        //update poplation1
        int updateIndex;
        double newobj, oldobj;
        double referencePoint[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint[i] = 0;
        }
        time = 0;
        for (int i = 0; i < 1; i++) {
            updateIndex = closestSubpsSequence[i];
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                oldIndNorm[j] = (population1_.get(updateIndex).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
            }

            newobj = fitnessFunction(indNorm, weights_[updateIndex], referencePoint, functionType1_);
            oldobj = fitnessFunction(oldIndNorm, weights_[updateIndex], referencePoint, functionType1_);

            if (newobj <= oldobj) {
                population1_.replace(updateIndex, individual);
                time++;
            }
            if (time >= nr1_)
                break;
        }

//        // old update2
//        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
//            referencePoint[i] = 1;
//        }
//        int[] permutation = new int[matingIndexes2.size()];
//        Utils.randomPermutation(permutation, matingIndexes2.size());
//        for (int i = 0; i < matingIndexes2.size(); i++) {
//            updateIndex = matingIndexes2.get(permutation[i]);
//            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
//                oldIndNorm[j] = (population2_.get(updateIndex).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
//            }
//
//            newobj = fitnessFunction(indNorm, weights_[updateIndex], referencePoint, functionType2_);
//            oldobj = fitnessFunction(oldIndNorm, weights_[updateIndex], referencePoint, functionType2_);
//
//            if (newobj <= oldobj) {
//                population2_.replace(updateIndex, individual);
//                time++;
//            }
//            if (time >= nr2_)
//                break;
//        }

        //new update2
        double indNormNadirRef[] = new double[individual.getNumberOfObjectives()];
        for (int i = 0; i < individual.getNumberOfObjectives(); i++) {
            indNormNadirRef[i] = indNorm[i] - 1;
        }
        for (int i = 0; i < populationSize_; i++) {
            distances[i] = calculateDistance(indNormNadirRef, weights_[i]);
        }
        // find the m subproblems' indexes with min distances
        for (int i = 0; i < populationSize_; i++)
            closestSubpsSequence[i] = i;
        Utils.minFastSort(distances, closestSubpsSequence, populationSize_, populationSize_);
        individual.setClosestSubp(closestSubpsSequence[0]);

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint[i] = 1.0;
        }
        time = 0;
        for (int i = 0; i < populationSize_; i++) {
            updateIndex = closestSubpsSequence[i];
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                oldIndNorm[j] = (population2_.get(updateIndex).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
            }

            newobj = fitnessFunction(indNorm, weights_[updateIndex], referencePoint, functionType2_);
            oldobj = fitnessFunction(oldIndNorm, weights_[updateIndex], referencePoint, functionType2_);

            if (newobj <= oldobj) {
                population2_.replace(updateIndex, individual);
                subpCloseness_[updateIndex] = i;
                time++;
            }
            if (time >= nr2_)
                break;
        }
    } // updateProblem

    private void subpsSelect() {
        double oldObjs1Norm[] = new double[problem_.getNumberOfObjectives()];
        double oldObjs2Norm[] = new double[problem_.getNumberOfObjectives()];
        double newObjs1Norm[] = new double[problem_.getNumberOfObjectives()];
        double newObjs2Norm[] = new double[problem_.getNumberOfObjectives()];
        double oldObj1, oldObj2, newObj1, newObj2;
        double percentage1, percentage2;

        double referencePoint1[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint1[i] = 0;
        }

        double referencePoint2[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint2[i] = 1;
        }

        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                oldObjs1Norm[j] = (oldObjs1_[i][j] - z_[j]) / (nz_[j] - z_[j]);
                newObjs1Norm[j] = (population1_.get(i).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
                oldObjs2Norm[j] = (oldObjs2_[subpPair12_[i]][j] - z_[j]) / (nz_[j] - z_[j]);
                newObjs2Norm[j] = (population2_.get(subpPair12_[i]).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
            }
            oldObj1 = fitnessFunction(oldObjs1Norm, weights_[i], referencePoint1, functionType1_);
            newObj1 = fitnessFunction(newObjs1Norm, weights_[i], referencePoint1, functionType1_);
            oldObj2 = fitnessFunction(oldObjs2Norm, weights_[subpPair12_[i]], referencePoint2, functionType2_);
            newObj2 = fitnessFunction(newObjs2Norm, weights_[subpPair12_[i]], referencePoint2, functionType2_);
            percentage1 = (oldObj1 - newObj1) / oldObj1;
            percentage2 = (oldObj2 - newObj2) / Math.abs(oldObj2);
            if (percentage1 > percentage2) {
                subpPopIndex_[i] = 1;
            } else if (percentage1 < percentage2) {
                subpPopIndex_[i] = 2;
            } else {
                subpPopIndex_[i] = PseudoRandom.randInt(1, 2);
            }
        }

        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                oldObjs1_[i][j] = population1_.get(i).getObjective(j);
                oldObjs2_[i][j] = population2_.get(i).getObjective(j);
            }
        }
    }

    private void subpSelect(int cid) {
        double oldObjs1Norm[] = new double[problem_.getNumberOfObjectives()];
        double oldObjs2Norm[] = new double[problem_.getNumberOfObjectives()];
        double newObjs1Norm[] = new double[problem_.getNumberOfObjectives()];
        double newObjs2Norm[] = new double[problem_.getNumberOfObjectives()];
        double oldObj1, oldObj2, newObj1, newObj2;
        double percentage1, percentage2;

        double referencePoint1[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint1[i] = 0;
        }

        double referencePoint2[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint2[i] = 1.0;
        }

        for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
            oldObjs1Norm[j] = (oldObjs1_[cid][j] - z_[j]) / (nz_[j] - z_[j]);
            newObjs1Norm[j] = (population1_.get(cid).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
            oldObjs2Norm[j] = (oldObjs2_[subpPair12_[cid]][j] - z_[j]) / (nz_[j] - z_[j]);
            newObjs2Norm[j] = (population2_.get(subpPair12_[cid]).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
        }
        oldObj1 = fitnessFunction(oldObjs1Norm, weights_[cid], referencePoint1, functionType1_);
        newObj1 = fitnessFunction(newObjs1Norm, weights_[cid], referencePoint1, functionType1_);
        oldObj2 = fitnessFunction(oldObjs2Norm, weights_[subpPair12_[cid]], referencePoint2, functionType2_);
        newObj2 = fitnessFunction(newObjs2Norm, weights_[subpPair12_[cid]], referencePoint2, functionType2_);
        percentage1 = (oldObj1 - newObj1) / oldObj1;
        percentage2 = (oldObj2 - newObj2) / Math.abs(oldObj2);
        if (percentage1 > percentage2) {
            subpPopIndex_[cid] = 1;
        } else if (percentage1 < percentage2) {
            subpPopIndex_[cid] = 2;
        } else {
            boolean valid1 = false, valid2_1 = false, valid2_2 = false;
            valid1 = population1_.get(cid).isNondominated();
//            valid2_1 = population2_.get(subpPair12_[cid]).isNondominated();
//
//            double distances[] = new double[populationSize_];
//            int closestSubpsSequence[] = new int[populationSize_];
//            for (int i = 0; i < populationSize_; i++) {
//                distances[i] = calculateDistance(newObjs2Norm, weights_[i]);
//            }
//            for (int i = 0; i < populationSize_; i++)
//                closestSubpsSequence[i] = i;
//            Utils.minFastSort(distances, closestSubpsSequence, populationSize_, problem_.getNumberOfObjectives());
//            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
//                if (closestSubpsSequence[i] == subpPair12_[cid]) {
//                    valid2_2 = true;
//                    break;
//                }
//            }
            if (subpCloseness_[subpPair12_[cid]] < problem_.getNumberOfObjectives()) {
                valid2_2 = true;
            }

            if (valid1 && !valid2_2) {
                subpPopIndex_[cid] = 1;
            } else if (!valid1 && valid2_2) {
                subpPopIndex_[cid] = 2;
//            } else if (!valid1 && !valid2_2) {
//                subpPopIndex_[cid] = 0;
            } else {
                subpPopIndex_[cid] = PseudoRandom.randInt(1, 2);
            }
        }

        for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
            oldObjs1_[cid][j] = population1_.get(cid).getObjective(j);
            oldObjs2_[subpPair12_[cid]][j] = population2_.get(subpPair12_[cid]).getObjective(j);
        }
    }

    private void updateIdealPoint(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < z_[n]) {
                z_[n] = individual.getObjective(n);

                idealArray_[n] = individual;
            }
        }
    }

    private void updateNadirPoint() {

        for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
            nz_[j] = -1.0e+30;
        }
        nondominatedSet_ = new NonDominatedSolutionList();
        for (int i = 0; i < populationSize_; i++) {
            if (population1_.get(i).isNondominated()) {
                nondominatedSet_.addWithoutCheck(population1_.get(i));
            }
            if (population2_.get(i).isNondominated()) {
                nondominatedSet_.addWithoutCheck(population2_.get(i));
            }
        }
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (nadirArray_[i].isNondominated()) {
                nondominatedSet_.add(nadirArray_[i]);
            }

            if (idealArray_[i].isNondominated()) {
                nondominatedSet_.add(idealArray_[i]);
            }
        }

        for (int i = 0; i < nondominatedSet_.size(); i++) {
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                if (nondominatedSet_.get(i).getObjective(j) > nz_[j]) {
                    nz_[j] = nondominatedSet_.get(i).getObjective(j);

                    nadirArray_[j] = nondominatedSet_.get(i);
                }
            }
        }

        boolean fail = false;
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if ((nz_[i] - z_[i]) < 0.001) {
                fail = true;
            }
        }
        if (fail) {
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                nz_[i] = -1.0e+30;
            }
            for (int i = 0; i < populationSize_; i++) {
                for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                    if (population1_.get(i).getObjective(j) > nz_[j]) {
                        nz_[j] = population1_.get(i).getObjective(j);
                    }
                }
            }
        }


//        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
//            nz_[i] = z_[i] + 1;
//        }
    }

    private void popMatch() {

        int[][] solPref = new int[populationSize_][populationSize_];
        double[][] solMatrix = new double[populationSize_][populationSize_];

        int[][]  subpPref   = new int[populationSize_][populationSize_];
        double[][] subpMatrix = new double[populationSize_][populationSize_];

        double referencePoint[] = new double[problem_.getNumberOfObjectives()];
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            referencePoint[i] = 0;
        }

        double pop2Norm[][] = new double[populationSize_][problem_.getNumberOfObjectives()];
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                pop2Norm[i][j] = (population2_.get(i).getObjective(j) - z_[j]) / (nz_[j] - z_[j]);
            }
        }

        // Calculate the preference values of subproblem matrix and solution matrix
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < populationSize_; j++) {
                subpMatrix[j][i] = fitnessFunction(pop2Norm[i], weights_[j], referencePoint, functionType1_);
                solMatrix[i][j]  = calculateDistance(pop2Norm[i], weights_[j]);
            }
        }

        // Sort the preference value matrix to get the preference rank matrix
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < populationSize_; j++)
                subpPref[i][j] = j;
            Utils.minFastSort(subpMatrix[i], subpPref[i], populationSize_, populationSize_);
        }
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < populationSize_; j++)
                solPref[i][j] = j;
            Utils.minFastSort(solMatrix[i], solPref[i], populationSize_, populationSize_);
        }

        StableMarriage smp = new StableMarriage(populationSize_, populationSize_, subpPref, solPref);

//        int[] subpPartners = new int[populationSize_];
        int[] solPartners = new int[populationSize_];
        int[] solPrefListLengths = new int[populationSize_];
        for (int i = 0; i < populationSize_; i++) {
            solPrefListLengths[i] = problem_.getNumberOfObjectives();
        }
        smp.stableMatchTwoLevel(subpPair12_, solPartners, relatedSubp12_, relatedSubp21_, solPrefListLengths);
    }

    public double fitnessFunction(double[] individual, double[] weight, double[] referencePoint, String type) {

        double fitness = 0.0;

        if (type.equals("_TCH1")) {
            double maxFun = -1.0e+30;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = individual[i] - referencePoint[i];

                double feval;
                if (weight[i] == 0) {
                    feval = 0.000001 * diff;
                } else {
                    feval = diff * weight[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for
            fitness = maxFun;
        } else if (type.equals("_TCH2")) {
            double maxFun = -1.0e+30;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = individual[i] - referencePoint[i];

                double feval;
                if (weight[i] == 0) {
                    feval = diff / 0.000001;
                } else {
                    feval = diff / weight[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for
            fitness = maxFun;
        } else if (type.equals("_PBI")) {
            double theta = 5.0;

            // normalize the weight vector (line segment)
            double nd = norm(weight);
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                weight[i] = weight[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realA[i] = (individual[i] - referencePoint[i]);

            // distance along the line segment
            double d1 = innerProduct(realA, weight);

            // distance to the line segment
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realB[i] = (individual[i] - (referencePoint[i] + d1 * weight[i]));
            double d2 = norm(realB);

            fitness = d1 + theta * d2;
        } else if (type.equals("_IPBI")) {
            double theta = 0.1;

            // normalize the weight vector (line segment)
            double nd = norm(weight);
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                weight[i] = weight[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realA[i] = (individual[i] - referencePoint[i]);

            // distance along the line segment
            double d1 = innerProduct(realA, weight);

            // distance to the line segment
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realB[i] = (individual[i] - (referencePoint[i] + d1 * weight[i]));
            double d2 = norm(realB);

            fitness = d1 + theta * d2;
        } else if (type.equals("_TCHA")) {
            double maxFun = -1.0e+30;
            double theta = 1.0e-6;
            double sumFun = 0;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = individual[i] - referencePoint[i];

                double feval;
                if (weight[i] == 0) {
                    feval = diff / 0.000001;
                } else {
                    feval = diff / weight[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
                sumFun += feval;
            } // for

            fitness = maxFun + theta * sumFun;
        } else if (type.equals("_TCHPD")) {
            double maxFun = -1.0e+30;
            double theta = 5.0;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = individual[i]- referencePoint[i];

                double feval;
                if (weight[i] == 0) {
                    feval = diff / 0.000001;
                } else {
                    feval = diff / weight[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for

            // normalize the weight vector (line segment)
            double nd = norm(weight);
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                weight[i] = weight[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realA[i] = (individual[i] - referencePoint[i]);

            // distance along the line segment
            double d1 = Math.abs(innerProduct(realA, weight));

            // distance to the line segment
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                realB[i] = (individual[i] - (referencePoint[i] + d1 * weight[i]));
            double d2 = norm(realB);

            fitness = maxFun + theta * d2;
        } else {
            System.out.println("MOEAD.fitnessFunction: unknown type " + type);
            System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation

    /**
     * Calculate the perpendicular distance between the solution and reference
     * line
     *
     * @param point
     * @param vector
     * @return
     */
    public double calculateDistance(double[] point, double[] vector) {
        double scale;
        double distance;

        double[] vecProj = new double[problem_.getNumberOfObjectives()];

        scale = innerProduct(point, vector) / innerProduct(vector, vector);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
            vecProj[i] = point[i] - scale * vector[i];

        distance = norm(vecProj);

        return distance;
    }

    /**
     * Calculate the dot product of two vectors
     * @param vec1
     * @param vec2
     */
    private double innerProduct(double[] vec1, double[] vec2) {
        double sum = 0;
        for (int i = 0; i < vec1.length; i++)
            sum += vec1[i] * vec2[i];
        return sum;
    }

    /**
     * Calculate the norm of the vector
     * @param vec
     */
    private double norm(double[] vec) {
        return Math.sqrt(innerProduct(vec, vec));
    }
} // MOEAD

