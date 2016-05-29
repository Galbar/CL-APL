func fib(x)
    if x < 2 then return x end
    if false then return fib('a') end
    return fib(x-1) + fib(x-2)
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
        write fib(i)
    end

    return 0
end
