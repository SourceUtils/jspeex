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
 * Class: SpeexEncoder.java                                                   *
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

package org.xiph.speex;

/**
 * Main Speex Encoder class.
 * This class encodes the given PCM 16bit samples into Speex packets.
 */
public class SpeexEncoder
{
  public static final String VERSION = "Java Speex Encoder v0.7 ($Revision$)";

  private Encoder       encoder;
  private Bits          bits;
  private float[]       rawData;
  private int           sampleRate;
  private int           channelCount;
  private int           frameSize;
    
  /**
   * Constructor
   */
  public SpeexEncoder() 
  {	    
    bits = new Bits();
  }

  /**
   * Initialisation
   */
  public boolean init(int mode, int quality, int sampleRate, int channels)
  {
    if (mode==0) {
      new ModesNB().init();
    }
//Wideband
    else {
      new ModesWB().init();
    }
//*/    
    switch (mode) {
      case 0:
        encoder = new NbEncoder();
        break;
//Wideband
      case 1:
        encoder = new WbEncoder();
        break;
      case 2:
        encoder = new UwbEncoder();
        break;
//*/
      default:
        return false;
    }

    /* initialize the speex decoder */
    encoder.init();
    encoder.setQuality(quality);
    
    /* set decoder format and properties */
    this.frameSize    = encoder.getFrameSize();
    this.sampleRate   = sampleRate;
    this.channelCount = channels;
    rawData           = new float[sampleRate*channelCount];
    
    bits.init();
    return true;
  }

  /**
   * Returns the Encoder being used (Narrowband, Wideband or Ultrawideband)
   */
  public Encoder getEncoder() 
  {
    return encoder;
  }

  /**
   * Returns the sample rate
   */
  public int getSampleRate() 
  {
    return sampleRate;
  }

  /**
   * Returns the number of channels
   */
  public int getChannels() 
  {
    return channelCount;
  }

  /**
   * Returns the size of a frame
   */
  public int getFrameSize() 
  {
    return frameSize;
  }

  /**
   * Pull the decoded data out into a byte array at the given offset
   * and returns the number of bytes of encoded data just read.
   */
  public int getProcessedData(byte data[], int offset) 
  {
    int size = bits.getBufferSize();
    System.arraycopy(bits.getBuffer(), 0, data, offset, size);
    bits.init();
    return size;
  }

  /**
   * Returns the number of bytes of encoded data ready to be read.
   */
  public int getProcessedDataByteSize() 
  {
    return bits.getBufferSize();	
  }
  
  /**
   * This is where the actual encoding takes place
   */
  public boolean processData(byte data[], int offset, int len)
  {
    // read raw bytes into samples
    mapPcm16bitLittleEndian2Float(data, offset, rawData, 0, len/2);
    // encode the bitstream
    if (channelCount==2) {
      Stereo.encode(bits, rawData, len/2);
    }
    encoder.encode(bits, rawData);
    return true;
  }

  /**
   * Converts a 16 bit linear PCM stream (in the form of a byte array)
   * into a floating point PCM stream (in the form of an float array).
   * Here are some important details about the encoding:
   * <ul>
   * <li> Java uses big endian for shorts and ints, and Windows uses little Endian.
   *      Therefore, shorts and ints must be read as sequences of bytes and
   *      combined with shifting operations.
   * </ul>
   * @param pcm16bitBytes - byte array of linear 16-bit PCM formated audio.
   * @param samples - float array to receive the 16-bit linear audio samples.
   */
  public static void mapPcm16bitLittleEndian2Float(byte[] pcm16bitBytes, int offsetInput, float[] samples, int offsetOutput, int length)
  {
    if (pcm16bitBytes.length - offsetInput < 2 * length) {
      throw new IllegalArgumentException("Insufficient Samples to convert to floats");
    }
    if (samples.length - offsetOutput < length) {
      throw new IllegalArgumentException("Insufficient float buffer to convert the samples");
    }
    for (int i = 0; i < length; i++) {
      samples[offsetOutput+i] = (float)((pcm16bitBytes[offsetInput+2*i] & 0xff) | (pcm16bitBytes[offsetInput+2*i+1] << 8)); // no & 0xff at the end to keep the sign
    }
  }
}
