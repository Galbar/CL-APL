#include <stdio.h>
#include <stdlib.h>

void main ( int, char** );

void main (int var0, char** var1)
{
int* var2 = NULL;
int var3;
var2 = malloc(4 * sizeof(int));
var2[0] = 1;
var2[1] = 2;
var2[2] = 3;
var2[3] = 4;
printf("%i\n", sizeof(var2));
printf("%i\n", sizeof(int));
for (int i1 = 0; i1 < sizeof(var2)/sizeof(int); ++i1)
{
var3 = var2[i1];
printf("%i\n", var3);
}
}

// HAY QUE GUARDARSE LA N