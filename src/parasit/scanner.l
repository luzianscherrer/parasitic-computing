/*
 *   Parasitic Computing
 *   $Id: scanner.l,v 1.3 2002/11/15 11:17:35 ls Exp $
 *
 *   Lex input defining the tokens of the 4IA-Language.
 */

%option lex-compat
%{
#include "parser.h"
%}

/* Regular Expressions */
newline              \n
whitespace           [ \t]
iregister            \*((r[0-9]+)|ip|fl)
dregister            (r[0-9]+)|ip|fl
constant             ((0d)?[0-9]+)|(0b[01]+)|(0x[0-9a-fA-F]+)
comment              ;.*
lineprefix           [0-9]+:

%%
{whitespace}             { /* No Operation */ }
"HLT"                    { return(HLT); }
"SET"                    { return(SET); }
"MOV"                    { return(MOV); }
"ADD"                    { return(ADD); }
"ARCH"                   { return(ARCH); }
{dregister}              { return(DREGISTER); }
{iregister}              { return(IREGISTER); }
","                      { return(COMMA); }
{newline}                { return(LINEBREAK); }
{lineprefix}             { return(LINEPREFIX); }
{comment}                { return(COMMENT); }
{constant}               { return(CONSTANT); }
.                        { return(UNKNOWNSYMBOL); }
%%
