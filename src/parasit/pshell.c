/* $Id: pshell.c,v 1.25 2003/01/06 10:33:00 ls Exp $ */

/** 
 * \file pshell.c
 * \brief The main loop of the pshell environment.
 * \author Juerg Reusser, Luzian Scherrer
 * 
 * This module is the main loop for the whole pshell environment, it 
 * contains the commandline interface and its parser. Upon comand entry
 * by the user, the appropriate module is passed the given arguments and
 * then called for execution.
 */

#include <stdio.h>
#include <stdlib.h>
#include <readline.h>
#include <history.h>
#include <errno.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/resource.h>

#if defined (LINUX)
#include <asm/timex.h>
#endif

#if defined (SOLARIS)
#include <sys/processor.h>
#endif

#include <time.h>
#include <unistd.h>
#include "globals.h"
#include "parser.h"
#include "vm.h"
#include "pshell.h"
#include "icmpcalc.h"
#include "hostlist.h"

/** Type of the current execution: simulation or parasitic. */
extern int executiontype;

/** Number of cycles that the executing CPU can do per seconds. */
unsigned long long cpu_clockspeed;

/**
 *  This function prints statistics about the last code execution, such as
 *  the number of required ICMP packets or the total execution time,
 *  to STDOUT.
 */
void print_stats()
{
  if(starttime.tv_sec == -1) {
    printf("Statistics are only available after execution.\n");
  } else {
    printf("Description                           Value\n");
    printf("-----------------------------  ------------\n");
    printf("Parallel bits per addition                %d\n", bits_per_add);
    printf("Number of registers required         %6d\n", number_of_registers);
    printf("Registers-width in bits              %6d\n", register_width);
    printf("Virtual machine cycles            %9d\n", cycles);
    printf("ICMP outbound-packets optimum     %9d\n", packets_optimum);
    printf("ICMP outbound-packets             %9d\n", packets_outbound);
    printf("ICMP inbound-packets              %9d\n", packets_inbound);
    printf("Total duration                  %10.6fs\n",  (float)
    ((endtime.tv_sec - starttime.tv_sec)*1000000 +
    (endtime.tv_usec - starttime.tv_usec))/(float)1000000);
    printf("User execution time             %10.6fs\n", (float)
    ((endusage.ru_utime.tv_sec - startusage.ru_utime.tv_sec)*1000000 +
    (endusage.ru_utime.tv_usec - startusage.ru_utime.tv_usec))/(float)1000000);
    printf("System execution time           %10.6fs\n", (float)
    ((endusage.ru_stime.tv_sec - startusage.ru_stime.tv_sec)*1000000 +
    (endusage.ru_stime.tv_usec - startusage.ru_stime.tv_usec))/(float)1000000);
#if defined LINUX|SOLARIS
    printf("CPU cycles used (approx.)    %14.0f\n",
    cpu_clockspeed * 
    (((endusage.ru_utime.tv_sec - startusage.ru_utime.tv_sec)*1000000 +
    (endusage.ru_utime.tv_usec - startusage.ru_utime.tv_usec))/(float)1000000 +
    ((endusage.ru_stime.tv_sec - startusage.ru_stime.tv_sec)*1000000 +
    (endusage.ru_stime.tv_usec -startusage.ru_stime.tv_usec))/(float)1000000));
#endif
  }
}

/**
 *  This function reads through the string pointed to by line and parses
 *  it as a whitespace-separated list of registers named as [rR][0-9]+.
 *  The corresponding values are printed to STDOUT.
 *
 *  @param line Pointer to the string containing the registers.
 */
