package param.elimination;

import param.MutablePMC;

public class ForwardReverseOrder extends ForwardOrder {

	public ForwardReverseOrder(MutablePMC pmc, int initialState) {
		super(pmc, initialState);
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
