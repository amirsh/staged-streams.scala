package microbenchmarks

import streams._
import lms._
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import scala.lms.common._
import scala.lms.internal._
import scala.language.reflectiveCalls
import scala.collection.mutable.ArrayBuffer
import java.io.PrintWriter

/*
 Naming conventions:
   - StagedStream the datatype
   - StagedStreamBenchmarks the name of the benchmark suite
   - StagedStreamBenchmarksS the staged benchmarks
   - StagedStreamBenchmarksT the type of the benchmarks after compilation
 */

trait StagedStreamBenchmarksS extends StagedStream {
  def flatten (xs : Rep[Array[Array[Int]]]) : Rep[Int] =
    Stream[Array[Int]](xs)
      .flatmap(d => Stream[Int](d))
      .fold(0, ((a : Rep[Int])=> (z : Rep[Int]) => a + z))
  def flatten3 (xs : Rep[Array[Array[Array[Int]]]]) : Rep[Int] =
    Stream[Array[Array[Int]]](xs)
      .flatmap(d => Stream[Array[Int]](d).flatmap(d2 => Stream[Int](d2)))
      // .flatmap(d => Stream[Array[Int]](d)).flatmap(d2 => Stream[Int](d2))
      .fold(0, ((a : Rep[Int])=> (z : Rep[Int]) => a + z))
  def sum (xs : Rep[Array[Int]]) : Rep[Int] =
    Stream[Int](xs)
      .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
  def sumOfSquares (xs : Rep[Array[Int]]) : Rep[Int]  =
    Stream[Int](xs)
      .map(x => x*x)
      .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
  def sumOfSquaresEven (xs : Rep[Array[Int]]) : Rep[Int]  =
    Stream[Int](xs)
      .filter(y => y % unit(2) == unit(0))
      .map(x => x*x)
      .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
  def cart (vHi : Rep[Array[Int]], vLo : Rep[Array[Int]]) : Rep[Int] =
    Stream[Int](vHi)
      .flatmap(d => Stream[Int](vLo).map (dp => dp * d))
      .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
  def maps (xs : Rep[Array[Int]]) : Rep[Int]  =
    Stream[Int](xs)
      .map(x => x * 1)
      .map(x => x * 2)
      .map(x => x * 3)
      .map(x => x * 4)
      .map(x => x * 5)
      .map(x => x * 6)
      .map(x => x * 7)
      .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
   def filters (xs : Rep[Array[Int]]) : Rep[Int]  =
     Stream[Int](xs)
       .filter(x => x > 1)
       .filter(x => x > 2)
       .filter(x => x > 3)
       .filter(x => x > 4)
       .filter(x => x > 5)
       .filter(x => x > 6)
       .filter(x => x > 7)
       .fold(unit(0), ((a : Rep[Int]) => (b : Rep[Int]) => a + b))
  def dotProduct (a : Rep[Array[Int]], b : Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](a)
      .zip(((a : Rep[Int])=> (b : Rep[Int]) => a * b), Stream[Int](b))
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
  def flatMap_after_zipWith(xs : Rep[Array[Int]], ys: Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](xs)
      .zip(((a : Rep[Int]) => (b : Rep[Int]) => a + b), Stream[Int](xs))
      .flatmap(x => Stream[Int](ys).map(y => (x + y)))
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
  def zipWith_after_flatMap(xs : Rep[Array[Int]], ys: Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](xs)
      .flatmap(x => Stream[Int](ys).map(y => (x + y)))
      .zip(((a : Rep[Int]) => (b : Rep[Int]) => a + b), Stream[Int](xs))
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
  def flatMap_take (vHi : Rep[Array[Int]], vLo : Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](vHi)
      .flatmap(x => Stream[Int](vLo).map(y => (x * y)))
      .take(20000000)
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
  def zip_flat_flat (vHi : Rep[Array[Int]], vLo : Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](vHi)
      .flatmap(x => Stream[Int](vLo).map(y => (x * y)))
      .zip(x => (y:Rep[Int]) => x + y,
        Stream[Int](vLo)
          .flatmap(x => Stream[Int](vHi).map(y => (x * y))))
      .take(20000000)
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
  def zip_filter_filter (xs : Rep[Array[Int]], ys: Rep[Array[Int]]) : Rep[Int] =
     Stream[Int](xs)
      .filter(_ < 3)
      .zip(((a : Rep[Int]) => (b : Rep[Int]) => a + b), 
        Stream[Int](ys).filter(_ > 5))
      .fold(unit(0), ((a : Rep[Int])=> (b : Rep[Int]) => a + b))
}

