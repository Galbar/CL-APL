func main()
  write "Enter ther order of the Fibonacci number: ";
  read n;
  write "Iterative or recursive algorithm (0 or 1)? ";
  read alg;

  if alg = 0 then fib = fib_iter(n)
             else fib = fib_rec(n)
  endif;

  write "Fibonacci("; write n; write ")="; write fib; write "%n"
endfunc

func fib_iter(n)
  i = 1;
  f_i = 1; f_i1 = 1;
  while i < n do
    f = f_i + f_i1;
    f_i1 = f_i;
    f_i = f;
    i = i+1
  endwhile;
  return f
endfunc

func fib_rec(n)
  if n <= 1 then return 1
            else return fib_rec(n-1) + fib_rec(n-2)
  endif
endfunc
