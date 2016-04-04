// Ackermann's function (n>=0, m>=0)
func ack(m,n)
  if m = 0 then return n+1 endif;
  if n = 0 then return ack(m-1, 1) endif;
  return ack(m-1, ack(m, n-1))
endfunc

func main()
  write "Enter m and n: ";
  read m; read n;
  write "A("; write m; write ","; write n; write ")=";
  write ack(m,n); write "%n"
endfunc
