#! /bin/sh
bin/Apl $1 | gcc -fopenmp -o $2 -x c -
