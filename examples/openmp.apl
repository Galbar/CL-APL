func main()
	N = 100
	a = int[N]
	b = int[N]
	i = 0
	sum = 0
	nsum = 0
	
	parallel shared(a,b,sum,nsum) private(i) num_threads(4)
		pfor i in 0:N
			a[i] = i
			b[i] = i
		end

		pfor i in 0:N reduction(+:sum)
			sum = sum + a[i] * b[i]
			nsum = nsum + 1
		end
	end

	str = char[20]
	write "Sum = " to str
	write sum to str
	write str
	free str 

	str = char[20]
	write "Number of sums = " to str
	write nsum to str
	write str
	free str 
end