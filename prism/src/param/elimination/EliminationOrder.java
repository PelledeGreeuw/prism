package param.elimination;

public enum EliminationOrder {
	/** arbitrary */
	BENCHMARK,
	ARBITRARY,
	/** states close to initial states first */
	FORWARD,
	/** states close to initial states last */
	FORWARD_REVERSED,
	/** states close to target states first */
	BACKWARD,
	/** states close to target states last */
	BACKWARD_REVERSED,
	/** random */
	RANDOM;
}