void print_regs(char *line)
{
  char *p, *reg;
  int validint, header, showall, r;

  p = line;
  while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
  while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */

  header = 0;
  showall = 1;
  reg = strtok(p, " ");
  while(reg != NULL)
  {
    showall = 0;
    p = reg;
    if(reg[0] == 'r' || reg[0] == 'R') p+=1;
    r = atoi(p);

    validint = 1;
    while(*p != '\0') {
      if(atoi(p) == 0 && *p != '0') {
        validint = 0;
      }
      p++;
    }

    if(!header) {
      printf("Register  Decimal      Hex  Comment\n");
      printf("--------  -------  -------  ---------------------------\n");
      header = 1;
    }
    if(validint && r < number_of_registers)
      switch(r)
      {
        case 0:
          printf("r%07d    %5d   0x%04x  Instruction pointer (ip)\n", 
                       r, getreg(r), getreg(r));
          break;
        case 1:
          printf("r%07d    %5d   0x%04x  Flag register (fl)\n", 
                       r, getreg(r), getreg(r));
          break;
        case 2:
          printf("r%07d    %5d   0x%04x  XIA error register\n", 
                       r, getreg(r), getreg(r));
          break;
        case 3:
          printf("r%07d    %5d   0x%04x  XIA carry register\n", 
                       r, getreg(r), getreg(r));
          break;
        default: 
          printf("r%07d    %5d   0x%04x\n", r, getreg(r), getreg(r));
          break;
      }
    else 
      printf("%8.8s                    Not a valid register\n", reg);

    reg = strtok(NULL, " ");
  }

  if(showall) {
    printf("Register  Decimal      Hex  Comment\n");
    printf("--------  -------  -------  ---------------------------\n");
    for(r=0; r<number_of_registers; r++)
      switch(r)
      {
        case 0:
          printf("r%07d    %5d   0x%04x  Instruction pointer (ip)\n",
                      r, getreg(r), getreg(r));
          break;
        case 1:
          printf("r%07d    %5d   0x%04x  Flag register (fl)\n",
                      r, getreg(r), getreg(r));
          break;
        case 2:
          printf("r%07d    %5d   0x%04x  XIA error register\n",
                      r, getreg(r), getreg(r));
          break;
        case 3:
          printf("r%07d    %5d   0x%04x  XIA carry register\n",
                      r, getreg(r), getreg(r));
          break;
        default: 
          printf("r%07d    %5d   0x%04x\n", r, getreg(r), getreg(r));
          break;
      }
  }

}

/**
 *  This function prints the pshell global configuation to STDOUT.
 */
void print_config()
{
  printf("Parameter                               Value\n");
  printf("-------------------------------------  ------\n");
  printf("Timeout threshold to disable host      %6d\n", timeout_threshold);
  printf("Timeout in seconds                     %6d\n", icmp_timeout_sec);
  printf("Parallel bits per addition                  %d\n", bits_per_add);
}

/**
 *  Print help overview to STDOUT.
 */
void print_help()
{
  printf("Shortcut  Command              Short Description\n");
  printf("--------  -------------------  ------------------------------------------------\n");
  printf("h         history              Show command history\n");
  printf("xs        execs <file.4ia>     Simulate execution of <file.4ia>\n");
  printf("xp        execp <file.4ia>     Parasitic ICMP execution of <file.4ia>\n");
  printf("sc        showconfig           Show current VM configuration\n");
  printf("sr        showreg [reg-list]   Show registers in space-separated list\n");
  printf("lh        loadhosts <file>     Load newline-separated hostlist\n");
  printf("sh        showhosts            Show hosts and statistics\n");
  printf("ed        edit <file>          Launch $EDITOR with <file>\n");
  printf("ss        stats                Statistics about last execution\n");
  printf("sb        setbits <number>     Set parallel bits per ICMP addition\n");
  printf("sti       settimeout <number>  Set number of seconds to wait for ICMP replies\n");
  printf("str       setthres <number>    Set number of times a host can reach timeout\n");
  printf("q         quit                 Terminate pshell\n");
}

/**
 *  Print a short banner to STDOUT.
 */
void print_banner()
{
  printf("Parasitic Computing (pshell %s)\n", VERSION);
  printf("Copyright (c) 2002 Juerg Reusser, Luzian Scherrer\n");
}

