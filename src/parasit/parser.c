/* $Id: parser.c,v 1.12 2002/11/25 12:02:41 ls Exp $ */

/**
 * \file parser.c
 * \brief Parser for the 4IA-Language.
 * \author Juerg Reusser, Luzian Scherrer
 *
 * This is the Parser for the 4IA-Language. It implements semantical, 
 * syntactical and logical checks during the scanning phase. Detailed
 * errormessages are provided in case compilation fails.
 */

#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include "globals.h"
#include "parser.h"
#include "debug.h"
#include "vm.h"

int yylex(void);

extern void yyrestart(FILE *);
extern int yyleng;
extern char yytext[];
extern FILE *yyin;
extern int yylineno;

/** The number of registers as requested by the current program. */
int number_of_registers;

/** The registerwidth as requested by the current program. */
int register_width;

/** State-machine variable. */
int codestatus;

/** Highest integer possible in current vm configuration. */
int maxint;

/** Internal boolean check value. */
int architecture_defined;

/**
 * Emit instruction into code array. This function reallocates memory
 * in the code array as needed.
 *
 * @param opc Instructions opcode
 * @param o1 Operand
 * @param o2 Operand
 */
void emit_code(int opc, int o1, int o2)
{
  char tmp[BUFSIZ];

  codesize++;

  code = (struct code4ia*) realloc(code, codesize*sizeof(struct code4ia));
  if(code == NULL) {
    printf("Out of memory while trying to allocate space for code\n");
    codestatus = -3;
  }

  code[codesize-1].opcode = opc;
  code[codesize-1].operand1 = o1;
  code[codesize-1].operand2 = o2;

  sprintf(tmp, "emit_code(%d, %5d, %5d)    codesize: %5d", 
          opc, o1, o2, codesize);
  debug(DEBUG_CODEGEN, tmp);

}

/**
 * Display logical errors during parsing.
 *
 * @param reason Pointer to the errormessage.
 */
void logic_error(char *reason)
{
  printf("Logical error at line %d\n", yylineno);
  printf("  Reason: %s\n", reason);
  codestatus = -4;
}

/**
 * Display syntactical errors during parsing.
 */
void syntactic_error()
{
  int i;

  printf("Syntactical error at line %d\n", yylineno);
  printf("  Expected: valid symbol\n");
  printf("     Found: ");
  for(i=0; i<yyleng; i++)
    printf("%c", *(yytext+i));
  printf("\n");
  codestatus = -1;
}

/**
 * Display semantical errors during parsing.
 *
 * @param expected Pointer to a string showing the expected symbol.
 */
void semantic_error(char *expected)
{
  int i;

  printf("Semantical error at line %d\n", yylineno);
  printf("  Expected: %s\n", expected);
  printf("     Found: ");
  for(i=0; i<yyleng; i++)
    printf("%c", *(yytext+i));
  printf("\n");
  codestatus = -2;
}

/**
 * Returns the integer value of the current constant at yytext.  
 * If the constant is too big, -1 is returned.  
 * 
 * Valid formats are:
 *  - 0x123  (Hexadecimal)
 *  - 0b110  (Binary)
 *  - 0d219  (Decimal) 
 *  -   219  (Decimal )
 *
 * @param checksize Boolean value indication whether size has to be checked.
 * @return Integer-value of the constant.
 */ 
int parse_constant(int checksize) {
  int value, base, offset; char tmp[BUFSIZ];

  base = 10;
  offset = 0;

  if(yyleng > 2) 
  {
    switch(yytext[1])
    {
      case 'x':
        base = 16;
        break;
      case 'b':
        base = 2;
        offset = 2;
        break;
      case 'd':
        base = 10;
        offset = 2;
        break;
    }
  }

  value = (int)strtoul(yytext+offset, NULL, base);

  if(checksize && value > maxint) {
    sprintf(tmp, "constant too big, range is 0 to %d", maxint);
    logic_error(tmp);
    return -1;
  }
  sprintf(tmp, "%d", value);
  debug(DEBUG_PARSER, tmp);

  return value;
}

