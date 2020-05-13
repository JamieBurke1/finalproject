/*
 * Quick response code generator and reader
 * 
 * @author IC
 * @version 1.0.0
 */
package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import javax.swing.JTextField;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class QRCode {
	static final int ROOM_TEXT_LENGTH = 5;
	static JTextField textField = null;
	public static String speaktext;
	public static String fromReception = "From reception, walk to ";
	
	/*
	 * Here is a list of the rooms supported and direcions.  You can add more rooms and directions here
	 * 
	 */
	public static String[] rooms = {
			"F1012",
			"B1015",
			"E0017",
			"E1019"};
	public static String[] roomDirection = {
			"your left into the Engineering building.  Climb one flight of stairs.  Walk straight over the bridge into the F block.  turn right through the door and the room is on the right.",
			"your right past reception and up the stairs.  Follow the corridor and go behind the barrier on the left.  Turn left into the corridor behind the barrier and the room is the fourth door on your left.",
			"your left into the engineering block.  Walk around to the right past the stairs and the room is the third room on the right.",
			"your left into the engineering block.  Walk up one flight of stairs.  Walk along the corridor on the left side and the room is the fifth room on the left after the stairs."
			};


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws WriterException, IOException, NotFoundException {
		// Initial hardcoded data for test program 
		String qrCodeData = "Day: Tuesday\nTime: 09:00 to 11:00\nSubject: Software Engineering\nRoom: F1.012";
		String filePath = "myQRCode.png";
		String charset = "UTF-8"; // or "ISO-8859-1"
		QRCode myVoice = new QRCode();
		String roomDir = null;
		String myroom1 = null;
		String roomInfo = null;
		Boolean foundMatch = false;
		
		Map hintMap = new HashMap();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
		System.out.println("QR Code image created successfully!");
		
		String myQRCode = readQRCode(filePath, charset, hintMap);	
		
		// Extract room name
		String chosenRoom = myQRCode.substring(myQRCode.length() - ROOM_TEXT_LENGTH);
		System.out.println("Data read from QR Code:\n" + chosenRoom);	
		
		// find directions
		for (int i = 0; i < rooms.length; i++) {
			if (rooms[i].equals(chosenRoom)) {
				myroom1  = rooms[i];
				roomDir = roomDirection[i];
				foundMatch = true;
				break;
			}
		}	
		if (foundMatch == false) {
			roomInfo = "I'm sorry.  I could not find this room in the building.";
		} else {
			roomInfo = "To get to, " + chosenRoom + "," + fromReception + roomDir;	// give directions
		}
		
		// speak the direction
		myVoice.dospeak(roomInfo, "kevin16");
	}

	/*
	 * void createQRCode()
	 * Description: This method writes out the passes qrCodeData string to the
	 * file specified in the current directory. 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void createQRCode(String qrCodeData, String filePath, String charset,
			Map hintMap, int qrCodeheight, int qrCodewidth)
					throws WriterException, IOException {

		Path p1 = Paths.get(filePath);
		// encode the data to the hashmap.
		BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		// write out QR code to file image
		MatrixToImageWriter.writeToPath(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), p1);
	}

	/*
	 * String readQRCode()
	 * Description: Reads the QR data from the image and returns the string of data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String readQRCode(String filePath, String charset, Map hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
		return qrCodeResult.getText();
	}
	
	/*
	 * This method speaks the passed string of text using the voice name.
	 */
	public void dospeak(String speak, String voicename) {	

		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
	    
		speaktext = speak;				// the text to speak
		String voiceName = voicename;	// this is fixed here
		try {
			SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", Locale.US, null, null);
			Synthesizer synthesizer = Central.createSynthesizer(desc);
			synthesizer.allocate();
			synthesizer.resume();
			desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
			
			// find the voice
			Voice[] voices = desc.getVoices();
			Voice voice = null;
			for (int i = 0; i < voices.length; i++) {
				if (voices[i].getName().equals(voiceName)) {
					voice = voices[i];
					break;
				}
			}
			
			synthesizer.getSynthesizerProperties().setVoice(voice);
			System.out.print("Speaking : " + speaktext);
			synthesizer.speakPlainText(speaktext, null);
			synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
			synthesizer.deallocate();
		} catch (Exception e) {
			String message = " missing speech.properties in " + System.getProperty("user.home") + "\n";
			System.out.println("" + e);
			System.out.println(message);
		}
	}

	
}

 