trait StagedStreamBenchmarksT extends StagedStreamBenchmarksS
    with PrimitiveOpsExp
    with ArrayOpsExp
    with IfThenElseExpOpt
    with BooleanOpsExp
    with OrderingOpsExp
    with NumericOpsExp
    with WhileExp
    with VariablesExpOpt
    with EqualExpOpt
    with StringOpsExp
    with TupledFunctionsExp
    with TupleOpsExp
    with StructExp
    with ScalaCompileMultiParams {

  val flatten : (Array[Array[Int]] => Int)
  val flatten3 : (Array[Array[Array[Int]]] => Int)
  val sum : (Array[Int] => Int)
  val sumOfSquares : (Array[Int] => Int)
  val sumOfSquaresEven : (Array[Int] => Int)
  val cart : ((Array[Int], Array[Int]) => Int)
  val maps : (Array[Int] => Int)
  val filters : (Array[Int] => Int)
  val dotProduct : ((Array[Int], Array[Int]) => Int)
  val flatMap_after_zipWith : ((Array[Int], Array[Int]) => Int)
  val zipWith_after_flatMap : ((Array[Int], Array[Int]) => Int)
  val flatMap_take : ((Array[Int], Array[Int]) => Int)
  val zip_flat_flat : ((Array[Int], Array[Int]) => Int)
  val zip_filter_filter : ((Array[Int], Array[Int]) => Int)
}

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@State(Scope.Thread)
@Fork(1)
class S {
  var NNN : Int = 100
  var NN : Int = 1000
  var N : Int = 100000000
  // var Limit : Int = (Int)(N*0.2)
  var v : Array[Int] = _
  var vHi : Array[Int] = _
  var vLo : Array[Int] = _
  var vFaZ : Array[Int] = _
  var vZaF : Array[Int] = _
  var nestedArr : Array[Array[Int]] = _
  var nestedArr3 : Array[Array[Array[Int]]] = _
  var staged : StagedStreamBenchmarksT = _

  @Setup
  def prepare() : Unit = {
    v          = Array.tabulate(N)(i => i.toInt % 10)
    vHi        = Array.tabulate(10000000)(i => i.toInt % 10)
    vLo        = Array.tabulate(10)(i => i.toInt % 10)
    vFaZ       = Array.tabulate(10000)(_.toInt)
    vZaF       = Array.tabulate(10000000)(_.toInt)
    nestedArr  = Array.tabulate(NN)(i => 
                  Array.tabulate(NN)(j => 
                    (i.toInt + j.toInt) % 10))
    nestedArr3 = Array.tabulate(NNN)(i => 
                  Array.tabulate(NNN)(j => 
                    Array.tabulate(NNN)(k =>
                      (i.toInt + j.toInt + k.toInt) % 10)))

    staged = new StagedStreamBenchmarksT { self =>

      val codegen = new ScalaGenEffect
        with ScalaGenPrimitiveOps
        with ScalaGenArrayOps
        with ScalaGenTupledFunctions
        with ScalaGenIfThenElse
        with ScalaGenBooleanOps
        with ScalaGenOrderingOps
        with ScalaGenWhile
        with ScalaGenNumericOps
        with ScalaGenStruct
        with ScalaGenTupleOps
        with ScalaGenEqual
        with ScalaGenStringOps
        with ScalaGenVariables { val IR: self.type = self }

      dumpGeneratedCode = false
      
      override val flatten : (Array[Array[Int]] => Int) = compile(self.flatten)
      override val flatten3 : (Array[Array[Array[Int]]] => Int) = compile(self.flatten3)
      override val sum : (Array[Int] => Int) = compile(self.sum)
      override val sumOfSquares : (Array[Int] => Int) = compile(self.sumOfSquares)
      override val sumOfSquaresEven : (Array[Int] => Int) = compile(self.sumOfSquaresEven)
      override val cart : ((Array[Int], Array[Int]) => Int) = compile2(self.cart)
      override val maps : (Array[Int] => Int) = compile(self.maps)
      override val filters : (Array[Int] => Int) = compile(self.filters)
      override val dotProduct : ((Array[Int], Array[Int]) => Int) = compile2(self.dotProduct)
      override val flatMap_after_zipWith : ((Array[Int], Array[Int]) => Int) = compile2(self.flatMap_after_zipWith)
      override val zipWith_after_flatMap : ((Array[Int], Array[Int]) => Int) = compile2(self.zipWith_after_flatMap)
      override val flatMap_take : ((Array[Int], Array[Int]) => Int) = compile2(self.flatMap_take)
      override val zip_flat_flat : ((Array[Int], Array[Int]) => Int) =  compile2(self.zip_flat_flat)
      override val zip_filter_filter : ((Array[Int], Array[Int]) => Int) = compile2(self.zip_filter_filter)
    }
  }

