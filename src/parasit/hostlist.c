/* $Id: hostlist.c,v 1.8 2002/11/30 11:29:41 ls Exp $ */

/**
 * \file hostlist.c
 * \brief Management of the hosts to be involved in the ICMP calculation.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides all the functionality required to distribute
 * the ICMP calculation among a group of hosts. The list of hosts is 
 * read from a flat file and can then be queried by an enumerator.
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <errno.h>
#include <string.h>
#include "hostlist.h"
#include "debug.h"

/** Enumerator: number of the next host to return. */
int next_host;

/** Dynamic list of the hosts. */
struct hostlist *hosts;

/** Size of the hostlist. */
int hostlistsize;


/**
 * Resolve hostname pointed to by buf and put adress into hosts array.
 *
 * @param buf Pointer to the hostname.
 */
void resolve_hostname(char *buf)
{
  struct hostent *hent;
  struct in_addr addr;
  int ret, *intp;

  hent = gethostbyname(buf);
  if(hent == NULL) {
    strcpy(hosts[hostlistsize-1].hostaddress, "unresolvable");
    hosts[hostlistsize-1].enabled = 0;
    hosts[hostlistsize-1].false_positive = -1;
  } else {
    intp = (int *)hent->h_addr_list[0];
    ret = *intp;
    addr.s_addr = ret;
    strcpy(hosts[hostlistsize-1].hostaddress, inet_ntoa(addr));
    hosts[hostlistsize-1].enabled = 1;
    hosts[hostlistsize-1].false_positive = -1;
  }
#if defined (LINUX)
  /* On Linux, localhost ICMP checksums are not calculated: disable */
  if(!strcmp(hosts[hostlistsize-1].hostaddress, "127.0.0.1")) {
    hosts[hostlistsize-1].enabled = 0;
    hosts[hostlistsize-1].false_positive = -1;
  }
#endif
}

/**
 * Enumeration implementation; this function gets the next host from 
 * the list.
 *
 * @return Positive number of the next host or an error-code:
 *         - -1 hostlist is empty
 *         - -2 no more valid hosts available
 */
int get_next_host()
{
  int current;

  if(hostlistsize == 0) {
    return -1;
  }

  current = next_host;
  while( !hosts[next_host].enabled ) {
    next_host++;
    if(next_host == hostlistsize) next_host = 0;
    if(current == next_host) {
      return -2;
    }
  }

  current = next_host;
  next_host++;
  if(next_host == hostlistsize) next_host = 0;
  return current;
}

/**
 * Read \n separated list of hostname from *filename, and put into
 * hosts array. Reallocates memory for the hosts array as needed.
 *
 * @param filename Pointer to the filename.
 * @return The following codes are returned:
 *         -  1 Success
 *         - -1 Unable to open filename
 *         - -2 Out of memory
 */
int read_hostlist(char *filename)
{
  FILE *fp;
  char buf[MAXHOSTNAMELEN];
  char tmp[BUFSIZ];
  char *p;

  free(hosts);
  hosts = NULL;
  hostlistsize = 0;
  next_host = 0;

  fp = fopen(filename, "r");
  if(fp == NULL) {
    printf("Cannot open %s: %s\n", filename, strerror(errno));
    return -1;
  }

  while( fgets(buf, MAXHOSTNAMELEN, fp) != NULL ) 
  {
    hostlistsize++;
    hosts = (struct hostlist*) realloc(hosts, 
                                       hostlistsize*sizeof(struct hostlist));
    if(hosts == NULL) {
      printf("Out of memory while trying to allocate space for hostlist\n");
      return -2;
    }
    p = buf;
    while(*p != '\0') {
      if(*p == '\t') *p = ' ';
      if(*p == '\n') { *p = '\0'; break; }
      p++;
    }
    strcpy(hosts[hostlistsize-1].hostname, buf);
    resolve_hostname(buf);
    hosts[hostlistsize-1].timeouts = 0;
    hosts[hostlistsize-1].packetcount = 0;
    sprintf(tmp, "Adding %s to list", buf);
    debug(DEBUG_HOSTLIST, tmp);
  }
  
  fclose(fp);


  return 1;
}

static char *false_positive_to_string(int value)
{
  switch(value)
  {
    case -1:
      return "untested";
      break;
    case 1:
      return "not passed";
      break;
    case 0:
      return "passed";
      break;
  }
  return "unknown";
}

/**
 * Print the content of the hosts array to STDOUT.
 */
void print_hostlist()
{
  int i;

  if(hostlistsize == 0) {
    printf("The hostlist is empty.\n");
    return;
  }

  printf("Hostname              Address          Packets  Timeouts  ");
  printf("Enabled  Falsepos.\n");
  printf("--------------------  ---------------  -------  --------  ");
  printf("-------  -----------\n");
  for(i=0; i<hostlistsize; i++) 
  {
    printf("%-20.20s  %-15s  %7d     %5d  %-3s      %s\n", 
           hosts[i].hostname, 
           hosts[i].hostaddress, 
           hosts[i].packetcount,
           hosts[i].timeouts,
           hosts[i].enabled ? "yes" : "no",
           false_positive_to_string(hosts[i].false_positive));
  }
}
