/* $Id: icmpcalc.c,v 1.22 2003/01/03 20:16:10 ls Exp $ */

/**
 * \file icmpcalc.c
 * \brief The parasitic computing core.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This is the effective parasitc computing part of the whole project.
 * The whole ICMP packet generation and reception including all checksum
 * verifying is done here. This part is explained in detail in the documents
 * available from http://parasit.org.
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netinet/in_systm.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h>
#include <strings.h>
#include <unistd.h>
#include "globals.h"
#include "icmpcalc.h"
#include "hostlist.h"
#include "vm.h"
#include "precalc.h"

/** \def IPVERSION Version of the IP protocol. */
#define IPVERSION 4

/** \def PARASIT_ID ID to identify our ICMP packets. */
#define PARASIT_ID 0xa800

/** \def RETBUFFSIZE Buffersize for returning ICMP packets. */
#define RETBUFFSIZE 4096

/** \def DATASIZE Size of ICMP packets. */
/** \def BUFFSIZE Buffersize for ICMP packets. */
#if !defined (LINUX)
#define DATASIZE  6
#define BUFFSIZE sizeof(struct icmp) + DATASIZE
#else
#define DATASIZE  18
#define BUFFSIZE sizeof(struct icmphdr) + DATASIZE
#endif

/** Type of execution: Simulation or parasitic. */
int executiontype;

/** Number of parallel bits per addition */
int bits_per_add = BITS_PER_ADD_DEFAULT;

/** Number of times a host can reach timeout before getting disabled */
int timeout_threshold = TIMEOUT_THRESHOLD_DEFAULT;

/** Number of seconds the reply from a host is waited for */
int icmp_timeout_sec = ICMP_TIMEOUT_SEC_DEFAULT;

/**
 * This function checks whether a host does reply to ICMP messages with
 * bad checksums. If so, the host in question has to be disabled immediately
 * and not be involved with any further communication. This check happens
 * only once per host.
 *
 * @param hostno Number of the host to be checked.
 * @return -  1 : if the host is ok
 *         - -1 : if it answers false packets as positive
 *         - -3 : Failed to create socket
 *         - -4 : Out of memory for header allocation
 *         - -5 : Failed to send packet
 *         - -6 : Failed to receive ICMP-packet
 */
int false_positive_check(int hostno)
{
#if !defined (LINUX)
  struct icmp *header;            /* ICMP header */
#else
  struct icmphdr *header;         /* ICMP header */
#endif
  struct sockaddr_in mysock;
  int socklength;
  int *socklen = &socklength;
  int sock;                       /* The sock */
  unsigned char buff[BUFFSIZE];   /* Buffer for ICMP packet body and header */
  struct timeval timeout;
  int ret = 1;
  int answer;
  fd_set readfds;
  int offset;
  int retbytes;

  packets_outbound++;
  hosts[hostno].packetcount++;

  /* Prepare the body of the packet */
#if !defined (LINUX)
  memset(buff+sizeof(struct icmp), 0x0, DATASIZE);
#else
  memset(buff+sizeof(struct icmphdr), 0x0, DATASIZE);
#endif

  /* Request a raw socket from the kernel */
  sock = socket(AF_INET,SOCK_RAW,IPPROTO_ICMP);
  if(sock < 0)
    return -3;

  /* Prepare the header of the packet */
#if !defined (LINUX)
  header = malloc(sizeof(struct icmp));
#else
  header = malloc(sizeof(struct icmphdr));
#endif
  if(header == NULL)
    return -4;
#if !defined (LINUX)
  memset(header, 0x0, sizeof(struct icmp));
  header->icmp_type = ICMP_ECHO;
  header->icmp_id = htons(PARASIT_ID);
  header->icmp_seq = htons(PARASIT_ID);
  header->icmp_cksum = htons(PARASIT_ID);
  memcpy(buff, header, sizeof(struct icmp));
#else
  memset(header, 0x0, sizeof(struct icmphdr));
  header->type = ICMP_ECHO;
  header->un.echo.id = htons(PARASIT_ID);
  header->un.echo.sequence = htons(PARASIT_ID);
  header->checksum = htons(PARASIT_ID);
  memcpy(buff, header, sizeof(struct icmphdr));
#endif

  /* Create the IP packet */
  memset((char *)&mysock, 0x0, sizeof(mysock));
  mysock.sin_family = AF_INET;
  mysock.sin_addr.s_addr = inet_addr(hosts[hostno].hostaddress);
  *socklen = sizeof(mysock);
  if(sendto(sock,(char *)buff,sizeof(buff),0x0,
    (struct sockaddr *)&mysock,sizeof(mysock)) < 0)  {
       return -5;
  }

  timeout.tv_sec = icmp_timeout_sec;
  timeout.tv_usec = 0;

  answer = 0;
  while(!answer)
  {
    FD_ZERO(&readfds);
    FD_SET(sock, &readfds);
    select(sock+1, &readfds, NULL, NULL, &timeout);
    if(FD_ISSET(sock,&readfds))
    {
      retbytes = recvfrom(sock, (char *)buff, RETBUFFSIZE, 0x0,
                 (struct sockaddr *)&mysock,socklen);
      if(retbytes < 0) {
        return -6;
      }
#if !defined (LINUX)
      offset = ((struct ip *)buff)->ip_hl;
      header = (struct icmp *)(buff+offset*4);
      if(header->icmp_type == ICMP_ECHOREPLY &&
         ntohs(header->icmp_id) == PARASIT_ID)
#else
      offset = ((struct iphdr *)buff)->ihl;
      header = (struct icmphdr *)(buff+offset*4);
      if(header->type == ICMP_ECHOREPLY &&
         ntohs(header->un.echo.id) == PARASIT_ID)
#endif
      {
        /* False positive */
        packets_inbound++;
        ret = -1;
        answer = 1;
        break;
      }
    } else {
      /* Timeout */
      answer = 1;
    }
  }

  free(header);
  close(sock);

  return ret;
}

