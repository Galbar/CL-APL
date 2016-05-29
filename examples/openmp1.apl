func a(x)
  r = q(x)
  if x > 1 then
    return 1 + a(r)
  end
  return x
end

func main ()
    c = 10
    write c

    a(c)

    l = int[c]
    l[0] = 5

    for i in 0:c
        read l[i]
    end

    for v in l
        write v
    end

    l2 = l

    pfor v in l2
        write v
    end

    c = l[0]

    while c > 0 do
        c = c - 1
    end

    if c == 0 then
        write "first case"
    elif c == 1 then
        write "noone"
    else
        write "second case"
    end

    free l
    return c
end