  // @Benchmark
  // def sum_staged () : Int = {
  //   val sum : Int = staged.sum(v)
  //   sum
  // }

  // @Benchmark
  // def sumOfSquares_staged () : Int = {
  //   val sum : Int = staged.sumOfSquares(v)
  //   sum
  // }

  // @Benchmark
  // def sumOfSquaresEven_staged () : Int = {
  //   val sum : Int = staged.sumOfSquaresEven(v)
  //   sum
  // }

  // @Benchmark
  // def cart_staged () : Int = {
  //   val sum : Int = staged.cart(vHi, vLo)
  //   sum
  // }

  // @Benchmark
  // def maps_staged () : Int = {
  //   val sum : Int = staged.maps(v)
  //   sum
  // }

  // @Benchmark
  // def filters_staged () : Int = {
  //   val sum : Int = staged.filters(v)
  //   sum
  // }

  // @Benchmark
  // def dotProduct_staged () : Int = {
  //   val sum : Int = staged.dotProduct(vHi, vHi)
  //   sum
  // }

  // @Benchmark
  // def flatMap_after_zipWith_staged () : Int = {
  //   val sum : Int = staged.flatMap_after_zipWith(vFaZ, vFaZ)
  //   sum
  // }

  // @Benchmark
  // def zipWith_after_flatMap_staged () : Int = {
  //   val sum : Int = staged.zipWith_after_flatMap(vZaF, vZaF)
  //   sum
  // }

  // @Benchmark
  // def flatMap_take_staged () : Int = {
  //   val sum : Int = staged.flatMap_take(v, vLo)
  //   sum
  // }
  
  // @Benchmark
  // def zip_flat_flat_staged () : Int = {
  //   val sum : Int = staged.zip_flat_flat(v, vLo)
  //   sum
  // }
  
  // @Benchmark
  // def zip_filter_filter_staged () : Int = {
  //   val sum : Int = staged.zip_filter_filter(vHi, vHi)
  //   sum
  // }

  // @Benchmark
  // def flatten_unstaged () : Int = {
  //   val sum : Int = nestedArr.flatten.sum
  //   sum
  // }
  
  // @Benchmark
  // def flatten_staged () : Int = {
  //   val sum : Int = staged.flatten(nestedArr)
  //   sum
  // }

  @Benchmark
  def flatten3_unstaged () : Int = {
    // val sum : Int = nestedArr3.flatten.flatten.sum
    val sum : Int = nestedArr3.flatMap(l => l.flatMap(l2 => l2)).sum
    sum
  }
  
  @Benchmark
  def flatten3_staged () : Int = {
    val sum : Int = staged.flatten3(nestedArr3)
    sum
  }
}