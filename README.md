
                ______                          __ __   __       
               |   __ \.---.-.----.---.-.-----.|__|  |_|__|.----.
               |    __/|  _  |   _|  _  |__ --||  |   _|  ||  __|
               |___|   |___._|__| |___._|_____||__|____|__||____|
            ______                              __   __              
           |      |.-----.--------.-----.--.--.|  |_|__|.-----.-----.
           |   ---||  _  |        |  _  |  |  ||   _|  ||     |  _  |
           |______||_____|__|__|__|   __|_____||____|__||__|__|___  |
                                  |__|                        |_____|

            Diploma Thesis - University of Applied Sciences, Bern CH
               Copyright (c) 2002 Juerg Reusser & Luzian Scherrer

                 $Id: README,v 1.7 2003/01/02 17:40:32 ls Exp $





## Preface

This README only serves as a quick getting started guide. Everything 
else is explained in detail in the documents in the docs folder. 
The very impatient can just run `make install`.

## Structure

- **Makefile** (Top-level makefile)
- **code** (Subdirectory for example-codes in the defined languages)
  - **4ia** (Example-codes in the 4ia-language)
  - **xia** (Example-codes in the xia-language)
- **src** (Sourcecode subdirectory)
  - **parasit** (Parasit core: pshell, 4ia-compiler, virtual machine)
  - **xia** (Xto4 [xia to 4ia] cross-compiler)
- **docs** (Documentation)

## Building

### Supported platforms

Sun Solaris, GNU Linux, Silicon Graphics IRIX

### Requirements

- A working C compiler http://gcc.gnu.org/
- Flex (Fast lexical analyser generator) http://www.gnu.org/software/flex/
- The GNU Readline Library http://cnswww.cns.cwru.edu/~chet/readline/rltop.html
- Ncurses (new curses) http://www.gnu.org/software/ncurses/ncurses.html
- A working JAVA compiler http://java.sun.com
- ANT http://jakarta.apache.org/ant/

### Building

`make`

### Installing

Alter the variable `PREFIX` in the `Makefile` if you would like to have
the software installed in a different than the default location 
`/usr/local`. Then execute (possibly as root):

`make install`

### The installed binaries
     
- `PREFIX/bin/pshell`
- `PREFIX/bin/xto4`
- `PREFIX/bin/Xto4.jar`

`xto4` is a shellscript wrapper to `Xto4.jar`; the latter does not have
to be executed directly.

### The installed code examples

- `PREFIX/share/parasit/4ia` (4ia code examples)
- `PREFIX/share/parasit/xia` (xia code examples

