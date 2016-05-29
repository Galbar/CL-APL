func append(&arr, val, &size)
    size = size + 1
    if len(arr) < size then
        newarr = int[2 * len(arr)]
        copy(arr, newarr)
        free arr
        arr = newarr
    end
    arr[size] = val
    return
end

func main(argc, argv)
    mult = 1
    l = int[1]
    for l[0] in 1:argc
        read x from argv[l[0]]
        mult = mult * x
    end

    str = char[20]

    write "Result: " to str
    write mult to str
    write str

    free str
    return 0
end
