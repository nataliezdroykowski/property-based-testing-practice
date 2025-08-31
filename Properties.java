import java.util.*;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.ListArbitrary;
import net.jqwik.api.constraints.*;
import com.google.gson.Gson;

import static java.util.Arrays.*;

// first problem
class ProblemB1 {
  
  // checks if a list stays the same size when it is sorted
  @Property
   boolean sameSizeProp(@ForAll Integer[] test) {
    int beforeLength = test.length;
    Arrays.sort(test);
    int afterLength = test.length;
    return beforeLength == afterLength;
  }
  
  // checks that possibly adding an item to a list will never reduce the size of the list
  @Property
  <T> boolean maybeAddProp(@ForAll List<T> test, @ForAll T item) {
    int beforeLength = test.size();
    if (!test.contains(item)) {
      test.add(item);
    }
    int afterLength = test.size();
    return afterLength >= beforeLength;
  }
  
}
// second problem
class ProblemB2 {
  
  // serializes a mapping
  String serialize(Mapping m) {
    Gson gson = new Gson();
    return gson.toJson(m);
  }
  
  // deserializes a mapping
  Mapping deserialize(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, Mapping.class);
  }
  
  // checks that when you deserialize a mapping that you serialized, you get back
  // the original mapping
  @Property
  boolean studentProp(@ForAll("mapping") Mapping maps) {
    return deserialize(serialize(maps)).equals(maps);
  }
  
  // creates random value generation for students
  @Provide
  Arbitrary<Student> student() {
    Arbitrary<String> names = Arbitraries.strings().withCharRange('a', 'z')
        .ofMinLength(3).ofMaxLength(10);
    Arbitrary<List<String>> interests = Arbitraries.strings()
        .ofMinLength(3).ofMaxLength(10)
        .list().ofMinSize(1).ofMaxSize(5);
    Arbitrary<String> address = Arbitraries.strings()
        .ofMinLength(3).ofMaxLength(20);
    
    Arbitrary<Student> students = Combinators.combine(names, interests, address)
        .as((n, i, p) -> new Student(n, i, p));
    return students;
  }
  
  // creates random value generation for pairs
  @Provide
  Arbitrary<Pair> pair() {
    Arbitrary<String> ids = Arbitraries.strings()
        .ofMinLength(6).ofMaxLength(6);
    Arbitrary<Pair> pairs = Combinators.combine(ids, student())
        .as((i, s) -> new Pair(i, s));
    return pairs;
  }
  
  // creates random value generation for a mapping
  @Provide
  Arbitrary<Mapping> mapping() {
    return pair().list().ofMinSize(1).ofMaxSize(10).map(Mapping::new);
  }
  
}
// third problem
class ProblemB3 {
  // unpacks an array for a color back to its color type
  Color unpack(List<Integer> list) {
    if (list.size() == 0 || list.size() > 5) {
      throw new IllegalArgumentException("Not a valid packed list");
    }
    int tag = list.get(0);
    if (tag == 0 && list.size() == 2) {
      return new Named(new Utils().getColor(list.get(1)));
    } else if (tag == 1 && list.size() == 4) {
      return new RGB(list.get(1), list.get(2), list.get(3));
    } else if (tag == 2 && list.size() == 5) {
      return new CMYK(list.get(1), list.get(2), list.get(3), list.get(4));
    } else {
      throw new IllegalArgumentException("Not a valid packed list");
    }
  }
  
  // checks that unpacking and packing are inverses of each other
  @Property
  boolean packingProp(@ForAll("colors") Color color) {
    return unpack(color.pack()).equals(color);
  }
  
  // checking that the unpack method handles all lists of integers
  @Property
  boolean unpackingProp(@ForAll("colorList") List<Integer> list) {
      try {
          Color color = unpack(list);
          return color.pack().equals(list);
      } catch (IllegalArgumentException e) {
          return true;
      }
  }
  
  //creates random value generation for colors
  @Provide
  Arbitrary<Color> colors() {
    List<String> validColors = List.of
        ("Red", "Green", "Blue", "Purple", "Orange", "Yellow");

    Arbitrary<Color> namedColors = Arbitraries.of(validColors)
            .map(name -> new Named(name));

      Arbitrary<Color> rgbColors = Combinators.combine(
              Arbitraries.integers().between(0, 255),
              Arbitraries.integers().between(0, 255),
              Arbitraries.integers().between(0, 255)
      ).as((r, g, b) -> new RGB(r, g, b));

      Arbitrary<Color> cmykColors = Combinators.combine(
              Arbitraries.integers().between(0, 255),
              Arbitraries.integers().between(0, 255),
              Arbitraries.integers().between(0, 255),
              Arbitraries.integers().between(0, 255)
      ).as((c, m, y, k) -> new CMYK(c, m, y, k));

      return Arbitraries.oneOf(namedColors, rgbColors, cmykColors);
  }
  
  //creates random value generation for packed list
  @Provide
  Arbitrary<List<Integer>> colorList() {
      return Arbitraries.integers()
              .between(0, 255)
              .list();
  }
}


