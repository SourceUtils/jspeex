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
 * Class: OggSpeexWriter.java                                                 *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 9th April  2003                                                      *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Ogg Speex Writer
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class OggSpeexWriter
  extends AudioFileWriter
{
  /** Number of packets in an Ogg page (must be less than 255) */
  public static final int PACKETS_PER_OGG_PAGE = 250;
  
  /** The OutputStream */
  private OutputStream out;

  /** Defines the encoder mode (0=NB, 1=WB and 2-UWB). */
  private int     mode;
  /** Defines the sampling rate of the audio input. */
  private int     sampleRate;
  /** Defines the number of channels of the audio input (1=mono, 2=stereo). */
  private int     channels;
  /** Defines the number of frames per speex packet. */
  private int     nframes;
  /** Defines whether or not to use VBR (Variable Bit Rate). */
  private boolean vbr;
  /** */
  private int     size;
  /** Ogg Stream Serial Number */
  private int     streamSerialNumber;
  /** Data buffer */
  private byte[]  dataBuffer;
  /** Pointer within the Data buffer */
  private int     dataBufferPtr;
  /** Header buffer */
  private byte[]  headerBuffer;
  /** Pointer within the Header buffer */
  private int     headerBufferPtr;
  /** Ogg Page count */
  private int     pageCount;
  /** Speex packet count within an Ogg Page */
  private int     packetCount;
  /**
   * Absolute granule position
   * (the number of audio samples from beginning of file to end of Ogg Packet).
   */
  private long    granulepos;
  
  /**
   * Builds an Ogg Speex Writer. 
   */
  public OggSpeexWriter()
  {
    if (streamSerialNumber == 0)
      streamSerialNumber = new Random().nextInt();
    dataBuffer         = new byte[65565];
    dataBufferPtr      = 0;
    headerBuffer       = new byte[255];
    headerBufferPtr    = 0;
    pageCount          = 0;
    packetCount        = 0;
    granulepos         = 0;
  }

  /**
   * Builds an Ogg Speex Writer. 
   * @param mode       the mode of the encoder (0=NB, 1=WB, 2=UWB).
   * @param sampleRate the number of samples per second.
   * @param channels   the number of audio channels (1=mono, 2=stereo, ...).
   * @param nframes    the number of frames per speex packet.
   * @param vbr
   */
  public OggSpeexWriter(int mode, int sampleRate, int channels, int nframes, boolean vbr)
  {
    this();
    setFormat(mode, sampleRate, channels, nframes, vbr);
  }

  /**
   * Sets the output format.
   * Must be called before WriteHeader().
   * @param mode
   * @param sampleRate
   * @param channels
   * @param nframes
   * @param vbr
   */
  private void setFormat(int mode, int sampleRate, int channels, int nframes, boolean vbr)
  {
    this.mode       = mode;
    this.sampleRate = sampleRate;
    this.channels   = channels;
    this.nframes    = nframes;
    this.vbr        = vbr;
  }

  /**
   * Sets the Stream Serial Number.
   * Must not be changed mid stream.
   * @param serialNumber
   */
  public void setSerialNumber(int serialNumber)
  {
    this.streamSerialNumber = serialNumber;
  }

  /**
   * Closes the output file.
   * @exception IOException
   */
  public void close()
    throws IOException 
  {
    flush(true);
    out.close(); 
  }
  
  /**
   * Open the output file. 
   * @param filename - file to open.
   * @exception IOException
   */
  public void open(String filename)
    throws IOException 
  {
    new File(filename).delete(); 
    out  = new FileOutputStream(filename);
    size = 0;   
  }
  
  /**
   * Writes the header pages that start the Ogg Speex file. 
   * Prepares file for data to be written.
   * @param comment description to be included in the header.
   * @exception IOException
   */
  public void writeHeader(String comment)
    throws IOException
  {
    // writes the OGG header page
    ByteArrayOutputStream baos = new ByteArrayOutputStream(108);
    baos.write("OggS".getBytes(), 0, 4); //  0 -  3: capture_pattern
    baos.write(0xff & 0);                //       4: stream_structure_version
    baos.write(0xff & 2);                //       5: header_type_flag (2=bos: beginning of sream)
    writeLong(baos, 0);                  //  6 - 13: absolute granule position
    writeInt(baos, streamSerialNumber);  // 14 - 17: stream serial number
    writeInt(baos, pageCount++);         // 18 - 21: page sequence no
    writeInt(baos, 0);                   // 22 - 25: page checksum
    baos.write(0xff & 1);                //      26: page_segments
    baos.write(0xff & 80);               //      27: segment_table (1 segment, size 80 = Speex Header)
    /* writes the Speex header */
    baos.write("Speex   ".getBytes(), 0, 8);  //  0 -  7: speex_string
    baos.write("speex-1.0".getBytes(), 0, 9); //  8 - 27: speex_version
    baos.write(new byte[11], 0, 11);          //        : speex_version (fill in up to 20 bytes)
    writeInt(baos, 1);                        // 28 - 31: speex_version_id
    writeInt(baos, 80);                       // 32 - 35: header_size
    writeInt(baos, sampleRate);               // 36 - 39: rate
    writeInt(baos, mode);                     // 40 - 43: mode (0=narrowband, 1=wb, 2=uwb)
    writeInt(baos, 4);                        // 44 - 47: mode_bitstream_version
    writeInt(baos, channels);                 // 48 - 51: nb_channels
    writeInt(baos, -1);                       // 52 - 55: bitrate
    writeInt(baos, mode==0?160:mode==1?320:640); // 56 - 59: frame_size
    writeInt(baos, vbr?1:0);                  // 60 - 63: vbr
    writeInt(baos, nframes);                  // 64 - 67: frames_per_packet
    writeInt(baos, 0);                        // 68 - 71: extra_headers
    writeInt(baos, 0);                        // 72 - 75: reserved1
    writeInt(baos, 0);                        // 76 - 79: reserved2
    /* Calculate Checksum */
    byte[] ogg = baos.toByteArray();
    int chksum = OggCrc.checksum(0, ogg, 0, ogg.length);
    ogg[22] = (byte)(0xff & chksum);
    ogg[23] = (byte)(0xff & (chksum >>>  8));
    ogg[24] = (byte)(0xff & (chksum >>> 16));
    ogg[25] = (byte)(0xff & (chksum >>> 24));
    out.write(ogg);
    /* writes the OGG comment page */
    baos = new ByteArrayOutputStream(64);
    baos.write("OggS".getBytes(), 0, 4); //  0 -  3: capture_pattern
    baos.write(0xff & 0);                //       4: stream_structure_version
    baos.write(0xff & 0);                //       5: header_type_flag
    writeLong(baos, 0);                  //  6 - 13: absolute granule position
    writeInt(baos, streamSerialNumber);  // 14 - 17: stream serial number
    writeInt(baos, pageCount++);         // 18 - 21: page sequence no
    writeInt(baos, 0);                   // 22 - 25: page checksum
    baos.write(0xff & 1);                //      26: page_segments
    baos.write(0xff & (comment.length()+8)); //  27: segment_table (1 segment, size 80 = Speex Header)
    /* writes the Comment */
    writeInt(baos, comment.length());                    // vendor comment size
    baos.write(comment.getBytes(), 0, comment.length()); // vendor comment
    writeInt(baos, 0);                                   // user comment list length
    /* Calculate Checksum */
    ogg = baos.toByteArray();
    chksum = OggCrc.checksum(0, ogg, 0, ogg.length);
    ogg[22] = (byte)(0xff & chksum);
    ogg[23] = (byte)(0xff & (chksum >>>  8));
    ogg[24] = (byte)(0xff & (chksum >>> 16));
    ogg[25] = (byte)(0xff & (chksum >>> 24));
    out.write(ogg);
  }
  
  /**
   * Writes a packet of audio. 
   * @param data - audio data.
   * @param offset - the offset from which to start reading the data.
   * @param len - the length of data to read.
   * @exception IOException
   */
  public void writePacket(byte[] data, int offset, int len)
    throws IOException 
  {
    if (len <= 0) { // nothing to write
      return;
    }
    if (packetCount > PACKETS_PER_OGG_PAGE) {
      flush(false);
    }
    System.arraycopy(data, offset, dataBuffer, dataBufferPtr, len);
    dataBufferPtr += len;
    headerBuffer[headerBufferPtr++]=(byte)len;
    packetCount++;
    granulepos += nframes * (mode==2 ? 640 : (mode==1 ? 320 : 160));
  }

  /**
   * Flush the Ogg page out of the buffers into the file.
   * @param eos - end of stream
   * @exception IOException
   */
  private void flush(boolean eos)
    throws IOException
  {
    /* writes the OGG header page */
    ByteArrayOutputStream baos = new ByteArrayOutputStream(284);
    baos.write("OggS".getBytes(), 0, 4); //  0 -  3: capture_pattern
    baos.write(0xff & 0);                //       4: stream_structure_version
    baos.write(0xff & (eos ? 4 : 0));    //       5: header_type_flag (4=eos: end of sream)
    writeLong(baos, granulepos);         //  6 - 13: absolute granule position
    writeInt(baos, streamSerialNumber);  // 14 - 17: stream serial number
    writeInt(baos, pageCount++);         // 18 - 21: page sequence no
    writeInt(baos, 0);                   // 22 - 25: page checksum
    baos.write(0xff & packetCount);      //      26: page_segments
    baos.write(headerBuffer, 0, packetCount); // 27 -  x: segment_table (1 segment, size 80 = Speex Header)
    /* Calculate Checksum */
    byte[] ogg = baos.toByteArray();
    int chksum = OggCrc.checksum(0, ogg, 0, ogg.length);
    chksum = OggCrc.checksum(chksum, dataBuffer, 0, dataBufferPtr);
    ogg[22] = (byte)(0xff & chksum);
    ogg[23] = (byte)(0xff & (chksum >>>  8));
    ogg[24] = (byte)(0xff & (chksum >>> 16));
    ogg[25] = (byte)(0xff & (chksum >>> 24));
    out.write(ogg);
    out.write(dataBuffer, 0, dataBufferPtr);
    dataBufferPtr   = 0;
    headerBufferPtr = 0;
    packetCount     = 0;
  }
}
