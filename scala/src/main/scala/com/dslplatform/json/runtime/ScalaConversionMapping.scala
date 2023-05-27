package com.dslplatform.json.runtime

import com.dslplatform.json.runtime.ScalaCollectionAnalyzer.CollectionConversion

import scala.collection.mutable

object ScalaConversionMapping {
  def collectionConversion(collection: Class[_]): Option[CollectionConversion] = {
    if (classOf[List[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => Nil, _.toList))
    } else if (classOf[Vector[_]].isAssignableFrom(collection)) {
      val empty = Vector.empty
      Some(CollectionConversion(() => empty, _.toVector))
    } else if (classOf[Set[_]].isAssignableFrom(collection)) {
      val empty = Set.empty
      Some(CollectionConversion(() => empty, _.toSet))
    } else if (classOf[mutable.ArrayBuffer[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.ArrayBuffer(0), identity))
    } else if (classOf[scala.collection.immutable.IndexedSeq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.immutable.IndexedSeq.empty
      Some(CollectionConversion(() => empty, _.toIndexedSeq))
    } else if (classOf[scala.collection.immutable.Seq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.immutable.Seq.empty
      Some(CollectionConversion(() => empty, _.toVector))
    } else if (classOf[mutable.Set[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.HashSet(), ab => new mutable.HashSet(ab.size, mutable.HashSet.defaultLoadFactor).addAll(ab)))
    } else if (classOf[mutable.Stack[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.Stack(), ab => new mutable.Stack(ab.size).addAll(ab)))
    } else if (classOf[mutable.Queue[_]].isAssignableFrom(collection)) {
      Some(CollectionConversion(() => new mutable.Queue(), ab => new mutable.Queue(ab.size).addAll(ab)))
    } else if (classOf[scala.collection.mutable.IndexedSeq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.mutable.IndexedSeq.empty
      Some(CollectionConversion(() => empty, identity))
    } else if (classOf[scala.collection.mutable.Seq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.mutable.Seq.empty
      Some(CollectionConversion(() => empty, identity))
    } else if (classOf[scala.collection.IndexedSeq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.IndexedSeq.empty
      Some(CollectionConversion(() => empty, identity))
    } else if (classOf[scala.collection.Seq[_]].isAssignableFrom(collection)) {
      val empty = scala.collection.Seq.empty
      Some(CollectionConversion(() => empty, identity))
    } else {
      None
    }
  }
}