/**
 *  Parse the command from the string pointed to by line into a token
 *  and advance the line pointer to the character directly after the
 *  parsed command.
 *
 *  @param line Pointer to the commandline-entered string.
 *  @return The token identifiyng the command.
 */
int parse_command(char *line)
{
  if(!strncmp(line, "quit", strlen("quit"))) {
    return PSH_QUIT;
  } else if(!strncmp(line, "q", strlen("q"))) {
    return PSH_QUIT;
  } else if(!strncmp(line, "exit", strlen("exit"))) {
    return PSH_QUIT;
  } else if(!strncmp(line, "help", strlen("help"))) {
    return PSH_HELP;
  } else if(!strncmp(line, "history", strlen("history"))) {
    return PSH_HISTORY;
  } else if(!strncmp(line, "h", strlen("h"))) {
    return PSH_HISTORY;
  } else if(!strncmp(line, "execs", strlen("execs"))) {
    return PSH_EXECS;
  } else if(!strncmp(line, "xs", strlen("xs"))) {
    return PSH_EXECS;
  } else if(!strncmp(line, "execp", strlen("execp"))) {
    return PSH_EXECP;
  } else if(!strncmp(line, "xp", strlen("xp"))) {
    return PSH_EXECP;
  } else if(!strncmp(line, "showconfig", strlen("showconfig"))) {
    return PSH_SHOWCONFIG;
  } else if(!strncmp(line, "sc", strlen("sc"))) {
    return PSH_SHOWCONFIG;
  } else if(!strncmp(line, "showregs", strlen("showregs"))) {
    return PSH_SHOWREG;
  } else if(!strncmp(line, "showreg", strlen("showreg"))) {
    return PSH_SHOWREG;
  } else if(!strncmp(line, "sr", strlen("sr"))) {
    return PSH_SHOWREG;
  } else if(!strncmp(line, "loadhosts", strlen("loadhosts"))) {
    return PSH_LOADHOSTLIST;
  } else if(!strncmp(line, "lh", strlen("lh"))) {
    return PSH_LOADHOSTLIST;
  } else if(!strncmp(line, "showhosts", strlen("showhosts"))) {
    return PSH_SHOWHOSTLIST;
  } else if(!strncmp(line, "sh", strlen("sh")) && line[2] != 'o') {
    return PSH_SHOWHOSTLIST;                   /* ^ makes it unique */
  } else if(!strncmp(line, "edit", strlen("edit"))) {
    return PSH_EDIT;
  } else if(!strncmp(line, "ed", strlen("ed"))) {
    return PSH_EDIT;
  } else if(!strncmp(line, "stats", strlen("stats"))) {
    return PSH_SHOWSTAT;
  } else if(!strncmp(line, "ss", strlen("ss"))) {
    return PSH_SHOWSTAT;
  } else if(!strncmp(line, "sb", strlen("sb"))) {
    return PSH_SETBITS;
  } else if(!strncmp(line, "setbits", strlen("setbits"))) {
    return PSH_SETBITS;
  } else if(!strncmp(line, "settimeout", strlen("settimeout"))) {
    return PSH_SETTIMEOUT;
  } else if(!strncmp(line, "sti", strlen("sti"))) {
    return PSH_SETTIMEOUT;
  } else if(!strncmp(line, "setthres", strlen("setthres"))) {
    return PSH_SETTHRESHOLD;
  } else if(!strncmp(line, "str", strlen("str"))) {
    return PSH_SETTHRESHOLD;
  } else {
    return PSH_UNKNOWN;
  }
}

/**
 *  Strips blanks from the end of the string pointed to by line.
 *
 *  @param line Pointer to the string to be stripped.
 */
void strip_command(char *line)
{
  char *ptr;

  ptr = line;
  ptr+= strlen(line)-1;
  while(*ptr == ' ') ptr--;
  *(ptr+1) = '\0';
}

