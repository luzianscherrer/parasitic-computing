/* $Id: hostlist.h,v 1.7 2002/11/30 11:29:41 ls Exp $ */

/**
 * \file hostlist.h
 * \brief Constants and datastructures for the hostlist management.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides constants and datastructures used by the hostlist
 * management.
 */

#include <netdb.h>
#ifndef MAXHOSTNAMELEN
/** \def MAXHOSTNAMELEN Maximum length for hostnames. */
#define MAXHOSTNAMELEN 256
#endif

/** 
 * \struct hostlist 
 * Datatype to hold the hosts to be contacted. 
 */
struct hostlist
{
  /** Name of the host */
  char hostname[MAXHOSTNAMELEN];

  /** Dotted address of the host */
  char hostaddress[MAXHOSTNAMELEN];

  /** Status can be enabled or disabled */
  int enabled;

  /** Number of times this host reached the timeout*/
  int timeouts;

  /** Number of packets sent to this host*/
  int packetcount;
 
  /** False positives flag */
  int false_positive;
};

extern struct hostlist *hosts;
extern int hostlistsize;


int read_hostlist(char *);
void print_hostlist(void);
int get_next_host(void);
