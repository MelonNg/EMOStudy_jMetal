package jmetal.metaheuristics.moead;

import javafx.collections.transformation.SortedList;
import jmetal.util.PseudoRandom;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mengyuawu3 on 20-Jun-16.
 */
public class CollegeAdmission {
    final int NOT_ENGAGED = -1;

    private int collegeSize, applicantSize;
    private int[][] collegePref, applicantPref;

    public CollegeAdmission(int collegeSize, int applicantSize, int[][] collegePref, int[][] applicantPref) {
        this.collegeSize = collegeSize;
        this.applicantSize = applicantSize;
        this.collegePref = collegePref;
        this.applicantPref = applicantPref;
    }

    public void admit(LinkedList<Integer>[] collegeMatchings, int[] applicantMatchings, int totalQuota) {

        LinkedList<Integer>[] studentRanks = new LinkedList[collegeSize];
        for (int i = 0; i < collegeSize; i++) {
            collegeMatchings[i] = new LinkedList<>();
            studentRanks[i] = new LinkedList<>();
        }
        for (int i = 0; i < applicantSize; i++)
            applicantMatchings[i] = NOT_ENGAGED;

        // List of men that are not currently engaged.
        List<Integer> candidateApplicants = new LinkedList<>();
        // next[i] is the next woman to whom i has not yet proposed.
        int[] next = new int[applicantSize];
        for (int i = 0; i < applicantSize; i++) {
            next[i] = 0;
            candidateApplicants.add(i);
        }

        int admittedSize = 0;
        while (!candidateApplicants.isEmpty()) {
            int a = candidateApplicants.remove(0);
            int c = applicantPref[a][next[a]];
            next[a]++;

            // add
            int index = 0;
            int rank_a = getApplicantRank(c,a);
            for (; index < collegeMatchings[c].size(); index++) {
                if (rank_a < studentRanks[c].get(index)) {
                    break;
                }
            }
            collegeMatchings[c].add(index, a);
            studentRanks[c].add(index, rank_a);
            applicantMatchings[a] = c;

            if (admittedSize < totalQuota) {
                admittedSize++;
            } else {
                // reject
                int maxNumStudents = 0;
                for (int i = 0; i < collegeSize; i++) {
                    if (collegeMatchings[i].size() > maxNumStudents) {
                        maxNumStudents = collegeMatchings[i].size();
                    }
                }
                List<Integer> reject_colleges = new LinkedList<>();
                for (int i = 0; i < collegeSize; i++) {
                    if (collegeMatchings[i].size() == maxNumStudents) {
                        reject_colleges.add(i);
                    }
                }
                int maxRank = 0;
                for (int i = 0; i < reject_colleges.size(); i++) {
                    if (studentRanks[reject_colleges.get(i)].getLast() > maxRank) {
                        maxRank = studentRanks[reject_colleges.get(i)].getLast();
                    }
                }
                for (int i = 0; i < reject_colleges.size(); i++) {
                    if (studentRanks[reject_colleges.get(i)].getLast() != maxRank) {
                        reject_colleges.remove(i);
                        i--;
                    }
                }
                int reject_c = reject_colleges.get((int) (PseudoRandom.randDouble() * reject_colleges.size()));
                int reject_a = collegeMatchings[reject_c].getLast();
                applicantMatchings[reject_a] = NOT_ENGAGED;
                collegeMatchings[reject_c].removeLast();
                studentRanks[reject_c].removeLast();
                if (next[reject_a] <= collegeSize) {
                    candidateApplicants.add(reject_a);
                }
            }
        }
    }

    public void admitIncompleteLists(LinkedList<Integer>[] collegeMatchings, int[] applicantMatchings, int[] applicantPrefListLengths, int totalQuota) {

        LinkedList<Integer>[] studentRanks = new LinkedList[collegeSize];
        for (int i = 0; i < collegeSize; i++) {
            collegeMatchings[i] = new LinkedList<>();
            studentRanks[i] = new LinkedList<>();
        }
        for (int i = 0; i < applicantSize; i++)
            applicantMatchings[i] = NOT_ENGAGED;

        // List of men that are not currently engaged.
        List<Integer> candidateApplicants = new LinkedList<>();
        // next[i] is the next woman to whom i has not yet proposed.
        int[] next = new int[applicantSize];
        for (int i = 0; i < applicantSize; i++) {
            next[i] = 0;
            if (applicantPrefListLengths[i] > 0)
                candidateApplicants.add(i);
        }

        int admittedSize = 0;
        while (!candidateApplicants.isEmpty()) {
            int a = candidateApplicants.remove(0);
            int c = applicantPref[a][next[a]];
            next[a]++;

            // add
            int index = 0;
            int rank_a = getApplicantRank(c,a);
            for (; index < collegeMatchings[c].size(); index++) {
                if (rank_a < studentRanks[c].get(index)) {
                    break;
                }
            }
            collegeMatchings[c].add(index, a);
            studentRanks[c].add(index, rank_a);
            applicantMatchings[a] = c;

            if (admittedSize < totalQuota) {
                admittedSize++;
            } else {
                // reject
                int maxNumStudents = 0;
                for (int i = 0; i < collegeSize; i++) {
                    if (collegeMatchings[i].size() > maxNumStudents) {
                        maxNumStudents = collegeMatchings[i].size();
                    }
                }
                List<Integer> reject_colleges = new LinkedList<>();
                for (int i = 0; i < collegeSize; i++) {
                    if (collegeMatchings[i].size() == maxNumStudents) {
                        reject_colleges.add(i);
                    }
                }
                int maxRank = 0;
                for (int i = 0; i < reject_colleges.size(); i++) {
                    if (studentRanks[reject_colleges.get(i)].getLast() > maxRank) {
                        maxRank = studentRanks[reject_colleges.get(i)].getLast();
                    }
                }
                for (int i = 0; i < reject_colleges.size(); i++) {
                    if (studentRanks[reject_colleges.get(i)].getLast() != maxRank) {
                        reject_colleges.remove(i);
                        i--;
                    }
                }
                int reject_c = reject_colleges.get((int) (PseudoRandom.randDouble() * reject_colleges.size()));
                int reject_a = collegeMatchings[reject_c].getLast();
                applicantMatchings[reject_a] = NOT_ENGAGED;
                collegeMatchings[reject_c].removeLast();
                studentRanks[reject_c].removeLast();
                if (next[reject_a] < applicantPrefListLengths[reject_a]) {
                    candidateApplicants.add(reject_a);
                }
            }
        }
    }

    private int getApplicantRank(int college, int applicant) {
        int rank = -1;
        for (int i = 0; i < applicantSize; i++) {
            if (collegePref[college][i] == applicant) {
                rank = i;
                break;
            }
        }
        return rank;
    }
}
