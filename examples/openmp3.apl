func append(arr, val, info)
    if info[0] == info[1] then
        tmp = arr
        info[1] = 2 * info[1]
        arr = (typeof arr[0])[info[1]]
        for i in 0:info[0]
            arr[i] = tmp[i]
        end
        free tmp
    end
    arr[info[0]] = val
    info[0] = info[0] + 1
    return arr
end

func main(argc, argv)
    info = int[2]
    info[0] = 0 // num_elems
    info[1] = 2 // size
    l = int[info[1]]
    x = 0

    for i in 1:argc
        read x from argv[i]
        l = append(l, x, info)
    end

    out = char[50]
    write "Num elements: " to out
    write info[0] to out
    write "\nAllocated size: " to out
    write info[1] to out
    write out
    free out

    out = char[255]
    for i in 0:info[0]
        write l[i] to out
        write ' ' to out
    end
    write out
    free out
end