/**
 *  Set the number of times a host can timeout before getting disabled
 *  from further communication. A default is defined by 
 *  TIMEOUT_THRESHOLD_DEFAULT.
 *
 *  @param arg Pointer to the requested threshold value
 */
void set_threshold(char *arg)
{
  int req_threshold;

  req_threshold = atoi(arg);

  if(req_threshold >= 1) {
    timeout_threshold = req_threshold;
    printf("Timeout threshold changed to %d.\n", timeout_threshold);
  } else {
    printf("%s is not a supported threshold value.\n", arg);
  }
}

/**
 *  Set the number of seconds to wait for a correct ICMP paket to be
 *  answered. A default is defined by ICMP_TIMEOUT_SEC_DEFAULT.
 *
 *  @param arg Pointer to the requested number of seconds
 */
void set_timeout(char *arg)
{
  int req_timeout;

  req_timeout = atoi(arg);

  if(req_timeout >= 1) {
    icmp_timeout_sec = req_timeout;
    printf("ICMP timeout changed to %d seconds.\n", icmp_timeout_sec);
  } else {
    printf("%s is not a supported timeout value; it must be greater than 0.\n", 
           arg);
  }
}

/**
 *  Set the number of parallel bits per addition. The pointer *arg is 
 *  atoi'ed and should return 1, 2, 4 or 8. Other values are not supported 
 *  and result in an error message printed to STDOUT. The default value 
 *  is 1. This function alters the global variable bits_per_add.
 *
 *  @param arg Pointer to the number of requested bits
 */
void set_bits_per_addition(char *arg)
{
  int req_bits;

  req_bits = atoi(arg);

  if(req_bits == 1 || req_bits == 2 || req_bits == 4) {
    bits_per_add = req_bits;
    printf("Parallel bits per addition changed to %d.\n", bits_per_add);
  } else {
    printf("The number of %s parallel additions per bit is not supported,\n", 
           arg);
    printf("please use one of the following constants: 1, 2, 4.\n");
  }
}

/**
 *  Interrupt handler, called when SIGINT (ctrl+c) is sent during 
 *  execution. Needed to terminate endless or very long programs 
 *  in the virtual machine.
 *
 *  @param signum Number of the calling signal.
 */
void sigint_handler(int signum)
{
  if(signum == SIGINT) {
    (void)signal(SIGINT, old_handler);  /* Reestablish old handler */
    runtime_status = -4;
  }
}

/**
 *  The main function shows the pshell banner and then goes into a
 *  prompt/action loop.
 *
 *  @param argc Number of arguments on the commandline.
 *  @param argv Array of pointers to the commandline arguments.
 *  @return Operating system returnvalue.
 */
