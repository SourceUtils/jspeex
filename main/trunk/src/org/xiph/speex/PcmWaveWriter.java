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
 * Class: PcmWaveWriter.java                                                  *
 *                                                                            *
 * Author: James LAWRENCE                                                     *
 * Modified by: Marc GIMPEL                                                   *
 *                                                                            *
 * Date: March 2003                                                           *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Writes basic PCM wave files from binary audio data.
 *
 * <p>Here's an example that writes 2 seconds of silence
 * <pre>
 * PcmWaveWriter s_wsw = new PcmWaveWriter();
 * byte[] silence = new byte[16*2*44100];
 * wsw.SetFormat(2, 44100);
 * wsw.Open("C:\\out.wav");
 * wsw.WriteHeader(); 
 * wsw.WriteData(silence, 0, silence.length);
 * wsw.WriteData(silence, 0, silence.length);
 * wsw.Close(); 
 * </pre>
 *
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class PcmWaveWriter
  extends AudioFileWriter
{
  /**
   * Table describing the number of frames per packet in a Speex Wave file,
   * depending on its mode-1 (1=NB, 2=WB, 3=UWB), channels-1 (1=mono, 2=stereo)
   * and the quality setting (0 to 10).
   * See end of file for exerpt from SpeexACM code for more explanations.
   */
  public static final int[][][] WAVE_FRAME_SIZES = new int[3][2][11]
    { { {8, 8, 8, 1, 1, 2, 2, 2, 2, 2, 2},     // NB mono
        {2, 1, 1, 7, 7, 8, 8, 8, 8, 3, 3} },   // NB stereo
      { {8, 8, 8, 2, 1, 1, 2, 2, 2, 2, 2},     // WB mono
        {1, 2, 2, 8, 7, 6, 3, 3, 3, 3, 3} },   // WB stereo
      { {8, 8, 8, 1, 2, 2, 1, 1, 1, 1, 1},     // UWB mono
        {2, 1, 1, 7, 8, 3, 6, 6, 5, 5, 5} } }; // UWB stereo

  private RandomAccessFile raf; 
  private int channels;
  private int sampleRate;
  private int size;
  
  /**
   * Constructor. 
   */
  public PcmWaveWriter()
  {
    size = 0;
  }

  /**
   * Constructor. 
   * @param channels
   * @param sampleRate
   */
  public PcmWaveWriter(int channels, int sampleRate)
  {
    this();
    setFormat(channels, sampleRate);
  }

  /**
   * Sets the output format. Must be called before WriteHeader().
   * @param channels
   * @param sampleRate
   */
  private void setFormat(int channels, int sampleRate)
  {
    this.channels   = channels;
    this.sampleRate = sampleRate;
  }

  /**
   * Closes the output file.
   * MUST be called to have a correct stream. 
   * @exception IOException
   */
  public void close()
    throws IOException 
  {
    /* update the total file length field from RIFF chunk */
    raf.seek(4);
    int fileLength = (int) raf.length() - 8;
    writeInt(raf, fileLength);
    
    /* update the data chunk length size */
    raf.seek(40);
    writeInt(raf, size);
    
    /* close the output file */
    raf.close(); 
  }
  
  /**
   * Open the output file. 
   * @param filename filename to open.
   * @exception IOException
   */
  public void open(String filename)
    throws IOException 
  {
    new File(filename).delete(); 
    raf = new RandomAccessFile(filename, "rw");
    size = 0;   
  }
    
  /**
   * Writes the initial data chunks that start the wave file. 
   * Prepares file for data samples to written.
   * @param comment ignored by the WAV header.
   * @exception IOException
   */
  public void writeHeader(String comment)
    throws IOException
  {
    /* writes the RIFF chunk indicating wave format */
    byte[] chkid = "RIFF".getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(raf, 0); /* total length must be blank */
    chkid = "WAVE".getBytes(); 
    raf.write(chkid, 0, chkid.length);
    
    /* format subchunk: of size 16 */
    chkid = "fmt ".getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(raf, 16); // Size of format chunk
    
    short   bits     = 16;
    
    writeShort(raf, (short)0x01);                 // Format type PCM
    writeShort(raf, (short)channels);             // Number of channels
    writeInt(raf, sampleRate);                    // Sampling frequency
    writeInt(raf, sampleRate*channels*(bits/8));  // Average bytes per second
    writeShort(raf, (short) (channels*(bits/8))); // Blocksize of data
    writeShort(raf, bits);                        // Bits per sample
    
    /* write the start of data chunk */
    chkid = "data".getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(raf, 0);
  }
  
  /**
   * Writes a packet of audio. 
   * @param data audio data
   * @param offset the offset from which to start reading the data.
   * @param len the length of data to read.
   * @exception IOException
   */
  public void writePacket(byte[] data, int offset, int len)
    throws IOException 
  {
    raf.write(data, offset, len);
    size+= len;
  }
}
// The following is taken from the SpeexACM 1.0.1.1 Source code (codec.c file).

