/* $Id: parser.h,v 1.7 2002/11/29 13:31:27 ls Exp $ */

/**
 * \file parser.h
 * \brief Constants relevant for the parser.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This module provides all constants (tokens) needed by the parser
 * of the 4IA-language. Prototypes are declared here as well.
 */


/* Lexical token definitions */
#define ENDOFFILE         0
#define ARCH              1
#define HLT               2
#define SET               3
#define MOV               4
#define ADD               5
#define COMMA             6
#define LINEBREAK         7
#define DREGISTER         8
#define IREGISTER         9
#define COMMENT          10
#define LINEPREFIX       11
#define CONSTANT         12
#define UNKNOWNSYMBOL    13


/* General constants */
#define FALSE 0
#define TRUE 1


/* Prototypes */
int parse(char *);
void parse_mov(void);
void parse_set(void);
void parse_add(void);
void parse_arch(void);
void syntactic_error(void);
void semantic_error(char *);
void logical_error(char *);
int parse_constant(int);
