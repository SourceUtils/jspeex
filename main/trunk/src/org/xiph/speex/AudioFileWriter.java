/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2004 Wimba S.A., All Rights Reserved.                   *
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
 * Class: RawWriter.java                                                      *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 6th January 2004                                                     *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract Class that defines an Audio File Writer.
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public abstract class AudioFileWriter
{
  /**
   * Closes the output file.
   * @exception IOException
   */
  public abstract void close()
    throws IOException;
  
  /**
   * Open the output file. 
   * @param filename - file to open.
   * @exception IOException
   */
  public abstract void open(String filename)
    throws IOException;

  /**
   * Writes the header pages that start the Ogg Speex file. 
   * Prepares file for data to be written.
   * @param comment description to be included in the header.
   * @exception IOException
   */
  public abstract void writeHeader(String comment)
    throws IOException;
  
  /**
   * Writes a packet of audio. 
   * @param data audio data
   * @param offset the offset from which to start reading the data.
   * @param len the length of data to read.
   * @exception IOException
   */
  public abstract void writePacket(byte[] data, int offset, int len)
    throws IOException;

  /**
   * Writes a Little-endian short.
   * @param out the data output to write to.
   * @param v value to write.
   * @exception IOException
   */  
  protected static void writeShort(DataOutput out, short v)
    throws IOException 
  {
    out.writeByte((0xff & v));
    out.writeByte((0xff & (v >>> 8)));
  }
  
  /**
   * Writes a Little-endian int.
   * @param out the data output to write to.
   * @param v value to write.
   * @exception IOException
   */
  protected static void writeInt(DataOutput out, int v)
    throws IOException 
  {
    out.writeByte(0xff & v);
    out.writeByte(0xff & (v >>>  8));
    out.writeByte(0xff & (v >>> 16));
    out.writeByte(0xff & (v >>> 24));
  }

  /**
   * Writes a Little-endian short.
   * @param os - the output stream to write to.
   * @param v - the value to write.
   * @exception IOException
   */
  protected static void writeShort(OutputStream os, short v)
    throws IOException 
  {
    os.write((0xff & v));
    os.write((0xff & (v >>> 8)));
  }
  
  /**
   * Writes a Little-endian int.
   * @param os - the output stream to write to.
   * @param v - the value to write.
   * @exception IOException
   */
  protected static void writeInt(OutputStream os, int v)
    throws IOException 
  {
    os.write(0xff & v);
    os.write(0xff & (v >>>  8));
    os.write(0xff & (v >>> 16));
    os.write(0xff & (v >>> 24));
  }

  /**
   * Writes a Little-endian long.
   * @param os - the output stream to write to.
   * @param v - the value to write.
   * @exception IOException
   */
  protected static void writeLong(OutputStream os, long v) throws IOException 
  {
    os.write((int)(0xff & v));
    os.write((int)(0xff & (v >>>  8)));
    os.write((int)(0xff & (v >>> 16)));
    os.write((int)(0xff & (v >>> 24)));
    os.write((int)(0xff & (v >>> 32)));
    os.write((int)(0xff & (v >>> 40)));
    os.write((int)(0xff & (v >>> 48)));
    os.write((int)(0xff & (v >>> 56)));
  }
}
