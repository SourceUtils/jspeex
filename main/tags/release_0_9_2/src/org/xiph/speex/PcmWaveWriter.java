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
