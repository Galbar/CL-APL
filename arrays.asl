func main()
  read x;
  a[0] = 1;
  a[1 + x] = 5;
  write a;
  write "%n";
  write "val: ";
  write a[1 + x];
  write "%n";
  c = a[2 + x];
  write c;
endfunc

