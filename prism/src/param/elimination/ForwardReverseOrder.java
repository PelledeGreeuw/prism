package param.elimination;

import param.MutablePMC;
import prism.PrismComponent;

public class ForwardReverseOrder extends ForwardOrder {

	public ForwardReverseOrder(MutablePMC pmc, int initialState, PrismComponent parent) {
		super(pmc, initialState, parent);
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
