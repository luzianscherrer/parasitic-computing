#
#   Parasitic Computing
#   $Id: Makefile,v 1.5 2003/01/02 18:04:47 ls Exp $
#
#   Top-level makefile to build the whole project.
#
#.............................................................................

CSUBDIRS= src/parasit
JSUBDIRS= src/xia
PWD= pwd
UNAME= uname
ANT= ant
INSTALL= cp
MKDIR= mkdir
CHMOD= chmod
PREFIX= /usr/local

all:
	@echo "Making all in `$(PWD)`"
	@for i in $(CSUBDIRS) $(ALLDIRS); do \
		echo "Entering subdirectory $$i"; \
		( cd $$i; $(MAKE) -f Makefile.`$(UNAME)` all ); \
		if test $$? != 0 ; then exit 1; fi ; \
	done
	@for i in $(JSUBDIRS) $(ALLDIRS); do \
		echo "Entering subdirectory $$i"; \
		( cd $$i; $(ANT) package ); \
		if test $$? != 0 ; then exit 1; fi ; \
	done
	@echo ""
	@echo "Compilation finished; all done."

install: all
	$(MKDIR) -p $(PREFIX)/bin
	$(INSTALL) ./src/parasit/pshell $(PREFIX)/bin
	$(INSTALL) ./src/xia/xto4 $(PREFIX)/bin
	$(INSTALL) ./src/xia/Xto4.jar $(PREFIX)/bin
	$(MKDIR) -p $(PREFIX)/share/parasit/4ia
	$(MKDIR) -p $(PREFIX)/share/parasit/xia
	@for i in code/4ia/*.4ia; do \
		$(INSTALL) $$i $(PREFIX)/share/parasit/4ia; \
	done
	@for i in code/xia/*.xia; do \
		$(INSTALL) $$i $(PREFIX)/share/parasit/xia; \
	done
	$(CHMOD) 755 $(PREFIX)/bin/pshell
	$(CHMOD) 755 $(PREFIX)/bin/xto4
	@echo ""
	@echo "---------------------------------------------------------------"
	@echo "   Installation completed!"
	@echo ""
	@echo "   Binaries are installed in $(PREFIX)/bin,"
	@echo "   code examples installed in $(PREFIX)/share/parasit."
	@echo ""
	@echo "   Make sure that $(PREFIX)/bin is in your PATH."
	@echo "---------------------------------------------------------------"

uninstall:
	rm -f $(PREFIX)/bin/pshell
	rm -f $(PREFIX)/bin/xto4
	rm -f $(PREFIX)/bin/Xto4.jar
	rm -rf $(PREFIX)/share/code

clean:
	@echo "Making all in `$(PWD)`"
	@for i in $(CSUBDIRS) $(ALLDIRS); do \
		echo "Entering subdirectory $$i"; \
		( cd $$i; $(MAKE) -f Makefile.`$(UNAME)` clean ); \
		if test $$? != 0 ; then exit 1; fi ; \
	done
	@for i in $(JSUBDIRS) $(ALLDIRS); do \
		echo "Entering subdirectory $$i"; \
		( cd $$i; $(ANT) clean ); \
		if test $$? != 0 ; then exit 1; fi ; \
	done
	@echo ""
	@echo "Cleaning finished; all done."
