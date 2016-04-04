func main()
  write "Number of disks: "; read n;
  nmoves = 0;
  hanoi(n, 1, 2, 3, nmoves);
  write "Total number of moves: "; write nmoves; write "%n";
endfunc

func hanoi(n, from, aux, to, &mov)
  if n > 0 then
    hanoi(n-1, from, aux, to, mov);
    write "Move from "; write from;
    write " to "; write to; write "%n";
    mov = mov + 1;
    hanoi(n-1, aux, to, from, mov);
  endif
endfunc