//
//	This array describes how many bits are required	by an encoded audio	frame.
//	It also	specifies the optimal framesperblock parameter to minimize
//	padding	loss. It also lists	the	effective bitrate (considering padding).
//
//	The	array indices are rate,	channels, quality (each	as a 0 based index)
//
/*
struct tagQualityInfo {
	UINT nBitsPerFrame;
	UINT nFrameSize;
	UINT nFramesPerBlock;
	UINT nEffectiveBitrate;
} QualityInfo[3][2][11]	= {
	   43, 160,	8,	2150,	//	8000 1 0
	   79, 160,	8,	3950,	//	8000 1 1
	  119, 160,	8,	5950,	//	8000 1 2
	  160, 160,	1,	8000,	//	8000 1 3
	  160, 160,	1,	8000,	//	8000 1 4
	  220, 160,	2, 11000,	//	8000 1 5
	  220, 160,	2, 11000,	//	8000 1 6
	  300, 160,	2, 15000,	//	8000 1 7
	  300, 160,	2, 15000,	//	8000 1 8
	  364, 160,	2, 18200,	//	8000 1 9
	  492, 160,	2, 24600,	//	8000 1 10
	   60, 160,	2,	3000,	//	8000 2 0
	   96, 160,	1,	4800,	//	8000 2 1
	  136, 160,	1,	6800,	//	8000 2 2
	  177, 160,	7,	8857,	//	8000 2 3
	  177, 160,	7,	8857,	//	8000 2 4
	  237, 160,	8, 11850,	//	8000 2 5
	  237, 160,	8, 11850,	//	8000 2 6
	  317, 160,	8, 15850,	//	8000 2 7
	  317, 160,	8, 15850,	//	8000 2 8
	  381, 160,	3, 19066,	//	8000 2 9
	  509, 160,	3, 25466,	//	8000 2 10
	   79, 320,	8,	3950,	// 16000 1 0
	  115, 320,	8,	5750,	// 16000 1 1
	  155, 320,	8,	7750,	// 16000 1 2
	  196, 320,	2,	9800,	// 16000 1 3
	  256, 320,	1, 12800,	// 16000 1 4
	  336, 320,	1, 16800,	// 16000 1 5
	  412, 320,	2, 20600,	// 16000 1 6
	  476, 320,	2, 23800,	// 16000 1 7
	  556, 320,	2, 27800,	// 16000 1 8
	  684, 320,	2, 34200,	// 16000 1 9
	  844, 320,	2, 42200,	// 16000 1 10
	   96, 320,	1,	4800,	// 16000 2 0
	  132, 320,	2,	6600,	// 16000 2 1
	  172, 320,	2,	8600,	// 16000 2 2
	  213, 320,	8, 10650,	// 16000 2 3
	  273, 320,	7, 13657,	// 16000 2 4
	  353, 320,	6, 17666,	// 16000 2 5
	  429, 320,	3, 21466,	// 16000 2 6
	  493, 320,	3, 24666,	// 16000 2 7
	  573, 320,	3, 28666,	// 16000 2 8
	  701, 320,	3, 35066,	// 16000 2 9
	  861, 320,	3, 43066,	// 16000 2 10
	   83, 640,	8,	4150,	// 32000 1 0
	  151, 640,	8,	7550,	// 32000 1 1
	  191, 640,	8,	9550,	// 32000 1 2
	  232, 640,	1, 11600,	// 32000 1 3
	  292, 640,	2, 14600,	// 32000 1 4
	  372, 640,	2, 18600,	// 32000 1 5
	  448, 640,	1, 22400,	// 32000 1 6
	  512, 640,	1, 25600,	// 32000 1 7
	  592, 640,	1, 29600,	// 32000 1 8
	  720, 640,	1, 36000,	// 32000 1 9
	  880, 640,	1, 44000,	// 32000 1 10
	  100, 640,	2,	5000,	// 32000 2 0
	  168, 640,	1,	8400,	// 32000 2 1
	  208, 640,	1, 10400,	// 32000 2 2
	  249, 640,	7, 12457,	// 32000 2 3
	  309, 640,	8, 15450,	// 32000 2 4
	  389, 640,	3, 19466,	// 32000 2 5
	  465, 640,	6, 23266,	// 32000 2 6
	  529, 640,	6, 26466,	// 32000 2 7
	  609, 640,	5, 30480,	// 32000 2 8
	  737, 640,	5, 36880,	// 32000 2 9
	  897, 640,	5, 44880,	// 32000 2 10
};
*/