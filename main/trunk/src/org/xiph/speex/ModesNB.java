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
 * Class: ModesNB.java                                                        *
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
 * Narrowband Modes
 */
public class ModesNB
  implements Tables
{
  public static final int NB_SUBMODES     = 16;
  public static final int NB_SUBMODE_BITS = 4;

  public static final int SB_SUBMODES     = 8;
  public static final int SB_SUBMODE_BITS = 3;

  public static SubMode nbsubmodes[];
    
  /**
   * initialisation
   */
  public void init()
  {
    Ltp3Tap ltpNb   = new Ltp3Tap(gain_cdbk_nb, 7, 7);
    Ltp3Tap ltpVlbr = new Ltp3Tap(gain_cdbk_lbr, 5, 0);
    Ltp3Tap ltpLbr  = new Ltp3Tap(gain_cdbk_lbr, 5, 7);
    Ltp3Tap ltpMed  = new Ltp3Tap(gain_cdbk_lbr, 5, 7);
    LtpForcedPitch ltpFP   = new LtpForcedPitch();

    NoiseSearch noiseSearch    = new NoiseSearch();
    SplitShapeSearch ssNbVlbrSearch = new SplitShapeSearch(40, 10, 4, exc_10_16_table, 4, 0);
    SplitShapeSearch ssNbLbrSearch  = new SplitShapeSearch(40, 10, 4, exc_10_32_table, 5, 0);
    SplitShapeSearch ssNbSearch     = new SplitShapeSearch(40, 5, 8, exc_5_64_table, 6, 0);
    SplitShapeSearch ssNbMedSearch  = new SplitShapeSearch(40, 8, 5, exc_8_128_table, 7, 0);
    SplitShapeSearch ssSbSearch     = new SplitShapeSearch(40, 5, 8, exc_5_256_table, 8, 0);
    SplitShapeSearch ssNbUlbrSearch = new SplitShapeSearch(40, 20, 2, exc_20_32_table, 5, 0);

    NbLspQuant nbLspQuant    = new NbLspQuant();
    LbrLspQuant lbrLspQuant  = new LbrLspQuant();
    
    /* initialize narrow-band modes */
    nbsubmodes = new SubMode[16];
    /* 2150 bps "vocoder-like" mode for comfort noise */
    nbsubmodes[1] = new SubMode(0, 1, 0, 0, lbrLspQuant, ltpFP, noiseSearch, .7f, .7f, -1, 43);
    /* 5.95 kbps very low bit-rate mode */
    nbsubmodes[2] = new SubMode(0, 0, 0, 0, lbrLspQuant, ltpVlbr, ssNbVlbrSearch, 0.7f, 0.5f, .55f, 119);
    /* 8 kbps low bit-rate mode */
    nbsubmodes[3] = new SubMode(-1, 0, 1, 0, lbrLspQuant, ltpLbr, ssNbLbrSearch, 0.7f, 0.55f, .45f, 160);
    /* 11 kbps medium bit-rate mode */
    nbsubmodes[4] = new SubMode(-1, 0, 1, 0, lbrLspQuant, ltpMed, ssNbMedSearch, 0.7f, 0.63f, .35f, 220);
    /* 15 kbps high bit-rate mode */
    nbsubmodes[5] = new SubMode(-1, 0, 3, 0, nbLspQuant, ltpNb, ssNbSearch, 0.7f, 0.65f, .25f, 300);
    /* 18.2 high bit-rate mode */
    nbsubmodes[6] = new SubMode(-1, 0, 3, 0, nbLspQuant, ltpNb, ssSbSearch, 0.68f, 0.65f, .1f, 364);
    /* 24.6 kbps high bit-rate mode */
    nbsubmodes[7] = new SubMode(-1, 0, 3, 1, nbLspQuant, ltpNb, ssNbSearch, 0.65f, 0.65f, -1, 492);
    /* 3.95 kbps very low bit-rate mode */
    nbsubmodes[8] = new SubMode(0, 1, 0, 0, lbrLspQuant, ltpFP, ssNbUlbrSearch, .7f, .5f, .65f, 79);
  }
}
