#include <stdio.h>
#include <math.h>

#define MAX_DIM 13
#define CHARACTER_REGION 10
#define CHARACTER_VALUE 1

float max_data[MAX_DIM];
int max_line = 0;

main(argc, argv)
int argc;
char *argv[];
{
	FILE *fp, *w_fp;

	if (argc < 3) {
		fprintf(stderr, "normarize [inputfile][outputfile]\n");
		exit(1);
	}
	if ((fp = fopen(argv[1],"r"))==NULL) {
		fprintf(stderr, "%s cannot be opened!!\n", argv[1]);
		exit(1);
	}
	if ((w_fp = fopen(argv[2],"w"))==NULL) {
		fprintf(stderr, "%s cannot be opened!!\n", argv[2]);
		exit(1);
	}
	printf(" get_max \n");
	get_max(fp);
	rewind(fp);
	printf(" normalize \n");
	normalize(fp, w_fp);
	fclose(w_fp);
}



get_max(fp)
FILE *fp;
{
	int dim;
	float data;

	max_line = 0;
	while (!feof(fp)) {
		max_line ++;
		printf("%d:", max_line);
		for (dim=0; dim<MAX_DIM; dim++) {
			if (fscanf(fp, "%f", &data)==EOF) break;
			printf(" %1.2f ", data);
			if (max_data[dim] < data) max_data[dim] = data;
		}
		printf("\n");
	}
	max_line--; //1つ多いため
	printf("max_line is %d\n", max_line);
}/* get_max() */



normalize(fp, w_fp)
FILE *fp, *w_fp;
{
	int dim, line;
	float data;

	for (line=0; line<max_line; line++) {
	        for (dim=0; dim<CHARACTER_REGION; dim++) {
			fscanf(fp, "%f", &data);
			if (data > 0) {
			  fprintf(w_fp, "%f ", (float)CHARACTER_VALUE);
			}else{
			  fprintf(w_fp, "%f ", data); 
			}
		}
		for (dim=CHARACTER_REGION; dim<MAX_DIM; dim++) {
			fscanf(fp, "%f", &data);
			fprintf(w_fp, "%f ", data/max_data[dim]);
		}
		fprintf(w_fp, "\n");
	}
}/* normalize() */
