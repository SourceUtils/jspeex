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
 * Class: TestJSpeex.java                                                     *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 6th January 2004                                                     *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit Tests for JSpeex
 *
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class TestJSpeex
  extends TestCase
{
  public final static byte[] ENCODED_SILENCE_NB_Q01_MONO = {70, -99, 102, 0, 1, -100, -25, 57, -50, 114};
  public final static byte[] ENCODED_SILENCE_NB_Q02_MONO = {22, -99, 102, 0, 0, -18, -18, 7, 119, 112, 59, -69, -127, -35, -36};
  public final static byte[] ENCODED_SILENCE_NB_Q03_MONO = {30, -99, 102, 0, 0, 103, 57, -56, 16, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57};
  public final static byte[] ENCODED_SILENCE_NB_Q04_MONO = ENCODED_SILENCE_NB_Q03_MONO;
  public final static byte[] ENCODED_SILENCE_NB_Q05_MONO = {38, -99, 102, 0, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80};
  public final static byte[] ENCODED_SILENCE_NB_Q06_MONO = ENCODED_SILENCE_NB_Q05_MONO;
  public final static byte[] ENCODED_SILENCE_NB_Q07_MONO = {46, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -128, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16};
  public final static byte[] ENCODED_SILENCE_NB_Q08_MONO = ENCODED_SILENCE_NB_Q07_MONO;
  public final static byte[] ENCODED_SILENCE_NB_Q09_MONO = {54, -99, 27, -102, 32, 0, 1, 104, -24, -24, -24, -24, -24, -24, -24, -128, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16};
  public final static byte[] ENCODED_SILENCE_NB_Q10_MONO = {62, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -37, 109, -74, -37, 109, -74, -128, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -48};
  public final static byte[] ENCODED_SILENCE_WB_Q01_MONO = {70, -99, 102, 0, 1, -100, -25, 57, -50, 115, 39, 108, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q02_MONO = {22, -99, 102, 0, 0, -18, -18, 7, 119, 112, 59, -69, -127, -35, -35, 39, 108, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q03_MONO = {30, -99, 102, 0, 0, 103, 57, -56, 16, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57, -109, -74, 0, 0, 0,};
  public final static byte[] ENCODED_SILENCE_WB_Q04_MONO = {38, -99, 102, 0, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -71, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q05_MONO = {46, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -128, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -7, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q06_MONO = {46, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -128, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -6, 59, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96};
  public final static byte[] ENCODED_SILENCE_WB_Q07_MONO = {54, -99, 27, -102, 32, 0, 1, 104, -24, -24, -24, -24, -24, -24, -24, -128, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 26, 59, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96};
  public final static byte[] ENCODED_SILENCE_WB_Q08_MONO = {54, -99, 27, -102, 32, 0, 1, 104, -24, -24, -24, -24, -24, -24, -24, -128, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 27 , 59, 96, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80,};
  public final static byte[] ENCODED_SILENCE_WB_Q09_MONO = {62, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -37, 109, -74, -37, 109, -74, -128, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -37, 59, 96, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80};
  public final static byte[] ENCODED_SILENCE_WB_Q10_MONO = {62, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -37, 109, -74, -37, 109, -74, -128, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -36, 59, 96, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -78, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -78, -78, -78, -78, -78, -80};
  public final static byte[] ENCODED_SILENCE_UWB_Q01_MONO = {70, -99, 102, 0, 1, -100, -25, 57, -50, 115, 39, 108, 0, 0, 18, 118, -64, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q02_MONO = {22, -99, 102, 0, 0, -18, -18, 7, 119, 112, 59, -69, -127, -35, -35, 39, 108, 0, 0, 18, 118, -64, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q03_MONO = {30, -99, 102, 0, 0, 103, 57, -56, 16, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57, -109, -74, 0, 0, 9, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q04_MONO = {38, -99, 102, 0, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -80, 0, 27, 54, 108, -39, -71, 59, 96, 0, 0, -109, -74, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q05_MONO = {46, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -128, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -7, 59, 96, 0, 0, -109, -74, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q06_MONO = {46, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -128, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -6, 59, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96, -75, -83, 105, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q07_MONO = {54, -99, 27, -102, 32, 0, 1, 104, -24, -24, -24, -24, -24, -24, -24, -128, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 26, 59, 96, -75, -83, 96, -75, -83, 96, -75, -83, 96, -75, -83, 105, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q08_MONO = {54, -99, 27, -102, 32, 0, 1, 104, -24, -24, -24, -24, -24, -24, -24, -128, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 27, 59, 96, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -71, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q09_MONO = {62, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -37, 109, -74, -37, 109, -74, -128, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -37, 59, 96, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -71, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q10_MONO = {62, -99, 27, -102, 32, 0, 1, 127, -1, -1, -1, -1, -1, -37, 109, -74, -37, 109, -74, -128, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -36, 59, 96, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -78, -78, -78, -78, -78, -80, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 2, -78, -78, -78, -78, -78, -78, -78, -78, -78, -71, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_NB_Q01_STEREO = {116, -64, 35, 78, -77, 0, 0, -50, 115, -100, -25, 57};
  public final static byte[] ENCODED_SILENCE_NB_Q02_STEREO = {116, -64, 11, 78, -77, 0, 0, 119, 119, 3, -69, -72, 29, -35, -64, -18, -18};
  public final static byte[] ENCODED_SILENCE_NB_Q03_STEREO = {116, -64, 15, 78, -77, 0, 0, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57, 2, 6, 115, -100, -128};
  public final static byte[] ENCODED_SILENCE_NB_Q04_STEREO = ENCODED_SILENCE_NB_Q03_STEREO;
  public final static byte[] ENCODED_SILENCE_NB_Q05_STEREO = {116, -64, 19, 78, -77, 0, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40};
  public final static byte[] ENCODED_SILENCE_NB_Q06_STEREO = ENCODED_SILENCE_NB_Q05_STEREO;
  public final static byte[] ENCODED_SILENCE_NB_Q07_STEREO = {116, -64, 23, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16, 0, 23, -1, -1, -1, -1, -1, -8};
  public final static byte[] ENCODED_SILENCE_NB_Q08_STEREO = ENCODED_SILENCE_NB_Q07_STEREO;
  public final static byte[] ENCODED_SILENCE_NB_Q09_STEREO = {116, -64, 27, 78, -115, -51, 16, 0, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16, 0, 22, -114, -114, -114, -114, -114, -114, -114, -120};
  public final static byte[] ENCODED_SILENCE_NB_Q10_STEREO = {};
  public final static byte[] ENCODED_SILENCE_WB_Q01_STEREO = {116, -64, 35, 78, -77, 0, 0, -50, 115, -100, -25, 57, -109, -74, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q02_STEREO = {116, -64, 11, 78, -77, 0, 0, 119, 119, 3, -69, -72, 29, -35, -64, -18, -18, -109, -74, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q03_STEREO = {116, -64, 15, 78, -77, 0, 0, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57, 2, 6, 115, -100, -55, -37, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q04_STEREO = {116, -64, 19, 78, -77, 0, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -36, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q05_STEREO = {116, -64, 23, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16, 0, 23, -1, -1, -1, -1, -1, -4, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_WB_Q06_STEREO = {116, -64, 23, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16, 0, 23, -1, -1, -1, -1, -1, -3, 29, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80};
  public final static byte[] ENCODED_SILENCE_WB_Q07_STEREO = {116, -64, 27, 78, -115, -51, 16, 0, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16, 0, 22, -114, -114, -114, -114, -114, -114, -114, -115, 29, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80};
  public final static byte[] ENCODED_SILENCE_WB_Q08_STEREO = {116, -64, 27, 78, -115, -51, 16, 0, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16, 0, 22, -114, -114, -114, -114, -114, -114, -114, -115, -99, -80, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88};
  public final static byte[] ENCODED_SILENCE_WB_Q09_STEREO = {116, -64, 31, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -48, 0, 23, -1, -1, -1, -1, -1, -3, -74, -37, 109, -74, -37, 109, -99, -80, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88};
  public final static byte[] ENCODED_SILENCE_WB_Q10_STEREO = {};
  public final static byte[] ENCODED_SILENCE_UWB_Q01_STEREO = {116, -64, 35, 78, -77, 0, 0, -50, 115, -100, -25, 57, -109, -74, 0, 0, 9, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q02_STEREO = {116, -64, 11, 78, -77, 0, 0, 119, 119, 3, -69, -72, 29, -35, -64, -18, -18, -109, -74, 0, 0, 9, 59, 96, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q03_STEREO = {116, -64, 15, 78, -77, 0, 0, 51, -100, -28, 8, 25, -50, 114, 4, 12, -25, 57, 2, 6, 115, -100, -55, -37, 0, 0, 4, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q04_STEREO = {116, -64, 19, 78, -77, 0, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -40, 0, 13, -101, 54, 108, -36, -99, -80, 0, 0, 73, -37, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q05_STEREO = {116, -64, 23, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16, 0, 23, -1, -1, -1, -1, -1, -4, -99, -80, 0, 0, 73, -37, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q06_STEREO = {116, -64, 23, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -64, 0, 95, -1, -1, -1, -1, -1, -32, 0, 47, -1, -1, -1, -1, -1, -16, 0, 23, -1, -1, -1, -1, -1, -3, 29, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80, 90, -42, -76, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q07_STEREO = {116, -64, 27, 78, -115, -51, 16, 0, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16, 0, 22, -114, -114, -114, -114, -114, -114, -114, -115, 29, -80, 90, -42, -80, 90, -42, -80, 90, -42, -80, 90, -42, -76, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q08_STEREO = {116, -64, 27, 78, -115, -51, 16, 0, 0, -76, 116, 116, 116, 116, 116, 116, 116, 64, 0, 90, 58, 58, 58, 58, 58, 58, 58, 32, 0, 45, 29, 29, 29, 29, 29, 29, 29, 16, 0, 22, -114, -114, -114, -114, -114, -114, -114, -115, -99, -80, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 92, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q09_STEREO = {116, -64, 31, 78, -115, -51, 16, 0, 0, -65, -1, -1, -1, -1, -1, -19, -74, -37, 109, -74, -37, 64, 0, 95, -1, -1, -1, -1, -1, -10, -37, 109, -74, -37, 109, -96, 0, 47, -1, -1, -1, -1, -1, -5, 109, -74, -37, 109, -74, -48, 0, 23, -1, -1, -1, -1, -1, -3, -74, -37, 109, -74, -37, 109, -99, -80, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 88, 21, -107, -107, -107, -107, -127, 89, 89, 89, 89, 92, -99, -80, 0, 0, 0};
  public final static byte[] ENCODED_SILENCE_UWB_Q10_STEREO = {};

  /**
   * Constructor
   * @param arg0
   */
  public TestJSpeex(String arg0) {
    super(arg0);
  }
  
  /**
   * Command line entrance.
   * @param args
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestJSpeex.suite());
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // TestCase classes to override
  ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   */
  protected void setUp()
  {
  }
  
  /**
   * 
   */
  protected void tearDown()
  {
  }
  
  /**
   * 
   */
//  protected void runTest()
//  {
//  }
  
  /**
   * Builds the Test Suite.
   * @return the Test Suite.
   */
  public static Test suite()
  {
    return new TestSuite(TestJSpeex.class);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // Tests
  ///////////////////////////////////////////////////////////////////////////
  
  /**
   * Test
   */
  public void test1()
  {
    assertTrue("It failed", true);
  }

  /**
   * 
   * @param mode
   * @param quality
   * @param sampleRate
   * @param channels
   * @param source
   * @param expected
   */
  protected void testEncode(int mode,
                            int quality,
                            int sampleRate,
                            int channels,
                            float[] source,
                            byte[] expected)
  {
    SpeexEncoder speexEncoder = new SpeexEncoder();
    speexEncoder.init(mode, quality, sampleRate, channels);
    //speexEncoder.getEncoder().setComplexity(3);
    //speexEncoder.getEncoder().setBitRate(bitrate);
    //speexEncoder.getEncoder().setVbr(vbr);
    //speexEncoder.getEncoder().setVbrQuality(vbr_quality);
    //speexEncoder.getEncoder().setVad(vad);
    //speexEncoder.getEncoder().setDtx(dtx);
    speexEncoder.processData(source, source.length);
    int actualsize = speexEncoder.getProcessedDataByteSize();
    assertEquals("Encoded data is not of the expected size",
                 checkSize(mode, quality, channels, speexEncoder),
                 actualsize);
    byte[] speex = new byte[actualsize];
    speexEncoder.getProcessedData(speex, 0);
    assertTrue("Encoded data appears wrong",
               Arrays.equals(expected, speex));
  }

  /**
   * 
   * @param mode
   * @param quality
   * @param channels
   * @return
   */
  protected int checkSize(int mode,
                          int quality,
                          int channels,
                          SpeexEncoder speexEncoder)
  {
    assertEquals("Number of channels don't match",
                 channels, speexEncoder.getChannels());
    int bitsize = 0;
    if (mode == 0) { // Narrowband
      bitsize = NbCodec.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[quality]];
      assertEquals("SubModes don't match",
                   NbEncoder.NB_QUALITY_MAP[quality],
                   speexEncoder.getEncoder().getMode());
    }
    else if (mode == 1) { // Wideband
      bitsize = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
      bitsize += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
    }
    else if (mode == 2) { // UltraWideband
      bitsize = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
      bitsize += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
      bitsize += SbCodec.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[quality]];
    }
    assertEquals("Number of encoded bits don't match",
                 bitsize,
                 speexEncoder.getEncoder().getEncodedFrameSize());
    if (channels > 1) {
      bitsize += 17; // 1+4(14=inband)+4(9=stereo)+8(stereo data)
    }
    return (bitsize + 7) / 8;
  }
  
  /**
   * 
   */
  public void testEncodeSilenceNBQ01mono()
  {
    testEncode(0, 1, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q01_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ02mono()
  {
    testEncode(0, 2, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q02_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ03mono()
  {
    testEncode(0, 3, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q03_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ04mono()
  {
    testEncode(0, 4, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q04_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ05mono()
  {
    testEncode(0, 5, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q05_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ06mono()
  {
    testEncode(0, 6, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q06_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ07mono()
  {
    testEncode(0, 7, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q07_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ08mono()
  {
    testEncode(0, 8, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q08_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ09mono()
  {
    testEncode(0, 9, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q09_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ10mono()
  {
    testEncode(0, 10, 8000, 1, new float[160], ENCODED_SILENCE_NB_Q10_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ01mono()
  {
    testEncode(1, 1, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q01_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ02mono()
  {
    testEncode(1, 2, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q02_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ03mono()
  {
    testEncode(1, 3, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q03_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ04mono()
  {
    testEncode(1, 4, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q04_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ05mono()
  {
    testEncode(1, 5, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q05_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ06mono()
  {
    testEncode(1, 6, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q06_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ07mono()
  {
    testEncode(1, 7, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q07_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ08mono()
  {
    testEncode(1, 8, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q08_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ09mono()
  {
    testEncode(1, 9, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q09_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ10mono()
  {
    testEncode(1, 10, 16000, 1, new float[320], ENCODED_SILENCE_WB_Q10_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ01mono()
  {
    testEncode(2, 1, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q01_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ02mono()
  {
    testEncode(2, 2, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q02_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ03mono()
  {
    testEncode(2, 3, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q03_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ04mono()
  {
    testEncode(2, 4, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q04_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ05mono()
  {
    testEncode(2, 5, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q05_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ06mono()
  {
    testEncode(2, 6, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q06_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ07mono()
  {
    testEncode(2, 7, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q07_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ08mono()
  {
    testEncode(2, 8, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q08_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ09mono()
  {
    testEncode(2, 9, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q09_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ10mono()
  {
    testEncode(2, 10, 32000, 1, new float[640], ENCODED_SILENCE_UWB_Q10_MONO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ1stereo()
  {
    testEncode(0, 1, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q01_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ2stereo()
  {
    testEncode(0, 2, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q02_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ3stereo()
  {
    testEncode(0, 3, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q03_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ4stereo()
  {
    testEncode(0, 4, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q04_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ5stereo()
  {
    testEncode(0, 5, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q05_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ6stereo()
  {
    testEncode(0, 6, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q06_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ7stereo()
  {
    testEncode(0, 7, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q07_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ8stereo()
  {
    testEncode(0, 8, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q08_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceNBQ9stereo()
  {
    testEncode(0, 9, 8000, 2, new float[320], ENCODED_SILENCE_NB_Q09_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ1stereo()
  {
    testEncode(1, 1, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q01_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ2stereo()
  {
    testEncode(1, 2, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q02_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ3stereo()
  {
    testEncode(1, 3, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q03_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ4stereo()
  {
    testEncode(1, 4, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q04_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ5stereo()
  {
    testEncode(1, 5, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q05_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ6stereo()
  {
    testEncode(1, 6, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q06_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ7stereo()
  {
    testEncode(1, 7, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q07_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ8stereo()
  {
    testEncode(1, 8, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q08_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceWBQ9stereo()
  {
    testEncode(1, 9, 8000, 2, new float[640], ENCODED_SILENCE_WB_Q09_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ1stereo()
  {
    testEncode(2, 1, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q01_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ2stereo()
  {
    testEncode(2, 2, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q02_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ3stereo()
  {
    testEncode(2, 3, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q03_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ4stereo()
  {
    testEncode(2, 4, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q04_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ5stereo()
  {
    testEncode(2, 5, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q05_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ6stereo()
  {
    testEncode(2, 6, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q06_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ7stereo()
  {
    testEncode(2, 7, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q07_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ8stereo()
  {
    testEncode(2, 8, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q08_STEREO);
  }

  /**
   * 
   */
  public void testEncodeSilenceUWBQ9stereo()
  {
    testEncode(2, 9, 8000, 2, new float[1280], ENCODED_SILENCE_UWB_Q09_STEREO);
  }
}
