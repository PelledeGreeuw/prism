package param.elimination.benchmark;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import param.MutablePMC;

public class DOTExport {
	public static void exportModel(MutablePMC model, OutputStream outputStream) {
		try (PrintWriter printWriter = new PrintWriter(outputStream)) {
			printWriter.println("digraph G {");
			for (int i = 0; i < model.getNumStates(); i++) {
				if (model.isInitState(i)) {
					printWriter.println(i + "[style=\"filled\"];");
				}
				for (int target : model.getTransitionTargets().get(i)) {
					printWriter.println(i + " -> " + target + ";");
				}
			}
			printWriter.println("}");
		}
	}

	public static void exportModel(MutablePMC model, String filename) {
		try (FileOutputStream fileOutputStream = new FileOutputStream(
				"/mnt/c/dev/master-thesis/master-scripts/dotfiles/" + filename)) {
			exportModel(model, fileOutputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
