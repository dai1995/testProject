/*
* shuffle.c Programmed by K.Yamauchi 1998.10.16
* This programm normalizes each dimension by a maximum value of the dimension and outputs the instances in a random sequence.
*/

#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>

typedef struct data {
float *dat; /* array of data */
char check; /* flag showing wheter the instance has been written or not. */
} DATA;

float *max; /* maximum value of each dimension */


int NUMBER_OF_INSTANCES, INPUT_DIM, OUTPUT_DIM, NUMBER_OF_FILES;
char output_head_name[50], output_filename[100];
DATA *all_data;

int get_number_of_instances(FILE *fp, int input_dim, int output_dim);
void allocate_memory(int number_of_instances, int input_dim, int output_dim);
void read_instances(FILE *targetfile, int number_of_instances, int input_dim, int output_dim);
void output_instances(FILE *outputfile, int NUMBER_OF_INSTANCES, int INPUT_DIM, int OUTPUT_DIM);
void normalize_instances(int number_of_instances, int input_dim, int output_dim);

main(int argc, char *argv[])
{
  int num;
  FILE *targetfile, *outputfile;

  if (argc < 6) {
    fprintf(stderr, "Usage shuffle [target-file][headname][number of files][input dimension][output dimension] \n");
    exit(1);
  }

  if ((targetfile = fopen(argv[1], "r"))==NULL) {
    fprintf(stderr, "The target file %s does not exist!!\n", argv[1]);
    exit(1);
  }

  srand(1); /* initialize random sequences */

  strcpy(output_head_name, argv[2]);
  NUMBER_OF_FILES = atoi(argv[3]);
  INPUT_DIM  = atoi(argv[4]);
  OUTPUT_DIM = atoi(argv[5]);

  NUMBER_OF_INSTANCES =
      get_number_of_instances(targetfile, INPUT_DIM, OUTPUT_DIM);
  rewind(targetfile);

  printf("number of instances are %d\n", NUMBER_OF_INSTANCES);
  allocate_memory(NUMBER_OF_INSTANCES, INPUT_DIM, OUTPUT_DIM);
  read_instances(targetfile, NUMBER_OF_INSTANCES, INPUT_DIM, OUTPUT_DIM);

  /*** avoid normalization for servo datasets */
  /*normalize_instances(NUMBER_OF_INSTANCES, INPUT_DIM, OUTPUT_DIM);*/

  for (num = 0; num < NUMBER_OF_FILES; num++) {
    sprintf(output_filename, "%s%d.dat", output_head_name, num);
    if ((outputfile = fopen(output_filename, "w"))==NULL) {
      fprintf(stderr, "%s cannot be open for write-mode\n", output_filename);
      exit(1);
    }
    output_instances(outputfile, NUMBER_OF_INSTANCES, INPUT_DIM, OUTPUT_DIM);
    fclose(outputfile);
  }
}


int get_number_of_instances(FILE *fp, int input_dim, int output_dim)
{
  float dat;
  int in, out, number_of_instances = 0, status;

  do {
    for (in=0; in<input_dim; in++) {
      fscanf(fp, "%f", &dat);
    }
    for (out=0; out<output_dim; out++) {
      status = fscanf(fp, "%f", &dat);
    }
    number_of_instances++;
  }while(status !=EOF);
  return (number_of_instances-1);
}/* get_number_of_instances() */



void allocate_memory(int number_of_instances, int input_dim, int output_dim)
{
  int n;

  if ((all_data = (DATA *)malloc(number_of_instances * sizeof(DATA)))==NULL) {
    fprintf(stderr, "Memory is full!!\n");
    exit(1);
  }

  for (n=0; n<number_of_instances; n++) {
    if ((all_data[n].dat = (float *)malloc((input_dim+output_dim)*sizeof(float)))==NULL) {
      fprintf(stderr, "Memory is full!!\n");
      exit(1);
    }
    all_data[n].check = 0;
  }
  if ((max = (float *)malloc((input_dim+output_dim)*sizeof(float)))==NULL) {
    fprintf(stderr, "Memory is full!!\n");
    exit(1);
  }
}/* allocate_memory() */

      
void read_instances(FILE *targetfile, int number_of_instances, int input_dim, int output_dim)
{
  int n, i;
  float one_data;
  for (n=0; n<number_of_instances; n++) {
    for (i=0; i<input_dim+output_dim; i++) {
      fscanf(targetfile, "%f", &one_data);
      all_data[n].dat[i] = one_data;
      max[i] = -1;
    }
  }
}/* read_instances() */
  
void normalize_instances(int number_of_instances, int input_dim, int output_dim)
{
  int n, i;
  float one_data;

  for (n=0; n<number_of_instances; n++) {
    for (i=0; i<input_dim+output_dim; i++) {
      if (all_data[n].dat[i] > max[i]) {
	max[i] = all_data[n].dat[i];
      }
    }
  }

  for (n=0; n<number_of_instances; n++) {
    for (i=0; i<input_dim+output_dim; i++) {
      all_data[n].dat[i] /= max[i];
    }
  }
}/* normalize_instance() */
  
void output_instances(FILE *outputfile, int NUMBER_OF_INSTANCES, int INPUT_DIM, int OUTPUT_DIM)
{
  int n, i, target;
  for (n=0; n< NUMBER_OF_INSTANCES; n++) {
    all_data[n].check = 0;
  }
  for (n=0; n< NUMBER_OF_INSTANCES; n++) {
    do {
      do {
	target = (int)((float)(NUMBER_OF_INSTANCES)*(float)rand()/(float)RAND_MAX);
      }while(target<0 && target>=NUMBER_OF_INSTANCES);
    }while(all_data[target].check);
    printf("target=%d\n", target);
    for (i=0; i<INPUT_DIM+OUTPUT_DIM; i++) {
      fprintf(outputfile, "%f ", all_data[target].dat[i]);
    }
    //fprintf(outputfile, "%f ", all_data[target].dat[0]); /* first dimensiton is the output */
    fprintf(outputfile, "\n");
    all_data[target].check = 1;
  }
}/* output_instances() */


