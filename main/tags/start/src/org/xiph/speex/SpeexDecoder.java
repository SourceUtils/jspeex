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
 * Class: SpeexDecoder.java                                                   *
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

package org.xiph.speex;
 
/**
 * Main Speex Decoder class.
 * This class decodes the given Speex packets into PCM 16bit samples.
 * 
 * <p>Here's an example that decodes and recovers one Speex packet.
 * <pre>
 * SpeexDecoder speexDecoder = new SpeexDecoder();
 * speexDecoder.processData(data, packetOffset, packetSize);
 * byte[] decoded = new byte[speexDecoder.getProcessedBataByteSize()];
 * speexDecoder.getProcessedData(decoded, 0);
 * </pre>
 */
public class SpeexDecoder
{
  public static final String VERSION = "Java Speex Decoder v0.7 ($Revision$)";

  private int           packetNo;
  private int           sampleRate;
  private int           channelCount;
  private float[]       decodedData;
  private short[]       outputData;
  private int           outputSize;
  private Bits          bits;
  private Decoder       decoder;
  private int           frameSize;
  private boolean       headerRead;
    
  /**
   * Constructor
   */
  public SpeexDecoder() 
  {	    
    bits = new Bits();
    packetNo     = 0;
    sampleRate   = 0;
    channelCount = 0;
    headerRead  = false;
  }
  
  /**
   * initialise the Speex Decoder.
   */
  public boolean init(int mode, int SampleRate, int channels)
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
      decoder = new NbDecoder();
      break;
//Wideband
    case 1:
      decoder = new WbDecoder();
      break;
    case 2:
      decoder = new UwbDecoder();
      break;
//*/
    default:
      return false;
    }  
    
    /* initialize the speex decoder */
    decoder.init();
    /* set decoder format and properties */
    this.frameSize  = decoder.getFrameSize();
    this.sampleRate = SampleRate;
    channelCount    = channels;
    int secondSize  = sampleRate*channelCount;
    decodedData     = new float[secondSize*2];
    outputData      = new short[secondSize*2];
    packetNo        = 0;
    outputSize      = 0;
    headerRead = true;
    bits.init();
    return true;
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
   * Pull the decoded data out into a byte array at the given offset
   * and returns tne number of bytes processed and just read
   */
  public int getProcessedData(byte data[], int offset) 
  {    
    if (outputSize<=0) {
      return outputSize;
    }
    for(int i=0; i<outputSize; i++) {
      int dx     =  offset + (i<<1);
      data[dx]   = (byte) (outputData[i] & 0xff);
      data[dx+1] = (byte) ((outputData[i] >> 8) &  0xff );
    }
    int size = outputSize*2;
    outputSize = 0;
    return size;
  }

  /**
   * Returns tne number of bytes processed and ready to be read
   */
  public int getProcessedDataByteSize() 
  {
    return (outputSize*2);	
  }
  
  /**
   * This is where the actual decoding takes place
   */
  public boolean processData(byte data[], int offset, int len)
  {
    int i=0;
    
    /* if we tried to read bad packet header, input stream is in error */
    if (packetNo>0 && !headerRead) {
      return false;
    }
    
    /* if first packet, process as Speex header*/
    if (!headerRead) {
      if (!readHeader(data, offset, len)) {
        packetNo++;
        return false;
      }
    }
    else if (packetNo!=1) /* ignores comment packets */
    {
      /* read packet bytes into bitstream */
      bits.read_from(data, offset, len);
      /* decode the bitstream */
      decoder.decode(bits, decodedData);
      if (channelCount == 2)
        decoder.decodeStereo(decodedData, frameSize);

      /* PCM saturation */
      for (i=0;i<frameSize*channelCount;i++) {
        if (decodedData[i]>32767.0f)
          decodedData[i]=32767.0f;
        else if (decodedData[i]<-32768.0f)
          decodedData[i]=-32768.0f;
      }

      /* convert to short and save to buffer */
      for (i=0; i<frameSize*channelCount; i++, outputSize++) {
        outputData[outputSize]= (decodedData[i]>0) ? 
                                    (short) (decodedData[i]+.5) : (short)(decodedData[i]-.5);
      } 
    }
    packetNo++;
    return true;
  }

  /**
   *  Reads the header packet. 
   */
  private boolean readHeader(byte[] packet, int offset, int bytes)
  {
    int mode;
    if (bytes!=80) {
      return false;
    }
    if (!"Speex   ".equals(new String(packet, 0, 8))) {
      return false;
    }
    mode         = packet[40+offset] & 0xFF;
    sampleRate   = bytestoint(packet, offset+36);
    channelCount = bytestoint(packet, offset+48);
    return init(mode, sampleRate, channelCount);
  }	
  
  /**
   * Converts the bytes from the given array to an integer
   * @param a - the array
   * @param i - the offset
   */
  private int bytestoint(byte[] a, int i)
  {
    return ((a[i+3] & 0xFF) << 24) | ((a[i+2] & 0xFF) << 16) | ((a[i+1] & 0xFF) << 8) | (a[i+0] & 0xFF);
  }
}
