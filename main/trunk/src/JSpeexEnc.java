/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: JSpeexEnc.java                                                      *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: 9th April 2003                                                       *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

/* Copyright (C) 2002 Jean-Marc Valin 

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:
   
   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   
   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   
   - Neither the name of the Xiph.org Foundation nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.io.*;
import org.xiph.speex.*;

/**
 * Java Speex Command Line Encoder.
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class JSpeexEnc
{
  public static final String VERSION = "Java Speex Command Line Encoder v0.8 ($Revision$)";
  
  private static int mode       = 0;
  private static int quality    = 8;
  private static int complexity = 3;
  private static int bitrate    = -1;
  private static int nframes    = 1;
  private static int sampleRate = -1;
  private static float vbr_quality = -1;
  private static boolean vbr = false;
  private static boolean vad = false;
  private static boolean dtx = false;
  private static int channels   = 1;
  
  /**
   * Command line entrance:
   * <pre>
   * Usage: JSpeexEnc [options] input.wav output.spx
   * </pre>
   */
  public static void main(String[] args)
    throws IOException
  {
    // make sure we have command args
    if (args.length < 2) {
      if (args.length==1 && (args[0].equalsIgnoreCase("-v") || args[0].equalsIgnoreCase("--version"))) {
        version();
        return;
      }
      usage();
      return;
    }
    String infile = args[args.length-2];
    String outfile = args[args.length-1];
    for (int i=0; i<args.length-2; i++) {
      if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
        usage();
        return;
      }
      else if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("--version")) {
        version();
        return;
      }
      else if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("--narrowband")) {
        mode = 0;
      }
      else if (args[i].equalsIgnoreCase("-w") || args[i].equalsIgnoreCase("--wideband")) {
        mode = 1;
      }
      else if (args[i].equalsIgnoreCase("-u") || args[i].equalsIgnoreCase("--ultra-wideband")) {
        mode = 2;
      }
      else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--quality")) {
        try {
          vbr_quality = Float.parseFloat(args[++i]);
          quality = (int) vbr_quality;
        }
        catch (NumberFormatException e) {
          usage();
          return;
        }
      }
      else if (args[i].equalsIgnoreCase("--complexity")) {
        try {
          complexity = Integer.parseInt(args[++i]);
        }
        catch (NumberFormatException e) {
          usage();
          return;
        }
      }
      else if (args[i].equalsIgnoreCase("--bitrate")) {
        try {
          bitrate = Integer.parseInt(args[++i]);
        }
        catch (NumberFormatException e) {
          usage();
          return;
        }
      }
      else if (args[i].equalsIgnoreCase("--vbr")) {
        vbr = true;
      }
      else if (args[i].equalsIgnoreCase("--vad")) {
        vad = true;
      }
      else if (args[i].equalsIgnoreCase("--dtx")) {
        dtx = true;
      }
      else if (args[i].equalsIgnoreCase("--nframes")) {
        try {
          nframes = Integer.parseInt(args[++i]);
        }
        catch (NumberFormatException e) {
          usage();
          return;
        }
      }
      else if (args[i].equalsIgnoreCase("--stereo")) {
        channels = 2;
      }
      else {
        usage();
        return;
      }
    }
    if (sampleRate < 0) {
      switch(mode){
      case 0:
        sampleRate = 8000;
        break;
      case 1:
        sampleRate = 16000;
        break;
      case 2:
        sampleRate = 32000;
        break;
      }
    }
    // encode to Speex
    encode(infile, outfile);
  }
  
  /**
   * Prints the usage guidelines.
   */
  public static void usage()
  {
    System.out.println("Usage: JSpeexEnc [options] input.wav output.spx");
    System.out.println("Where: input.wav the PCM wav file to use as input");
    System.out.println("       output.spx the Speex file to create");
    System.out.println("Options: -h, --help     This help");
    System.out.println("         -v, --version  Version information");
    System.out.println("         -n             Narrowband (8kHz)");
    System.out.println("         -w             Wideband (16kHz)");
    System.out.println("         -u             Ultra-Wideband (32kHz)");
    System.out.println("         --quality n    Encoding quality (0-10) default 8");
    System.out.println("         --complexity n Encoding complexity (0-10) default 3");
    System.out.println("         --nframes n    Number of frames per Ogg packet, default 1");
    System.out.println("         --vbr          Enable varible bit-rate (VBR)");
    System.out.println("         --vad          Enable voice activity detection (VAD)");
    System.out.println("         --dtx          Enable file based discontinuous transmission (DTX)");
    System.out.println("         --stereo       Consider input as stereo");
  }

  /**
   * Prints the version.
   */
  public static void version()
  {
    System.out.println(VERSION);
    System.out.println("using " + SpeexEncoder.VERSION);
    System.out.println("Copyright (C) 2002-2003 Wimba S.A.");
  }
  
  /**
   * Encodes a wave file to speex. 
   */
  public static void encode(String inputPath, String outputPath)
    throws IOException
  {
    byte[] temp    = new byte[2048];
    final int HEADERSIZE = 8;
    final String RIFF      = "RIFF";
    final String WAVE      = "WAVE";
    final String DATA      = "data";
    // open the input stream
    DataInputStream dis = new DataInputStream(new FileInputStream(inputPath));
    // construct a new decoder
    SpeexEncoder speexEncoder = new SpeexEncoder();
    speexEncoder.init(mode, quality, sampleRate, channels);
    if (complexity > 0) {
      speexEncoder.getEncoder().setComplexity(complexity);
    }
    if (bitrate > 0) {
      speexEncoder.getEncoder().setBitRate(bitrate);
    }
    if (vbr) {
      speexEncoder.getEncoder().setVbr(vbr);
      if (vbr_quality > 0) {
        speexEncoder.getEncoder().setVbrQuality(vbr_quality);
      }
    }
    if (vad) {
      speexEncoder.getEncoder().setVad(vad);
    }
    if (dtx) {
      speexEncoder.getEncoder().setDtx(dtx);
    }

    // read the WAVE header
    dis.readFully(temp, 0, HEADERSIZE+4);
    // make sure its a WAVE header
    if (!RIFF.equals(new String(temp, 0, 4)) &&
        !WAVE.equals(new String(temp, 8, 4))) {
      System.err.println("Not a WAVE file");
      return;
    }
    else {
//      System.out.println("Wave file size: " + readInt(temp, 4));
    }
    // Read other header chunks
    dis.readFully(temp, 0, HEADERSIZE);
    String chunk = new String(temp, 0, 4);
    int size = readInt(temp, 4);
    while (!chunk.equals(DATA)) {
      //      System.out.println(chunk + " chunk, size: " + size);
      dis.readFully(temp, 0, size);
      dis.readFully(temp, 0, HEADERSIZE);
      chunk = new String(temp, 0, 4);
      size = readInt(temp, 4);
    }
//    System.out.println("data size: " + size);
    
    OggSpeexWriter oggWriter = new OggSpeexWriter();
    oggWriter.setFormat(mode, sampleRate, channels, nframes);
    oggWriter.open(outputPath);
    oggWriter.writeHeader("Encoded with: " + VERSION);
    int pcmPacketSize = 2 * channels * speexEncoder.getFrameSize();
    try {
      // read until we get to EOF
      while (true) {
        dis.readFully(temp, 0, nframes*pcmPacketSize);
        for (int i=0; i<nframes; i++)
          speexEncoder.processData(temp, i*pcmPacketSize, pcmPacketSize);
        int encsize = speexEncoder.getProcessedData(temp, 0);
        if (encsize > 0) {
          oggWriter.writePacket(temp, 0, encsize);
        }
      }
    }
    catch (EOFException e) {}
    oggWriter.close(); 
  }
  
  /**
   * Converts Little Endian (Windows) bytes to an int (Java uses Big Endian).
   */
  private static int readInt(byte[] data, int offset)
  {
    return (data[offset] & 0xff) | ((data[offset+1] & 0xff) << 8) | ((data[offset+2] & 0xff) << 16) | (data[offset+3] << 24); // no 0xff on the last one to keep the sign
  }
}
