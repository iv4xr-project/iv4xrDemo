package game;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import world.BeliefState;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import static agents.TestSettings.*;

public class BasicSoundTest {

	private static LabRecruitsTestServer labRecruitsTestServer;

	@BeforeAll
	static void start() {
		String labRecruitsPath = "C:\\Users\\FernandoTESTAR\\Desktop\\IV4XR\\gym\\Windows\\bin\\LabRecruits.exe";
		labRecruitsTestServer = new LabRecruitsTestServer(true, labRecruitsPath);
		labRecruitsTestServer.waitForGameToLoad();
	}
//	@BeforeAll
//	static void start() {
//		if(USE_SERVER_FOR_TEST){
//			String labRecruitesExeRootDir = System.getProperty("user.dir") ;
//			labRecruitsTestServer = new LabRecruitsTestServer(
//					USE_GRAPHICS,
//					Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
//			labRecruitsTestServer.waitForGameToLoad();
//		}
//	}

	@AfterAll
	static void close() {
		labRecruitsTestServer.close();
	}
//	@AfterAll
//	static void close() {
//		if(USE_SERVER_FOR_TEST)
//			labRecruitsTestServer.close();
//	}

	/**
	 * These are pre-recorded sounds from LabRecruits. 
	 * Test that function works and maintains the max sound value in the correct ranges
	 */
	@Test
	public void graphFromWavFile() {
		String soundsPath = System.getProperty("user.dir") + "\\src\\test\\resources\\sounds\\";
		// Without or residual sound from min -69 to max 65
		Assertions.assertTrue(maxSoundFromWavFile(soundsPath + "no_sound_labRecordButtonDoor1.wav") < 70);

		// Bad recorded sound from -450 to 476
		Assertions.assertTrue(
				maxSoundFromWavFile(soundsPath + "bad_sound_labRecordButtonDoor1.wav") > 450
				&& maxSoundFromWavFile(soundsPath + "bad_sound_labRecordButtonDoor1.wav") < 480);

		// High recorded sound from -32768 to 32 767
		Assertions.assertTrue(maxSoundFromWavFile(soundsPath + "high_sound_labRecordButtonDoor1.wav") > 32500);
	}

	@Test
	public void soundIsRecorded() {
		try {
			AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

			DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			if(!AudioSystem.isLineSupported(dataInfo)) {
				System.err.println("Audio system not supported");
			}

			TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(dataInfo);
			targetLine.open();
			targetLine.start();

			Thread audioRecorderThread = new Thread() {
				@Override
				public void run() {
					AudioInputStream recordingStream = new AudioInputStream(targetLine);
					File outputRecord = new File("labRecordTest.wav");
					System.out.println("Creating record file : " + outputRecord.getAbsolutePath());
					try {
						AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputRecord);
					} catch(IOException ioe) {
						ioe.printStackTrace();
					}

					System.out.println("Stop recording");
				}
			};

			audioRecorderThread.start();
			targetLine.stop();
			targetLine.close();

		} catch(Exception e) {
			e.printStackTrace();
		}

