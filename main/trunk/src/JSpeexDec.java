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
 * Class: JSpeexDec.java                                                      *
 *                                                                            *
 * Author: James LAWRENCE                                                     *
 * Modified by: Marc GIMPEL                                                   *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: March 2003                                                           *
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
import java.util.*;
import org.xiph.speex.*;

/**
 * Java Speex Command Line Encoder.
 * 
 * Decodes SPX files created by Speex's speexenc utility to WAV entirely in pure java.
 * Currently this code has been updated to be compatible with release 1.0.1.
 *
 * NOTE!!! A number of advanced options are NOT supported. 
 * 
 * --  DTX implemented but untested.
 * --  Packet loss support implemented but untested.
 * --  SPX files with more than one comment. 
 * --  Can't force decoder to run at another rate, mode, or channel count. 
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class JSpeexDec
{
  public static final String VERSION = "Java Speex Command Line Decoder v0.8.3 ($Revision$)";

  private static Random random = new Random();
  private static PcmWaveWriter waveWriter;
  private static SpeexDecoder  speexDecoder;

  private static boolean ogg       = true;
  private static boolean enhanced  = true;
  private static int mode          = 0;
  private static int quality       = 8;
  private static int nframes       = 1;
  private static int sampleRate    = -1;
  private static float vbr_quality = -1;
  private static boolean vbr       = false;
  private static int channels      = 1;
  private static int loss          = 0;

  /**
   * Command line entrance:
   * <pre>
   * Usage: JSpeexDec [options] input.spx output.wav
   * </pre>
   */
  public static void main(String[] args)
    throws IOException
  {
    // make sure we have command args
    if (args.length < 2) {
      if (args.length==1 && (args[0].equals("-v") || args[0].equals("--version"))) {
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
      else if (args[i].equalsIgnoreCase("--enh")) {
        enhanced = true;
      }
      else if (args[i].equalsIgnoreCase("--no-enh")) {
        enhanced = false;
      }
      else if (args[i].equalsIgnoreCase("--packet-loss")) {
        try {
          loss = Integer.parseInt(args[++i]);
          }
        catch (NumberFormatException e) {
          usage();
          return;
        }
      }
      else if (args[i].equalsIgnoreCase("--raw")) {
        ogg = false;
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
      else if (args[i].equalsIgnoreCase("--vbr")) {
        vbr = true;
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
    // decode the spx
    decode(infile, outfile);
  }
  
  /**
   * Prints the usage guidelines
   */
  public static void usage()
  {
    System.out.println("Usage: JSpeexDec [options] input.spx output.wav");
    System.out.println("Where: input.spx the Speex file to use as input");
		System.out.println("       output.wav the PCM wave file to create");
		System.out.println("Options: -h, --help     This help");
		System.out.println("         -v, --version    Version information");
		System.out.println("         --enh            Enable perceptual enhancement (default)");
		System.out.println("         --no-enh         Disable perceptual enhancement");
		System.out.println("         --packet-loss n  Simulate n % random packet loss");
		System.out.println("         if the input file is raw Speex (not Ogg Speex)");
		System.out.println("         --raw            Input file is raw Speex");
		System.out.println("         -n               Narrowband (8kHz)");
		System.out.println("         -w               Wideband (16kHz)");
		System.out.println("         -u               Ultra-Wideband (32kHz)");
		System.out.println("         --quality n      Encoding quality (0-10) default 8");
		System.out.println("         --nframes n      Number of frames per Ogg packet, default 1");
		System.out.println("         --vbr            Enable varible bit-rate (VBR)");
		System.out.println("         --stereo         Consider input as stereo");
  }
  
  /**
   * Prints the version
   */
  public static void version()
  {
		System.out.println(VERSION);
		System.out.println("using " + SpeexDecoder.VERSION);
		System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
  }

  /**
	 * Decodes a spx file to wave. 
	 */
	public static void decode(String inputPath, String outputPath)
    throws IOException
  {
		byte[] header    = new byte[2048];
		byte[] payload   = new byte[65536];
		byte[] decdat    = new byte[44100*2*2];
		final int    HEADERSIZE = 27;
		final int    SEGOFFSET  = 26;
		final String OGGID      = "OggS";
		int segments=0, curseg=0, bodybytes=0, decsize=0; 
		int packetNo=0;
    
    // construct a wave writer
    waveWriter   = new PcmWaveWriter();
    // construct a new decoder
    speexDecoder = new SpeexDecoder();
    // open the input stream
    DataInputStream dis =  new DataInputStream(new FileInputStream(inputPath));

    int origchksum;
    int chksum;
		try {
      // read until we get to EOF
      while(true) {
        if (ogg) {
          // read the OGG header
          dis.readFully(header, 0, HEADERSIZE);
          origchksum = bytestoint(header, 22);
          header[22] = 0;
          header[23] = 0;
          header[24] = 0;
          header[25] = 0;
          chksum=OggCrc.checksum(0, header, 0, HEADERSIZE);

          // make sure its a OGG header
          if (!OGGID.equals(new String(header, 0, 4))) {
            System.err.println("missing ogg id!");
            return;
          }
          
          /* how many segments are there? */
          segments = header[SEGOFFSET] & 0xFF;
          dis.readFully(header, HEADERSIZE, segments);
          chksum=OggCrc.checksum(chksum, header, HEADERSIZE, segments);
          
          /* decode each segment, writing output to wav */
          for (curseg=0; curseg < segments; curseg++) {
            /* get the number of bytes in the segment */
            bodybytes = header[HEADERSIZE+curseg] & 0xFF;
            if (bodybytes==255) {
              System.err.println("sorry, don't handle 255 sizes!"); 
              return;
            }
            dis.readFully(payload, 0, bodybytes);
            chksum=OggCrc.checksum(chksum, payload, 0, bodybytes);
            
            /* decode the segment */
            /* if first packet, read the Speex header */
            if (packetNo == 0) {
              if (readSpeexHeader(payload, 0, bodybytes)) {
                /* once Speex header read, initialize the wave writer with output format */
                waveWriter.open(outputPath);  
                waveWriter.setFormat(speexDecoder.getChannels(),
                                     speexDecoder.getSampleRate());
                waveWriter.writeHeader();
                packetNo++;
              }
              else {
                packetNo = 0;
              }
            }
            else if (packetNo == 1) { // Ogg Comment packet
                packetNo++;
            }
            else {
              if (loss>0 && random.nextInt(100)<loss) {
                speexDecoder.processData(null, 0, bodybytes);
                for (int i=1; i<nframes; i++) {
                  speexDecoder.processData(true);
                }
              }
              else {
                speexDecoder.processData(payload, 0, bodybytes);
                for (int i=1; i<nframes; i++) {
                  speexDecoder.processData(false);
                }
              }
              /* get the amount of decoded data */
              if ((decsize=speexDecoder.getProcessedData(decdat, 0)) > 0) {
                waveWriter.writeData(decdat, 0, decsize);
              }
              packetNo++;
            }
          }
          if (chksum != origchksum)
            throw new IOException("Ogg CheckSums do not match");
        }
        else  { // Raw Speex
          /* if first packet, initialise everything */
          if (packetNo == 0) {
            /* initialize the Speex decoder */
            speexDecoder.init(mode, sampleRate, channels, enhanced);
            /* initialize the wave writer with output format */
            waveWriter.open(outputPath);
            waveWriter.setFormat(channels, sampleRate);
            waveWriter.writeHeader();
            if (!vbr) {
              switch (mode) {
                case 0:
                  bodybytes = NbEncoder.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[quality]];
                  break;
                case 1:
                  bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                  bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                  break;
                case 2:
                  bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                  bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                  bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[quality]];
                  break;
              }
              bodybytes = (bodybytes + 7) >> 3;
            }
            else {
              // We have read the stream to find out more
              bodybytes = 0;
            }
            packetNo++;
          }
          else {
            dis.readFully(payload, 0, bodybytes);
            if (loss>0 && random.nextInt(100)<loss) {
              speexDecoder.processData(null, 0, bodybytes);
              for (int i=1; i<nframes; i++) {
                speexDecoder.processData(true);
              }
            }
            else {
              speexDecoder.processData(payload, 0, bodybytes);
              for (int i=1; i<nframes; i++) {
                speexDecoder.processData(false);
              }
            }
            /* get the amount of decoded data */
            if ((decsize=speexDecoder.getProcessedData(decdat, 0)) > 0) {
              waveWriter.writeData(decdat, 0, decsize);
            }
            packetNo++;
          }
        }
      }
		}
    catch (EOFException eof) {}
		/* close the output file */
		waveWriter.close();
	}

  /**
   * Reads the header packet.
   * 
   * <pre>
   *  0 -  7: speex_string: "Speex   "
   *  8 - 27: speex_version: "speex-1.0"
   * 28 - 31: speex_version_id: 1
   * 32 - 35: header_size: 80
   * 36 - 39: rate
   * 40 - 43: mode: 0=narrowband, 1=wb, 2=uwb
   * 44 - 47: mode_bitstream_version: 4
   * 48 - 51: nb_channels
   * 52 - 55: bitrate: -1
   * 56 - 59: frame_size: 160
   * 60 - 63: vbr
   * 64 - 67: frames_per_packet
   * 68 - 71: extra_headers: 0
   * 72 - 75: reserved1
   * 76 - 79: reserved2
   * </pre>
   */
  private static boolean readSpeexHeader(byte[] packet, int offset, int bytes)
  {
    if (bytes!=80) {
      return false;
    }
    if (!"Speex   ".equals(new String(packet, 0, 8))) {
      return false;
    }
    mode       = packet[40+offset] & 0xFF;
    sampleRate = bytestoint(packet, offset+36);
    channels   = bytestoint(packet, offset+48);
    nframes    = bytestoint(packet, offset+64);
    return speexDecoder.init(mode, sampleRate, channels, enhanced);
  }

  /**
   * Converts the bytes from the given array to an integer
   * @param a - the array
   * @param i - the offset
   */
  private static int bytestoint(byte[] a, int i)
  {
    return ((a[i+3] & 0xFF) << 24) | ((a[i+2] & 0xFF) << 16) | ((a[i+1] & 0xFF) << 8) | (a[i] & 0xFF);
  }
}
