package mgrnn;
/*
 * This class is for representing leanring status of the LGRNN.
 * Therefore, lgrnn leanring methods return the selected learning option as well as the current residual error.
 */
public class LgrnnLearningStatus {
	static final int SUBSTITUTION = 1, PRUNING = 2, MODIFY = 3, IGNORE = 4, AGGREGATE = 5, GRNN=6;
	int LearningOption;
	double residual_error;
	double tau;
}
