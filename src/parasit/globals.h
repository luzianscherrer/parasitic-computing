/* $Id: globals.h,v 1.19 2002/11/30 09:10:03 ls Exp $ */

/**
 * \file globals.h
 * \brief Global constants.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This header provides all the global constants.
 */


#include "debug.h"

/** \def DEBUG_LEVEL The current debug level */
#define DEBUG_LEVEL DEBUG_NONE

/** \def TIMEOUT_THRESHOLD_DEFAULT Number of times a host is can timeout */
#define TIMEOUT_THRESHOLD_DEFAULT 3 

/** \def ICMP_TIMEOUT_SEC_DEFAULT Number of seconds to wait for timeout */
#define ICMP_TIMEOUT_SEC_DEFAULT 2

/** \def BITS_PER_ADD_DEFAULT Number of parallel bits per addition */
#define BITS_PER_ADD_DEFAULT 2

/** Number of registers required by the last executed code */
extern int number_of_registers;

/** Register-width required by the last executed code */
extern int register_width;

/** Number of parallel bits per addition */
extern int bits_per_add;

/** Number of times a host is can timeout */
extern int timeout_threshold;

/** Number of seconds to wait for timeout */
extern int icmp_timeout_sec;
