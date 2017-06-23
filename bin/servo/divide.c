#include <stdio.h>
#include <string.h>

main(argc,argv)
int argc;
char *argv[];
{
   FILE *in_file, *out_file;
   int number;
   char filename[50], data[300];
	if (argc<2) {
		fprintf(stderr, "Usage: divide [inputfile]\n");
		exit(1);
	}
	if ((in_file = fopen(argv[1], "r"))==NULL) {
		fprintf(stderr, "inputfile %s cannot be opened!!\n", argv[1]);
		exit(1);
	}

	number = 0;
	while (fgets(data, 300, in_file)!=NULL) {
		printf("readed string is %s\n", data);
		sprintf(filename, "%d.dat", number);
		printf("filename is %s\n", filename);
		if ((out_file = fopen(filename, "w"))==NULL) {
			fprintf(stderr, "outputfile %s cannot be opened!!\n", argv[2]);
			exit(1);
		}
		fprintf(out_file, "%s", data);
		fclose(out_file);
		number ++;
	}/* while */
}/* main */
	