/**
 * Integer-addition with overflow on register_width. This is the simulation.
 *
 * @param fl Pointer to the flag-register
 * @param op1 Operand for addition
 * @param op2 Operand for addition
 * @return Result of addition
 */
int parasit_add_simulate(int *fl, int op1, int op2)
{
  int result; 
  int maxint;

  maxint = (1<<register_width)-1;

  result = op1 + op2;
  if(result > maxint) {
    result = result%(maxint+1);
    *fl = 1;
  }

  return result;
}


/**
 * This function waits timeout_seconds for a returning ICMP ECHO_REPLY
 * and sets the pointer to res and carry with the corresponding value.
 *
 * @param res Pointer to the result of the correct packet.
 * @param carry Pointer to the carry-result of the correct packet.
 * @param sock The socket to be used.
 * @param mysock Socket-structure containing the address, etc.
 * @param timeout_seconds Number of seconds to wait.
 * @return Positive result of addition or:
 *         - -6  Failed to receive ICMP-packet
 *         - -7  Operation timed out
 */
int get_answer(int *res, int *carry, 
               int sock, struct sockaddr_in *mysock, int timeout_seconds)
{
  char tmp[BUFSIZ];
  fd_set readfds;
  int answer;
  int retbytes;
  struct timeval timeout;
  unsigned char buff[RETBUFFSIZE];
#if !defined (LINUX)
  struct icmp *header;
#else
  struct icmphdr *header;
#endif
  int offset;
  int i;
  int socklength;
  int *socklen = &socklength;

  timeout.tv_sec = timeout_seconds;
  timeout.tv_usec = 0;

  answer = 0;
  while(!answer)
  {
    FD_ZERO(&readfds);
    FD_SET(sock, &readfds);
    select(sock+1, &readfds, NULL, NULL, &timeout);
    if(FD_ISSET(sock,&readfds))
    {
      retbytes = recvfrom(sock, (char *)buff, RETBUFFSIZE, 0x0,
                 (struct sockaddr *)&mysock,socklen);
      if(retbytes < 0) {
        return -6;
      }
#if !defined (LINUX)
      offset = ((struct ip *)buff)->ip_hl;
      header = (struct icmp *)(buff+offset*4);
      if(header->icmp_type == ICMP_ECHOREPLY && 
         ntohs(header->icmp_id) == PARASIT_ID)
#else
      offset = ((struct iphdr *)buff)->ihl;
      header = (struct icmphdr *)(buff+offset*4);
      if(header->type == ICMP_ECHOREPLY && 
         ntohs(header->un.echo.id) == PARASIT_ID)
#endif
      {
        for(i=0; i<(1<<(bits_per_add+1)); i++) {
#if !defined (LINUX)
          if(sequence[i] == ntohs(header->icmp_seq)) {
#else
          if(sequence[i] == ntohs(header->un.echo.sequence)) {
#endif
            sprintf(tmp, "Returning packet no is %d", i);
            debug(DEBUG_ICMP, tmp);

            packets_inbound++;

            /* Invert the sent checksum and get the highest bit */
            *carry = (~((1<<(bits_per_add+1))-i-1)>>bits_per_add & 1);
            
            /* Invert the sent checksum and mask out the result */
            *res = (~((1<<(bits_per_add+1))-i-1)&((1<<bits_per_add)-1));

            answer = 1;
            break;
          }
        }
      }
    } else {
      /* Timeout */
      debug(DEBUG_ICMP, "Timeout!");
      return -7;
    }
  }
  return 1;
}

/**
 * Adds the two Operands op1 and op2 and sets the flag register fl using
 * the host hostno with parasitic computing.
 *
 * @param fl Pointer to the flag register.
 * @param op1 Operand
 * @param op2 Operand
 * @param hostno Number of the host to be used in the hostlist.
 * @return Positive result of the addition or:
 *         - -3 : Failed to create socket
 *         - -4 : Out of memory for header allocation
 *         - -5 : Failed to send packet
 */
int icmp_addition(int *fl, int op1, int op2, int hostno)
{
  char bit;                       /* Char buffer for operands */
  unsigned char buff[BUFFSIZE];   /* Buffer for ICMP packet body and header */
#if !defined (LINUX)
  struct icmp *header;            /* ICMP header */
#else
  struct icmphdr *header;         /* ICMP header */
#endif
  struct sockaddr_in mysock;      /* IP sock */
  int socklength;
  int *socklen = &socklength;
  int sock;                       /* The sock */
  int i;
  int res, carry;                 /* Result and result carry bit */
  int chk;
  char tmp[BUFSIZ];
  
  /* Prepare the body of the packet */
#if !defined (LINUX)
  memset(buff+sizeof(struct icmp), 0x0, DATASIZE);
  if( op1 ) {
    sprintf(&bit, "%c", (char)op1); /* Convert int to char byte */
    memcpy(buff+sizeof(struct icmp)+1, &bit, 1);
  }
  if( op2 ) {
    sprintf(&bit, "%c", (char)op2); /* Convert int to char byte */
    memcpy(buff+sizeof(struct icmp)+3, &bit, 1);
  }
  if( *fl ) {
    bit = 1;
    memcpy(buff+sizeof(struct icmp)+5, &bit, 1);
  }
#else
  memset(buff+sizeof(struct icmphdr), 0x0, DATASIZE);
  if( op1 ) {
    sprintf(&bit, "%c", (char)op1); /* Convert int to char byte */
    memcpy(buff+sizeof(struct icmphdr)+1, &bit, 1);
  }
  if( op2 ) {
    sprintf(&bit, "%c", (char)op2); /* Convert int to char byte */
    memcpy(buff+sizeof(struct icmphdr)+3, &bit, 1);
  }
  if( *fl ) {
    bit = 1;
    memcpy(buff+sizeof(struct icmphdr)+5, &bit, 1);
  }
#endif

  /* Request a raw socket from the kernel */
  sock = socket(AF_INET,SOCK_RAW,IPPROTO_ICMP);
  if(sock < 0)
    return -3;

  /* Prepare the header of the packet */
#if !defined (LINUX)
  header = malloc(sizeof(struct icmp));
#else
  header = malloc(sizeof(struct icmphdr));
#endif
  if(header == NULL)
    return -4;
#if !defined (LINUX)
  memset(header, 0x0, sizeof(struct icmp));
  header->icmp_type = ICMP_ECHO;
  header->icmp_id = htons(PARASIT_ID);
#else
  memset(header, 0x0, sizeof(struct icmphdr));
  header->type = ICMP_ECHO;
  header->un.echo.id = htons(PARASIT_ID);
#endif

  /* Create the IP packet */
  memset((char *)&mysock, 0x0, sizeof(mysock));
  mysock.sin_family = AF_INET;
  mysock.sin_addr.s_addr = inet_addr(hosts[hostno].hostaddress);
  *socklen = sizeof(mysock);

  /* Send out the 2^(bits_per_add+1) packets (candidate-solutions) */
  sprintf(tmp, "Sending the %d candidate packets", 1<<(bits_per_add+1));
  debug(DEBUG_ICMP, tmp);
  for(i=0; i<(1<<(bits_per_add+1)); i++)
  {
    packets_outbound++;
    hosts[hostno].packetcount++;
#if !defined (LINUX)
    header->icmp_seq = htons(sequence[i]);
    header->icmp_cksum = htons(checksum[i]-i);
    memcpy(buff, header, sizeof(struct icmp));
#else
    header->un.echo.sequence = htons(sequence[i]);
    header->checksum = htons(checksum[i]-i);
    memcpy(buff, header, sizeof(struct icmphdr));
#endif

    if(sendto(sock,(char *)buff,sizeof(buff),0x0,
      (struct sockaddr *)&mysock,sizeof(mysock)) < 0)  {
         return -5;
    }
  }

  /* Wait for the correct packet to be answered */
  chk = get_answer(&res, &carry, sock, &mysock, icmp_timeout_sec);
  if(chk < 0 ) return chk;

  /* Close socket */
  close(sock);
  
  /* Copy results */
  *fl = carry;
  return res;
}


/**
 * Parasitic addition. This is a loop through all operand bits.
 *
 * @param fl Pointer to the flag register.
 * @param op1 Operand 
 * @param op2 Operand
 * @return Result of the addition.
 */
int parasit_add_icmp(int *fl, int op1, int op2)
{
  int complete_result = 0;      /* Addition result */
  int opU = 0;                  /* Initial Carry is 0 */
  int hostno;                   /* Number of the current host */
  int opA, opB;                 /* Current operands */
  int res;                      /* Result of the current addition */
  int i, ret;
  char tmp[BUFSIZ];

  for(i=0; i<register_width; i+=bits_per_add) 
  {
    /* Get operand bits */
    opA = op1&((1<<bits_per_add)-1); 
    opB = op2&((1<<bits_per_add)-1);

    /* Repeat this bit operation on next host as long as we get a timeout */
    res = -7;
    while(res == -7)
    {
      hostno = get_next_host();
      if(hostno < 0) return hostno; /* No more hosts available */

      sprintf(tmp, "Using host %s", hosts[hostno].hostname);
      debug(DEBUG_ICMP, tmp);
      sprintf(tmp, "Bitaddition for pos [%d-%d] of operands %d and %d",
              i, i+(bits_per_add-1), opA, opB);
      debug(DEBUG_ICMP, tmp);

      if(hosts[hostno].false_positive == -1) /* First usage of this host */
      {
        ret = false_positive_check(hostno);
        if(ret == -1) {
          hosts[hostno].enabled = 0;
          hosts[hostno].false_positive = 1;
          continue;
          return -1;
        } else if(ret == 1) {
          hosts[hostno].false_positive = 0;
        } else {
          return ret;
        }
      }

      res = icmp_addition(&opU, opA, opB, hostno);
      if(res == -6) return res;
      if(res == -7) hosts[hostno].timeouts++;

      /* Disable host if timeout and timeout_threshold is reached */
      if(hosts[hostno].timeouts >= timeout_threshold) 
        hosts[hostno].enabled = 0;
    }

    /* Add bit result to complete integer result */
    complete_result += (res<<i);
    sprintf(tmp, "Intermediate total: %d", complete_result);
    debug(DEBUG_ICMP, tmp);

    /* Shift to next operand bits */
    op1>>=bits_per_add;
    op2>>=bits_per_add;
  }

  if(opU) *fl = 1;
  return complete_result;
}

/**
 * Wrapper for simulation or ICMP calculation. Depending on the defined
 * executiontype, the simulation of the effective ICMP calculation 
 * functions are called.
 *
 * @param fl Pointer to the flag register.
 * @param op1 Operand
 * @param op2 Operand
 * @return Positive result or error-code:
 *         - integer >= 0 : the result
 *         - integer <  0 : calculation failed
 */
int parasit_add(int *fl, int op1, int op2)
{
  int ret = -1;
  char tmp[BUFSIZ];

  sprintf(tmp, "Requested operation: %d + %d", op1, op2);
  debug(DEBUG_ICMP, tmp);


  switch(executiontype)
  {
    case SIMULATION:
      ret = parasit_add_simulate(fl, op1, op2);
      packets_optimum += 
        (register_width/bits_per_add)*(1<<(bits_per_add+1));
      break;
    case ICMPCALC:
      ret = parasit_add_icmp(fl, op1, op2);
      switch(ret)
      {
        case -1:
          printf("Hostlist is empty! Use 'loadhosts' first.\n");
          break;
        case -2:
          printf("No valid hosts available (try 'showhosts' to see why).\n");
          break;
        case -3:
          printf("Failed to create socket.\n");
          break;
        case -4:
          printf("Out of memory during header allocation.\n");
          break;
        case -5:
          printf("Failed to send ICMP-packet.\n");
          break;
        case -6:
          printf("Failed to receive ICMP-packet.\n");
          break;
        default: /* Valid result */
          packets_optimum += 
            (register_width/bits_per_add)*(1<<(bits_per_add+1));
          break;
      }
      break;
  }

  sprintf(tmp, "Result: %d", ret);
  debug(DEBUG_ICMP, tmp);

  return ret;
}

