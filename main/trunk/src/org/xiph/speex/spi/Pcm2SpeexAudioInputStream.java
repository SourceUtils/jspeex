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
 * Class: Pcm2SpeexAudioInputStream.java                                      *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 12th July 2003                                                       *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.spi;

import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import javax.sound.sampled.AudioFormat;

import org.xiph.speex.OggCrc;
import org.xiph.speex.Encoder;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbEncoder;
import org.xiph.speex.SpeexEncoder;

/**
 * Converts a PCM 16bits/sample mono audio stream to Ogg Speex
 *
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class Pcm2SpeexAudioInputStream
  extends FilteredAudioInputStream
{
//  public static final boolean DEFAULT_VBR              = true;
  public static final int DEFAULT_SAMPLERATE           = 8000;
  public static final int DEFAULT_QUALITY              = 3;
  public static final int DEFAULT_FRAMES_PER_PACKET    = 1;
  public static final int DEFAULT_PACKETS_PER_OGG_PAGE = 20; // .4s of audio

  // Speex variables
  private SpeexEncoder encoder;
  private int     mode;
  private int     frameSize;
  private int     framesPerPacket;
  // Ogg variables
  private String  comment = null;
  private int     granulpos;
  private int     streamSerialNumber;
  private int     packetsPerOggPage;
  private int     packetCount;
  private int     pageCount;
  private int     oggCount;
  private boolean first;
  
  /**
   * Constructor
   * @param in     the underlying input stream.
   * @param format
   * @param length
   */
  public Pcm2SpeexAudioInputStream(InputStream in,
                                   AudioFormat format, long length)
  {
    this(DEFAULT_SAMPLERATE, in, DEFAULT_BUFFER_SIZE, format, length);
  }

  /**
   * Constructor
   * @param samplerate   the samplerate of the audio stream.
   * @param in     the underlying input stream.
   * @param format
   * @param length
   */
  public Pcm2SpeexAudioInputStream(int samplerate, InputStream in,
                                   AudioFormat format, long length)
  {
    this(samplerate, in, DEFAULT_BUFFER_SIZE, format, length);
  }

  /**
   * Constructor
   * @param in     the underlying input stream.
   * @param size   the buffer size.
   * @param format
   * @param length
   * @exception IllegalArgumentException if size <= 0.
   */
  public Pcm2SpeexAudioInputStream(InputStream in, int size,
                                   AudioFormat format, long length)
  {
    this(DEFAULT_SAMPLERATE, in, size, format, length);
  }
  
  /**
   * Constructor
   * @param samplerate   the samplerate of the audio stream.
   * @param in     the underlying input stream.
   * @param size   the buffer size.
   * @param format
   * @param length
   * @exception IllegalArgumentException if size <= 0.
   */
  public Pcm2SpeexAudioInputStream(int samplerate, InputStream in, int size,
                                   AudioFormat format, long length)
  {
    super(in, size, format, length);
    // Ogg initialisation
    granulpos = 0;
    streamSerialNumber = new Random().nextInt();
    packetsPerOggPage = DEFAULT_PACKETS_PER_OGG_PAGE;
    packetCount = 0;
    pageCount = 0;
    // Speex initialisation
    framesPerPacket = DEFAULT_FRAMES_PER_PACKET;
    // mode 0: narrowband, 1: wideband, 2: ultra-wideband 
    mode = (samplerate < 12000) ? 0 : ((samplerate < 24000) ? 1 : 2);
    encoder = new SpeexEncoder();
    encoder.init(mode, DEFAULT_QUALITY, samplerate, 1);
    frameSize = 2 * encoder.getFrameSize();
    // Misc initialsation
    comment = "Encoded with " + encoder.VERSION;
    first = true;
  }

  /**
   * Sets the number of Audio Frames that are to be put in every Speex Packet.
   * @param framesPerPacket
   * @see #DEFAULT_FRAMES_PER_PACKET
   */
  public void setFramesPerPacket(int framesPerPacket)
  {
    if (framesPerPacket <= 0) {
      packetsPerOggPage = DEFAULT_FRAMES_PER_PACKET;
    }
    this.framesPerPacket = framesPerPacket;
  }

  /**
   * Sets the number of Speex Packets that are to be put in every Ogg Page.
   * @param packetsPerOggPage
   * @see #DEFAULT_PACKETS_PER_OGG_PAGE
   */
  public void setPacketsPerOggPage(int packetsPerOggPage)
  {
    if (packetsPerOggPage <= 0) {
      packetsPerOggPage = DEFAULT_PACKETS_PER_OGG_PAGE;
    }
    if (packetsPerOggPage > 255) {
      packetsPerOggPage = 255;
    }
    this.packetsPerOggPage = packetsPerOggPage;
  }
  
  /**
   * Sets the comment for the Ogg Comment Header.
   * @param comment
   * @param appendVersion
   */
  public void setComment(String comment, boolean appendVersion)
  {
    this.comment = comment;
    if (appendVersion) {
      comment += encoder.VERSION;
    }
  }

  /**
   * Sets the Speex encoder Quality
   * @param quality
   */
  public void setQuality(int quality)
  {
    encoder.getEncoder().setQuality(quality);
    if (encoder.getEncoder().getVbr()) {
      encoder.getEncoder().setVbrQuality((float)quality);
    }
  }
  
  /**
   * Sets whether of not the encoder is to use VBR
   * @param vbr
   */
  public void setVbr(boolean vbr)
  {
    encoder.getEncoder().setVbr(vbr);
  }

  /**
   * Returns the Encoder
   * @return the Encoder
   */
  public Encoder getEncoder()
  {
    return encoder.getEncoder();
  }

  /**
   * Fills the buffer with more data, taking into account
   * shuffling and other tricks for dealing with marks.
   * Assumes that it is being called by a synchronized method.
   * This method also assumes that all data has already been read in,
   * hence pos > count.
   * @exception IOException
   */
  protected void fill()
    throws IOException
  {
    makeSpace();
    if (first) {
      writeHeaderFrame();
      writeCommentFrame();
      first = false;
    }
    while (true) {
      int n = in.read(prebuf, precount, prebuf.length - precount);
      if (n < 0) { // inputstream has ended
        if ((precount-prepos) % 2 != 0) { // we don't have a complete last PCM sample
          throw new StreamCorruptedException("Incompleted last PCM sample when stream ended");
        }
        while (prepos < precount) { // still data to encode
          if ((precount - prepos) < framesPerPacket*frameSize) {
            // fill end of frame with zeros
            if ((prebuf.length - prepos) < framesPerPacket*frameSize) {
              // grow prebuf
              int nsz = prepos + framesPerPacket*frameSize;
              byte[] nbuf = new byte[nsz];
              System.arraycopy(prebuf, 0, nbuf, 0, precount);
              prebuf = nbuf;
            }
            for (;precount < (prepos+framesPerPacket*frameSize); precount++) {
              prebuf[precount] = 0;
            }
          }
          if (packetCount == 0) {
            writeOggPageHeader(packetsPerOggPage, 0);
          }
          for (int i=0; i<framesPerPacket; i++) {
            encoder.processData(prebuf, prepos, frameSize);
          }
          prepos += framesPerPacket*frameSize;
          int size = encoder.getProcessedDataByteSize();
          while ((buf.length - oggCount) < size) { // grow buffer
            int nsz = buf.length * 2;
            byte[] nbuf = new byte[nsz];
            System.arraycopy(buf, 0, nbuf, 0, oggCount);
            buf = nbuf;
          }
          buf[count + 27 + packetCount] = (byte)(0xff & size);
          encoder.getProcessedData(buf, oggCount);
          oggCount += size;
          packetCount++;
          if (packetCount >= packetsPerOggPage) {
            writeOggPageChecksum();
            return;
          }
        }
        if (packetCount > 0) {
          // we have less than the normal number of packets in this page.
          buf[count+5] = (byte)(0xff & 4); // set page header type to end of stream
          buf[count+26] = (byte)(0xff & packetCount);
          System.arraycopy(buf, count+27+packetsPerOggPage, buf, count+27+packetCount, oggCount-(count+27+packetsPerOggPage));
          oggCount -= packetsPerOggPage-packetCount;
          writeOggPageChecksum();
        }
        return;
      }
      else if (n > 0) {
        precount += n;
        if ((precount - prepos) >= framesPerPacket*frameSize) { // enough data to encode frame
          while ((precount - prepos) >= framesPerPacket*frameSize) { // lets encode all we can
            if (packetCount == 0) {
              writeOggPageHeader(packetsPerOggPage, 0);
            }
            for (int i=0; i<framesPerPacket; i++) {
              encoder.processData(prebuf, prepos, frameSize);
            }
            prepos += framesPerPacket*frameSize;
            int size = encoder.getProcessedDataByteSize();
            while ((buf.length - oggCount) < size) { // grow buffer
              int nsz = buf.length * 2;
              byte[] nbuf = new byte[nsz];
              System.arraycopy(buf, 0, nbuf, 0, oggCount);
              buf = nbuf;
            }
            buf[count + 27 + packetCount] = (byte)(0xff & size);
            encoder.getProcessedData(buf, oggCount);
            oggCount += size;
            packetCount++;
            if (packetCount >= packetsPerOggPage) {
              writeOggPageChecksum();
            }
          }
          System.arraycopy(prebuf, prepos, prebuf, 0, precount-prepos);
          precount -= prepos;
          prepos = 0;
          if (packetCount >= packetsPerOggPage) {
            writeOggPageChecksum();
            // we have encoded some data (all that we could),
            // so we can leave now, otherwise we return to a potentially
            // blocking read of the underlying inputstream.
            return;
          }
        }
      }
      else { // n == 0
        // read 0 bytes from underlying stream yet it is not finished.
        if (precount >= prebuf.length) {
          // no more room in buffer
          if (prepos > 0) {
            // free some space
            System.arraycopy(prebuf, prepos, prebuf, 0, precount-prepos);
            precount -= prepos;
            prepos = 0;
          }
          else {
            // we could grow the pre-buffer but that risks in turn growing the
            // buffer which could lead sooner or later to an
            // OutOfMemoryException. 
            return;
          }
        }
        else {
          return;
        }
      }
    }
  }

  /**
   * Returns the number of bytes that can be read from this inputstream without
   * blocking. 
   * <p>
   * The <code>available</code> method of <code>FilteredAudioInputStream</code>
   * returns the sum of the the number of bytes remaining to be read in the
   * buffer (<code>count - pos</code>) and the result of calling the
   * <code>available</code> method of the underlying inputstream. 
   *
   * @return the number of bytes that can be read from this inputstream without
   * blocking.
   * @exception IOException if an I/O error occurs.
   * @see #in
   */
  public synchronized int available()
    throws IOException
  {
    int avail = super.available();
    int unencoded = precount - prepos + in.available();
    if (encoder.getEncoder().getVbr()) {
      switch(mode) {
        case 0: // Narrowband
          // ogg header size = 27 + packetsPerOggPage
          // count 1 byte (min 5 bits) for each block available
          return avail + (27 + 2 * packetsPerOggPage) *
                         (unencoded / (packetsPerOggPage*framesPerPacket*320));
        case 1: // Wideband
          // ogg header size = 27 + packetsPerOggPage
          // count 2 byte (min 9 bits) for each block available
          return avail + (27 + 2 * packetsPerOggPage) *
                         (unencoded / (packetsPerOggPage*framesPerPacket*640));
        case 2: // Ultra wideband
          // ogg header size = 27 + packetsPerOggPage
          // count 2 byte (min 13 bits) for each block available
          return avail + (27 + 3 * packetsPerOggPage) *
                         (unencoded / (packetsPerOggPage*framesPerPacket*1280));
        default:
          return avail;
      }
    }
    else {
      int packetsize;
      switch(mode) {
        case 0: // Narrowband
          packetsize = NbEncoder.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize = (packetsize + 7) >> 3; // convert packetsize to bytes
          // 1 frame = 20ms = 160ech = 320bytes
          avail += (27 + packetsPerOggPage + packetsPerOggPage * packetsize) *
                   (unencoded / (packetsPerOggPage * framesPerPacket * 320));
          return avail;
        case 1: // Wideband
          packetsize = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize = (packetsize + 7) >> 3; // convert packetsize to bytes
          // 1 frame = 20ms = 320ech = 640bytes
          avail += (27 + packetsPerOggPage + packetsPerOggPage * packetsize) *
                   (unencoded / (packetsPerOggPage * framesPerPacket * 640));
          return avail;
        case 2: // Ultra wideband
          packetsize = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize += SbEncoder.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[encoder.getEncoder().getMode()]];
          packetsize = (packetsize + 7) >> 3; // convert packetsize to bytes
          // 1 frame = 20ms = 640ech = 1280bytes
          avail += (27 + packetsPerOggPage + packetsPerOggPage * packetsize) *
                   (unencoded / (packetsPerOggPage * framesPerPacket * 1280));
          return avail;
        default:
          return avail;
      }
    }
  }
  
  //---------------------------------------------------------------------------
  // Ogg Specific Code
  //---------------------------------------------------------------------------
  
  /**
   * Write an OGG page header.
   * @param packets - the number of packets in the Ogg Page (must be between 1 and 255)
   * @param headertype - 2=bos: beginning of sream, 4=eos: end of sream
   * @exception IOException
   */
  private void writeOggPageHeader(int packets, int headertype)
    throws IOException
  {
    while ((buf.length - count) < (27 + packets)) { // grow buffer
      int nsz = buf.length * 2;
      byte[] nbuf = new byte[nsz];
      System.arraycopy(buf, 0, nbuf, 0, count);
      buf = nbuf;
    }
    writeString(buf, count, "OggS");             //  0 -  3: capture_pattern
    buf[count+4] = 0;                            //       4: stream_structure_version
    buf[count+5] = (byte) headertype;            //       5: header_type_flag (2=bos: beginning of stream, 4=eos: end of stream)
    writeLong(buf, count+6, granulpos);          //  6 - 13: absolute granule position
    writeInt(buf, count+14, streamSerialNumber); // 14 - 17: stream serial number
    writeInt(buf, count+18, pageCount++);        // 18 - 21: page sequence no
    writeInt(buf, count+22, 0);                  // 22 - 25: page checksum
    buf[count+26] = (byte)(0xff & packets);      //      26: page_segments
    for (int i=0; i<packets; i++) {
      buf[count+27+i] = 0;                       // 27 -...: segment_table
    }
    oggCount = count + 27 + packets;
  }

  /**
   * Calculate and write the OGG page checksum. This now closes the Ogg page.
   * @exception IOException
   */
  private void writeOggPageChecksum()
    throws IOException
  {
    // write the granulpos
    granulpos += frameSize*packetCount/2;
    writeLong(buf, count+6, granulpos);
    // write the checksum
    int chksum = OggCrc.checksum(0, buf, count, oggCount-count);
    writeInt(buf, count+22, chksum);
    // reset variables for a new page.
    count = oggCount;
    packetCount = 0;
  }
  
  /**
   * Write the OGG Speex header.
   * @exception IOException
   */
  private void writeHeaderFrame()
    throws IOException
  {
    while ((buf.length - count) < 108) {
      // grow buffer (108 = 28 + 80 = size of Ogg Header Frame)
      int nsz = buf.length * 2;
      byte[] nbuf = new byte[nsz];
      System.arraycopy(buf, 0, nbuf, 0, count);
      buf = nbuf;
    }
    // writes the OGG header page
    writeOggPageHeader(1, 2);
    buf[count+27] = 80; // size of SpeexHeader
    /* writes the Speex header */
    writeString(buf, oggCount, "Speex   ");               //  0 -  7: speex_string
    writeString(buf, oggCount+8, "speex-1.0           "); //  8 - 27: speex_version
    writeInt(buf, oggCount+28, 1);                        // 28 - 31: speex_version_id
    writeInt(buf, oggCount+32, 80);                       // 32 - 35: header_size
    writeInt(buf, oggCount+36, encoder.getSampleRate());  // 36 - 39: rate
    writeInt(buf, oggCount+40, mode);                     // 40 - 43: mode (0=narrowband, 1=wb, 2=uwb)
    writeInt(buf, oggCount+44, 4);                        // 44 - 47: mode_bitstream_version
    writeInt(buf, oggCount+48, encoder.getChannels());    // 48 - 51: nb_channels
    writeInt(buf, oggCount+52, -1);                       // 52 - 55: bitrate
    writeInt(buf, oggCount+56, encoder.getFrameSize());   // 56 - 59: frame_size
    writeInt(buf, oggCount+60, (encoder.getEncoder().getVbr() ? 1 : 0)); // 60 - 63: vbr
    writeInt(buf, oggCount+64, framesPerPacket);          // 64 - 67: frames_per_packet
    writeInt(buf, oggCount+68, 0);                        // 68 - 71: extra_headers
    writeInt(buf, oggCount+72, 0);                        // 72 - 75: reserved1
    writeInt(buf, oggCount+76, 0);                        // 76 - 79: reserved2
    /* Calculate Checksum */
    oggCount += 80;
    writeOggPageChecksum();
  }
  
  /**
   * Write the OGG Speex Comment header.
   * @exception IOException
   */
  private void writeCommentFrame()
    throws IOException
  {
    if (comment.length() > 247) {
      comment = comment.substring(0, 247);
    }
    int length = comment.length();
    while ((buf.length - count) < length + 8 + 28) { // grow buffer
      int nsz = buf.length * 2;
      byte[] nbuf = new byte[nsz];
      System.arraycopy(buf, 0, nbuf, 0, count);
      buf = nbuf;
    }
    // writes the OGG header page
    writeOggPageHeader(1, 0);
    buf[count+27] = (byte)(0xff & length+8); // size of CommentHeader
    /* writes the OGG comment page */
    writeInt(buf, oggCount, length);       // vendor comment size
    writeString(buf, oggCount+4, comment); // vendor comment
    writeInt(buf, oggCount+length+4, 0);   // user comment list length
    /* Calculate Checksum */
    oggCount += length+8;
    writeOggPageChecksum();
  }

  /**
   * Writes a Little-endian short
   */  
  private static void writeShort(byte[] data, int offset, short v)
    throws IOException 
  {
    data[offset]   = (byte) (0xff & v);
    data[offset+1] = (byte) (0xff & (v >>> 8));
  }
  
  /**
   * Writes a Little-endian int
   */
  private static void writeInt(byte[] data, int offset, int v)
    throws IOException 
  {
    data[offset]   = (byte) (0xff & v);
    data[offset+1] = (byte) (0xff & (v >>>  8));
    data[offset+2] = (byte) (0xff & (v >>> 16));
    data[offset+3] = (byte) (0xff & (v >>> 24));
  }

  /**
   * Writes a Little-endian long
   */
  private static void writeLong(byte[] data, int offset, long v)
    throws IOException 
  {
    data[offset]   = (byte) (0xff & v);
    data[offset+1] = (byte) (0xff & (v >>>  8));
    data[offset+2] = (byte) (0xff & (v >>> 16));
    data[offset+3] = (byte) (0xff & (v >>> 24));
    data[offset+4] = (byte) (0xff & (v >>> 32));
    data[offset+5] = (byte) (0xff & (v >>> 40));
    data[offset+6] = (byte) (0xff & (v >>> 48));
    data[offset+7] = (byte) (0xff & (v >>> 56));
  }

  /**
   * Writes a String
   */
  private static void writeString(byte[] data, int offset, String v)
    throws IOException 
  {
    byte[] str = v.getBytes();
    System.arraycopy(str, 0, data, offset, str.length);
  }
}