		// Wait 2 seconds so the file will be saved correctly
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		File assertFile = new File("labRecordTest.wav");
		System.out.println("Assert record file was created : " + assertFile.getAbsolutePath());
		Assertions.assertTrue(assertFile.exists());
		System.out.println("Deleting rescord test file...");
		assertFile.delete();
		Assertions.assertFalse(assertFile.exists());
	}

	/**
	 * Next recordThread implementation only works with record devices. 
	 * We need to do a workaround in the Windows 10 devices to test the audio. 
	 * 
	 * "Windows 10 stereo mix" is a virtual device that redirects system output audio to record audio. 
	 * To avoid issues, this device needs to be the only record device in the system. 
	 * 
	 * To correctly execute this test, 
	 * - Install and enable "Windows 10 stereo mix" record device. 
	 * - "Windows 10 stereo mix" sound depend on the volume of the system (0 - 100)
	 * - Disable Windows 10 system microphones if exists. 
	 * - Do not use headphones :D (at least I had problems with that)
	 */
	@Test
	public void LabRecruitsSoundIsRecorded() {
		try {
			AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);            

			DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			if(!AudioSystem.isLineSupported(dataInfo)) {
				System.err.println("Audio system not supported");
			}

			TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(dataInfo);
			targetLine.open();
			targetLine.start();

			Thread audioRecorderThread = new Thread() {
				@Override
				public void run() {
					AudioInputStream recordingStream = new AudioInputStream(targetLine);
					File outputRecord = new File("labRecordButtonDoor1.wav");
					System.out.println("Creating record file : " + outputRecord.getAbsolutePath());
					try {
						AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputRecord);
					} catch(IOException ioe) {
						ioe.printStackTrace();
					}

					System.out.println("Stop recording");
				}
			};

			// Launch record thread before start LabRecruits Test
			audioRecorderThread.start();

			// Prepare and Run LabRecruits test
			var config = new LabRecruitsConfig("buttons_doors_1");
			var environment = new LabRecruitsEnvironment(config);

			// create a test agent
			var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
					. attachState(new BeliefState())
					. attachEnvironment(environment);
			// define the testing-task:
			var testingTask = SEQ(GoalLib.entityInteracted("button1"));

			// attaching the goal and testdata-collector
			var dataCollector = new TestDataCollector();
			testAgent.setTestDataCollector(dataCollector).setGoal(testingTask);

			// this will press the "Play" button in the game for you
			environment.startSimulation();

			//goal not achieved yet
			assertFalse(testAgent.success());

			int i = 0 ;
			// keep updating the agent
			while (testingTask.getStatus().inProgress()) {
				System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
				Thread.sleep(50);
				i++ ;
				testAgent.update();
				if (i>200) {
					break ;
				}
			}

			testingTask.printGoalStructureStatus();

			// goal status should be success
			assertTrue(testAgent.success());
			// close
			testAgent.printStatus();
			environment.close();

			// Wait 2 seconds so the last game sound will be saved correctly
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Stop recording LabRecruits sound
			targetLine.stop();
			targetLine.close();

		} catch(Exception e) {
			e.printStackTrace();
		}

		// Wait 2 seconds so the file will be saved correctly
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		File assertFile = new File("labRecordButtonDoor1.wav");
		System.out.println("Assert record file was created : " + assertFile.getAbsolutePath());
		Assertions.assertTrue(assertFile.exists());

		// TODO: Check the sound range of labRecordButtonDoor1.wav depending on the system volume
		maxSoundFromWavFile("labRecordButtonDoor1.wav");

		System.out.println("Deleting record labButtonDoor1 test file...");
		assertFile.delete();
		Assertions.assertFalse(assertFile.exists());
	}

	private int maxSoundFromWavFile(String wavFileName) {
		int finalMaxSound = -1;

		// https://stackoverflow.com/a/27517678
		File file = new File(wavFileName);
		AudioInputStream ais = null;
		try {
			ais = AudioSystem.getAudioInputStream(file);
			int frameLength = (int) ais.getFrameLength();
			int frameSize = (int) ais.getFormat().getFrameSize();
			byte[] eightBitByteArray = new byte[frameLength * frameSize];

			int result = ais.read(eightBitByteArray);

			int channels = ais.getFormat().getChannels();
			int[][] samples = new int[channels][frameLength];

			int sampleIndex = 0;
			for (int t = 0; t < eightBitByteArray.length;) {
				for (int channel = 0; channel < channels; channel++) {
					int low = (int) eightBitByteArray[t];
					t++;
					int high = (int) eightBitByteArray[t];
					t++;
					int sample = getSixteenBitSample(high, low);
					samples[channel][sampleIndex] = sample;
				}
				sampleIndex++;
			}

			int min = 0;
			int max = 0;
			for (int sample : samples[0]) {
				max = Math.max(max, sample);
				min = Math.min(min, sample);
			}

			System.out.println("File: " + wavFileName + ", Min: " + min);
			System.out.println("File: " + wavFileName + ", Max: " + max);
			finalMaxSound = max;

		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			try {
				ais.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return finalMaxSound;
	}

	private int getSixteenBitSample(int high, int low) {
		return (high << 8) + (low & 0x00ff);
	} 

}
