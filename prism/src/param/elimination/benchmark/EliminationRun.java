package param.elimination.benchmark;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import param.elimination.EliminationOrder;

public class EliminationRun {
	@JsonIgnore
	private File stepsFile;
	private String stepsFilename;
	private EliminationOrder order;
	private long calculations;
	@JsonIgnore
	private File model;
	private String modelFilename;
	private String property;
	private int propertyIndex;
	private Instant start;
	private Instant end;
	@JsonIgnore
	private List<EliminationStep> steps = new ArrayList<>();
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().findAndAddModules().build();
	@JsonIgnore
	private int index;

	public EliminationRun(String property, int propertyIndex) {
		this.property = property;
		this.propertyIndex = propertyIndex;
	}

	public void writeStepsToFile(Collection<EliminationStep> steps) throws IOException {
		OBJECT_MAPPER.writeValue(stepsFile, steps);
	}

	public void conclude() throws IOException {
		end = Instant.now();
		setStepsFile(new File(MessageFormat.format("steps-{0}.json", UUID.randomUUID())));
		calculations = steps.stream().map(EliminationStep::getCalculations).reduce(0, Integer::sum);
		writeStepsToFile(steps);
		steps = null;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public void createStepsFile() {
		stepsFile = new File(MessageFormat.format("{0}-{1}.json", propertyIndex, order.name()));
		stepsFilename = stepsFile.getName();
	}

	public File getStepsFile() {
		return stepsFile;
	}

	public void setStepsFile(File stepsFile) {
		this.stepsFile = stepsFile;
		this.stepsFilename = stepsFile.getName();
	}

	public EliminationOrder getOrder() {
		return order;
	}

	public void setOrder(EliminationOrder order) {
		this.order = order;
	}

	public long getCalculations() {
		if (calculations == 0) {
			return steps.stream().map(EliminationStep::getCalculations).reduce(0, Integer::sum);
		}
		return calculations;
	}

	public void setCalculations(long calculations) {
		this.calculations = calculations;
	}

	public File getModel() {
		return model;
	}

	public void setModel(File model) {
		this.model = model;
		this.modelFilename = model.getName();
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public Instant getEnd() {
		return end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}

	public void addStep(EliminationStep eliminationStep) {
		steps.add(eliminationStep);
	}

	public List<EliminationStep> getSteps() {
		return steps;
	}

	public int getPropertyIndex() {
		return propertyIndex;
	}

	public void setPropertyIndex(int propertyIndex) {
		this.propertyIndex = propertyIndex;
	}

	public String getStepsFilename() {
		return stepsFilename;
	}

	public String getModelFilename() {
		return modelFilename;
	}

}
