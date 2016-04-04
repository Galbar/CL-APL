/**
  * This program reads a sequence of numbers and calculates
  * the maximum value.
  */
  
func main()
  write "Enter the length of a sequence of numbers: ";
  read n;
  if n <= 0 then return 0 endif;

  write "Enter the sequence: ";
  read max;
  i = 1;
  while i < n do
    read x;
    if x > max then max = x endif;
    i = i + 1
  endwhile;

  write "The maximum is "; write max; write "%n"
endfunc
  