// represents a student
class Student {
  String name;
  List<String> interests;
  String address;
  
  Student(String n, List<String> i, String a) {
    this.name = n;
    this.interests = i;
    this.address = a;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Student)) return false;
      Student student = (Student) o;
      return Objects.equals(this.name, student.name) &&
             Objects.equals(this.interests, student.interests) &&
             Objects.equals(this.address, student.address);
  }

  @Override
  public int hashCode() {
      return this.name.hashCode() * 7 + this.interests.hashCode() * 31
          + this.address.hashCode() * 101;
  }
}

// represents the pairing between a student and their student id
class Pair {
  String studentID;
  Student student;
  
  Pair(String id, Student s) {
    this.studentID = id;
    this.student = s;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Pair)) return false;
      Pair pair = (Pair) o;
      return Objects.equals(studentID, pair.studentID) &&
             Objects.equals(student, pair.student);
  }

  @Override
  public int hashCode() {
      return this.student.hashCode() * 1000 + this.studentID.hashCode();
  }
}

// represents a mapping of many students and student ids
class Mapping {
  List<Pair> pairs;
  
  Mapping(List<Pair> s) {
    this.pairs = s;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Mapping)) return false;
      Mapping mapping = (Mapping) o;
      return Objects.equals(pairs, mapping.pairs);
  }

  @Override
  public int hashCode() {
      return this.pairs.hashCode();
  }
}

// represents a color
interface Color {
  // turns the color into a packed list of integers
  List<Integer> pack();
}

// represents a color by its name
class Named implements Color {
  String color;
  int tag = 0;
  
  Named(String c) {
    this.color = new Utils().isColor(c);
  }

  //turns the color into a packed list of integers
  public List<Integer> pack() {
    List<Integer> arr = new ArrayList<Integer>();
    arr.add(this.tag);
    arr.add(new Utils().colorPlace(this.color));
    return arr;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Named)) return false;
      Named named = (Named) o;
      return Objects.equals(this.color, named.color);
  }

  @Override
  public int hashCode() {
      return this.color.hashCode();
  }
}

// represents a color by its rgb values
class RGB implements Color {
  int red;
  int green;
  int blue;
  int tag = 1;
  
  RGB(int r, int g, int b) {
    Utils u = new Utils();
    this.red = u.checkRange(0, 255, r);
    this.green = u.checkRange(0, 255, g);
    this.blue = u.checkRange(0, 255, b);
  }

  //turns the color into a packed list of integers
  public List<Integer> pack() {
    List<Integer> arr = new ArrayList<Integer>();
    arr.add(this.tag);
    arr.add(this.red);
    arr.add(this.green);
    arr.add(this.blue);
    return arr;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof RGB)) return false;
      RGB rgb = (RGB) o;
      return Objects.equals(this.red, rgb.red)
          && Objects.equals(this.green, rgb.green)
          && Objects.equals(this.blue, rgb.blue);
  }

  @Override
  public int hashCode() {
      return this.red * 1000 + this.blue * 10000 + this.green * 100000;
  }
}

// represents a color by its cmyk values
class CMYK implements Color {
  int cyan;
  int magenta;
  int yellow;
  int black;
  int tag = 2;
  
  CMYK(int c, int m, int y, int k) {
    Utils u = new Utils();
    this.cyan = u.checkRange(0, 255, c);
    this.magenta = u.checkRange(0, 255, m);
    this.yellow = u.checkRange(0, 255, y);
    this.black = u.checkRange(0, 255, k);
  }

  //turns the color into a packed list of integers
  public List<Integer> pack() {
    List<Integer> arr = new ArrayList<Integer>();
    arr.add(this.tag);
    arr.add(this.cyan);
    arr.add(this.magenta);
    arr.add(this.yellow);
    arr.add(this.black);
    return arr;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof CMYK)) return false;
      CMYK cmyk = (CMYK) o;
      return Objects.equals(this.cyan, cmyk.cyan)
          && Objects.equals(this.magenta, cmyk.magenta)
          && Objects.equals(this.yellow, cmyk.yellow)
          && Objects.equals(this.black, cmyk.black);
  }

  @Override
  public int hashCode() {
      return this.cyan * 1000 + this.magenta * 10000
          + this.yellow * 100000 + black * 1000000;
  }
}

// extra methods for colors
class Utils {
  ArrayList<String> colors = new ArrayList<String>(Arrays.asList(
      "Red", "Green", "Blue", "Purple", "Orange", "Yellow"));
  
  // checks that an argument is within the given range
  int checkRange(int low, int high, int num) {
    if (num < low || num > high) {
      throw new IllegalArgumentException("Not a valid number");
    }
    return num;
  }
  
  // checks that the string is a valid color
  String isColor(String c) {
    if (!colors.contains(c)) {
      throw new IllegalArgumentException("Not a valid color");
    }
    return c;
  }
  
  // finds the place of the color within the color list
  int colorPlace(String c) {
    return colors.indexOf(c);
  }
  
  // find the color based on the index of the color list
  String getColor(int i) {
    return colors.get(i);
  }
}

