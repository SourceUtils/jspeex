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
 * Class: Encoder.java                                                        *
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
 * Abstract Speex Encoder, used as a base for the Narrowband, Wideband and
 * Ultra-wideband encoders.
 */
public abstract class Encoder
{
  protected Lsp      m_lsp;
  protected Filters  filters;
  
  protected int complexity;     /**< Complexity setting (0-10 from least complex to most complex) */
  protected int vbr_enabled;    /**< 1 for enabling VBR, 0 otherwise */
  protected int vad_enabled;    /**< 1 for enabling VAD, 0 otherwise */
  protected int dtx_enabled;    /**< 1 for enabling DTX, 0 otherwise */
  protected int abr_enabled;    /**< ABR setting (in bps), 0 if off */
  protected float vbr_quality;      /**< Quality setting for VBR encoding */
  protected float relative_quality; /**< Relative quality that will be needed by VBR */
  protected float abr_drift;
  protected float abr_drift2;
  protected float abr_count;
  protected int sampling_rate;

  protected SubMode submodes[];     /**< Sub-mode data */
  protected int     submodeID;      /**< Activated sub-mode */
  protected int     submodeSelect;  /**< Mode chosen by the user (may differ from submodeID if VAD is on) */

  /**
   * Constructor
   */
  public Encoder()
  {
    m_lsp   = new Lsp();
    filters = new Filters();
  }

  /**
   * Initialisation
   */
  public void init()
  {
    complexity  = 3; // in C it's 2 here, but set to 3 automatically by the encoder
    vbr_enabled = 0; // disabled by default
    vad_enabled = 0; // disabled by default
    dtx_enabled = 0; // disabled by default
    abr_enabled = 0; // disabled by default
    vbr_quality = 8;
  }

  /**
   * Encode the given input signal
   */
  abstract public int encode(Bits bits, float in[]);

  //---------------------------------------------------------------------------
  // Tools
  //---------------------------------------------------------------------------
  
  /**
   * Builds an Asymmetric "pseudo-Hamming" window
   */
  protected float[] window(int windowSize, int subFrameSize)
  {
    int i;
    int part1 = subFrameSize*7/2;
    int part2 = subFrameSize*5/2;
    float[] window = new float[windowSize];
    for (i=0; i<part1; i++)
      window[i]=(float)(.54-.46*Math.cos(Math.PI*i/part1));
    for (i=0; i<part2; i++)
      window[part1+i]=(float)(.54+.46*Math.cos(Math.PI*i/part2));
    return window;
  }
  
  //---------------------------------------------------------------------------
  // Speex Control Functions
  //---------------------------------------------------------------------------

  public abstract int     getFrameSize();
  public abstract void    setQuality(int quality);
  public abstract int     getBitRate();
//  public abstract void    resetState();

  /**
   * Returns the Pitch Gain array.
   */
  public abstract float[] getPiGain();

  /**
   * Returns the excitation array.
   */
  public abstract float[] getExc();
  
  /**
   * Returns the innovation array.
   */
  public abstract float[] getInnov();

  /**
   * Sets the encoding submode.
   */
  public void setMode(int mode)
  {
    submodeID = submodeSelect = mode;
  }
  
  /**
   * Returns the encoding submode currently in use.
   */
  public int getMode()
  {
    return submodeID;
  }
  
  /**
   * Sets the bitrate.
   */
  public void setBitRate(int bitrate)
  {
    for (int i=10; i>=0; i--) {
      setQuality(i);
      if (getBitRate() <= bitrate)
        return;
    }
  }
  
  /**
   * Sets whether or not to use Variable Bit Rate encoding.
   */
  public void setVbr(boolean vbr)
  {
    vbr_enabled = vbr ? 1 : 0;
  }
  
  /**
   * Returns whether or not we are using Variable Bit Rate encoding.
   */
  public boolean getVbr()
  {
    return vbr_enabled != 0;
  }
  
  /**
   * Sets whether or not to use Voice Activity Detection encoding.
   */
  public void setVad(boolean vad)
  {
    vad_enabled = vad ? 1 : 0;
  }
  
  /**
   * Returns whether or not we are using Voice Activity Detection encoding.
   */
  public boolean getVad()
  {
    return vad_enabled != 0;
  }
  
  /**
   * Sets whether or not to use Discontinuous Transmission encoding.
   */
  public void setDtx(boolean dtx)
  {
    dtx_enabled = dtx ? 1 : 0;
  }
  
  /**
   * Returns whether or not we are using Discontinuous Transmission encoding.
   */
  public boolean getDtx()
  {
    return dtx_enabled != 0;
  }

  /**
   * Returns the Average Bit Rate used (0 if ABR is not turned on).
   */
  public int getAbr()
  {
    return abr_enabled;
  }
  
  /**
   * Sets the Average Bit Rate.
   */
  public void    setAbr(int abr)
  {
    abr_enabled = (abr!=0) ? 1 : 0;
    vbr_enabled = 1;
    {
      int i=10, rate, target;
      float vbr_qual;
      target = abr;
      while (i>=0)
      {
        setQuality(i);
        rate = getBitRate();
        if (rate <= target)
          break;
        i--;
      }
      vbr_qual=i;
      if (vbr_qual<0)
        vbr_qual=0;
      setVbrQuality(vbr_qual);
      abr_count=0;
      abr_drift=0;
      abr_drift2=0;
    }
  }

  /**
   * Sets the Varible Bit Rate Quality.
   */
  public void setVbrQuality(float quality)
  {
    if (quality < 0)
      quality = 0;
    if (quality > 10)
      quality = 10;
    vbr_quality = quality;
  }
  
  /**
   * Returns the Varible Bit Rate Quality.
   */
  public float getVbrQuality()
  {
    return vbr_quality;
  }
  
  /**
   * Sets the algorthmic complexity.
   */
  public void setComplexity(int complexity)
  {
    if (complexity < 0)
      complexity = 0;
    if (complexity > 10)
      complexity = 10;
    this.complexity = complexity;
  }
  
  /**
   * Returns the algorthmic complexity.
   */
  public int getComplexity()
  {
    return complexity;
  }
  
  /**
   * Sets the sampling rate.
   */
  public void setSamplingRate(int rate)
  {
    sampling_rate = rate;
  }
    
  /**
   * Returns the sampling rate.
   */
  public int getSamplingRate()
  {
    return sampling_rate;
  }

  /**
   * Returns the relative quality.
   */
  public float getRelativeQuality()
  {
    return relative_quality;
  }
}
