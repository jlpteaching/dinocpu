/** Handy container class for storing test results. */
package com.gradescope.jh61b.grader;

public class TestResult {
	final String name;
	final String number;
	final double maxScore;
	double score;
	final String visibility;

	/* outputSB is any text that we want to relay to the user when teh test is done running. */
	private StringBuilder outputSB;

	/* private List<String> tags; // Not yet implemented */


	public TestResult(String name, String number, double maxScore, String visibility) {
		this.name = name;
		this.number = number;
		this.maxScore = maxScore;
		this.outputSB = new StringBuilder();
		this.visibility = visibility;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void addOutput(String x) {
		outputSB.append(x);
	}

	/* Return in JSON format. TODO: Need to escape newlines and possibly other characters. */
	public String toJSON() {
		String output = outputSB.toString();
		String noWindowsNewLines = output.replace("\r\n", "\\n");
		String noWeirdNewLines = noWindowsNewLines.replace("\r", "\\n");
		String noLinuxNewLines = noWeirdNewLines.replace("\n", "\\n");
		String noTabs = noLinuxNewLines.replace("\t", "    ");
		String noQuotes = noTabs.replace("\"", "\\\"");
		String noSingeQuotes = noQuotes.replace("\'", "\\\'");

		return "{" + String.join(",", new String[] {
			String.format("\"%s\": \"%s\"", "name", name),
			String.format("\"%s\": \"%s\"", "number", number),
			String.format("\"%s\": %s", "score", score),
			String.format("\"%s\": %s", "max_score", maxScore),
			String.format("\"%s\": \"%s\"", "visibility", visibility),
			String.format("\"%s\": \"%s\"", "output", noSingeQuotes)
		}) + "}";
	}

	/* For debugging only. */
	public String toString() {
		return("name: " + name + ", number: " + number + ", score: " + score + ", max_score: " + maxScore + ", detailed output if any (on next line): \n"  + outputSB.toString());
	}
}
