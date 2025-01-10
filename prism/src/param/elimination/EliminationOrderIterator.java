package param.elimination;

import java.util.Iterator;

import param.MutablePMC;

public abstract class EliminationOrderIterator implements Iterator<Integer> {
	protected MutablePMC pmc;
	private int initialState;
	
	public EliminationOrderIterator(MutablePMC pmc, int initialState) {
		this.pmc = pmc;
		this.initialState = initialState;
	}

	public int getInitialState() {
		return initialState;
	}

	public void setInitialState(int initialState) {
		this.initialState = initialState;
	}

	public MutablePMC getPmc() {
		return pmc;
	}

	public void setPmc(MutablePMC pmc) {
		this.pmc = pmc;
	}
	
	
}
