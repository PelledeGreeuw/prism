package param.elimination;

import param.MutablePMC;
import prism.PrismComponent;

public class BackwardReverseOrder extends BackwardOrder {
	public BackwardReverseOrder(MutablePMC pmc, int initialState, PrismComponent parent, boolean onlyStatesReachingTarget) {
		super(pmc, initialState, parent, onlyStatesReachingTarget);
	}
	
	@Override
	public int[] getOrder() {
		 int[] order = super.getOrder();
		 for(int i = 0; i < order.length / 2; i++)
		 {
		     int temp = order[i];
		     order[i] = order[order.length - i - 1];
		     order[order.length - i - 1] = temp;
		 }
		 return order;
	}

}
