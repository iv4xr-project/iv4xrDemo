package bdd.state;

import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;

public class LoadLevelState {

	private String labRecruitsPath;
	private String labRecruitsLevel;
	private boolean labRecruitsGraphics;
	private LabRecruitsTestServer labRecruitsTestServer;
	private LabRecruitsEnvironment labRecruitsEnvironment;

	public String getLabRecruitsPath() {
		return labRecruitsPath;
	}

	public void setLabRecruitsPath(final String labRecruitsPath) {
		this.labRecruitsPath = labRecruitsPath;
	}

	public String getLabRecruitsLevel() {
		return labRecruitsLevel;
	}

	public void setLabRecruitsLevel(final String labRecruitsLevel) {
		this.labRecruitsLevel = labRecruitsLevel;
	}

	public boolean isLabRecruitsGraphics() {
		return labRecruitsGraphics;
	}

	public void setLabRecruitsGraphics(boolean labRecruitsGraphics) {
		this.labRecruitsGraphics = labRecruitsGraphics;
	}

	public LabRecruitsTestServer getLabRecruitsTestServer() {
		return labRecruitsTestServer;
	}

	public void setLabRecruitsTestServer(LabRecruitsTestServer labRecruitsTestServer) {
		this.labRecruitsTestServer = labRecruitsTestServer;
	}

	public LabRecruitsEnvironment getLabRecruitsEnvironment() {
		return labRecruitsEnvironment;
	}

	public void setLabRecruitsEnvironment(final LabRecruitsEnvironment labRecruitsEnvironment) {
		this.labRecruitsEnvironment = labRecruitsEnvironment;
	}

}
