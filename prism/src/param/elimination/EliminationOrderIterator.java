package param.elimination;

import java.util.Iterator;

import param.MutablePMC;
import prism.PrismComponent;

public abstract class EliminationOrderIterator implements Iterator<Integer> {
	protected MutablePMC pmc;
	private int initialState;
	protected final PrismComponent parent;
	
	public EliminationOrderIterator(MutablePMC pmc, int initialState, PrismComponent parent) {
		this.pmc = pmc;
		this.initialState = initialState;
		this.parent = parent;
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