/**
 * Return the integer value of the current register at yytext.
 * The parameter offset is added to the value (used for direct
 * adressing in MOV instruction).
 *
 * If the register is >= number_of_registers, -1 is returned.
 *
 * @param offset Offset to add to the register.
 * @return Integer-value of the requested register or -1 on error.
 */
int parse_register(int offset)
{
  int value = -1;
  char tmp[BUFSIZ];

  switch(yytext[0])
  {
    case '*': /* Indirect adressing */
      value = atoi(yytext+2);
      if(value >= number_of_registers) {
        sprintf(tmp, "no register %d available, range is 0 to %d", 
                value, number_of_registers-1);
        logic_error(tmp);
        return -1;
      }
      value+=offset;
      sprintf(tmp, "*r%d", value);
      debug(DEBUG_PARSER, tmp);
      break;
    case 'r': /* Direct adressing */
      value = atoi(yytext+1);
      if(value >= number_of_registers) {
        sprintf(tmp, "no register %d available, range is 0 to %d", 
                value, number_of_registers-1);
        logic_error(tmp);
        return -1;
      }
      value+=offset;
      sprintf(tmp, "r%d", value);
      debug(DEBUG_PARSER, tmp);
      break;
    case 'i': /* ip alias */
      value = 0;
      value+=offset;
      debug(DEBUG_PARSER, "ip");
      break;
    case 'f': /* fl alias */
      value = 1;
      value+=offset;
      debug(DEBUG_PARSER, "fl");
      break;
  }

  return value;
}

/**
 * Parse code for the ARCH declaration. This declaration specified the
 * architecture of the virtual machine: register-width and number of 
 * registers.
 */
void parse_arch()
{
  int c; 
  int req_regwidth = -1; 
  int req_regno = -1;

  debug(DEBUG_PARSER, "ARCH");
  if(architecture_defined) {
    logic_error("architecture already defined");
    return;
  }

  c = yylex();
  if(c == CONSTANT) {
    req_regwidth = parse_constant(FALSE);
    if(req_regwidth == -1) return;
  } else {
    semantic_error("constant");
    return;
  }

  c = yylex();
  if(c == CONSTANT) {
    req_regno = parse_constant(FALSE);
    if(req_regno == -1) return;
  } else {
    semantic_error("constant");
    return;
  }

  if(req_regno < 2) {
    logic_error("at least 2 registers are required");
    return;
  }

  if(req_regwidth < 2) {
    logic_error("minimal register width is 2 bits");
    return;
  }

  if(req_regwidth > 16) {
    logic_error("maximal register width is 16 bits");
    return;
  }

  number_of_registers = req_regno;
  register_width = req_regwidth;
  maxint = (1<<register_width)-1;
  architecture_defined = 1;

  return;
}

/**
 *  Parse code for the SET instruction.
 */
void parse_set()
{
  int c; 
  int op1, op2;

  debug(DEBUG_PARSER, "SET");

  if(!architecture_defined) {
    semantic_error("architecture definition");
    return;
  }

  c = yylex();
  if(c == DREGISTER) {
    op1 = parse_register(0);
    if(op1 == -1) return;
  } else  {
    semantic_error("direct register");
    return;
  }

  c = yylex();
  if(c == COMMA) {
    debug(DEBUG_PARSER, ",");
  } else {
    semantic_error(",");
    return;
  }

  c = yylex();
  if(c == CONSTANT) {
    op2 = parse_constant(TRUE);
    if(op2 == -1) return;
  } else {
    semantic_error("constant");
    return;
  }

  emit_code(OP_SET, op1, op2);
}

/**
 * Parse code for the MOV instruction.
 */
