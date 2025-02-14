package param.elimination;

import param.MutablePMC;
import prism.PrismComponent;

public class BruteForceHelperIterator extends OrderWithShadowIterator {
	int[] order;
	

	public BruteForceHelperIterator(MutablePMC pmc, int initialState, PrismComponent parent, int[] order) {
		super(pmc, initialState, parent);
		this.order = order;
	}

	@Override
	public int[] getOrder() {
		return order;
	}

}
