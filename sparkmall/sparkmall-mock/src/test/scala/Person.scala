

class Person{
  def fun(name: String): Unit ={
    println(name)
  }
}
class Person1{
  def fun(name: String): Unit ={
    println(name)
  }
}


object Person{

  def apply(age: Int*): Unit ={
    println("age " + age)
  }

  def main(args: Array[String]): Unit = {
    val p = new Person
    p.fun("aa")
    Person.fun("aaa")
    Person()

  }

  def fun(name: String): Unit ={
    println(111)
  }

}
