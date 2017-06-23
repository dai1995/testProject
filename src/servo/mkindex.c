#include <stdio.h>
#include <math.h>
#include <string.h>
#include <stdlib.h>

int MAX_NUMBER;
int Index[10000];
int exist();

#define LEARNING_PATTERN  167
#define TSET_PATTERN      50

main(argc,argv)
int argc;
char *argv[];
{
   FILE *in_file, *out_file, *out_file2;
   int number, num, lines, exist_data=1, data;
   char filename[50], readfilename[50], read_data[300];

   if (argc < 3) {
     fprintf(stderr, "mkshell [head filename][max_number]\n");
     exit(1);
   }
   
   MAX_NUMBER = atoi(argv[2]);

   printf("maxnumber = %d\n", MAX_NUMBER);
   srand(1);

   sprintf(filename, "%s.sh", argv[1]);
   if ((out_file = fopen(filename, "w"))==NULL) {
     fprintf(stderr, "outputfile %s cannot be opened!!\n", filename);
     exit(1);
   }

   for (number=0; number<100; number++) {

     for (num = 0; num < MAX_NUMBER; num++) {
       Index[num] = -1;
     }

     for (num = 0; num < MAX_NUMBER; num++) {
       do {
	 data = (int)(MAX_NUMBER * (float)rand() / RAND_MAX);
       }while(exist(data));
       Index[num] = data;
     }
     fprintf(out_file, "date > servo0.time\n");
     for (num=0; num<LEARNING_PATTERN; num++) {
       fprintf(out_file, "cbrnet L servo.def servo/%d.dat servo/conv%d.dat\n", Index[num], number);
     }

     fprintf(out_file, "date > servo1.time\n");
     fprintf(out_file, "mv total_error.dat total_error_servo%d.dat\n", number);
     fprintf(out_file, "clean\n");

     /**** for making convX.dat ****/
     sprintf(filename, "conv%d.dat", number);
     printf("data filename for conventional model is %s\n", filename);
     if ((out_file2 = fopen(filename, "w"))==NULL) {
       fprintf(stderr, "outputfile %s cannot be opened!!\n", argv[2]);
       exit(1);
     }
     for (lines=0; lines<LEARNING_PATTERN; lines++) {
       sprintf(readfilename, "%d.dat", Index[lines]);
       if ((in_file = fopen(readfilename, "r"))==NULL) {
	 fprintf(stderr, "%s cannot be opened!!\n", readfilename);
	 exit(1);
       }
       fgets(read_data, 300, in_file);
       fprintf(out_file2, "%s", read_data);
       fclose(in_file);
     }
     fclose(out_file2);

     /*** for making test data ***/
     sprintf(filename, "test%d.dat", number);
     printf("test data filename is %s\n", filename);
     if ((out_file2 = fopen(filename, "w"))==NULL) {
       fprintf(stderr, "outputfile %s cannot be opened!!\n", argv[2]);
       exit(1);
     }
     for (lines=LEARNING_PATTERN; lines<MAX_NUMBER; lines++) {
       sprintf(readfilename, "%d.dat", Index[lines]);
       if ((in_file = fopen(readfilename, "r"))==NULL) {
	 fprintf(stderr, "%s cannot be opened!!\n", readfilename);
	 exit(1);
       }
       fgets(read_data, 300, in_file);
       fprintf(out_file2, "%s", read_data);
       fclose(in_file);
     }
     fclose(out_file2);
   }/* for */
   fclose(out_file);
}/* main */
	
int exist(data)
int data;
{
  int num;
  for (num=0; num<MAX_NUMBER; num++) {
    if (Index[num] == data) return(1);
  }
  return(0);
}/* exist() */
			    