void parse_mov()
{
  int c;
  int op1 = 0, op2 = 0;

  debug(DEBUG_PARSER, "MOV");

  if(!architecture_defined) {
    semantic_error("architecture definition");
    return;
  }

  c = yylex();
  if(c == DREGISTER || c == IREGISTER) {
    if(c == DREGISTER) op1 = parse_register(number_of_registers);
    if(c == IREGISTER) op1 = parse_register(0);
    if(op1 == -1) return;
  } else {
    semantic_error("direct or indirect register");
    return;
  }

  c = yylex();
  if(c == COMMA) {
    debug(DEBUG_PARSER, ",");
  } else {
    semantic_error(",");
    return;
  }

  c = yylex();
  if(c == DREGISTER || c == IREGISTER) {
    if(c == DREGISTER) op2 = parse_register(number_of_registers);
    if(c == IREGISTER) op2 = parse_register(0);
    if(op2 == -1) return;
  } else {
    semantic_error("direct or indirect register");
    return;
  }

  emit_code(OP_MOV, op1, op2);
}

/**
 * Parse code for the ADD instruction.
 */
void parse_add()
{
  int c;
  int op1, op2;

  debug(DEBUG_PARSER, "ADD");

  if(!architecture_defined) {
    semantic_error("architecture definition");
    return;
  }

  c = yylex();
  if(c == DREGISTER) {
    op1 = parse_register(0);
    if(op1 == -1) return;
  } else {
    semantic_error("direct register");
    return;
  }

  c = yylex();
  if(c == COMMA) {
    debug(DEBUG_PARSER, ",");
  } else {
    semantic_error(",");
    return;
  }

  c = yylex();
  if(c == DREGISTER) {
    op2 = parse_register(0);
    if(op2 == -1) return;
  } else {
    semantic_error("direct register");
    return;
  }

  emit_code(OP_ADD, op1, op2);
}

/**
 * This is the main parsing function. It parses the file pointed to by
 * *filename and fills the global struct code4ia code by allocating the 
 * required memory.
 *  
 * @param filename Pointer to the filename.
 * @return The following returncodes are possible:
 *         -  1  Code valid, parsing successfull
 *         - -1  Syntactic parse error
 *         - -2  Semantic parse error
 *         - -3  Memory allocation error
 *         - -4  Logical parse error
 */
int parse(char *filename)
{
  int c;
  char tmp[BUFSIZ];

  free(code);
  code = NULL;
  codesize = 0;
  yylineno = 1;
  architecture_defined = 0;

  yyin = fopen(filename, "r");
  yyrestart(yyin);
  if(yyin == NULL) {
    printf("Cannot open %s: %s\n", filename, strerror(errno));
    return -1;
  }

  sprintf(tmp, "Starting to parse %s", filename);
  debug(DEBUG_PARSER, tmp);

  codestatus = 1;

  do {
    c = yylex();
    switch(c)
    {
      case LINEBREAK:
        debug(DEBUG_PARSER, "LINEBREAK");
        break;
      case COMMENT:
        debug(DEBUG_PARSER, "; COMMENT");
        break;
      case LINEPREFIX:
        debug(DEBUG_PARSER, "LINEPREFIX:");
        break;
      case HLT:
        debug(DEBUG_PARSER, "HLT");
        if(!architecture_defined) {
          semantic_error("architecture definition");
        } else {
          emit_code(OP_HLT, 0, 0);
        }
        break;
      case SET:
        parse_set();
        break;
      case MOV:
        parse_mov();
        break;
      case ADD:
        parse_add();
        break;
      case ARCH:
        parse_arch();
        break;
      case UNKNOWNSYMBOL:
        syntactic_error();
        break;
      case ENDOFFILE:
        break;
      default:
        syntactic_error();
        break;
    }
  } while(c != ENDOFFILE && codestatus == 1);

  sprintf(tmp, "Finished parsing %s", filename);
  debug(DEBUG_PARSER, tmp);

  fclose(yyin);

  return codestatus;
}
