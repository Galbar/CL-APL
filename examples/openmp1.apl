func a(x)
  r = q(x)
  if x > 1 then
    return 1 + a(r)
  end
  return x
end

func main ()
    c = 10
    a(c)
    l = int[c]
    for i in 0:c
        read l[i]
    end
    f = 1
    l2 = l
    pfor v in l2
        write v
    end
end
