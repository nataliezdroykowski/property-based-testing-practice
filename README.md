These are the files for a class assignment where we investigated a Property-Based-Testing library in a language of our choice.

memo.rkt is a write-up discussing the strengths and weaknesses I found in this library, and there is also an introduction to how to use the library with examples I 
came up with.

properties.java is my code for specific problems using property-based-testing. the problems are listed below:

Problem B1
Test out your property-based testing library by implementing the following functionality in your language, writing property-based tests, and running them:
Show that sorting a collection (array, list, etc) does not change the size of the collection.
Write code that adds an element to a collection (array, list, etc), if it doesn’t already exist in it, and show that it does not decrease the size of the collection.

Problem B2
In this problem, we want you to use an existing JSON serialization/deserialization library (if your language does not have one, you can of course implement it, 
but that’s not our intention). First, you need to define a data structure (in your programming language) that represents a map from student ids (as strings) 
to structures representing students (with a name, list of interests, and address). This data structure is part of a hypothetical application for matching students
with people who live near them and have similar interests, the rest of the application will not be implemented! Now, you should write code that turns that data structure
into JSON (using whatever functionality is available in your language’s JSON library), and separate code that does the reverse. This would be used to either store the data, 
or send it over the network. Now, write property-based tests that show that for random examples of your data structure, the serialization and deserialization functions 
are inverses of each other; i.e., that deserialize(serialize(x)) = x for all x.

Problem B3
In this problem, first define a native data type for a color, which should be able to represent colors in the following three ways:
Named colors (Red, Green, Blue, etc: how many is up to you, but a finite number)
RGB colors (three numbers, 0 to 255, that represent the amount of Red, Green, or Blue in the color)
CMYK (four numbers, 0 to 255, that represent how much Cyan, Magenta, Yellow, and Black, in the color)
Now, write code that turns your color into a "packed" representation as an array (or list) of numbers. You are welcome to choose any representation as you like,
but we’d suggest using the first number as a "tag" that indicates which possibility you are in.
Next, write code that takes a "packed" representation as an array (or list) of numbers, and convert it back into the native color data structure.
Finally, write property based tests that show that for random examples of your color, the "unpacking" and "packing" functions are inverses of each other; i.e.,
that unpack(pack(x)) = x for all x. You should also show that for any array (or list) of numbers, the "unpacking" function either gives a sensible error, or returns a color
that, when "packed", gives the same array (or list) of numbers.
