package param.elimination.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import param.elimination.EliminationOrder;

public class EliminationRunGroup {
	private Instant start;
	private Instant end;
	@JsonIgnore
	private File modelFile;
	private boolean recordData = false;
	private List<EliminationRun> runs = new ArrayList<>();
	private EliminationRun currentRun = null;
	private boolean deleteModelFile = true;
	@JsonIgnore
	private File runsFile;
	private static EliminationRunGroup instance;
	public static final DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-DD'T'HH:mm:ss")
			.withZone(ZoneId.of("Europe/Amsterdam"));

	public EliminationRunGroup() {
		start = Instant.now();
	}

	public void addRun(EliminationRun eliminationRun) {
		runs.add(eliminationRun);
		if (end == null || end.isBefore(eliminationRun.getEnd())) {
			end = eliminationRun.getEnd();
		}
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

	public List<EliminationRun> getRuns() {
		return runs;
	}

	public void setRuns(List<EliminationRun> runs) {
		this.runs = runs;
	}

	public boolean isRecordData() {
		return recordData;
	}

	public void setRecordData(boolean recordData) {
		this.recordData = recordData;
	}

	public File getModelFile() {
		if (modelFile == null) {
			modelFile = new File("model.mn");
		}
		return modelFile;
	}

	public void setModelFile(File modelFile) {
		this.modelFile = modelFile;
		deleteModelFile = false;
	}

	public void concludeCurrentRun() throws IOException {
		currentRun.setIndex(runs.size());
		currentRun.conclude();
		runs.add(currentRun);
	}

	public EliminationRun getCurrentRun() {
		return currentRun;
	}

	public void newRun(EliminationRun run) {
		currentRun = run;
		run.setModel(modelFile);
	}

	public EliminationRun newRunWithDifferentOrder(EliminationOrder order) {
		EliminationRun newRun = new EliminationRun(currentRun.getProperty(), currentRun.getPropertyIndex());
		newRun.setOrder(order);
		newRun.setModel(currentRun.getModel());
		currentRun = newRun;
		return currentRun;
	}

	public void writeDataToFile() throws IOException {
		if (end == null) {
			end = Instant.now();
		}
		runsFile = new File(MessageFormat.format("{0}-runs.json", FILENAME_DATE_FORMATTER.format(start)));
		ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
		objectMapper.writeValue(runsFile, this);
	}

	public void compressResults(String outputFilename) throws IOException {
		if (outputFilename == null) {
			outputFilename = MessageFormat.format("{0}-{1}.zip", getModelFile().getName().split("\\.")[0],
					FILENAME_DATE_FORMATTER.format(start));
		}
		try (FileOutputStream fos = new FileOutputStream(
				"/mnt/c/dev/master-thesis/master-scripts/data_dump/" + outputFilename);
				ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			List<File> files = runs.stream().map(EliminationRun::getStepsFile)
					.collect(Collectors.toCollection(() -> new ArrayList<>()));
			files.add(runsFile);
			files.add(modelFile);
			for (File file : files) {
				try (FileInputStream fis = new FileInputStream(file)) {
					ZipEntry zipEntry = new ZipEntry(file.getName());
					zipOut.putNextEntry(zipEntry);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
				if (!file.equals(modelFile) || deleteModelFile) {
					file.deleteOnExit();
				}
			}
		}
	}

	public static EliminationRunGroup getInstance() {
		if (instance == null) {
			instance = new EliminationRunGroup();
		}
		return instance;
	}

	public void setDeleteModelFile(boolean deleteModelFile) {
		this.deleteModelFile = deleteModelFile;
	}

}