int main(int argc, char *argv[])
{
  char *line;
  char *p;
  HIST_ENTRY **hist;
  int i;
  int command = -1;
  char buf[BUFSIZ];
  char *envp;
  int ret;

#if defined (LINUX)
  unsigned long long c_start, c_end;
#endif

#if defined (SOLARIS)
  processor_info_t inf;
#endif

  print_banner();

  starttime.tv_sec = -1;

#if defined (LINUX)
  printf("Determining CPU clockspeed... ");
  fflush(stdout);
  c_start = get_cycles();
  sleep(1);
  c_end   = get_cycles();
  cpu_clockspeed = c_end-c_start;
  if(cpu_clockspeed) {
    printf("%.2f MHz\n", cpu_clockspeed/(float)1000000);
  } else {
    printf("unknown (assuming 1 GHz for statistics)\n");
    cpu_clockspeed = 1000000000;
  }
#endif

#if defined (SOLARIS)
  processor_info(0,&inf);
  cpu_clockspeed = inf.pi_clock*1000000;
  printf("Determining CPU clockspeed... %.2f Mhz\n", 
          cpu_clockspeed/(float)1000000);
#endif

  printf("Type help for help.\n");

  while(command != PSH_QUIT) 
  {
    line = readline(PROMPT);
    if(line && *line) 
    {
      strip_command(line);
      add_history(line);
      command = parse_command(line);
      switch(command)
      {
        case PSH_HELP:
          print_help();
          break;
        case PSH_HISTORY:
          hist = history_list();
          if(hist) for(i=0; hist[i]; i++)
            printf("%d: %s\n", i, hist[i]->line);
          break;
        case PSH_EXECS:
          executiontype = SIMULATION;
          p = line;  
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("Which file would you like to have executed?\n");
          } else {
            if( parse(p) > 0 ) {
              old_handler = signal(SIGINT, sigint_handler);
              ret = execute();
              if(ret > 0)
                printf("Execution successfully terminated.\n");
              else if(ret == -4)
                printf("Execution interrupted.\n");
              else
                printf("Execution aborted.\n");
              (void)signal(SIGINT, old_handler);
            }
          }
          break;
        case PSH_EXECP:
          if(geteuid() != 0) {
            printf("The command 'execp' is only available for root!\n");
          } else {
            executiontype = ICMPCALC;
            p = line;  
            while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
            while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
            if(*p == '\0') {
              printf("Which file would you like to have executed?\n");
            } else {
              while(*p == ' ') p++;
              if( parse(p) > 0 ) {
                if(register_width < bits_per_add) {
                  printf("Execution not possible:\n");
                  printf("  Number of parallel bits per addition is higher ");
                  printf("than the register width.\n");
                  break;
                } 
                if(register_width != 2  && 
                   register_width != 4  &&
                   register_width != 8  &&
                   register_width != 16 ) 
                {
                  printf("Execution not possible:\n");
                  printf("  In parasitic mode only 2, 4, 8 and 16 are ");
                  printf("supported register widths.\n");
                  break;
                }
                old_handler = signal(SIGINT, sigint_handler);
                ret = execute();
                if(ret > 0)
                  printf("Execution successfully terminated.\n");
                else if(ret == -4)
                  printf("Execution interrupted.\n");
                else
                  printf("Execution aborted.\n");
                (void)signal(SIGINT, old_handler);
              }
            }
          }
          break;
        case PSH_SHOWCONFIG:
          print_config();
          break;
        case PSH_SHOWREG:
          print_regs(line);
          break;
        case PSH_LOADHOSTLIST:
          p = line;
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("And what's the filename?\n");
          } else {
            if( read_hostlist(p) > 0 )
              printf("Hostlist loaded\n");
          }
          break;
        case PSH_SETBITS:
          p = line;
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("What's the requested number of bits?\n");
          } else {
            set_bits_per_addition(p);
          }
          break;
        case PSH_SETTIMEOUT:
          p = line;
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("Please define a timeout in seconds\n");
          } else {
            set_timeout(p);
          }
          break;
        case PSH_SETTHRESHOLD:
          p = line;
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("Please set an integer value as threshold\n");
          } else {
            set_threshold(p);
          }
          break;
        case PSH_SHOWHOSTLIST:
          print_hostlist();
          break;
        case PSH_EDIT:
          p = line;
          while(*p != ' ' && *p != '\0') p++; /* Go to end of command */
          while(*p == ' ' && *p != '\0') p++; /* Go to end of whitespace */
          if(*p == '\0') {
            printf("Which file would you like to edit?\n");
          } else {
            envp = getenv("EDITOR");
            if(envp == NULL) {
              printf("Environment-variable $EDITOR is not set.\n");
            } else {
              sprintf(buf, "%s %s", envp, p);
              system(buf);
            }
          }
          break;
        case PSH_SHOWSTAT:
          print_stats();
          break;
        case PSH_UNKNOWN:
          p = line;
          while(*p != ' ' && *p != '\0') p++;
          *p = '\0';
          printf("The command '%s' is not known.\n", line);
          break;
      }
    }
  }

  return 0;
}
