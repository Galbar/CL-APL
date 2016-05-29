func append(arr, val, size)
    size = size + 1
    arr[size] = val
    return
end

func main(argc, argv)
    str = char[20]
    mult = 1
    parallel shared(a,b,c) private(d,e)
        l = int[1]
        for l[0] in 1:argc
            read x from argv[l[0]]
            mult = mult * x
        end

        append(l, 1, 1)
    end

    write "Result: " to str
    write mult to str
    write str

    free str
    return 0
end
