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
import org.xiph.speex.*;

/**
 * Java Speex Command Line Encoder.
 * 
 * Decodes SPX files created by Speex's speexenc utility to WAV entirely in pure java.  
 * Currently this code is based off of Release Candidate #2. 
 *
 * NOTE!!! A number of advanced options are NOT supported. 
 * 
 * --  DTX in any shape or form. 
 * --  SPX files with more than one comment. 
 * --  Perceptual Enhancement is always on. 
 * --  Can't force decoder to run at another rate, mode, or channel count. 
 * --  No packet loss support. 
 * 
 * @author Jim Lawrence, helloNetwork.com
 */
public class JSpeexDec
{
  public static final String VERSION = "Java Speex Command Line Decoder v0.7 ($Revision$)";

  /**
   * Main
   * Command line entrance
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
    // decode the spx
    decode(args[0], args[1]);
  }
  
  /**
   * Prints the usage guidelines
   */
  public static void usage()
  {
    System.out.println("Usage: JSpeexDec input.spx output.wav");
    System.out.println("Where: input.spx the Speex file to use as input");
		System.out.println("       output.wav the PCM wave file to create");
  }
  
  /**
   * Prints the version
   */
  public static void version()
  {
		System.out.println(VERSION);
		System.out.println("using " + SpeexDecoder.VERSION);
		System.out.println("Copyright (C) 2002-2003 Wimba S.A.");
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
		int segments=0, curseg=0, i=0, bodybytes=0, decsize=0; 
		int packetCount = 0;
    
    // construct a wave writer
    PcmWaveWriter waveWriter   = new PcmWaveWriter();
    // construct a new decoder
    SpeexDecoder  speexDecoder = new SpeexDecoder();
    // open the input stream
    DataInputStream dis =  new DataInputStream(new FileInputStream(inputPath));

    int origchksum;
    int chksum;
		try {
      // read until we get to EOF
      while(true) {
        // read the OGG header
        dis.readFully(header, 0, HEADERSIZE);
        origchksum = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) | ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
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
          speexDecoder.processData(payload, 0, bodybytes);
          
          /* if first packet, initialize the wave writer with output format */
          if (packetCount==0) {
            waveWriter.open(outputPath);  
            waveWriter.setFormat(speexDecoder.getChannels(),
                                 speexDecoder.getSampleRate());
            waveWriter.writeHeader();                     
            packetCount++;
          }
          
          /* get the amount of decoded data */
          if ((decsize=speexDecoder.getProcessedData(decdat, 0)) > 0) {
            waveWriter.writeData(decdat, 0, decsize);
          }
          
          packetCount++;
        }
        if (chksum != origchksum)
          throw new IOException("Ogg CheckSums do not match");
      }
		}
    catch (EOFException eof) {}
		/* close the output file */
		waveWriter.close();
	}
}
