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
 * Class: Speex2PcmAudioInputStream.java                                      *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 12th July 2003                                                       *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.spi;

import java.io.InputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import javax.sound.sampled.AudioFormat;

import org.xiph.speex.Bits;
import org.xiph.speex.Decoder;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbDecoder;

/**
 * Converts an Ogg Speex bitstream into a PCM 16bits/sample audio stream.
 *
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class Speex2PcmAudioInputStream
  extends FilteredAudioInputStream
{
  // audio parameters
  private int     sampleRate;
  private int     channelCount;
  // Speex variables
  private float[] decodedData;
  private byte[]  outputData;
  private Bits    bits;
  private Decoder decoder;
  private int     frameSize;
  private int     framesPerPacket;
  // Ogg variables
  private int     streamSerialNumber;
  private int     packetsPerPage;
  private int     packetCount;
  private byte[]  packetSizes;
  private boolean first;

  /**
   * Constructor
   * @param in     the underlying input stream.
   * @param format the format of this stream's audio data.
   * @param length the length in sample frames of the data in this stream.
   */
  public Speex2PcmAudioInputStream(InputStream in, AudioFormat format, long length)
  {
    this(in, DEFAULT_BUFFER_SIZE, format, length);
  }

  /**
   * Constructor
   * @param in     the underlying input stream.
   * @param size   the buffer size.
   * @param format the format of this stream's audio data.
   * @param length the length in sample frames of the data in this stream.
   * @exception IllegalArgumentException if size <= 0.
   */
  public Speex2PcmAudioInputStream(InputStream in, int size, AudioFormat format, long length)
  {
    super(in, size, format, length);
    bits = new Bits();
    packetSizes = new byte[256];
    first = true;
  }

  /**
   * Initialise the Speex Decoder after reading the Ogg Header.
   * <pre>
   * Ogg Header description
   *  0 -  3: capture_pattern
   *       4: stream_structure_version
   *       5: header_type_flag (2=bos: beginning of sream)
   *  6 - 13: absolute granule position
   * 14 - 17: stream serial number
   * 18 - 21: page sequence no
   * 22 - 25: page checksum
   *      26: page_segments
   * 27 -...: segment_table
   * Speex Header description
   *  0 -  7: speex_string
   *  8 - 27: speex_version
   * 28 - 31: speex_version_id
   * 32 - 35: header_size
   * 36 - 39: rate
   * 40 - 43: mode (0=narrowband, 1=wb, 2=uwb)
   * 44 - 47: mode_bitstream_version
   * 48 - 51: nb_channels
   * 52 - 55: bitrate
   * 56 - 59: frame_size
   * 60 - 63: vbr
   * 64 - 67: frames_per_packet
   * 68 - 71: extra_headers
   * 72 - 75: reserved1
   * 76 - 79: reserved2
   * </pre>
   * @exception IOException
   */
  protected void initDecoder()
    throws IOException
  {
    if (!(new String(prebuf, 0, 4).equals("OggS"))) {
      throw new StreamCorruptedException("The given stream does not appear to be Ogg.");
    }
    streamSerialNumber = readInt(prebuf, 14);
    if (!(new String(prebuf, 28, 8).equals("Speex   "))) {
      throw new StreamCorruptedException("The given stream does not appear to be Ogg Speex.");
    }
    sampleRate      = readInt(prebuf, 28+36);
    channelCount    = readInt(prebuf, 28+48);
    framesPerPacket = readInt(prebuf, 28+64);
    int mode = readInt(prebuf, 28+40);
    switch (mode) {
      case 0:
        decoder = new NbDecoder();
        ((NbDecoder)decoder).nbinit();
        break;
      case 1:
        decoder = new SbDecoder();
        ((SbDecoder)decoder).wbinit();
        break;
      case 2:
        decoder = new SbDecoder();
        ((SbDecoder)decoder).uwbinit();
        break;
      default:
    }  
    /* initialize the speex decoder */
    decoder.setPerceptualEnhancement(true);
    /* set decoder format and properties */
    frameSize   = decoder.getFrameSize();
    decodedData = new float[framesPerPacket*frameSize*channelCount];
    outputData  = new byte[2*framesPerPacket*frameSize*channelCount];
    bits.init();
  }
  
  /**
   * Fills the buffer with more data, taking into account shuffling and other
   * tricks for dealing with marks.
   * Assumes that it is being called by a synchronized method.
   * This method also assumes that all data has already been read in, hence
   * pos > count.
   * @exception IOException
   */
  protected void fill()
    throws IOException
  {
    makeSpace();
    while (true) {
      int n = in.read(prebuf, precount, prebuf.length - precount);
      if (n < 0) { // inputstream has ended
        if (first) {
          throw new StreamCorruptedException("Incomplete Ogg Headers");
        }
        while (prepos < precount) { // still data to decode
          if (packetCount >= packetsPerPage) { // read new Ogg Page header
            readOggPageHeader();
          }
          n = packetSizes[packetCount++];
          if ((precount-prepos) < n) { // we don't have enough data for a complete speex frame
            throw new StreamCorruptedException("Incompleted last Speex packet");
          }
          // do last stuff here
          decode(prebuf, prepos, n);
          prepos += n;
          while ((buf.length - count) < outputData.length) { // grow buffer
            int nsz = buf.length * 2;
            byte[] nbuf = new byte[nsz];
            System.arraycopy(buf, 0, nbuf, 0, count);
            buf = nbuf;
          }
          System.arraycopy(outputData, 0, buf, count, outputData.length);
          count += outputData.length;
        }
        return;
      }
      else if (n > 0) {
        precount += n;
        // do stuff here
        if (first) {
          if (decoder==null && precount>=108) { // we can process the speex header
            initDecoder();
          }
          if (decoder!=null && precount>=108+27) { // we can process the comment (skip them)
            packetsPerPage = 0xff & prebuf[108+26];
            if (precount>=108+27+packetsPerPage) {
              int size = 0;
              for (int i=0; i<packetsPerPage; i++) {
                size += 0xff & prebuf[108+27+i];
              }
              if (precount>=108+27+packetsPerPage+size) { // we have read the complete comment page
                prepos = 108+27+packetsPerPage+size;
                packetsPerPage = 0;
                packetCount = 255;
                first = false;
              }
            }
          }
        }
        if (!first) {
          if (packetCount >= packetsPerPage) { // read new Ogg Page header
            readOggPageHeader();
          }
          if (packetCount < packetsPerPage) { // read the next packet
            if ((precount-prepos) >= packetSizes[packetCount]) { // we have enough data, lets start decoding
              while (((precount-prepos) >= packetSizes[packetCount]) && packetCount < packetsPerPage) { // lets decode all we can
                n = packetSizes[packetCount++];
                decode(prebuf, prepos, n);
                prepos += n;
                while ((buf.length - count) < outputData.length) { // grow buffer
                  int nsz = buf.length * 2;
                  byte[] nbuf = new byte[nsz];
                  System.arraycopy(buf, 0, nbuf, 0, count);
                  buf = nbuf;
                }
                System.arraycopy(outputData, 0, buf, count, outputData.length);
                count += outputData.length;
                if (packetCount >= packetsPerPage) { // read new Ogg Page header
                  readOggPageHeader();
                }
              }
              System.arraycopy(prebuf, prepos, prebuf, 0, precount-prepos);
              precount -= prepos;
              prepos = 0;
              return; // we have decoded some data (all that we could), so we can leave now, otherwise we return to a potentially blocking read of the underlying inputstream.
            }
          }
        }
      }
      else { // n == 0
        // read 0 bytes from underlying stream yet it is not finished.
      }
    }
  }

  /**
   * This is where the actual decoding takes place.
   * @param data the array of data to decode.
   * @param offset the offset from which to start reading the data.
   * @param len the length of data to read from the array.
   */
  protected void decode(byte[] data, int offset, int len)
  {
    int i;
    short val;
    int outputSize = 0;
    /* read packet bytes into bitstream */
    bits.read_from(data, offset, len);
    for (int frame=0; frame<framesPerPacket; frame++) {
      /* decode the bitstream */
      decoder.decode(bits, decodedData);
      /* PCM saturation */
      for (i=0;i<frameSize*channelCount;i++) {
        if (decodedData[i]>32767.0f)
          decodedData[i]=32767.0f;
        else if (decodedData[i]<-32768.0f)
          decodedData[i]=-32768.0f;
      }
      /* convert to short and save to buffer */
      for (i=0; i<frameSize*channelCount; i++) {
        val = (decodedData[i]>0) ? (short)(decodedData[i]+.5) : (short)(decodedData[i]-.5);
        outputData[outputSize++] = (byte) (val & 0xff);
        outputData[outputSize++] = (byte) ((val >> 8) &  0xff );
      }
    }
  }

  /**
   * Returns the number of bytes that can be read from this inputstream without
   * blocking. 
   * <p>
   * The <code>available</code> method of <code>FilteredAudioInputStream</code>
   * returns the sum of the the number of bytes remaining to be read in the
   * buffer (<code>count - pos</code>).
   * The result of calling the <code>available</code> method of the underlying
   * inputstream is not used, as this data will have to be filtered, and thus
   * may not be the same size after processing (although subclasses that do the
   * filtering should override this method and use the amount of data available
   * in the underlying inputstream).
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
    // See how much we could decode from the underlying stream.
    if (packetCount < packetsPerPage) {
      int undecoded = precount - prepos + in.available();
      int size = packetSizes[packetCount];
      int tempCount = 0;
      while (size < undecoded &&
             packetCount + tempCount < packetsPerPage) {
        undecoded -= size;
        avail += 2 * frameSize * framesPerPacket;
        tempCount++;
        size = packetSizes[packetCount + tempCount];
      }
    }
    return avail;
  }

  //---------------------------------------------------------------------------
  // Ogg Specific code
  //---------------------------------------------------------------------------
  
  /**
   * Read the Ogg Page header and extract the speex packet sizes.
   * Note: the checksum is ignores.
   */
  private void readOggPageHeader()
    throws IOException
  {
    int packets = 1;
    if (precount-prepos>=28) { // can read beginning of Page header
      if (!(new String(prebuf, prepos, 4).equals("OggS"))) {
        throw new StreamCorruptedException("Lost Ogg Sync");
      }
      if (streamSerialNumber != readInt(prebuf, prepos+14)) {
        throw new StreamCorruptedException("Ogg Stream Serial Number mismatch");
      }
      packets = 0xff & prebuf[prepos+26];
    }
    if (precount-prepos>=27+packets) { // can read entire Page header
      System.arraycopy(prebuf, prepos+27, packetSizes, 0, packets);
      packetCount = 0;
      prepos += 27+packets;
      packetsPerPage = packets;
    }
  }
  
  /**
   * Converts Little Endian (Windows) bytes to an int (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   */
  private int readInt(byte[] data, int offset)
  {
    return (data[offset] & 0xff) |
           ((data[offset+1] & 0xff) <<  8) |
           ((data[offset+2] & 0xff) << 16) |
           (data[offset+3] << 24); // no & 0xff at the end to keep the sign
  }
}
