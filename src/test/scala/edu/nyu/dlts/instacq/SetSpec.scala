package edu.nyu.dlts.instacq

import org.scalatest.FlatSpec

class SetSpec extends UnitSpec {

  "A Set with three members" should "have size 3" in {
    val set = Set(1,2,3)
     assert(set.size == 3)
  }

  it should "produce NoSuchElementException when head is invoked" in {
    intercept[NoSuchElementException] {
      Set.empty.head
    }
  }
}
