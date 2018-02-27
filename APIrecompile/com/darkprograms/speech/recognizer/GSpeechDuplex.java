package com.darkprograms.speech.recognizer;

import com.darkprograms.speech.util.StringUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import net.sourceforge.javaflacencoder.FLACFileWriter;

public class GSpeechDuplex
{
  private static final long MIN = 10000000L;
  private static final long MAX = 900000009999999L;
  private static final String GOOGLE_DUPLEX_SPEECH_BASE = "https://www.google.com/speech-api/full-duplex/v1/";
  private List<GSpeechResponseListener> responseListeners = new ArrayList();
  private final String API_KEY;
  private String language = "en-US";
  private static final int MAX_SIZE = 1048576;
  private static final byte[] FINAL_CHUNK = { 48, 13, 10, 13, 10 };

  public GSpeechDuplex(String paramString)
  {
    this.API_KEY = paramString;
  }

  public String getLanguage()
  {
    return this.language;
  }

  public void setLanguage(String paramString)
  {
    this.language = paramString;
  }

  public void recognize(File paramFile, int paramInt)
    throws IOException
  {
    recognize(mapFileIn(paramFile), paramInt);
  }

  public void recognize(byte[] paramArrayOfByte, int paramInt)
  {
    if (paramArrayOfByte.length >= 1048576)
    {
      byte[][] arrayOfByte1 = chunkAudio(paramArrayOfByte);
      byte[][] arrayOfByte2;
      int i = (arrayOfByte2 = arrayOfByte1).length;
      for (int j = 0; j < i; j++)
      {
        byte[] arrayOfByte = arrayOfByte2[j];
        recognize(arrayOfByte, paramInt);
      }
    }
    long l = 10000000L + (long)(Math.random() * 9.0E14D);

    String str1 = "https://www.google.com/speech-api/full-duplex/v1/down?maxresults=1&pair=" + l;

    String str2 = "https://www.google.com/speech-api/full-duplex/v1/up?lang=" + this.language + "&lm=dictation&client=chromium&pair=" + l + "&key=" + this.API_KEY;

    downChannel(str1);

    upChannel(str2, chunkAudio(paramArrayOfByte), paramInt);
  }

  public void recognize(TargetDataLine paramTargetDataLine, AudioFormat paramAudioFormat)
    throws IOException, LineUnavailableException
  {
    long l = 10000000L + (long)(Math.random() * 9.0E14D);

    String str1 = "https://www.google.com/speech-api/full-duplex/v1/down?maxresults=1&pair=" + l;

    String str2 = "https://www.google.com/speech-api/full-duplex/v1/up?lang=" + this.language + "&lm=dictation&client=chromium&pair=" + l + "&key=" + this.API_KEY + "&continuous=true&interim=true";

    downChannel(str1);

    upChannel(str2, paramTargetDataLine, paramAudioFormat);
  }

  private void downChannel(String paramString)
  {
    new Thread("Downstream Thread")
    {
      public void run()
      {
        Scanner localScanner = GSpeechDuplex.this.openHttpsConnection(paramString);
        if (localScanner == null)
        {
          System.out.println("Error has occured"); return;
        }
        String str;
        while ((localScanner.hasNext()) && ((str = localScanner.nextLine()) != null)) {
          if (str.length() > 17)
          {
            GoogleResponse localGoogleResponse = new GoogleResponse();
            GSpeechDuplex.this.parseResponse(str, localGoogleResponse);
            GSpeechDuplex.this.fireResponseEvent(localGoogleResponse);
          }
        }
        localScanner.close();
        System.out.println("Finished write on down stream...");
      }
    }.start();
  }

  private void upChannel(String paramString, byte[][] paramArrayOfByte, int paramInt)
  {
    final String str = paramString;
    final byte[][] arrayOfByte = paramArrayOfByte;
    final int i = paramInt;
    new Thread("Upstream File Thread")
    {
      public void run()
      {
        GSpeechDuplex.this.openHttpsPostConnection(str, arrayOfByte, i);
      }
    }.start();
  }

  private void upChannel(String paramString, TargetDataLine paramTargetDataLine, AudioFormat paramAudioFormat)
    throws IOException, LineUnavailableException
  {
    final String str = paramString;
    final TargetDataLine localTargetDataLine = paramTargetDataLine;
    final AudioFormat localAudioFormat = paramAudioFormat;
    if (!localTargetDataLine.isOpen())
    {
      localTargetDataLine.open(localAudioFormat);
      localTargetDataLine.start();
    }
    new Thread("Upstream Thread")
    {
      public void run()
      {
        GSpeechDuplex.this.openHttpsPostConnection(str, localTargetDataLine, (int)localAudioFormat.getSampleRate());
      }
    }.start();
  }

