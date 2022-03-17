tests = """Q(a,b,c,d,x,y,z) :- P(a,b,c,d), D(x,y,z), a = z
Q(a,b,c,d,x,y) :- P(a,b,c,d), D(x,y,a)
Q(a,b,c,d,e,f,g,h,i,j) :- P(a,b,c,d), D(e,f,g), A(h,i,j), a = h, e = i
Q(a,b,c,d,e,f,g,h) :- P(a,b,c,d), D(e,f,g), A(h,i,j), a = h, e = i
Q(d,e,f,g,h) :- P(a,b,c,d), D(e,f,g), A(h,i,j), a = h, e = i
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(a,b,c,d,e,f,g) :- P(a,b,c,d), D(e,f,g), A(a,e,1)
Q(SUM(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(SUM(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(SUM(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(AVG(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(AVG(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(AVG(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,SUM(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,SUM(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,SUM(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,AVG(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,AVG(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(f,AVG(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(g,f,SUM(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(j,f,SUM(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(e,f,SUM(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(g,f,AVG(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(j,f,AVG(g)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(e,f,AVG(j)) :- P(a,b,c,d), D(e,f,g), A(a,e,j)
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j = 1, g <= 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j != 0, g <= 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j = 1, g < 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j != 0, g < 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j != 0, g >= 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j != 0, g > 7
Q(a,b,c,d,e,f,g,j) :- P(a,b,c,d), D(e,f,g), A(a,e,j), j != 0, g > 7
Q(a,b,c,d,e,f,g) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2
Q(a,b,c,d,e,f,g) :- P(a,b,c,d), D(e,f,g), A(a,e,1), g >= 7
Q(a,b,c,d,e,f,g) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery'
Q(g,f,SUM(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery'
Q(j,f,AVG(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery'
Q(g,f,SUM(e)) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery'
Q(a,b,c,d) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery'
Q(a,b,c,d,e,g) :- P(a,b,c,d), D(e,'Surgery',g), A(a,e,1), e >= 2, g >= 7
Q(a,b,c,d,e,f,g) :- P(a,b,c,d), D(e,f,g), A(a,e,1), e >= 2, g >= 7, f = 'Surgery', a < 7
R(a,b,c,d,e,f,g,h,i,j,k,l,m,n) :- P(a,b,c,d), D(e,f,g), A(a,e,h), Q(i,j,k,l,m,n)
R(a,b,c,d,e,f,g,h,i,j,k,l,m,n) :- P(a,b,c,d), D(e,f,g), A(a,e,h), Q(i,j,k,l,m,n), n = a 
R(a,b,c,d,e,f,g,h,i,j,k,l,m,n) :- P(a,b,c,d), D(e,f,g), A(a,e,h), Q(i,j,k,l,m,n), a = n
R(a,b,c) :- P(a,b,c,d), D(e,f,g), A(a,e,h), Q(i,j,k,l,m,n), a = n
R(a,b,c,d,e,f,g,h,i,j,k,l,m,n) :- P(a,b,c,d), D(e,f,g), A(a,e,j), Q(k,l,n,m,o,a)
R(a,b,k,l,m,n) :- P(a,b,c,d), D(e,f,g), A(a,e,j), Q(k,l,n,m,o,a)"""

start_num = 1

test_lines = tests.split('\n')
for i in range(len(test_lines)):
    with open("input/query" + '%02d' % (start_num + i) + '.txt', 'w') as f:
        f.write(test_lines[i].encode('utf-8'))