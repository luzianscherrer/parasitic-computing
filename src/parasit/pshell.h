/* $Id: pshell.h,v 1.14 2002/11/30 09:10:04 ls Exp $ */

/**
 * \file pshell.h
 * \brief Constants and prototypes for the pshell-environment.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides all required constants and prototypes for the
 * pshell-environment. 
 */

/** \def PROMPT The prompt to be displayed in the pshell */
#define PROMPT      "> "

/** \def VERSION Symbolic version of the current release */
#define VERSION     "1.0"

/* Tokens for the pshell commands */
#define PSH_UNKNOWN       0
#define PSH_QUIT          1
#define PSH_HELP          2
#define PSH_HISTORY       3
#define PSH_EXECS         4
#define PSH_SHOWCONFIG    5
#define PSH_SHOWREG       6
#define PSH_EXECP         7
#define PSH_LOADHOSTLIST  8
#define PSH_SHOWHOSTLIST  9
#define PSH_EDIT         10
#define PSH_SHOWSTAT     11
#define PSH_SETBITS      12
#define PSH_SETTIMEOUT   13
#define PSH_SETTHRESHOLD 15


/* Prototypes */
void print_banner(void);
void print_config(void);
void print_stats(void);
void print_help(void);
void (* old_handler)(int);
void sigint_handler(int);
void set_bits(char *);
void set_timeout(char *);
void set_threshold(char *);