  private Scanner openHttpsConnection(String paramString)
  {
    int i = -1;
    try
    {
      URL localURL = new URL(paramString);
      URLConnection localURLConnection = localURL.openConnection();
      if (!(localURLConnection instanceof HttpsURLConnection)) {
        throw new IOException("URL is not an Https URL");
      }
      HttpsURLConnection localHttpsURLConnection = (HttpsURLConnection)localURLConnection;
      localHttpsURLConnection.setAllowUserInteraction(false);

      localHttpsURLConnection.setInstanceFollowRedirects(true);
      localHttpsURLConnection.setRequestMethod("GET");
      localHttpsURLConnection.connect();
      i = localHttpsURLConnection.getResponseCode();
      if (i == 200) {
        return new Scanner(localHttpsURLConnection.getInputStream(), "UTF-8");
      }
      System.out.println("Error: " + i);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localMalformedURLException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  private void openHttpsPostConnection(String paramString, TargetDataLine paramTargetDataLine, int paramInt)
  {
    try
    {
      URL localURL = new URL(paramString);
      URLConnection localURLConnection = localURL.openConnection();
      if (!(localURLConnection instanceof HttpsURLConnection)) {
        throw new IOException("URL is not an Https URL");
      }
      HttpsURLConnection localHttpsURLConnection = (HttpsURLConnection)localURLConnection;
      localHttpsURLConnection.setAllowUserInteraction(false);
      localHttpsURLConnection.setInstanceFollowRedirects(true);
      localHttpsURLConnection.setRequestMethod("POST");
      localHttpsURLConnection.setDoOutput(true);
      localHttpsURLConnection.setChunkedStreamingMode(0);
      localHttpsURLConnection.setRequestProperty("Transfer-Encoding", "chunked");
      localHttpsURLConnection.setRequestProperty("Content-Type", "audio/x-flac; rate=" + paramInt);

      localHttpsURLConnection.connect();

      OutputStream localOutputStream = localHttpsURLConnection.getOutputStream();
      System.out.println("Starting to write data to output...");
      AudioInputStream localAudioInputStream = new AudioInputStream(paramTargetDataLine);
      AudioSystem.write(localAudioInputStream, FLACFileWriter.FLAC, localOutputStream);

      System.out.println("Upstream Closed...");
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  private Scanner openHttpsPostConnection(String paramString, byte[][] paramArrayOfByte, int paramInt)
  {
    byte[][] arrayOfByte1 = paramArrayOfByte;
    int i = -1;
    OutputStream localOutputStream = null;
    try
    {
      URL localURL = new URL(paramString);
      URLConnection localURLConnection = localURL.openConnection();
      if (!(localURLConnection instanceof HttpsURLConnection)) {
        throw new IOException("URL is not an Https URL");
      }
      HttpsURLConnection localHttpsURLConnection = (HttpsURLConnection)localURLConnection;
      localHttpsURLConnection.setAllowUserInteraction(false);
      localHttpsURLConnection.setInstanceFollowRedirects(true);
      localHttpsURLConnection.setRequestMethod("POST");
      localHttpsURLConnection.setDoOutput(true);
      localHttpsURLConnection.setChunkedStreamingMode(0);
      localHttpsURLConnection.setRequestProperty("Transfer-Encoding", "chunked");
      localHttpsURLConnection.setRequestProperty("Content-Type", "audio/x-flac; rate=" + paramInt);

      localHttpsURLConnection.connect();

      localOutputStream = localHttpsURLConnection.getOutputStream();

      System.out.println("Starting to write");
      byte[][] arrayOfByte2;
      int j = (arrayOfByte2 = arrayOfByte1).length;
      for (int k = 0; k < j; k++)
      {
        byte[] arrayOfByte = arrayOfByte2[k];
        localOutputStream.write(arrayOfByte);
      }
      localOutputStream.write(FINAL_CHUNK);
      System.out.println("IO WRITE DONE");

      i = localHttpsURLConnection.getResponseCode();
      if (i / 100 != 2) {
        System.out.println("ERROR");
      }
      if (i == 200) {
        return new Scanner(localHttpsURLConnection.getInputStream(), "UTF-8");
      }
      System.out.println("HELP: " + i);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localMalformedURLException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  private byte[] mapFileIn(File paramFile)
    throws IOException
  {
    return Files.readAllBytes(paramFile.toPath());
  }

  private void parseResponse(String paramString, GoogleResponse paramGoogleResponse)
  {
    if ((paramString == null) || (!paramString.contains("\"result\"")) || (paramString.equals("{\"result\":[]}"))) {
      return;
    }

    paramGoogleResponse.getOtherPossibleResponses().clear();

    if (paramString.contains("\"confidence\":")) {
      paramGoogleResponse.setConfidence(StringUtil.substringBetween(paramString, "\"confidence\":", "}"));
    } else {
      paramGoogleResponse.setConfidence(String.valueOf(1));
    }
    paramGoogleResponse.setResponse(paramString);
  }

  public synchronized void addResponseListener(GSpeechResponseListener paramGSpeechResponseListener)
  {
    this.responseListeners.add(paramGSpeechResponseListener);
  }

  public synchronized void removeResponseListener(GSpeechResponseListener paramGSpeechResponseListener)
  {
    this.responseListeners.remove(paramGSpeechResponseListener);
  }

  private synchronized void fireResponseEvent(GoogleResponse paramGoogleResponse)
  {
    for (GSpeechResponseListener localGSpeechResponseListener : this.responseListeners) {
      localGSpeechResponseListener.onResponse(paramGoogleResponse);
    }
  }

  private byte[][] chunkAudio(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length >= 1048576)
    {
      int i = 524288;
      int j = paramArrayOfByte.length / i + 1;
      byte[][] arrayOfByte2 = new byte[j][];
      int k = 0;
      for (int m = 0; (k < paramArrayOfByte.length) && (m < arrayOfByte2.length); m++)
      {
        int n = paramArrayOfByte.length - k < i ? paramArrayOfByte.length - k : i;
        arrayOfByte2[m] = new byte[n];
        System.arraycopy(paramArrayOfByte, k, arrayOfByte2[m], 0, n);k += i;
      }
      return arrayOfByte2;
    }
    byte[][] arrayOfByte1 = new byte[1][paramArrayOfByte.length];
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte1[0], 0, paramArrayOfByte.length);
    return arrayOfByte1;
  }
}
