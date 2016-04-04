/* Euclid's algorithm for gcd */

func gcd(a, b)
  while b != 0 do
    z = a % b;
    a = b;
    b = z
  endwhile;
  return a
endfunc

func main()
  write "Enter two numbers: ";
  read x; read y;
  write "The gcd is ";
  write gcd(x, y);
  write "%n"
endfunc
  
