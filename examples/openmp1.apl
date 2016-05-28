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
    f = 1
    l2 = l
    pfor v in l2
        write v
    end
    return l[0]
end
