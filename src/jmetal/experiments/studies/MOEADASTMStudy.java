package jmetal.experiments.studies;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.MOEAD_DRA_CA_Settings;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mengyuawu3 on 23-Jun-16.
 */
public class MOEADASTMStudy extends Experiment {

    /**
     * Configures the algorithms in each independent run
     *
     * @param problemName  The problem to solve
     * @param problemIndex
     * @throws ClassNotFoundException
     */
    public void algorithmSettings(String problemName,
                                  int problemIndex,
                                  Algorithm[] algorithm) throws ClassNotFoundException {
        try {
            int numberOfAlgorithms = algorithmNameList_.length;

            HashMap[] parameters = new HashMap[numberOfAlgorithms];

            for (int i = 0; i < numberOfAlgorithms; i++) {
                parameters[i] = new HashMap();
            } // for

//            if (!paretoFrontFile_[problemIndex].equals("")) {
//                for (int i = 0; i < numberOfAlgorithms; i++)
//                    parameters[i].put("paretoFrontFile_", paretoFrontFile_[problemIndex]);
//            } // if

            algorithm[0] = new MOEAD_DRA_CA_Settings(problemName).configure(parameters[0]);
//            algorithm[0] = new MOEAD_DRA_ASTM_Settings(problemName).configure(parameters[0]);
//            algorithm[0] = new MOEAD_IR_Settings(problemName).configure(parameters[0]);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MOEADASTMStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MOEADASTMStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMException ex) {
            Logger.getLogger(MOEADASTMStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // algorithmSettings

    /**
     * Main method
     *
     * @param args
     * @throws JMException
     * @throws IOException
     */
    public static void main(String[] args) throws JMException, IOException {
        MOEADASTMStudy exp = new MOEADASTMStudy();

        exp.experimentName_ = "MOEAD_ASTM_Study";
//        exp.algorithmNameList_ = new String[]{"MOEAD_DRA_ASTM_v"};
//        exp.algorithmNameList_ = new String[]{"MOEAD_DRA_ASTM_correct_RmaxN","MOEAD_DRA_CA_thresholdT_correct_RmaxN"};
        exp.algorithmNameList_ = new String[]{"MOEAD_DRA_CA_thresholdT_correct_RmaxN"};
//        exp.algorithmNameList_ = new String[]{"MOEAD_DRA_CA_thresholdT_v"};


        exp.problemList_ = new String[]{
                "UF1","UF2","UF3","UF4","UF5","UF6","UF7","UF8","UF9","UF10",
//                "MOP1","MOP2","MOP3","MOP4","MOP5","MOP6","MOP7",
//                "WFG1_2", "WFG2_2", "WFG3_2", "WFG4_2", "WFG5_2", "WFG6_2", "WFG7_2", "WFG8_2", "WFG9_2",
//                "WFG1_3", "WFG2_3", "WFG3_3", "WFG4_3", "WFG5_3", "WFG6_3", "WFG7_3", "WFG8_3", "WFG9_3",
//                "WFG1_5", "WFG2_5", "WFG3_5", "WFG4_5", "WFG5_5", "WFG6_5", "WFG7_5", "WFG8_5", "WFG9_5",
//                "WFG1_8", "WFG2_8", "WFG3_8", "WFG4_8", "WFG5_8", "WFG6_8", "WFG7_8", "WFG8_8", "WFG9_8",
//                "WFG1_10", "WFG2_10", "WFG3_10", "WFG4_10", "WFG5_10", "WFG6_10", "WFG7_10", "WFG8_10", "WFG9_10",
        };



//        exp.paretoFrontFile_ = new String[]{"WFG1.3D.pf", "WFG2.3D.pf", "WFG3.3D.pf",
//                "WFG4.3D.pf", "WFG5.3D.pf", "WFG6.3D.pf",
//                "WFG7.3D.pf", "WFG8.3D.pf", "WFG9.3D.pf",
//                "DTLZ1.3D.pf", "DTLZ2.3D.pf", "DTLZ3.3D.pf",
//                "DTLZ4.3D.pf"};

//        exp.indicatorList_ = new String[]{"HV", "IGD"};

        int numberOfAlgorithms = exp.algorithmNameList_.length;

        exp.experimentBaseDirectory_ = "../experiments/" + exp.experimentName_;
        exp.paretoFrontDirectory_ = "./data/paretoFronts";

        exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

        exp.independentRuns_ = 5;

        exp.initExperiment();

        // Run the experiments
        int numberOfThreads;
        exp.runExperiment(numberOfThreads = 5);


    } // main
} // StandardStudy
