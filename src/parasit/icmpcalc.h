/* $Id: icmpcalc.h,v 1.5 2002/11/25 12:02:41 ls Exp $ */

/**
 * \file icmpcalc.h
 * \brief Constants for the parasitic ICMP calculation.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides constants for the parasitic ICMP calculation
 * and the required prototypes for the corresponding sourcefile.
 */



/* Prototype */
int parasit_add(int *fl, int op1, int op2);


/** \def SIMULATION Constant defining simulation mode */
#define SIMULATION 1

/** \def ICMPCALC Constant defining parasitic mode */
#define ICMPCALC   2

