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
 * Class: WbEncoder.java                                                      *
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
 * Wideband Speex Encoder
 */
public class WbEncoder
  extends Encoder
  implements QmfTables
{
  public static final int[] NB_QUALITY_MAP = {1, 8, 2, 3, 4, 5, 5, 6, 6, 7, 7};
  public static final int[] WB_QUALITY_MAP = {1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4};

  protected Encoder lowenc;
  
  private int   full_frame_size;
  private int   frame_size;
  private int   subframeSize;
  private int   nbSubframes;
  private int   windowSize;
  private int   bufSize;
  private int   lpcSize;
  private float lag_factor;
  private float lpc_floor;
  private float gamma1;         /**< Perceptual filter: A(z/gamma1) */
  private float gamma2;         /**< Perceptual filter: A(z/gamma2) */
  private int   first;
//  private float folding_gain;

  private float[] x0d;
  private float[] x1d;
  private float[] high;
  private float[] y0, y1;
  private float[] h0_mem;
  private float[] g0_mem, g1_mem;

  private float buf[];
  private float excBuf[];
  private int   excIdx;
  private float swBuf[];        /**< Weighted signal buffer */
  private float res[];
  private float target[];
  private float window[];
  private float lagWindow[];

  private float[] rc;           /**< Reflection coefficients */
  private float[] autocorr;     /**< auto-correlation */
  private float[] lpc;          /**< LPCs for current frame */
  private float[] lsp;          /**< LSPs for current frame */
  private float[] qlsp;         /**< Quantized LSPs for current frame */
  private float[] old_lsp;      /**< LSPs for previous frame */
  private float[] old_qlsp;     /**< Quantized LSPs for previous frame */
  private float[] interp_lsp;   /**< Interpolated LSPs */
  private float[] interp_qlsp;  /**< Interpolated quantized LSPs */
  private float[] interp_lpc;   /**< Interpolated LPCs */
  private float[] interp_qlpc;  /**< Interpolated quantized LPCs */
  private float[] bw_lpc1;      /**< LPCs after bandwidth expansion by gamma1 for perceptual weighting*/
  private float[] bw_lpc2;      /**< LPCs after bandwidth expansion by gamma2 for perceptual weighting*/

  private float[] pi_gain;
  private float[] mem_sp;        /**< Filter memory for synthesis signal */
  private float[] mem_sp2;
  private float[] mem_sw;        /**< Filter memory for perceptually-weighted signal */

  protected int nb_modes;

  /**
   * Initialisation
   */
  public void init()
  {
    lowenc = new NbEncoder();
    sbinit(160, 40, 8, 640, ModesWB.wbsubmodes, 3, .9f);
    nb_modes = 5;
    sampling_rate = 16000;
  }

  /**
   * Initialisation
   */
  protected void sbinit(int mframeSize, int msubframeSize, int mlpcSize, int mbufSize, 
                        SubMode msubmodes[], int mdefaultSubmode, float mfolding_gain)
  {
    lowenc.init();
    super.init();
    first=1;

    full_frame_size = 2*mframeSize;
    frame_size      = mframeSize;
    windowSize      = frame_size*3/2;
    subframeSize    = msubframeSize;
    nbSubframes     = mframeSize/msubframeSize;
    lpcSize         = 8;
    bufSize         = mbufSize;
//    folding_gain    = mfolding_gain;
    gamma1          = 0.9f;
    gamma2          = 0.6f;
    lag_factor      = 0.002f;
    lpc_floor       = 1.0001f;

    submodes        = msubmodes;
    submodeID       = mdefaultSubmode;
    submodeSelect   = mdefaultSubmode;

    x0d         = new float[frame_size];
    x1d         = new float[frame_size];
    high        = new float[full_frame_size];
    y0          = new float[full_frame_size];
    y1          = new float[full_frame_size];

    h0_mem      = new float[QMF_ORDER];
    g0_mem      = new float[QMF_ORDER];
    g1_mem      = new float[QMF_ORDER];

    buf         = new float[windowSize];
    excBuf      = new float[bufSize];
    excIdx      = bufSize - windowSize;
    swBuf       = new float[frame_size];
    res         = new float[frame_size];
    target      = new float[subframeSize];

    window = window(windowSize, subframeSize);
    lagWindow = new float[lpcSize+1];
    for (int i=0; i<lpcSize+1; i++)
      lagWindow[i]=(float) Math.exp(-.5*(2*Math.PI*lag_factor*i)*(2*Math.PI*lag_factor*i));

    rc          = new float[lpcSize];
    autocorr    = new float[lpcSize+1];
    lpc         = new float[lpcSize+1];
    lsp         = new float[lpcSize];
    qlsp        = new float[lpcSize];
    old_lsp     = new float[lpcSize];
    old_qlsp    = new float[lpcSize];
    interp_lsp  = new float[lpcSize];
    interp_qlsp = new float[lpcSize];
    interp_lpc  = new float[lpcSize+1];
    interp_qlpc = new float[lpcSize+1];
    bw_lpc1     = new float[lpcSize+1];
    bw_lpc2     = new float[lpcSize+1];

    pi_gain     = new float[nbSubframes];
    mem_sp      = new float[lpcSize];
    mem_sp2     = new float[lpcSize];
    mem_sw      = new float[lpcSize];
    
    abr_count   = 0;
  }  

  /**
   * Encode the given input signal.
   */
  public int encode(Bits bits, float in[])
  {
    int i;
    float[] mem, innov, syn_resp;
    float[] low_pi_gain, low_exc, low_innov;
    int dtx;
    
    /* Compute the two sub-bands by filtering with h0 and h1*/
    Filters.qmf_decomp(in, h0, x0d, x1d, full_frame_size, QMF_ORDER, h0_mem);
    /* Encode the narrowband part*/
    lowenc.encode(bits, x0d);

    /* High-band buffering / sync with low band */
    for (i=0;i<windowSize-frame_size;i++)
      high[i] = high[frame_size+i];
    for (i=0;i<frame_size;i++)
      high[windowSize-frame_size+i]=x1d[i];

    System.arraycopy(excBuf, frame_size, excBuf, 0, bufSize-frame_size);

    low_pi_gain = lowenc.getPiGain();
    low_exc     = lowenc.getExc();
    low_innov   = lowenc.getInnov();

    int low_mode = lowenc.getMode();
    if (low_mode==0)
      dtx=1;
    else
      dtx=0;

    /* Start encoding the high-band */
    for (i=0; i<windowSize; i++)
      buf[i] = high[i] * window[i];

    /* Compute auto-correlation */
    Lpc.autocorr(buf, autocorr, lpcSize+1, windowSize);

    autocorr[0] += 1;        /* prevents NANs */
    autocorr[0] *= lpc_floor; /* Noise floor in auto-correlation domain */
    /* Lag windowing: equivalent to filtering in the power-spectrum domain */
    for (i=0; i<lpcSize+1; i++)
      autocorr[i] *= lagWindow[i];

    /* Levinson-Durbin */
    float tmperr = Lpc.wld(lpc, autocorr, rc, lpcSize);
    System.arraycopy(lpc, 0, lpc, 1, lpcSize);
    lpc[0]=1;

    /* LPC to LSPs (x-domain) transform */
    int roots = Lsp.lpc2lsp (lpc, lpcSize, lsp, 15, 0.2f);
    if (roots != lpcSize) {
      roots = Lsp.lpc2lsp (lpc, lpcSize, lsp, 11, 0.02f);
      if (roots != lpcSize) {
        /*If we can't find all LSP's, do some damage control and use a flat filter*/
        for (i=0; i<lpcSize; i++) {
          lsp[i]=(float)Math.cos(Math.PI*((float)(i+1))/(lpcSize+1));
        }
      }
    }

    /* x-domain to angle domain*/
    for (i=0; i<lpcSize; i++)
      lsp[i] = (float) Math.acos(lsp[i]);

    float lsp_dist=0;
    for (i=0;i<lpcSize;i++)
       lsp_dist += (old_lsp[i] - lsp[i])*(old_lsp[i] - lsp[i]);

    /*VBR stuff*/
    if ((vbr_enabled != 0 || vad_enabled != 0) && dtx == 0) {
      float e_low=0, e_high=0;
      float ratio;
      if (abr_enabled != 0) {
        float qual_change=0;
        if (abr_drift2 * abr_drift > 0) {
          /* Only adapt if long-term and short-term drift are the same sign */
          qual_change = -.00001f*abr_drift/(1+abr_count);
          if (qual_change>.1f)
            qual_change=.1f;
          if (qual_change<-.1f)
            qual_change=-.1f;
        }
        vbr_quality += qual_change;
        if (vbr_quality>10)
          vbr_quality=10;
        if (vbr_quality<0)
          vbr_quality=0;
      }

      for (i=0;i<frame_size;i++) {
        e_low  += x0d[i]* x0d[i];
        e_high += high[i]* high[i];
      }
      ratio = (float) Math.log((1+e_high)/(1+e_low));
      relative_quality = lowenc.getRelativeQuality();

      if (ratio<-4)
        ratio=-4;
      if (ratio>2)
        ratio=2;
      /*if (ratio>-2)*/
      if (vbr_enabled != 0) {
        int modeid;
        modeid = nb_modes-1;
        relative_quality+=1.0*(ratio+2);
        if (relative_quality<-1) {
          relative_quality=-1;
        }
        while (modeid != 0) {
          int v1;
          float thresh;
          v1=(int)Math.floor(vbr_quality);
          if (v1==10)
            thresh = Vbr.hb_thresh[modeid][v1];
          else
            thresh = (vbr_quality-v1)   * Vbr.hb_thresh[modeid][v1+1] + 
                     (1+v1-vbr_quality) * Vbr.hb_thresh[modeid][v1];
          if (relative_quality >= thresh)
            break;
          modeid--;
        }
        setMode(modeid);
        if (abr_enabled != 0)
        {
          int bitrate;
          bitrate = getBitRate();
          abr_drift+=(bitrate-abr_enabled);
          abr_drift2 = .95f*abr_drift2 + .05f*(bitrate-abr_enabled);
          abr_count += 1.0;
        }
      }
      else {
        /* VAD only */
        int modeid;
        if (relative_quality<2.0)
          modeid=1;
        else
          modeid=submodeSelect;
        /*speex_encoder_ctl(state, SPEEX_SET_MODE, &mode);*/
        submodeID=modeid;

      }
      /*fprintf (stderr, "%f %f\n", ratio, low_qual);*/
    }
    
    bits.pack(1, 1);
    if (dtx != 0)
      bits.pack(0, ModesWB.SB_SUBMODE_BITS);
    else
      bits.pack(submodeID, ModesWB.SB_SUBMODE_BITS);

    /* If null mode (no transmission), just set a couple things to zero*/
    if (dtx != 0 || submodes[submodeID] == null)
    {
      for (i=0; i<frame_size; i++)
        excBuf[excIdx+i]=swBuf[i]=0;

      for (i=0; i<lpcSize; i++)
        mem_sw[i]=0;
      first=1;

      /* Final signal synthesis from excitation */
      Filters.iir_mem2(excBuf, excIdx, interp_qlpc, high, 0, subframeSize, lpcSize, mem_sp);

      /* Reconstruct the original */
      filters.fir_mem_up(x0d, h0, y0, full_frame_size, QMF_ORDER, g0_mem);
      filters.fir_mem_up(high, h1, y1, full_frame_size, QMF_ORDER, g1_mem);

      for (i=0; i<full_frame_size; i++)
        in[i]=2*(y0[i]-y1[i]);

      if (dtx != 0)
         return 0;
      else
         return 1;
    }

    /* LSP quantization */
    submodes[submodeID].lsqQuant.quant(lsp, qlsp, lpcSize, bits);   

    if (first != 0)
    {
      for (i=0; i<lpcSize; i++)
        old_lsp[i] = lsp[i];
      for (i=0; i<lpcSize; i++)
        old_qlsp[i] = qlsp[i];
    }
   
    mem      = new float[lpcSize];
    syn_resp = new float[subframeSize];
    innov    = new float[subframeSize];

    for (int sub=0; sub<nbSubframes; sub++) {
      float tmp, filter_ratio;
      int exc, sp, sw, resp;
      int offset;
      float rl, rh, eh=0, el=0;
      int fold;

      offset = subframeSize*sub;
      sp=offset;
      exc=excIdx+offset;
      resp=offset;
      sw=offset;

      /* LSP interpolation (quantized and unquantized) */
      tmp = (1.0f + sub)/nbSubframes;
      for (i=0; i<lpcSize; i++)
        interp_lsp[i] = (1-tmp)*old_lsp[i] + tmp*lsp[i];
      for (i=0; i<lpcSize; i++)
        interp_qlsp[i] = (1-tmp)*old_qlsp[i] + tmp*qlsp[i];

      Lsp.enforce_margin(interp_lsp, lpcSize, .05f);
      Lsp.enforce_margin(interp_qlsp, lpcSize, .05f);

      /* Compute interpolated LPCs (quantized and unquantized) */
      for (i=0; i<lpcSize; i++)
        interp_lsp[i] = (float) Math.cos(interp_lsp[i]);
      for (i=0; i<lpcSize; i++)
        interp_qlsp[i] = (float) Math.cos(interp_qlsp[i]);

      m_lsp.lsp2lpc(interp_lsp, interp_lpc, lpcSize);
      m_lsp.lsp2lpc(interp_qlsp, interp_qlpc, lpcSize);

      Filters.bw_lpc(gamma1, interp_lpc, bw_lpc1, lpcSize);
      Filters.bw_lpc(gamma2, interp_lpc, bw_lpc2, lpcSize);

      /* Compute mid-band (4000 Hz for wideband) response of low-band and high-band
         filters */
      rl=rh=0;
      tmp=1;
      pi_gain[sub]=0;
      for (i=0; i<=lpcSize; i++) {
         rh += tmp*interp_qlpc[i];
         tmp = -tmp;
         pi_gain[sub]+=interp_qlpc[i];
      }
      rl = low_pi_gain[sub];
      rl=1/(Math.abs(rl)+.01f);
      rh=1/(Math.abs(rh)+.01f);
      /* Compute ratio, will help predict the gain */
      filter_ratio=Math.abs(.01f+rh)/(.01f+Math.abs(rl));

      fold = filter_ratio<5 ? 1 : 0;
      /*printf ("filter_ratio %f\n", filter_ratio);*/
      fold=0;

      /* Compute "real excitation" */
      Filters.fir_mem2(high, sp, interp_qlpc, excBuf, exc, subframeSize, lpcSize, mem_sp2);
      /* Compute energy of low-band and high-band excitation */
      for (i=0; i<subframeSize; i++)
         eh+=excBuf[exc+i]*excBuf[exc+i];

      if (submodes[submodeID].innovation == null) {/* 1 for spectral folding excitation, 0 for stochastic */
        float g;
        /*speex_bits_pack(bits, 1, 1);*/
        for (i=0; i<subframeSize; i++)
          el+=low_innov[offset+i]*low_innov[offset+i];

        /* Gain to use if we want to use the low-band excitation for high-band */
        g=eh/(.01f+el);
        g=(float) Math.sqrt(g);

        g *= filter_ratio;
        /*print_vec(&g, 1, "gain factor");*/
        /* Gain quantization */
        {
          int quant = (int) Math.floor(.5 + 10 + 8.0 * Math.log((g+.0001)));
          /*speex_warning_int("tata", quant);*/
          if (quant<0)
            quant=0;
          if (quant>31)
            quant=31;
          bits.pack(quant, 5);
          g=(float)(.1*Math.exp(quant/9.4));
        }
        /*printf ("folding gain: %f\n", g);*/
        g /= filter_ratio;

      } else {
        float gc, scale, scale_1;

        for (i=0; i<subframeSize; i++)
          el+=low_exc[offset+i]*low_exc[offset+i];
        /*speex_bits_pack(bits, 0, 1);*/

        gc = (float) (Math.sqrt(1+eh)*filter_ratio/Math.sqrt((1+el)*subframeSize));
        {
          int qgc = (int)Math.floor(.5+3.7*(Math.log(gc)+2));
          if (qgc<0)
            qgc=0;
          if (qgc>15)
            qgc=15;
          bits.pack(qgc, 4);
          gc = (float) Math.exp((1/3.7)*qgc-2);
        }

        scale = gc*(float)Math.sqrt(1+el)/filter_ratio;
        scale_1 = 1/scale;

        for (i=0; i<subframeSize; i++)
          excBuf[exc+i]=0;
        excBuf[exc]=1;
        Filters.syn_percep_zero(excBuf, exc, interp_qlpc, bw_lpc1, bw_lpc2, syn_resp, subframeSize, lpcSize);

        /* Reset excitation */
        for (i=0; i<subframeSize; i++)
          excBuf[exc+i]=0;
        
        /* Compute zero response (ringing) of A(z/g1) / ( A(z/g2) * Aq(z) ) */
        for (i=0; i<lpcSize; i++)
          mem[i]=mem_sp[i];
        Filters.iir_mem2(excBuf, exc, interp_qlpc, excBuf, exc, subframeSize, lpcSize, mem);

        for (i=0; i<lpcSize; i++)
          mem[i]=mem_sw[i];
        Filters.filter_mem2(excBuf, exc, bw_lpc1, bw_lpc2, res, resp, subframeSize, lpcSize, mem, 0);

        /* Compute weighted signal */
        for (i=0; i<lpcSize; i++)
          mem[i]=mem_sw[i];
        Filters.filter_mem2(high, sp, bw_lpc1, bw_lpc2, swBuf, sw, subframeSize, lpcSize, mem, 0);

        /* Compute target signal */
        for (i=0; i<subframeSize; i++)
          target[i]=swBuf[sw+i]-res[resp+i];

        for (i=0; i<subframeSize; i++)
          excBuf[exc+i]=0;

        for (i=0; i<subframeSize; i++)
          target[i]*=scale_1;
        
        /* Reset excitation */
        for (i=0; i<subframeSize; i++)
          innov[i]=0;

        /*print_vec(target, st->subframeSize, "\ntarget");*/
        submodes[submodeID].innovation.quant(target, interp_qlpc, bw_lpc1, bw_lpc2, 
                                             lpcSize, subframeSize, innov, 0, syn_resp,
                                             bits, (complexity+1)>>1);
        /*print_vec(target, st->subframeSize, "after");*/

        for (i=0; i<subframeSize; i++)
          excBuf[exc+i] += innov[i]*scale;

        if (submodes[submodeID].double_codebook != 0) {
          float[] innov2 = new float[subframeSize];
          for (i=0; i<subframeSize; i++)
            innov2[i]=0;
          for (i=0; i<subframeSize; i++)
            target[i]*=2.5;
          submodes[submodeID].innovation.quant(target, interp_qlpc, bw_lpc1, bw_lpc2, 
                                               lpcSize, subframeSize, innov2, 0, syn_resp, 
                                               bits, (complexity+1)>>1);
          for (i=0; i<subframeSize; i++)
            innov2[i]*=scale*(1/2.5);
          for (i=0; i<subframeSize; i++)
            excBuf[exc+i] += innov2[i];
        }
      }

      /*Keep the previous memory*/
      for (i=0; i<lpcSize; i++)
        mem[i]=mem_sp[i];
      /* Final signal synthesis from excitation */
      Filters.iir_mem2(excBuf, exc, interp_qlpc, high, sp, subframeSize, lpcSize, mem_sp);
               
      /* Compute weighted signal again, from synthesized speech (not sure it's the right thing) */
      Filters.filter_mem2(high, sp, bw_lpc1, bw_lpc2, swBuf, sw, subframeSize, lpcSize, mem_sw, 0);
    }

//#ifndef RELEASE
    /* Reconstruct the original */
    filters.fir_mem_up(x0d, h0, y0, full_frame_size, QMF_ORDER, g0_mem);
    filters.fir_mem_up(high, h1, y1, full_frame_size, QMF_ORDER, g1_mem);

    for (i=0; i<full_frame_size; i++)
      in[i]=2*(y0[i]-y1[i]);
//#endif
   for (i=0; i<lpcSize; i++)
     old_lsp[i] = lsp[i];
   for (i=0; i<lpcSize; i++)
     old_qlsp[i] = qlsp[i];
   first=0;
   return 1;
 }

  //---------------------------------------------------------------------------
  // Speex Control Functions
  //---------------------------------------------------------------------------

  /**
   * 
   */
  public int getFrameSize()
  {
    return full_frame_size;
  }
  
  /**
   * Sets the Quality.
   */
  public void setQuality(int quality)
  {
    if (quality < 0) {
      quality = 0;
    }
    if (quality > 10) {
      quality = 10;
    }
    lowenc.setMode(NB_QUALITY_MAP[quality]);
    this.setMode(WB_QUALITY_MAP[quality]);
  }
  
  /**
   * Sets the Varible Bit Rate Quality.
   */
  public void setVbrQuality(float quality)
  {
    vbr_quality = quality;
    float qual = quality + 0.6f;
    if (qual>10)
      qual=10;
    lowenc.setVbrQuality(qual);
    int q = (int)Math.floor(.5+quality);
    if (q>10)
      q=10;
    setQuality(q);
  }
  
  /**
   * Sets whether or not to use Variable Bit Rate encoding.
   */
  public void    setVbr(boolean vbr)
  {
    super.setVbr(vbr);
    lowenc.setVbr(vbr);
  }
  
  /**
   * Sets the Average Bit Rate.
   */
  public void    setAbr(int abr)
  {
    lowenc.setVbr(true);
    super.setAbr(abr);
  }

  /**
   * Returns the bitrate.
   */
  public int getBitRate()
  {
    if (submodes[submodeID] != null)
      return lowenc.getBitRate() + sampling_rate*submodes[submodeID].bits_per_frame/frame_size;
    else
      return lowenc.getBitRate() + sampling_rate*(ModesWB.SB_SUBMODE_BITS+1)/frame_size;
  }
  
  /**
   * Sets the sampling rate.
   */
  public void setSamplingRate(int rate)
  {
    super.setSamplingRate(rate);
    lowenc.setSamplingRate(rate);
  }
    
  /**
   * 
   */
//  public void resetState()
//  {
//  }
  
  /**
   * Returns the Pitch Gain array.
   */
  public float[] getPiGain()
  {
    return pi_gain;
  }
  
  /**
   * Returns the excitation array.
   */
  public float[] getExc()
  {
    float[] excTmp = new float[full_frame_size];
//    for (int i=0;i<full_frame_size;i++)
//      excTmp[i]=0;
    for (int i=0;i<frame_size;i++)
      excTmp[2*i]=2*excBuf[excIdx+i];
    return excTmp;
  }
  
  /**
   * Returns the innovation array.
   */
  public float[] getInnov()
  {
    return getExc();
  }
}
