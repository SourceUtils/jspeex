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
 * Class: QmfTables.java                                                      *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: 18th April 2003                                                      *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

/**
 * Quadrature Mirror Filter to Split the band in two.
 * A 16kHz signal is thus divided into two 8kHz signals representing the low and high bands.
 * (used by wideband encoder)
 */
public interface QmfTables
{
  public static float h0[] = {
    3.596189e-05f, -0.0001123515f,
    -0.0001104587f, 0.0002790277f,
    0.0002298438f, -0.0005953563f,
    -0.0003823631f, 0.00113826f,
    0.0005308539f, -0.001986177f,
    -0.0006243724f, 0.003235877f,
    0.0005743159f, -0.004989147f,
    -0.0002584767f, 0.007367171f,
    -0.0004857935f, -0.01050689f,
    0.001894714f, 0.01459396f,
    -0.004313674f, -0.01994365f,
    0.00828756f, 0.02716055f,
    -0.01485397f, -0.03764973f,
    0.026447f, 0.05543245f,
    -0.05095487f, -0.09779096f,
    0.1382363f, 0.4600981f,
    0.4600981f, 0.1382363f,
    -0.09779096f, -0.05095487f,
    0.05543245f, 0.026447f,
    -0.03764973f, -0.01485397f,
    0.02716055f, 0.00828756f,
    -0.01994365f, -0.004313674f,
    0.01459396f, 0.001894714f,
    -0.01050689f, -0.0004857935f,  
    0.007367171f, -0.0002584767f,
    -0.004989147f, 0.0005743159f,
    0.003235877f, -0.0006243724f,
    -0.001986177f, 0.0005308539f,
    0.00113826f, -0.0003823631f,
    -0.0005953563f, 0.0002298438f,
    0.0002790277f, -0.0001104587f,
    -0.0001123515f, 3.596189e-05f
  };

  public static float h1[] = {
    3.596189e-05f, 0.0001123515f,
    -0.0001104587f, -0.0002790277f,
    0.0002298438f, 0.0005953563f,
    -0.0003823631f, -0.00113826f,
    0.0005308539f, 0.001986177f,
    -0.0006243724f, -0.003235877f,
    0.0005743159f, 0.004989147f,
    -0.0002584767f, -0.007367171f,
    -0.0004857935f, 0.01050689f,
    0.001894714f, -0.01459396f,
    -0.004313674f, 0.01994365f,
    0.00828756f, -0.02716055f,
    -0.01485397f, 0.03764973f,
    0.026447f, -0.05543245f,
    -0.05095487f, 0.09779096f,
    0.1382363f, -0.4600981f,
    0.4600981f, -0.1382363f,
    -0.09779096f, 0.05095487f,
    0.05543245f, -0.026447f,
    -0.03764973f, 0.01485397f,
    0.02716055f, -0.00828756f,
    -0.01994365f, 0.004313674f,
    0.01459396f, -0.001894714f,
    -0.01050689f, 0.0004857935f,
    0.007367171f, 0.0002584767f,
    -0.004989147f, -0.0005743159f,
    0.003235877f, 0.0006243724f,
    -0.001986177f, -0.0005308539f,
    0.00113826f, 0.0003823631f,
    -0.0005953563f, -0.0002298438f,
    0.0002790277f, 0.0001104587f,
    -0.0001123515f, -3.596189e-05f
  };

  public static final int QMF_ORDER = 64;
}
