func a(x)
  if x > 1 then
    return 1 + a(x-1)
  end
  return x
end

func main ()
    c = 10
    write c

    c = a(c)

    l = int[c]
    l[0] = 5

    for i in 0:c
        read l[i]
    end

    for v in 0:c
        write l[v]
    end

    l2 = l

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
