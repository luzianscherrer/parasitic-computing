# Parasitic Computing

*Diploma Thesis - University of Applied Sciences, Bern, Switzerland. Copyright (c) 2002 Juerg Reusser & Luzian Scherrer.*

The term **Parasitic Computing** was first introduced in August 2001, when scientists at the [Notre Dame University, Indiana (USA)](https://www.nd.edu) found a way to solve mathematical problems using external computational power without knowledge or permission of their respective owners. This Diploma Thesis of the [University of Applied Sciences in Bern (Switzerland)](https://www.bfh.ch/) does extend that concept into a fully programmable virtual machine that is capable of solving any known problem in classic computer science. 

## Preface

This README only serves as a quick getting started guide. Everything 
else is explained in detail in the documents in the docs folder. 
The technical main concept is described in [Realisierungskonzept.pdf](docs/Realisierungskonzept.pdf).
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

