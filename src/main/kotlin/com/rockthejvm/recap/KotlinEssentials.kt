package com.rockthejvm.recap

object KotlinEssentials {

    // val, var, types
    val meaningOfLife: Int = 42
    val meaningOfLife_v2 = 42 // type inference
    var changeableObjective = 10

    // instructions (are executed) vs expressions (are evaluated)
    val anExpression = 2 + 3
    val anIfExpression = if (2 > 3) 43 else 99 // if-expression

    // functions
    fun complexFun(arg: Int): Int {
        println("just called my complex fun with an argument: $arg") // string interpolation
        return arg + 20
    }

    // repetition (loops or recursion)
    fun concatenateString(aString: String, count: Int): String {
        var result = ""
        for (i in 1 .. count)
            result += aString
        return result
    }

    // FP - recursion instead of looping
    fun concatenateStringRec(aString: String, count: Int): String =
        if (count <= 0) ""
        else aString + concatenateStringRec(aString, count - 1)

    // OOP
    open class Animal {
        val size: Int = 100 // property
        open fun eat() { // methods
            println("I am eating, what'ya lookin' at?")
        }
    }

    val myAnimal = Animal() // "instantiate" the Animal type

    // inheritance = subtyping
    class Dog(val name: String): Animal() { // Dog "extends" Animal
        override fun eat() {
            println("Woof, woof, eating my food!")
        }
    }
    // subtype polymorphism
    val myDog: Animal = Dog("Lassie")

    // interfaces = "abstract" data types
    interface Carnivore {
        infix fun eat(animal: Animal): String // infix = for methods with a single arg
        companion object { // object Carnivore
            // stores "static" properties & methods
            // properties & methods that depend on the Carnivore TYPE
            val eatsAnimals: Boolean = true
            fun build(kind: String): Carnivore =
                when(kind) {
                    "croc" -> Crocodile()
                    else -> GenericCarnivore
                }
        }
    }

    // inheritance = subtype a single class + zero or more interfaces
    class Crocodile: Animal(), Carnivore {
        override fun eat(animal: Animal): String =
            "I'm a croc, I'm eating this poor fella"
    }

    // objects and companions
    object MySingleton { // a type + the only instance of this type
        val aProperty: Int = 57
        fun getSomething(config: String): Int = 45
    }

    object GenericCarnivore: Carnivore {
        override fun eat(animal: Animal): String =
            "I eat everything"
    }

    // generics = reuse code on many (potentially unrelated) types
    interface LList<A> { // <-- type parameter
        fun add(elem: A): LList<A>
    }

    // data classes = lightweight data structures
    // equals, hashCode, toString
    data class Person(val name: String, val age: Int)

    // enums
    enum class Color {
        RED, GREEN, BLUE // these are the only options
    }

    // FP
    // able to pass functions as values, return them as results
    val aFunction: (Int) -> String =
        { x -> "Kotlin $x" }

    // higher order functions (HOFs)
    val aProcessedList = listOf(1,2,3,4).map(aFunction) // ["Kotlin 1", "Kotlin 2", ...]
    // map, flatMap, filter, take, takeWhile, drop, let, run, ...

    @JvmStatic
    fun main(args: Array<String>) {
        // meaningOfLife = 45 // error - vals cannot be changed
        changeableObjective = 15 // okay
        if (changeableObjective > 10)
            println("bigger")
        else
            println("smaller")

        val complexFunInvocation = complexFun(16)
        println(concatenateString("Kotlin", 5))
        println(concatenateStringRec("Kotlin", 5))
        println(myAnimal.size)
        myAnimal.eat()
        myDog.eat()

        val croc = Crocodile()
        croc.eat(myDog)
        croc eat myDog // "natural" language
        // object method argument = infix notation

        val croc_v2 = Carnivore.build("croc")

        // FP
        val funcInvocation = aFunction(3) // "Kotlin 3"
    }
}