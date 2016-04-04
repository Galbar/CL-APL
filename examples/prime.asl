func main()
  write "Enter a number: "; read x;
  d=1;
  p = is_prime(x,d);
  write "It is ";
  if not p then write "not " endif;
  write "prime.%n";
  
  if not p then
    write d;
    write " is a divisor of ";
    write x; write ".%n"
  endif
endfunc

func is_prime(n, &div)
  if n = 1 then return false endif;
  div = 2;
  while div*div <= n do
    if n%div = 0 then return false endif;
    div = div + 1;
  endwhile;
  return true
endfunc
