package param.elimination.benchmark;

public class EliminationStep {
	private final int step;
	private final int state;
	private final int calculations;
	private final int transitions;

	public EliminationStep(int step, int state, int calculations, int transitions) {
		this.step = step;
		this.state = state;
		this.calculations = calculations;
		this.transitions = transitions;
	}

	public int getStep() {
		return step;
	}

	public int getState() {
		return state;
	}

	public int getCalculations() {
		return calculations;
	}

	public int getTransitions() {
		return transitions;
	}
	
	
}
