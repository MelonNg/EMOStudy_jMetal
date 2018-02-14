package jmetal.problems.MOP;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;

/**
 * Created by mengyuawu3 on 26-Oct-16.
 */
public class MOP7 extends Problem {

    /**
     * Constructor.
     * Creates a default instance of problem MOP7 (10 decision variables)
     *
     * @param solutionType The solution type must "Real" or "BinaryReal".
     */
    public MOP7(String solutionType) throws ClassNotFoundException {
        this(solutionType, 10); // 10 variables by default
    }

    /**
     * Creates a new instance of problem MOP7.
     *
     * @param numberOfVariables Number of variables.
     * @param solutionType      The solution type must "Real" or "BinaryReal".
     */
    public MOP7(String solutionType, Integer numberOfVariables) {
        numberOfVariables_ = numberOfVariables;
        numberOfObjectives_ = 3;
        numberOfConstraints_ = 0;
        problemName_ = "MOP7";

        upperLimit_ = new double[numberOfVariables_];
        lowerLimit_ = new double[numberOfVariables_];

        for (int var = 0; var < numberOfVariables_; var++) {
            lowerLimit_[var] = 0.0;
            upperLimit_[var] = 1.0;
        } //for

        if (solutionType.compareTo("BinaryReal") == 0)
            solutionType_ = new BinaryRealSolutionType(this);
        else if (solutionType.compareTo("Real") == 0)
            solutionType_ = new RealSolutionType(this);
        else {
            System.out.println("Error: solution type " + solutionType + " invalid");
            System.exit(-1);
        }
    }

    /**
     * Evaluates a solution.
     *
     * @param solution The solution to evaluate.
     * @throws JMException
     */
    public void evaluate(Solution solution) throws JMException {
        Variable[] decisionVariables = solution.getDecisionVariables();

        double[] x = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++)
            x[i] = decisionVariables[i].getValue();

        double g = 0.0;
        double t;
        for (int i = 2; i < x.length; i++) {
            t = x[i] - x[0] * x[1];
            g += -0.9 * Math.pow(t, 2) + Math.pow(Math.abs(t), 0.6);
        }
        g *= 2 * Math.sin(Math.PI * x[0]);

        solution.setObjective(0, (1.0 + g) * Math.cos(Math.PI * x[0] / 2) * Math.cos(Math.PI * x[1] / 2));
        solution.setObjective(1, (1.0 + g) * Math.cos(Math.PI * x[0] / 2) * Math.sin(Math.PI * x[1] / 2));
        solution.setObjective(2, (1.0 + g) * Math.sin(Math.PI * x[0] / 2));

    } // evaluate
}