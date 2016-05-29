func fib1(x)
    if x < 2 then return x end
    return fib2(x-1) + fib2(x-2)
end

func fib2(x)
    if x < 2 then return x end
    return fib3(x-1) + fib3(x-2)
end

func fib3(x)
    if x < 2 then return x end
    return fib1(x-1) + fib1(x-2)
end

func main(argc, argv)
    if argc != 2 then
        msg = char[30]

        write "Usage: " to msg
        write argv[0] to msg
        write " <n>" to msg
        write msg

        free msg
        return 1
    end

    read x from argv[1]
    for i in 0:x+1
        write fib1(i)
    end

    return 0
end
