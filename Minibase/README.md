# Minibase - extracting join conditions from the body of a query

```
JoinOperator(
    Operator child1, 
    Operator child2, 
    RelationalAtom atom1, 
    RelationalAtom atom2
)
```

A join operator in my Minibase only takes the child operators and 
relational atom (schema) of child operators, but no explicit comparison atoms.
The join operator will handle implicit equalities in a join. 
For example, the `R._2 = S._1` condition in `R(a,b), S(b,c)` will be checked 
by the join operator.

If there is a query like `Q(a,b,bb,c) :- R(a,b), S(bb,c), b = bb`, my join operator
is not sufficient to handle the conditions. A select operator will be added as the
parent node of join operator and make a tree like this:

```
SelectOp(R_JOIN_S(a,b,bb,c), [b = bb])
            |
JoinOp(R(a,b), S(bb,c))
       /       \     
      /         \ 
ScanOp(R(a,b))  ScanOp(S(bb,c))
```

### Keep only necessary comparison atoms for select over join

As it is required to avoid Cartesian product on join, the most intuitive idea is 
pushing down comparisons which are irrelevant to the join. Therefore, all comparisons 
can be performed on individual relation level will not be added into the SelectOp over the
JoinOp. There is a simple ruleset for filtering out the comparisons which really relates to
this join:
1. the comparison atom is a variable to variable comparison;
2. the two variables are not in one atom before join;
3. one variable in comparison occurred in one relation, and another one occurred in another.

Following are several on comparison atoms, with the join as `R(a,b), S(bb,c)` :
1. `a < bb` - Take
1. `b = bb` - Take
1. `a < c` - Take
1. `c != b` - Take
1. `a < 1` - Not take (rule 1)
1. `c = 2` - Not take  (rule 2)
1. `a < b` - Not take  (rule 2)
1. `bb < c` - Not take  (rule 2)

Therefore, unrelated comparison atoms will not be added into the select over join. 
The eliminated comparison atoms are either 1. can be resolved with a single relation. 
or 2. can be applied to other joins.

### Selection on a single relation (push down)

After a scan operator is instantiated for a relation, a select operator will be built 
as its parent in some cases. 

The first case is an implicit comparison which already explained well in the coursework 
manual.

And the second case is my optimization to Cartesian product. A select operator will be added 
if one or more comparison atoms can be solved purely on this atom. The rule is simple - a 
comparison atom will added into the selection list as long as all variables in the comparison 
exist in the relation.

Following are several on comparison atoms, with the relation as `R(a,b, c)` :
1. `a < 1` - Take
1. `b = 1` - Take
1. `1 < 1` - Take (takes comparison atoms with two constants as not violation)
1. `c > 1` - Take
1. `a < b` - Take
1. `a = d` - Not take
1. `d = 1` - Not take
1. `d = e` - Not take
 
Therefore, all single atom wide comparisons can be performed before the join, 
then avoiding Cartesian product.

### A systematic example

For query like `Q(a,b,c,bb,e) :- R(a,b,c), S(bb,c,e), b = bb, c = 1, 1 < 2, a = b`, 
following tree will be built:

```
       SelectOp(R_JOIN_S(a,b,c,bb,e), [b = bb])
                           |
             JoinOp(R(a,b,c), S(bb,c,e))
             /                      \     
           /                          \     
         /                              \ 
Select(R(a,b,c),[1<2,a=b])   Select(S(bb,c,e),[c=1,1<2])
        |                                |
ScanOp(R(a,b,c))                 ScanOp(S(bb,c,c))
```
