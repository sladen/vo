// Copyright 2010 - UDS/CNRS
// The Aladin program is distributed under the terms
// of the GNU General Public License version 3.
//
//This file is part of Aladin.
//
//    Aladin is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, version 3 of the License.
//
//    Aladin is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    The GNU General Public License is available in COPYING file
//    along with Aladin.
//

package cds.allsky;

import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;
import healpix.tools.SpatialVector;

import java.util.ArrayList;

import cds.tools.pixtools.Constants;

public class TestHealpixPackage {

	public static void main(String[]args) {
		verif1();verif2();
	}
	
	/**
	 * The npix list provides by query_disc() :  295 304 271 280 292 271 280 268 263
	 * Some pixels are missing (269,282) , 
	 * some others appear twice (280,271), 
	 * and some others are very far (292,295) 
	 */
	static void verif1() {
	      try {
	         int nside = 8;
	         double radius = 2.875;
	         SpatialVector vector = new SpatialVector(4.49208, -6.62294);
	                 HealpixIndex hi = new HealpixIndex(nside);
	                 LongRangeSet list = hi.queryDisc(vector,
	                		 radius / 180 * Constants.PI, 1, 1);
//	         ArrayList list = hi.query_disc(nside, vector,
//	               radius / 180 * Constants.PI, 1, 1);
	                 System.out.print(vector.ra()+ " "+ vector.dec()+" rad="+radius+" => ");
//	         for( int i = 0; i < list.size(); i++ ) {
	            System.out.print(list);
//	         }
	         System.out.println();
	      } catch( Exception e ) {
	         e.printStackTrace();
	      }
	   }
	
	/**
	 * I get : [278335, 278376, 278420, 278335, 278376, 278332, 278327]
	 * but 278333 and 278334 are missing, am I right ?
	 * and if I put the "inclusive" flag to 0, so I get only 278333
	 */
	static void verif2() {
		try {
			HealpixIndex h = new HealpixIndex(256);
			double radius = Math.toRadians(0.04266);
//			ArrayList list = h.query_disc(
//					256,
//					new SpatialVector(0.19344,-2.71955),
//					Math.toRadians(0.04266),
//					1, 1);
			LongRangeSet list = h.queryDisc(
					new SpatialVector(0.19344,-2.71955),
					radius,
					1, 1);
//			for( int i = 0; i < list.size(); i++ ) {
				System.out.print(list);
//			}	
			System.out.println();
			
			// option inclusive à false, ajoute la taille d'un pixel entier
			radius += 2.*Math.PI/(4.*256); 
			list = h.queryDisc(
					new SpatialVector(0.19344,-2.71955),
					radius,
					1, 0);
//			for( int i = 0; i < list.size(); i++ ) {
			System.out.print(list);
//			}				
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
