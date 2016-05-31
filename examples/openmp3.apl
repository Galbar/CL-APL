func append(&arr, val, &num_elems, &size)
    if num_elems == size then
        tmp = arr
        size = 2 * size
        arr = int[size]
        for i in 0:num_elems
            arr[i] = tmp[i]
        end
        free tmp
    end
    arr[num_elems] = val
    num_elems = num_elems + 1
end

func main(argc, argv)
    num_elems = 0 // num_elems
    size = 1 // size
    l = int[size]
    x = 0

    for i in 1:argc
        read x from argv[i]
        append(l, x, num_elems, size)
    end

    out = char[50]
    write "Num elements: " to out
    write num_elems to out
    write "\nAllocated size: " to out
    write size to out
    write out
    free out

    out = char[255]
    for i in 0:num_elems
        write l[i] to out
        write ' ' to out
    end
    write out
    free out
end
