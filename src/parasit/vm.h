/* $Id: vm.h,v 1.10 2002/11/25 12:02:42 ls Exp $ */

/**
 * \file vm.h
 * \brief Constants and datastructures used by the virtual machine.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides the datastructures and constants used by the
 * virtual machine. Also is contains token-definitions for all opcodes
 * and the required prototypes.
 */

/** 
 * \struct code4ia
 * Structure for compiled microcode 
 */
struct code4ia
{
  /** The opcode */
  int opcode;

  /** The first operand */
  int operand1;

  /** The second operand */
  int operand2;
};

/** Points to the compiled microcode */
extern struct code4ia *code;

/** Number of available instructions */
extern int codesize;


/* Opcodes */
#define OP_HLT 0              /* Opcode for instruction HLT            */
#define OP_SET 1              /* Opcode for instruction SET            */
#define OP_MOV 2              /* Opcode for instruction MOV            */
#define OP_ADD 3              /* Opcode for instruction ADD            */


/* Global counters for the current execution */
extern int cycles;
extern int packets_outbound;
extern int packets_inbound;
extern int packets_optimum;
#include <sys/time.h>
extern struct timeval starttime;
extern struct timeval endtime;
extern struct rusage startusage;
extern struct rusage endusage;

extern int runtime_status;


/* Register aliases */
/** \def IP Alias for the instruction pointer register (r0) */
#define IP 0

/** \def FL Alias for the flag register (r1) */
#define FL 1


/* Prototypes */
int execute(void);
int getreg(int);
int initregs(void);
