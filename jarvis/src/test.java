import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import com.darkprograms.speech.microphone.*;
import com.darkprograms.speech.recognizer.*;
import net.sourceforge.javaflacencoder.*;
import org.json.*;
import com.darkprograms.speech.util.StringUtil;
import java.nio.file.Files;

public class test {
	private static MicrophoneAnalyzer mic = new MicrophoneAnalyzer(FLACFileWriter.FLAC);
	private static int micVolume = 0;
	public static void main(String[] args) throws Exception {
		GSpeechDuplex dup = new GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
			dup.addResponseListener(new GSpeechResponseListener(){
			public void onResponse(GoogleResponse gr){
					String result = StringUtil.substringBetween(gr.getResponse(), "[{\"transcript\":\"", "\"}],");
					String command = "";
					if (result != null) {
						for (int x = 0; x < result.length(); x++) {
							if (result.charAt(x) == '"')
								break;
							command += result.charAt(x);
						}
						System.out.println(command);
					}
			}});

		File file = new File("test.flac");
		mic.open();
		while(true){
			mic.captureAudioToFile(file);
			long time = System.nanoTime();
			micVolume = mic.getAudioVolume(10);
			System.out.println(micVolume);
			if (micVolume < 27) {
				do {
					Thread.sleep(490);
					micVolume = mic.getAudioVolume(10);
					if (System.nanoTime() - time > 3000000000l && micVolume < 30) {
						time = System.nanoTime();
						mic.close();
						mic.getAudioFile().delete();
						mic.captureAudioToFile(file);
					}
				} while (micVolume < 27);
			}
			System.out.println("Recording");
			time = System.nanoTime();
			do {
				Thread.sleep(740);
				micVolume = mic.getAudioVolume(10);
				System.out.println(micVolume);
			} while (micVolume > 15 && System.nanoTime() - time < 7000000000l);
			System.out.println("Stopped");
			if ( System.nanoTime() - time < 7000000000l) {
				Thread.sleep(50);
				mic.close();
				byte[] data = Files.readAllBytes(mic.getAudioFile().toPath());
				dup.recognize(data, (int)mic.getAudioFormat().getSampleRate());
			}

			mic.getAudioFile().delete();
		}
	}
}