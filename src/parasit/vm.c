/* $Id: vm.c,v 1.12 2002/11/25 12:02:42 ls Exp $ */

/**
 * \file vm.c
 * \brief The virtual machine.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * The virtual machine as described in the "Realisierungskonzept".
 * Additional checking for runtime exceptions has been added.
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sys/types.h>
#include <sys/resource.h>
#include "globals.h"
#include "vm.h"
#include "icmpcalc.h"

/** Pointer to the first register */
int *r;

/** Number of cycles spent in execution */
int cycles;

/** Optimal number of packets to be sent */
int packets_outbound;

/** Packets sent */
int packets_inbound;

/** Packets revcd */
int packets_optimum;

/** Time when the execution started */
struct timeval starttime;

/** Time when the execution terminated */
struct timeval endtime;

/** Usage when the execution started */
struct rusage startusage;

/** Usage when the execution terminated */
struct rusage endusage;

/** The array containtng the microcode to execute */
struct code4ia *code;

/** Size of the code array */
int codesize;

/** Status to prevent runtime errors */
int runtime_status;      



/**
 * Returns the content of register 'reg'.
 * 
 * @param reg Register-number.
 * @return Value of the requested register.
 */
int getreg(int reg)
{
  return r[reg];
}

/**
 * Initializes all registers with 0 and the registers from 
 * number_of_registers to number_of_registers*2 with the corresponding
 * value used for direct adressing as specialization of indirect adressing.
 *
 * @return The possible codes are:
 *         -  1 Success.
 *         - -1 Out of memory.
 */
int initregs()
{
  int i;

  free(r);
  r = (int *)malloc(sizeof(int) * number_of_registers * 2);
  if(r == NULL) {
    printf("Out of memory while trying to allocate space for registers\n");
    return -1;
  }
  for(i=0; i<number_of_registers; i++) {
    r[i] = 0;
    r[i+number_of_registers] = i;
  }
  return 1;
}

/**
 * The main execution cycle for the virtual machine.
 *
 * @return The return-values are:
 *         -  1:  Success
 *         - -1:  Runtime error, indirect adressing out of bounds
 *         - -2:  Runtime error, instruction pointer out of bounds
 *         - -3:  ICMP communication error
 *         - -4:  Execution interrupted by user (SIGINT)
 *         - -5:  Out of memory
 */
int execute()
{
  char tmp[BUFSIZ];     /* Debug message buffer */
  int i;                /* Loopvariable for debug messages */
  int oldip;            /* Used to print in case of "ip out of range" */
  int ret;              /* Used to check parasit_add for errors */

  if( initregs() == -1 )
    return -5;

  /* Current execution statistics */
  cycles = 0; 
  packets_outbound = 0;
  packets_inbound = 0;
  packets_optimum = 0;
  gettimeofday(&starttime, NULL);
  getrusage(RUSAGE_SELF, &startusage);
    

  runtime_status = 1;
  while(code[r[IP]].opcode != OP_HLT && runtime_status == 1)
  {

    sprintf(tmp, "IP: %5d    FL: %1d", r[IP], r[FL]);
    debug(DEBUG_EXECUTION, tmp);
    oldip = r[IP];

    switch(code[r[IP]].opcode)
    {
      case OP_ADD:
        debug(DEBUG_EXECUTION, "ADD");
        ret = parasit_add(&r[FL], r[code[r[IP]].operand1], 
                                  r[code[r[IP]].operand2]);
        if(ret < 0) {
          runtime_status = -3;
        } else {
          r[code[r[IP]].operand1] = ret;
        }
        break;
      case OP_MOV:
        debug(DEBUG_EXECUTION, "MOV");
        if(r[code[r[IP]].operand1] >= number_of_registers ||
           r[code[r[IP]].operand2] >= number_of_registers ) 
        {
          printf("Runtime error:\n");
          printf("  Indirect adressed register at instruction %d is out ", 
                 r[IP]);
          printf("of range.\n  Only registers 0 to %d are valid.\n",
                 number_of_registers-1);
          runtime_status = -1;
        } else {
          r[r[code[r[IP]].operand1]] = r[r[code[r[IP]].operand2]];
        }
        break;
      case OP_SET:
        debug(DEBUG_EXECUTION, "SET");
        r[code[r[IP]].operand1] = code[r[IP]].operand2;
        break;
    }
    if(runtime_status == 1) {
      debug(DEBUG_EXECUTION, "IP++");
      ret = parasit_add(&r[FL], r[IP], 1);
      if(ret < 0) {
        runtime_status = -3;
      } else {
        r[IP] = ret;
        if(r[IP] >= codesize || r[IP] < 0) {
          printf("Runtime error:\n");
          printf("  Instruction pointer out of range after instruction %d.\n",
                 oldip);
          printf("  Requested position is %d, maximum allowed ", r[IP]); 
          printf("position is %d.\n", codesize-1);
          runtime_status = -2;
        }
      }
      cycles++;
      sprintf(tmp, 
            "IP: %07d  CY#: %07d  PKo#: %07d  PK->#: %07d  PK<-#: %07d",
            r[IP], cycles, packets_optimum, packets_outbound, packets_inbound);
      debug(DEBUG_TRACE, tmp);
    }

  }

  sprintf(tmp, "IP: %5d    FL: %1d", r[IP], r[FL]);
  debug(DEBUG_EXECUTION, tmp);
  for(i=0; i<number_of_registers; i++) {
    sprintf(tmp, "R%02d: %d", i, r[i]);
    debug(DEBUG_DUMPREGS, tmp);
  }

  gettimeofday(&endtime, NULL);
  getrusage(RUSAGE_SELF, &endusage);
  return runtime_status;
}
