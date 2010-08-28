---
title: A micro-manual for LISP Implemented in C
tags: lisp c
---

Recently I had to go through some code that uses the
[uIP](http://www.sics.se/~adam/uip/index.php/Main_Page) TCP/IP stack,
which reminded me, it has been a long time since I did something in C so
I ended up spending the weekend implementing the 10 rules [John
McCarthy](http://en.wikipedia.org/wiki/John_McCarthy_(computer_scientist\))
described in his paper [A Micro-Manual for Lisp - not the whole
Truth](https://docs.google.com/fileview?id=0B0ZnV_0C-Q7IOTRkNzVjZjMtMWE1NC00YzQ3LTgzMWEtM2UwY2I1YzdmNmM5&hl=en).

     enum type {CONS, ATOM, FUNC, LAMBDA};

     typedef struct{
       enum type type;
     } object;

     typedef struct {
       enum type type;
       char *name;
     } atom_object;

     typedef struct {
       enum type type;
       object *car;
       object *cdr;
     } cons_object;

     typedef struct {
       enum type type;
       object* (*fn)(object*,object*);
     } func_object;

     typedef struct {
       enum type type;
       object* args;
       object* sexp;
     } lambda_object;

We begin by defining four types of objects we will be using. CONS is
what we use to hold lists, ATOMs are letters or digits anything that is
not used by LISP, a FUNC holds a reference to a C function and a LAMBDA
holds a lambda expression.

     object *read_tail(FILE *in) {
       object *token = next_token(in);

       if(strcmp(name(token),")") == 0)
         return NULL;
       else if(strcmp(name(token),"(") == 0) {
         object *first = read_tail(in);
         object *second = read_tail(in);
         return cons(first, second);
       }else{
         object *first = token;
         object *second = read_tail(in);
         return cons(first, second);
       }
     }

     object *read(FILE *in) {
       object *token = next_token(in);

       if(strcmp(name(token),"(") == 0)
         return read_tail(in);

       return token;
     }

*read* gets the next token from the file, if it is a left parentheses it
calls *read_tail* to parse the rest of the list, otherwise returns the
token read. A list (LIST e1 ... en) is defined for each n to be (CONS
e1 (CONS ... (CONS en NIL))) so *read_tail* will keep calling itself
concatenating cons cells until it hits a right parentheses.

     object* init_env(){
       object *env = cons(cons(atom("QUOTE"),cons(func(&fn_quote),NULL)),NULL);
       append(env,cons(atom("CAR"),cons(func(&fn_car),NULL)));
       append(env,cons(atom("CDR"),cons(func(&fn_cdr),NULL)));
       append(env,cons(atom("CONS"),cons(func(&fn_cons),NULL)));
       append(env,cons(atom("EQUAL"),cons(func(&fn_equal),NULL)));
       append(env,cons(atom("ATOM"),cons(func(&fn_atom),NULL)));
       append(env,cons(atom("COND"),cons(func(&fn_cond),NULL)));
       append(env,cons(atom("LAMBDA"),cons(func(&fn_lambda),NULL)));
       append(env,cons(atom("LABEL"),cons(func(&fn_label),NULL)));

       tee = atom("#T");
       nil = cons(NULL,NULL);

       return env;
     }

Now that we have a list to execute, we need to define the environment we
will be evaluating the expressions in. Environment is a list of pairs
during evaluation we replace those atoms with their values, we also
define tee to be the atom *#T* and nil to be the empty list.

     object *eval_fn (object *sexp, object *env){
       object *symbol = car(sexp);
       object *args = cdr(sexp);

       if(symbol->type == LAMBDA)
         return fn_lambda(sexp,env);
       else if(symbol->type == FUNC)
         return (((func_object *) (symbol))->fn)(args, env);
       else
         return sexp;
     }

     object *eval (object *sexp, object *env) {
       if(sexp->type == CONS){
         if(car(sexp)->type == ATOM && strcmp(name(car(sexp)), "LAMBDA") == 0){
           object* largs = car(cdr(sexp));
           object* lsexp = car(cdr(cdr(sexp)));

           return lambda(largs,lsexp);
         }else{
           object *accum = cons(eval(car(sexp),env),NULL);
           sexp = cdr(sexp);

           while (sexp != NULL && sexp->type == CONS){
             append(accum,eval(car(sexp),env));
             sexp = cdr(sexp);
           }


           return eval_fn(accum,env);
         }
       }else{
         object *val = lookup(name(sexp),env);
         if(val == NULL)
           return sexp;
         else
           return val;
       }
     }

When we pass an S-Expression to eval, first we need to check if it is a
lambda expression if it is we don't evaluate it we just return a lambda
object, if it is a list we call eval for each cell, this allows us to
iterate through all the atoms in the list when we hit an atom we lookup
its value in the environment if it has a value associated with it we
return that otherwise we return the atom, at this point,

    (QUOTE A)

is transformed into,

    (func-obj atom-obj)

all eval\_fn has to do is check the type of the car of the list, if it is
a function\_object it will call the function pointed by the
function\_object passing cdr of the list as argument, if it is a
lambda\_object we call the fn\_lambda which executes the lambda
expression else we return the S-Expression.

Each function_object holds a pointer to a function that takes two
arguments, arguments to the function and the environment we are executing
it in and returns an object.

     object *fn_lambda (object *args, object *env) {
       object *lambda = car(args);
       args = cdr(args);

       object *list = interleave((((lambda_object *) (lambda))->args),args);
       object* sexp = replace_atom((((lambda_object *) (lambda))->sexp),list);
       return eval(sexp,env);
     }

A lambda_object holds two lists,

    (LAMBDA (X Y) (CONS (CAR X) Y))
    args -> (X Y)
    sexp -> (CONS (CAR X) Y))

to execute it first thing we do is interleave the args list with the
arguments passed so while executing following,

    ((LAMBDA (X Y) (CONS (CAR X) Y)) (QUOTE (A B)) (CDR (QUOTE (C D))))

list will be,

    ((X (A B)) (Y (D)))

then we iterate over the sexp and replace every occurrence of X with (A
B) and every occurrence of Y with (D) then call eval on the resulting
expression.

This covers everything we need to interpret the LISP defined in the
paper passing a file containing the following,

     (QUOTE A)
     (QUOTE (A B C))
     (CAR (QUOTE (A B C)))
     (CDR (QUOTE (A B C)))
     (CONS (QUOTE A) (QUOTE (B C)))
     (EQUAL (CAR (QUOTE (A B))) (QUOTE A))
     (EQUAL (CAR (CDR (QUOTE (A B)))) (QUOTE A))
     (ATOM (QUOTE A))
     (COND ((ATOM (QUOTE A)) (QUOTE B)) ((QUOTE T) (QUOTE C)))
     ((LAMBDA (X Y) (CONS (CAR X) Y)) (QUOTE (A B)) (CDR (QUOTE (C D))))
     (LABEL FF (LAMBDA (X Y) (CONS (CAR X) Y)))
     (FF (QUOTE (A B)) (CDR (QUOTE (C D))))
     (LABEL XX (QUOTE (A B)))
     (CAR XX)

should produce,

     lisp/ $ gcc -Wall lisp.c && ./a.out test.lisp 
     > A
     > (A B C)
     > A
     > (B C)
     > (A B C)
     > #T
     > ()
     > #T
     > B
     > (A D)
     > #T
     > (A D)
     > #T
     > A

Download [lisp.c](/code/misc/lisp.c)
