package jmetal.experiments.studies;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.*;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mengyuawu3 on 23-Jun-16.
 */
public class MOEAADStudy extends Experiment {

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

            algorithm[0] = new MOEAAD_Settings(problemName).configure(parameters[0]);
//            algorithm[0] = new GWASFGA_Settings(problemName).configure(parameters[0]);
//            algorithm[1] = new GWASFGA_Settings(problemName).configure(parameters[1]);
//            algorithm[1] = new NSGAII_Settings(problemName).configure(parameters[1]);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MOEAADStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MOEAADStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMException ex) {
            Logger.getLogger(MOEAADStudy.class.getName()).log(Level.SEVERE, null, ex);
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
        long startTime=System.currentTimeMillis();

        MOEAADStudy exp = new MOEAADStudy();

        exp.experimentName_ = "MOEAAD_Study_TimeAnalysis";
//        exp.algorithmNameList_ = new String[]{"MOEAD_IPBI_SBX_T20"};
        exp.algorithmNameList_ = new String[]{"MOEAAD"};
//        exp.algorithmNameList_ = new String[]{"GWASF_GA","NSGAII"};


        exp.problemList_ = new String[]{
//                "iWFG1_3", "iWFG2_3", "iWFG3_3", "iWFG4_3", "iWFG5_3", "iWFG6_3", "iWFG7_3", "iWFG8_3", "iWFG9_3",
//                "iDTLZ1_3", "iDTLZ2_3", "iDTLZ3_3", "iDTLZ4_3",
//                "iWFG1_5", "iWFG2_5", "iWFG3_5", "iWFG4_5", "iWFG5_5", "iWFG6_5", "iWFG7_5", "iWFG8_5", "iWFG9_5",
//                "iDTLZ1_5", "iDTLZ2_5", "iDTLZ3_5", "iDTLZ4_5",
//                "iWFG1_8", "iWFG2_8", "iWFG3_8", "iWFG4_8", "iWFG5_8", "iWFG6_8", "iWFG7_8", "iWFG8_8", "iWFG9_8",
//                "iDTLZ1_8", "iDTLZ2_8", "iDTLZ3_8", "iDTLZ4_8",
//                "iWFG1_10", "iWFG2_10", "iWFG3_10", "iWFG4_10", "iWFG5_10", "iWFG6_10", "iWFG7_10", "iWFG8_10", "iWFG9_10",
//                "iDTLZ1_10", "iDTLZ2_10", "iDTLZ3_10", "iDTLZ4_10",
                "iWFG1_15", "iWFG2_15", "iWFG3_15", "iWFG4_15", "iWFG5_15", "iWFG6_15", "iWFG7_15", "iWFG8_15", "iWFG9_15",
//                "iDTLZ1_15", "iDTLZ2_15", "iDTLZ3_15", "iDTLZ4_15",
//                "WFG1_3", "WFG2_3", "WFG3_3", "WFG4_3", "WFG5_3", "WFG6_3", "WFG7_3", "WFG8_3", "WFG9_3",
//                "DTLZ1_3", "DTLZ2_3", "DTLZ3_3", "DTLZ4_3",
//                "WFG1_5", "WFG2_5", "WFG3_5", "WFG4_5", "WFG5_5", "WFG6_5", "WFG7_5", "WFG8_5", "WFG9_5",
//                "DTLZ1_5", "DTLZ2_5", "DTLZ3_5", "DTLZ4_5",
//                "WFG1_8", "WFG2_8", "WFG3_8", "WFG4_8", "WFG5_8", "WFG6_8", "WFG7_8", "WFG8_8", "WFG9_8",
//                "DTLZ1_8", "DTLZ2_8", "DTLZ3_8", "DTLZ4_8",
//                "WFG1_10", "WFG2_10", "WFG3_10", "WFG4_10", "WFG5_10", "WFG6_10", "WFG7_10", "WFG8_10", "WFG9_10",
//                "DTLZ1_10", "DTLZ2_10", "DTLZ3_10", "DTLZ4_10",
                "WFG1_15", "WFG2_15", "WFG3_15", "WFG4_15", "WFG5_15", "WFG6_15", "WFG7_15", "WFG8_15", "WFG9_15",
//                "DTLZ1_15", "DTLZ2_15", "DTLZ3_15", "DTLZ4_15"
        };
        ;

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

        exp.independentRuns_ = 1;

        exp.initExperiment();

        // Run the experiments
        int numberOfThreads;
        exp.runExperiment(numberOfThreads = 1);

        long endTime=System.currentTimeMillis();
        System.out.println("Running Time： "+(endTime-startTime)+"ms");
    } // main
} // StandardStudy
