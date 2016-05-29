func main()
    read n
    l = int[n]
    for i in 0:n
        read l[i]
    end

    mult = 1
    for i in 0:n
        mult = mult * l[i]
    end

    write mult
    return 0
end
