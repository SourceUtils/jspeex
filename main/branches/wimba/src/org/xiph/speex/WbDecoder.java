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
 * Class: WbDecoder.java                                                      *
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
 * Wideband Speex Decoder
 */
public class WbDecoder
  extends Decoder
  implements QmfTables
{
  protected Decoder lowdec;
  
  private int   full_frame_size;
  private int   frame_size;
  private int   subframeSize;
  private int   nbSubframes;
  private int   lpcSize;
  private int   first;
  private float folding_gain;

  private float x0d[];
  private float high[];
  private float y0[], y1[];
  private float g0_mem[], g1_mem[];

  private float exc[];
  private float qlsp[];
  private float old_qlsp[];
  private float interp_qlsp[];
  private float interp_qlpc[];

  private float mem_sp[];
  private float pi_gain[];
  private float awk1[], awk2[], awk3[];
  private float innov2[];

  private SubMode  submodes[];
  private int      submodeID;
  
  /**
   * Initialisation
   */
  public void init()
  {
    lowdec = new NbDecoder();
    sbinit(160, 40, 8, 640, ModesWB.wbsubmodes, 3, .7f);
  }

  /**
   * Initialisation
   */
  protected void sbinit(int  mframeSize, int msubframeSize, int mlpcSize, int mbufSize, 
                        SubMode msubmodes[], int mdefaultSubmode, float mfolding_gain)
  {
    lowdec.init();

    full_frame_size = 2*mframeSize;
    frame_size      = mframeSize;
    subframeSize    = msubframeSize;
    nbSubframes     = mframeSize/msubframeSize;
    lpcSize         = 8;
    folding_gain    = mfolding_gain;
    submodes        = msubmodes;
    submodeID       = mdefaultSubmode;
    first=1;

    x0d         = new float[frame_size];
    high        = new float[full_frame_size];
    y0          = new float[full_frame_size];
    y1          = new float[full_frame_size];

    g0_mem      = new float[QMF_ORDER];
    g1_mem      = new float[QMF_ORDER];

    exc         = new float[frame_size];

    qlsp        = new float[lpcSize];
    old_qlsp    = new float[lpcSize];
    interp_qlsp = new float[lpcSize];
    interp_qlpc = new float[(lpcSize+1)];

    pi_gain     = new float[nbSubframes];
    mem_sp      = new float[2*lpcSize];
    
    awk1        = new float[9];
    awk2        = new float[9];
    awk3        = new float[9];  
    innov2      = new float[subframeSize];
  }

  /**
   * Decode the given bits
   */
  public int decode(Bits bits, float out[])
  {
    int i, sub, wideband, ret;
    float low_pi_gain[], low_exc[], low_innov[];

    /* Decode the low-band */
    if ((ret = lowdec.decode(bits, x0d))!=0) {
      return ret;
    }
    /* vheck "wideband bit" */
    wideband = bits.peek(); 
    if (wideband!=0) {
      /*Regular wideband frame, read the submode*/
      wideband  = bits.unpack(1);
      submodeID = bits.unpack(3);
    } 
    else {
      /* was a narrowband frame, set "null submode"*/
      submodeID = 0;
    }

    for (i=0;i<frame_size;i++)
      exc[i]=0;

    /* If null mode (no transmission), just set a couple things to zero*/
    if (submodes[submodeID] == null) {
      for (i=0;i<frame_size;i++)
        exc[i]=0;

      first=1;
      /* Final signal synthesis from excitation */
      filters.iir_mem2(exc, 0, interp_qlpc, high, 0, frame_size, lpcSize, mem_sp);
      filters.fir_mem_up(x0d, h0, y0, full_frame_size, QMF_ORDER, g0_mem);
      filters.fir_mem_up(high, h1, y1, full_frame_size, QMF_ORDER, g1_mem);

      for (i=0;i<full_frame_size;i++)
        out[i]=2*(y0[i]-y1[i]);
      return 0;
    }
    low_pi_gain = lowdec.getPiGain();
    low_exc     = lowdec.getExc();
    low_innov   = lowdec.getInnov();
    submodes[submodeID].lsqQuant.unquant(qlsp, lpcSize, bits);
    
    if (first!=0) {
      for (i=0;i<lpcSize;i++)
        old_qlsp[i] = qlsp[i];
    }

    for (sub=0;sub<nbSubframes;sub++) {
      float tmp, filter_ratio, el=0.0f, rl=0.0f,rh=0.0f;
      int subIdx=subframeSize*sub;
      
      /* LSP interpolation */
      tmp = (1.0f + sub)/nbSubframes;
      for (i=0;i<lpcSize;i++)
        interp_qlsp[i] = (1-tmp)*old_qlsp[i] + tmp*qlsp[i];

      m_lsp.enforce_margin(interp_qlsp, lpcSize, .05f);

      /* LSPs to x-domain */
      for (i=0;i<lpcSize;i++)
        interp_qlsp[i] = (float)Math.cos(interp_qlsp[i]);

      /* LSP to LPC */
      m_lsp.lsp2lpc(interp_qlsp, interp_qlpc, lpcSize);

      float r=.9f, k1,k2,k3;
      k1=submodes[submodeID].lpc_enh_k1;
      k2=submodes[submodeID].lpc_enh_k2;
      k3=k1-k2;
      filters.bw_lpc(k1, interp_qlpc, awk1, lpcSize);
      filters.bw_lpc(k2, interp_qlpc, awk2, lpcSize);
      filters.bw_lpc(k3, interp_qlpc, awk3, lpcSize);

      /* Calculate reponse ratio between low & high filter in band middle (4000 Hz) */      
      tmp=1;
      pi_gain[sub]=0;
      for (i=0;i<=lpcSize;i++) {
        rh += tmp*interp_qlpc[i];
        tmp = -tmp;
        pi_gain[sub]+=interp_qlpc[i];
      }
      rl           = low_pi_gain[sub];
      rl           = 1/(Math.abs(rl)+.01f);
      rh           = 1/(Math.abs(rh)+.01f);
      filter_ratio = Math.abs(.01f+rh)/(.01f+Math.abs(rl));
      
      /* reset excitation buffer */
      for (i=subIdx;i<subIdx+subframeSize;i++)
        exc[i]=0;

      if (submodes[submodeID].innovation==null) {
        float g;
        int quant;

        quant = bits.unpack(5);
        g     = (float)Math.exp(((double)quant-10)/8.0);       
        g     /= filter_ratio;
        
        /* High-band excitation using the low-band excitation and a gain */
        for (i=subIdx;i<subIdx+subframeSize;i++)
          exc[i]=folding_gain*g*low_innov[i];
      } 
      else {
        float gc, scale;
        int qgc = bits.unpack(4);

        for (i=subIdx;i<subIdx+subframeSize;i++)
          el+=low_exc[i]*low_exc[i];

        gc    = (float)Math.exp((1/3.7f)*qgc-2);
        scale = gc*(float)Math.sqrt(1+el)/filter_ratio;
        submodes[submodeID].innovation.unquant(exc, subIdx, subframeSize, bits); 

        for (i=subIdx;i<subIdx+subframeSize;i++)
          exc[i]*=scale;

        if (submodes[submodeID].double_codebook!=0) {
          for (i=0;i<subframeSize;i++)
            innov2[i]=0;
          submodes[submodeID].innovation.unquant(innov2, 0, subframeSize, bits); 
          for (i=0;i<subframeSize;i++)
            innov2[i]*=scale*(1/2.5f);
          for (i=0;i<subframeSize;i++)
            exc[subIdx+i] += innov2[i];
        }
      }

      for (i=subIdx;i<subIdx+subframeSize;i++)
        high[i]=exc[i];

      filters.filter_mem2(high, subIdx, awk2, awk1, subframeSize, lpcSize, mem_sp, lpcSize);
      filters.filter_mem2(high, subIdx, awk3, interp_qlpc, subframeSize, lpcSize, mem_sp, 0);
    }

    filters.fir_mem_up(x0d, h0, y0, full_frame_size, QMF_ORDER, g0_mem);
    filters.fir_mem_up(high, h1, y1, full_frame_size, QMF_ORDER, g1_mem);

    for (i=0;i<full_frame_size;i++)
      out[i]=2*(y0[i]-y1[i]);

    for (i=0;i<lpcSize;i++)
      old_qlsp[i] = qlsp[i];

    first = 0;
    return 0;
  }
    
  /**
   *
   */
  public int  getFrameSize()
  {
    return full_frame_size;
  }
    
  /**
   * Returns the Pitch Gain array
   */
  public float[] getPiGain()
  {
    return pi_gain;
  }
  
  /**
   * Returns the excitation array
   */
  public float[] getExc()
  {
    int i;
    float[] excTmp = new float[full_frame_size];
    for (i=0;i<full_frame_size;i++)
      excTmp[i]=0;
    for (i=0;i<frame_size;i++)
      excTmp[2*i]=2*exc[i];
    return excTmp;
  }
  
  /**
   * Returns the innovation array
   */
  public float[] getInnov()
  {
    return getExc();
  }
}