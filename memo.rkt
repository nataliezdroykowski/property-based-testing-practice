#lang lsl

#|

The library that I've identified as a potential Java PBT Library to use for property based testing is jqwik. It provides a way to create properties in Java
that can be tested, and all of the features of the library work to help you be able to refine these property tests to ensure your methods are
behaving as they are supposed to. After looking into this library, I have identified several strengths and weaknesses.

There are many strengths that I've found with this library. Firstly, it is very useful how customizable everything is. There are additional options
for what you want to be reported with a property failure, and there are many ways to get more information about what you're doing. Also, customizing
value generation is pretty intuitive, and there are several incredibly useful built in options for them. Mapping and filtering over input lists is
also built in, which allows for you to make the values pretty much exactly how you want them to be. Another strength that I've found is that the information
provided for a property failure is very thorough, which is valuable for understanding why the property failed in order to fix the method or property accordingly.
It is also very easy to mix in this property based testing with standard unit based testing, which is a plus.

One weakness that I have noticed is that it is somewhat complicated to create a contract that allows you to specify what the output should be. For properties,
the output of its method should always be true, so it isn't necessary for their to be constraints on the output, but being able to put these contraints
on methods can be helpful for creating a property. Checking that a method is returning the right kind of value is a good first step to making sure it is
behaving properly, which is why these contracts are so important. Also, there aren't many built in methods for arbitraries, which is how the values being generated
as input are constrained, which can be challenging and time consuming as well since methods have to be made manually. Also, recursion and other
more complicated forms of properties are a bit difficult to use and understand, which isn't good because they're very important to property
based testing.

Overall, there are many different aspects of this library that make it so useful, so incorporating it could be very beneficial.
I've also provided some examples of how to use this library in Java to implement PBT.

Example 1:
First, I'm going to implement a basic property that checks to see if the result of a function that multiplies a given integer by 2
is an even number, which is a property that should pass.

// this is a simple method that takes an int and returns the value of that int doubled
int double(int number) {
   return number * 2;
}

// this is the property that checks that the result of the double method is an even number
@Property
boolean doublesArePositive(@ForAll int number) {
   return double(number) % 2 == 0;
}

As you can see, the way to denote a property method is to include @Property before the method. Another important difference is the inclusion of the @ForAll
tag before the arguments. This is important because this is how you indicate that this is a value that should be tested through random value generation, which
is a key attribute of property testing. In this example, I used a property test method that would return a boolean, giving us true if the property
succeeds and false if it fails, but there is also a way to implement these tests within void methods.

To summarize this property method, for 1000 times, which is the default number of tries but can be specified to be different, a random set of parameter values (a random integer)
will be generated. This integer will be used in the double method where it is multiplied by 2 and returned. Then, back in the property method, this returned value
will be calculated mod 2, and if that equals 0, the property returns true. If this method returns true for all 1000 tries, which is should in this case, that means
the property is true. If there would to be case that failed, that failure would be reported, and the property would be incorrect.

Example 2:
Now, I'm going to make a property that should fail, so I can explain how this library deals with reporting these failures. This example will also demonstrate
one of the ways you can customize a property's parameters' contracts.

// this is a method that returns the last value of the given list of integers
int getLast(List<Integer> loi) {
   return loi.get(loi.size() - 1);
}

// this is a property that generates a list of 10 integers, each element being different and between 0 and 10, and checks that
// the getLast method returns a value that is greater than the rest of the elements in the list
@Property
boolean lastIsHighest(@ForAll @Size(10) @UniqueElements List<@IntRange(min=0, max=10) Integer> loi) {
   int last = getLast(loi);
   boolean highest = true;
   for (int i = 0; i < loi.size() - 1; i++) {
      if (loi.get(i) > last) {
         highest = false;
      }
   }
   return highest;
}

Something new that this example demonstrates is a way to constrain the default generation of parameter values. The idea behind this is that if
you're defining a property that only needs to be true for specific inputs, you can constrain the program to only check values within these bounds.
In this example, I used the @Size(10), @UniqueElements and @IntRange(min=0, max=10) to specify exactly what kind of lists I wanted to test this property with,
meaning that it would have 10 elements that are all different and all between 0 and 10.

This example takes a list that fulfills these constraints, and it checks that when you run this method that returns the last integer of a list, this result will
be greater than or equal to that last integer of the list. However, this property would only pass if the list parameter was contrained to sorted lists, but its not,
so this property would fail. The information that jqwik provides about this failure are the relevant exception, an AssertionError, details about the parameter
value generation like how they are generated and how they deal with base cases, how many times the property ran successfully, and what case made the property fail,
which could be any unsorted list in this case, Jqwik also provides a way to get additional reporting, which could be useful in some cases.

Example 3:
Finally, I'm also going to show how jqwik's arbitraries feature allows for more complex contracts. The way I'm going to demonstrate this is by implementing the
same property as the example before, but in a way in which the property should pass.

// this is a method that returns the last value of the given list of integers
int getLast(List<Integer> loi) {
   return loi.get(loi.size() - 1);
}

// this is a method that checks whether or not a list of integers is sorted low to high
boolean sorted(List<Integer> loi) {
   // implementation of sorted has been excluded
}

// this is a property that generates a list of 10 or less sorted integers, and checks that
// the getLast method returns a value that is greater than the rest of the elements in the list
@Property
boolean lastIsHighest(@ForAll("sortedIntLists") List<Integer> loi) {
   int last = getLast(loi);
   boolean highest = true;
   for (int i = 0; i < loi.size() - 1; i++) {
      if (loi.get(i) > last) {
         highest = false;
      }
   }
   return highest;
}

// this is the specification for what values should be generated
@Provide
Arbitrary<List> sortedIntLists {
   return Arbitraries.integers().list().ofMaxSize(10).ofMinSize(1).filter(aList -> sorted(aList));
}

This example introduces arbitraries, which is indicated by the @Provide. This allows for value generation to be even more customizable because
you are creating a specific set of values that should be tested with the property. In this case, using the Arbitraries.integers.list() provides
that lists of integers should be being tested. Then, I specify that the lists should only be up to size 10, and I also use filter, which allows
me to set completely custom constraints for these lists. I have a method that checks if a list is sorted, so the filter calls this method
on each list, so it would only be tested if it is true. Then, the difference within the actual property is that after the @ForAll tag, you
specify the name of the provided values you want to be generated.

By doing this, only sorted lists will be tested by the property, which should make the property true.

|#
