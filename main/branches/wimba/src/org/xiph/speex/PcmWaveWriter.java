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

import java.io.*;

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
 */
public class PcmWaveWriter
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
    size       = 0;
  }

  /**
   * Closes the output file.
   * MUST be called to have a correct stream. 
   */
  public void close()
    throws IOException 
  {
    /* update the total file length field from RIFF chunk */
    raf.seek(4);
    int fileLength = (int) raf.length() - 8;
    writeInt(fileLength);
    
    /* update the data chunk length size */
    raf.seek(40);
    writeInt(size);
    
    /* close the output file */
    raf.close(); 
  }
  
  /**
   *  Open the output file. 
   *  @param filename filename to open.
   */
  public void open(String filename)
    throws IOException 
  {
    new File(filename).delete(); 
    raf = new RandomAccessFile(filename, "rw");
    size = 0;   
  }
  
  /**
   *  Sets the output format. Must be called before WriteHeader().
   */
  public void setFormat(int channels, int sampleRate)
  {
    this.channels   = channels;
    this.sampleRate = sampleRate;
  }
  
  /**
   *  Writes the initial data chunks that start the wave file. 
   *  Prepares file for data samples to written. 
   */
  public void writeHeader()
    throws IOException
  {
    /* writes the RIFF chunk indicating wave format */
    byte[] chkid = new String("RIFF").getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(0); /* total length must be blank */
    chkid = new String("WAVE").getBytes(); 
    raf.write(chkid, 0, chkid.length);
    
    /* format subchunk: of size 16 */
    chkid = new String("fmt ").getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(16);
    
    short   bits     = 16;
    
    writeShort((short)0x01);
    writeShort((short)channels);
    writeInt(sampleRate);
    writeInt(sampleRate*channels*(bits/8));
    writeShort((short) (channels*(bits/8)));
    writeShort(bits);
    
    /* write the start of data chunk */
    chkid = new String("data").getBytes(); 
    raf.write(chkid, 0, chkid.length);
    writeInt(0);
  }
  
  /**
   *  Writes a packet of audio. 
   *  @param data audio data
   */
  public void writeData(byte[] data, int offset, int len)
    throws IOException 
  {
    raf.write(data, offset, len);
    size+= len;
  }
  
  /**
   * Writes a Little-endian short
   */  
  private void writeShort(short v)
    throws IOException 
  {
    raf.writeByte((0xff & v));
    raf.writeByte((0xff & (v >>> 8)));
  }
  
  /**
   * Writes a Little-endian int
   */
  private void writeInt(int v)
    throws IOException 
  {
    raf.writeByte(0xff & v);
    raf.writeByte(0xff & (v >>>  8));
    raf.writeByte(0xff & (v >>> 16));
    raf.writeByte(0xff & (v >>> 24));
  }
}